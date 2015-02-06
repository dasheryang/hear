package hear.app.util;

import android.util.Log;

/**
 * Created by power on 14-8-10.
 */
public class LogUtil {

    public static boolean debug=true;

    public static String getClassName (int i) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[i];
        return ste.getClassName();
    }

    public static void d(String msg){
        if(!debug){
            return;
        }
        String name = getClassName(4);
        Log.d(name, msg);
    }

    public static void e(String msg){
        if(!debug){
            return;
        }
        String name = getClassName(4);
        Log.e(name, msg);
    }
}