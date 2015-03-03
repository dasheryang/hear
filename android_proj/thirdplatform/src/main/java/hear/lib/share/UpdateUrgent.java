package hear.lib.share;

import android.content.Context;

import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import hear.lib.share.controllers.CustomUpdateDialog;

/**
 * Created by ZhengYi on 15/3/2.
 */
public class UpdateUrgent {
    private UpdateUrgent() {
    }

    public static void checkUpdate(Context context) {

        final Context appContext = context.getApplicationContext();
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int statusCode, UpdateResponse updateInfo) {
                if (statusCode == UpdateStatus.Yes && updateInfo.hasUpdate)
                    new CustomUpdateDialog(appContext).setUpdateResponse(updateInfo).show();
            }
        });
        UmengUpdateAgent.forceUpdate(context);
    }
}
