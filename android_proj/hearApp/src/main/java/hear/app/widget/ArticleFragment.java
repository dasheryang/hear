package hear.app.widget;

import hear.app.Article;
import hear.app.ArticleLike;
import hear.app.PlayListener;
import hear.app.Player;
import hear.app.R;
import hear.app.engine.BaseHttpAsyncTask;
import hear.app.engine.JsonRespWrapper;
import hear.app.util.DeviceUtil;
import hear.app.util.LogUtil;
import hear.app.util.SDCardUtils;
import hear.app.util.ToastUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

/**
 * Created by power on 14-8-11.
 */
public class ArticleFragment extends Fragment implements PlayListener {

	private LinearLayout likeContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.article_fragment, null);
	}

	private ImageView playIcon = null;

	private ImageView playLoading = null;

	private ImageView likeIcon = null;

	private TextView likeCount = null;

	private TextView articleContent = null;

	private TextView articleAuthor = null;

	private ImageView coverPicture = null;

	private TextView articleVol = null;

	private TextView date = null;

	private SeekBar seekBar;

	private TextView musicCurrentTimeTextView = null;

	private TextView musicTotalTimeTextView = null;

	public static final String KEY_ARTICLE = "article";

	private static final String TAG = "ArticleFragment";

	/**
	 * 获取article
	 * 
	 * @return
	 */
	public Article getArticle() {
		int pageno = getArguments().getInt(KEY_ARTICLE);
		Article article = Article.getArticleByPageNo(pageno);
		return article;
	}

	public static int LEVEL_PLAY = 1;
	public static int LEVEL_PAUSE = 2;

	private Handler mHandler = new Handler();

	private SeekBarHandler mSeekBarHandler = new SeekBarHandler(this);

	private static class SeekBarHandler extends Handler {

		private WeakReference<ArticleFragment> instance;

		public SeekBarHandler(ArticleFragment fragment) {
			instance = new WeakReference<ArticleFragment>(fragment);
		}

		public void handleMessage(android.os.Message msg) {

			if (instance != null && instance.get() != null) {
				instance.get().updateSeekBarProgress();

				if (Player.instance().isPlaying()) {
					sendEmptyMessageDelayed(0, 1000);
				}

			}

		};
	};

	@Override
	public void onOtherStart() {
		setPlayIconLevel(LEVEL_PLAY);

	}

	private void setPlayIconLevel(final int level) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (playIcon != null) {
					playLoading.setVisibility(View.GONE);
					playLoading.clearAnimation();
					playIcon.setVisibility(View.VISIBLE);
					LevelListDrawable bg = (LevelListDrawable) playIcon
							.getBackground();
					bg.setLevel(level);
				}
			}
		});
	}

	/**
	 * loading
	 */
	private void setLoading() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				playIcon.setVisibility(View.GONE);
				playLoading.setVisibility(View.VISIBLE);
				playLoading.startAnimation(getAnimation());
			}
		});
	}

	private int getPlayIconLeve() {
		LevelListDrawable bg = (LevelListDrawable) playIcon.getBackground();
		return bg.getLevel();
	}

	@Override
	public void onComplete() {
		setPlayIconLevel(LEVEL_PLAY);
	}

	@Override
	public void onLoadingEnd() {
		mSeekBarHandler.sendEmptyMessage(0);
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				musicCurrentTimeTextView.setVisibility(View.VISIBLE);
				musicTotalTimeTextView.setVisibility(View.VISIBLE);
			}
		});
		setPlayIconLevel(LEVEL_PAUSE);
	}

	public class ImageShowerListener extends SimpleImageLoadingListener {

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			pb.setVisibility(View.GONE);

			// super.onLoadingComplete(imageUri, view, loadedImage);
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			pb.setVisibility(View.GONE);
			ToastUtil.Short("拉取图片失败.");
		}
	}

	public final int UN_LIKE_LEVEL = 1;
	public final int LIKE_LEVEL = 2;

	private Animation animation;

	public Animation getAnimation() {
		if (animation == null) {
			animation = AnimationUtils.loadAnimation(this.getActivity(),
					R.anim.tip);
			LinearInterpolator lin = new LinearInterpolator();
			animation.setInterpolator(lin);
		}
		return animation;
	}

	private ProgressBar pb;

	private void init() {
		final Article article = getArticle();
		date.setText(article.getShowTime());
		articleVol.setText("VOL." + article.pageno);

		articleAuthor.setText(article.showauthor);

		LogUtil.d("haha");
		// coverPicture.

		ImageLoader.getInstance().displayImage(
				article.getImageURL(getActivity()), coverPicture,
				new ImageShowerListener());
		articleContent.setText(article.txt);
		articleContent.setMovementMethod(new ScrollingMovementMethod());

		LogUtil.d("yes");

		playIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (getPlayIconLeve() == LEVEL_PLAY) {

					String playUrl = getPlayUrl(article.soundurl);

					if (Player.instance().isPause(playUrl)) {
						Player.instance().resume();
						setPlayIconLevel(LEVEL_PAUSE);
					} else {
						Player.instance().play(playUrl, ArticleFragment.this);
						setLoading();
					}

				} else {
					Player.instance().pause();
					setPlayIconLevel(LEVEL_PLAY);
				}

				/*
				 * //TODO open play activity Intent i=new Intent(getActivity(),
				 * PlayActivity.class);
				 * i.putExtra(PlayActivity.KEY_PAGE_NO,getArticle().pageno);
				 * startActivity(i);
				 */
			}
		});

		LogUtil.d("yes");
		final LevelListDrawable drawable = (LevelListDrawable) likeIcon
				.getBackground();

		int articleLike = ArticleLike.getLikeArticle(getArticle().pageno);
		articleLike = (articleLike == -1) ? getArticle().haslike : articleLike;

		LogUtil.d("yes");
		if (articleLike == 0) {
			drawable.setLevel(UN_LIKE_LEVEL);
		} else {
			drawable.setLevel(LIKE_LEVEL);
		}

		LogUtil.d("yes");

		likeCount.setText("" + ArticleLike.getLikeCount(article.pageno));

		likeContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int level = drawable.getLevel();
				if (level == UN_LIKE_LEVEL) {
					drawable.setLevel(LIKE_LEVEL);
					ArticleLike.setLikeArticle(getArticle().pageno, 1);
					likeArticleRemote(getArticle().pageno);
					ArticleLike.incLikeCount(getArticle().pageno);
					incrLikeCount();
				} else {
					drawable.setLevel(UN_LIKE_LEVEL);
					ArticleLike.setLikeArticle(getArticle().pageno, 0);
					unLikeArticleRemote(getArticle().pageno);
					descLikeCount();
					ArticleLike.descLikeCount(getArticle().pageno);
				}
			}
		});

		if (Player.instance().isPlaying(getPlayUrl(article.soundurl))) {
			setPlayIconLevel(LEVEL_PAUSE);
		} else {
			setPlayIconLevel(LEVEL_PLAY);
		}

	}

	private String getPlayUrl(String soundUrl) {
		String mediaName = soundUrl.substring(soundUrl.lastIndexOf("/") + 1);

		File file = new File(SDCardUtils.getMediaCachePath(), mediaName);

		if (file.exists()) {
			Log.d(TAG, mediaName + " 在本地已经有缓存");
			return file.getPath();
		}

		return soundUrl;

	}

	private void updateSeekBarProgress() {

		seekBar.setMax(Player.instance().getMax());
		seekBar.setProgress(Player.instance().getCurrentPos());

		SimpleDateFormat df = new SimpleDateFormat("mm:ss");

		Date currentDate = new Date(Player.instance().getCurrentPos());

		Date totalDate = new Date(Player.instance().getMax());

		musicCurrentTimeTextView.setText(df.format(currentDate));

		musicTotalTimeTextView.setText(df.format(totalDate));

	}

	/**
	 * 增加like count
	 */
	private void incrLikeCount() {
		int val = Integer.parseInt(likeCount.getText().toString());
		likeCount.setText(String.valueOf(val + 1));
	}

	/**
	 * 减少likecount
	 */
	private void descLikeCount() {
		int val = Integer.parseInt(likeCount.getText().toString());
		if (val > 0) {
			likeCount.setText(String.valueOf(val - 1));
		}
	}

	/**
	 * 喜欢文章
	 */
	private void likeArticleRemote(int pageno) {

		String url = "http://www.hearheart.com/clicklike";

		BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

			@Override
			protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {
				if (jsonRespWrapper.ret == 0) {
				} else {
				}
			}
		};

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("PhoneId", DeviceUtil.getPhoneId());
		params.put("pageno", String.valueOf(pageno));
		asyncTask.get(params).execute();
	}

	/**
	 * 取消喜欢文章
	 */
	private void unLikeArticleRemote(int pageno) {

		String url = "http://www.hearheart.com/cancellike";

		BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

			@Override
			protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {
				if (jsonRespWrapper.ret == 0) {

				} else {

				}
			}
		};

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("PhoneId", DeviceUtil.getPhoneId());
		params.put("pageno", String.valueOf(pageno));
		asyncTask.get(params).execute();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		View view = getView();
		seekBar = (SeekBar) view.findViewById(R.id.play_seekbar);
		pb = (ProgressBar) view.findViewById(R.id.image_loading);
		date = (TextView) view.findViewById(R.id.date);
		articleVol = (TextView) view.findViewById(R.id.vol_id);
		coverPicture = (ImageView) view.findViewById(R.id.cover_picture);
		articleContent = (TextView) view.findViewById(R.id.content);
		articleAuthor = (TextView) view.findViewById(R.id.author);
		likeCount = (TextView) view.findViewById(R.id.like_count);
		likeIcon = (ImageView) view.findViewById(R.id.like_icon);
		likeContainer = (LinearLayout) view.findViewById(R.id.id_like_contaner);
		playIcon = (ImageView) view.findViewById(R.id.play);
		playLoading = (ImageView) view.findViewById(R.id.play_loading);

		musicCurrentTimeTextView = (TextView) view
				.findViewById(R.id.play_current_time);
		musicTotalTimeTextView = (TextView) view
				.findViewById(R.id.play_total_time);

		init();
	}
}
