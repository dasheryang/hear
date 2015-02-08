package hear.app.helper;

import hear.app.helper.AppContext;

import java.security.MessageDigest;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Created by power on 14-8-22.
 */
public class DeviceUtil {

    public static String CACHE_ANDROID_ID;

    public static String getPhoneId() {
        if(CACHE_ANDROID_ID != null){
            return CACHE_ANDROID_ID;
        }
        String deviceID = null;
        // 获取设备码
        TelephonyManager tManager = (TelephonyManager) AppContext.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        //deviceId 有些山寨机器可能会是一样的，正规厂商的id应该不一样
        //Android_ID每次系统重新刷机一次，会随机产生一个
        deviceID = "\"DEVICEID\":\"" + tManager.getDeviceId() + "\"-" + "\"ANDROID_ID\":\""
                + Settings.Secure.getString(AppContext.getContext().getContentResolver(), Settings.Secure.ANDROID_ID) + "\"";

        CACHE_ANDROID_ID = "IMEI_" + MD5(deviceID);

        return CACHE_ANDROID_ID;
    }

    public static String MD5(String inStr) {
        String result = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5.digest(inStr.getBytes("UTF-8"));
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }

            result = hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
