package hear.app.models;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SnsAccount;
import com.umeng.socialize.utils.OauthHelper;

import hear.app.helper.AppContext;
import hear.app.helper.SharedPreferencesCache;

/**
 * Created by ZhengYi on 15/2/11.
 */
public class SNSAccountStore {
    private static final String KEY_LOGIN_TYPE = "login_type";
    private static final String KEY_LOGIN_ACCOUNT = "login_account";

    private static SNSAccountStore sInstance;
    private SnsAccount mLoginAccount;
    private SHARE_MEDIA mLoginType;

    private SNSAccountStore() {
        unarchive();
    }

    public void synchronize() {
        archive();
    }

    public static SNSAccountStore getInstance() {
        if (sInstance == null)
            sInstance = new SNSAccountStore();

        return sInstance;
    }

    public SNSAccountStore setLoginAccount(SnsAccount loginAccount) {
        mLoginAccount = loginAccount;
        return this;
    }

    public SnsAccount getLoginAccount() {
        return mLoginAccount;
    }

    public SNSAccountStore setLoginType(SHARE_MEDIA loginType) {
        mLoginType = loginType;
        return this;
    }

    public SHARE_MEDIA getLoginType() {
        return mLoginType;
    }

    public boolean isLogin() {
        return mLoginType != null && mLoginAccount != null && OauthHelper.isAuthenticatedAndTokenNotExpired(AppContext.getContext(), mLoginType);
    }

    public void logout() {
        SNSAccountStore.getInstance().setLoginAccount(null).setLoginType(null).synchronize();
    }

    private void archive() {
        SharedPreferencesCache cache = AppContext.getSharedPrefernce();

        cache.put(KEY_LOGIN_TYPE, mLoginType != null ? mLoginType.toString() : "");

        cache.put(KEY_LOGIN_ACCOUNT, mLoginAccount != null ? new Gson().toJson(mLoginAccount) : "");
    }

    private void unarchive() {
        SharedPreferencesCache cache = AppContext.getSharedPrefernce();

        String value = cache.get(KEY_LOGIN_TYPE);
        if (!TextUtils.isEmpty(value)) {
            mLoginType = SHARE_MEDIA.convertToEmun(value);

            if (OauthHelper.isAuthenticatedAndTokenNotExpired(AppContext.getContext(), mLoginType)) {
                value = cache.get(KEY_LOGIN_ACCOUNT);
                if (!TextUtils.isEmpty(value)) {
                    mLoginAccount = new Gson().fromJson(value, SnsAccount.class);
                }
            } else {
                mLoginType = null;
                mLoginAccount = null;
                archive();
            }
        }
    }
}
