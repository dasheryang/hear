package hear.app;

import hear.app.util.NetUtil;
import hear.app.util.SDCardUtils;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by power on 14-8-10.
 */
public class MainApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		AppContext.setAppContext(this);

		init();
	}

	BroadcastReceiver receiver;

	private void registerNetworkChangeReceiver() {
		IntentFilter intentFilter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Intent service = new Intent(AppContext.getContext(),
						CacheMediaService.class);
				if (NetUtil.isWiFiActive()) {
					startService(service);
				} else {
					stopService(service);
				}

			}
		};
		registerReceiver(receiver, intentFilter);
	}

	/**
	 * app初始化
	 */
	private void init() {
		initImageLoader(this);
		NetUtil.init(getApplicationContext());
		registerNetworkChangeReceiver();
		SDCardUtils.makeDirs();
	}

	/**
	 * Get ImageLoader.
	 * 
	 * @return
	 */
	public ImageLoader getImageLoader() {
		if (ImageLoader.getInstance().isInited()) {
			return ImageLoader.getInstance();
		}
		initImageLoader(this);
		return ImageLoader.getInstance();
	}

	/**
	 * Initial ImageLoader before using it. <note> Remove
	 * {@linkplain com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder#writeDebugLogs()}
	 * when released. </note>
	 * 
	 * @param ctx
	 */
	public synchronized static void initImageLoader(final Context ctx) {
		DisplayImageOptions mImageOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				ctx).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.defaultDisplayImageOptions(mImageOptions)
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.FIFO)
				// .writeDebugLogs() // Remove for release app
				.build();
		ImageLoader.getInstance().init(config);
	}
}
