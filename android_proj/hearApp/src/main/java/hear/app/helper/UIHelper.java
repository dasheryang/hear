package hear.app.helper;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by power on 14-8-15.
 */
public class UIHelper {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static Point displaySize = null;

    public static Point getDisplaySize(Context context) {
        if (displaySize == null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            displaySize = new Point();
            displaySize.set(display.getWidth(), display.getHeight());
        }
        return displaySize;
    }
}
