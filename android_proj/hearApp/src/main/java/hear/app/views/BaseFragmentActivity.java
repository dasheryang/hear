package hear.app.views;

import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by power on 14-9-10.
 */
public class BaseFragmentActivity extends ActionBarActivity {

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
}
