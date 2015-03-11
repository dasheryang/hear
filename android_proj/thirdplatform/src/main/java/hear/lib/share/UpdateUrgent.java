package hear.lib.share;

import android.content.Context;
import android.support.annotation.Nullable;

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

    public static void checkUpdate(final Context context, final boolean updateIfIgnored, final boolean ignoreIfCancel, @Nullable Callback callback) {
        final Callback callbackRef = callback == null ? Callback.NULL : callback;
        final Context appContext = context.getApplicationContext();
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int statusCode, final UpdateResponse updateInfo) {
                if (statusCode == UpdateStatus.Yes && updateInfo.hasUpdate) {
                    if (UmengUpdateAgent.isIgnore(context, updateInfo) && !updateIfIgnored) {
                        callbackRef.onFinishCheckUpdate();
                    } else {
                        new CustomUpdateDialog(appContext).setUpdateResponse(updateInfo).setListener(new CustomUpdateDialog.Listener() {
                            @Override
                            public void onConfirmButtonClick() {
                                callbackRef.onFinishCheckUpdate();
                            }

                            @Override
                            public void onCancelButtonClick() {
                                if (ignoreIfCancel) {
                                    UmengUpdateAgent.ignoreUpdate(context, updateInfo);
                                }
                                callbackRef.onFinishCheckUpdate();
                            }
                        }).show();
                    }
                } else {
                    callbackRef.onFinishCheckUpdate();
                }
            }
        });
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(context);
    }

    public static interface Callback {
        void onFinishCheckUpdate();

        public static Callback NULL = new Callback() {
            @Override
            public void onFinishCheckUpdate() {
            }
        };
    }
}
