package hear.app.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;

/**
 * Created by power on 14-8-15.
 */
public class UIHelper {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static Point displaySize=null;

    public static Point getDisplaySize(Activity context){
        if(displaySize==null) {
            Display display = context.getWindowManager().getDefaultDisplay();
            displaySize = new Point();
            displaySize.set(display.getWidth(), display.getHeight());
        }
        return displaySize;
    }
}
