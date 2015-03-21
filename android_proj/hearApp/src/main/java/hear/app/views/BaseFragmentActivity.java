package hear.app.views;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import hear.app.R;

/**
 * Created by power on 14-9-10.
 */
public class BaseFragmentActivity extends ActionBarActivity {

    private boolean isFirstResumed = false;

    public boolean needActivityStat() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View titleView = getLayoutInflater().inflate(R.layout.layer_actionbar_title, null, false);
        ((TextView) titleView.findViewById(R.id.label_title)).setText(getActivityLabel());
        getSupportActionBar().setCustomView(titleView, new ActionBar.LayoutParams(-2, -2, Gravity.CENTER));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onFirstResume() {
    }

    private String getActivityLabel() {
        String ret = getString(R.string.app_name);
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(new ComponentName(this, getClass().getName()), 0);
            if (ai.labelRes != 0)
                ret = getString(ai.labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
