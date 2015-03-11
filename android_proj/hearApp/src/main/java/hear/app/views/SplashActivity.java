package hear.app.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import hear.app.R;
import hear.app.helper.AppContext;
import hear.lib.share.UpdateUrgent;


/**
 * Created by power on 14-8-10.
 */
public class SplashActivity extends BaseActivity {

    private Handler mHandlers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        mHandlers = new Handler();
        init();
    }

    public static int REQ_GUIDE = 0;

    private void showGuideActivity() {
        Intent i = new Intent(this, GuideActivity.class);
        startActivity(i);
        finish();
    }


    private InActivityHelper helper = new InActivityHelper(this);


    private void init() {
        final Runnable mLaunchTask = new Runnable() {
            @Override
            public void run() {
                if (!isGuidActivityShowed()) {
                    showGuideActivity();
                    setGuideActivityShowed();
                } else {
                    helper.initEntranceActivity();
                }
            }
        };

        UpdateUrgent.checkUpdate(this, false, true, new UpdateUrgent.Callback() {
            @Override
            public void onFinishCheckUpdate() {
                mLaunchTask.run();
            }
        });
    }


    private boolean isGuidActivityShowed() {
        //return false;
        boolean showed = AppContext.getSharedPrefernce().get("show_guide", false);
        return showed;

    }

    private void setGuideActivityShowed() {
        AppContext.getSharedPrefernce().put("show_guide", true);
    }


}
