package hear.app.util;

import hear.app.AppContext;
import android.widget.Toast;

/**
 * Created by power on 14-8-18.
 */
public class ToastUtil {

    public static void Long(String s){
        Toast.makeText(AppContext.getContext(), s, Toast.LENGTH_LONG).show();
    }

    public static void Long(int sid){
        Toast.makeText(AppContext.getContext(), sid, Toast.LENGTH_LONG).show();
    }

    public static void Short(String s){
        Toast.makeText(AppContext.getContext(), s, Toast.LENGTH_SHORT).show();
    }

    public static void Short(int sid){
        Toast.makeText(AppContext.getContext(), sid, Toast.LENGTH_SHORT).show();
    }

}
