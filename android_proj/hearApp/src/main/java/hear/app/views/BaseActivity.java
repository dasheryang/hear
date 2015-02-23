package hear.app.views;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by power on 14-9-10.
 */
public class BaseActivity extends Activity {
    private boolean isFirstResumed = false;

    public boolean needActivityStat() {
        return true;
    }

    @Override
    protected void onResume() {
        if (!isFirstResumed) {
            isFirstResumed = true;
            onFirstResume();
        }
        if (needActivityStat()) {
            MobclickAgent.onResume(this);
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        if (needActivityStat()) {
            MobclickAgent.onPause(this);
        }
        super.onPause();
    }

    protected void onFirstResume() {
    }
}
