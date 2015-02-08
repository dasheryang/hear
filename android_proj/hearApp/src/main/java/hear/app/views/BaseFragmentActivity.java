package hear.app.views;

import android.support.v4.app.FragmentActivity;

import com.baidu.mobstat.StatService;

/**
 * Created by power on 14-9-10.
 */
public class BaseFragmentActivity extends FragmentActivity {


    public boolean needActivityStat(){
        return true;
    }

    @Override
    protected void onPause() {
        if(needActivityStat()){
            StatService.onPause(this);
        }
        super.onPause();
    }

    private boolean isFirstResumed=false;

    /**
     * 用来被覆盖
     */
    protected void onFirstResume(){
        //pass
    }

    @Override
    protected void onResume() {
        if(!isFirstResumed){
            isFirstResumed=true;
            onFirstResume();
        }
        if(needActivityStat()){
            StatService.onResume(this);
        }
        super.onResume();
    }
}
