package hear.app.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetUtil {

    private static Context context;

    private static TelephonyManager mTelephonyMgr = null;

    /**
     * 初始化
     */
    public static void init(Context ct) {
        context = ct;
        mTelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 测试用户是否是wifi
     * @return
     */
    public static boolean isWifi() {

        NetworkInfo networkInfo = getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isAvailable()) {
            return true;
        }

        return false;
    }

    /**
     * 获取用户是否是2G信号
     * @return
     */
    public static boolean isMobile() {
        NetworkInfo networkInfo = getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                && networkInfo.isAvailable()) {
            return true;
        }
        return false;
    }

    /**
     * 获取用户3G的信息
     * @return
     */
    public static boolean is3G(){

        int type = mTelephonyMgr.getNetworkType();

        if (isMobile()){
            switch (type){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    return false;
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * 获取用户是否联网的信息
     * @return
     */
    public static boolean hasAvailableNet() {
        /*
        NetworkInfo networkInfo = getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return true;
        }
        return false;
        */

        boolean available = false;
        NetworkInfo[] networkInfos =getAllNetworkInfos();
        if (networkInfos != null)
        {
            for (NetworkInfo info : networkInfos)
            {
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI
                            || info.getType() == ConnectivityManager.TYPE_MOBILE)
                    {
                        String mCurrentAPN = info.getExtraInfo();
                        LogUtil.d("Current AP:" + mCurrentAPN);
                        available = true;
                        break;
                    }
                }
            }
        }

        return available;

    }

    /**
     * 获取激活的网络类型
     * @return
     */
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo;
    }

    /**
     * wifi是否激活
     * @return
     */
    public static boolean isWiFiActive() {
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
    public static NetworkInfo[] getAllNetworkInfos() {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = manager.getAllNetworkInfo();
        return networkInfo;
    }

}
