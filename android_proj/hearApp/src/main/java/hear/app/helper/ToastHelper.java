package hear.app.helper;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import hear.app.R;

/**
 * Created by ZhengYi on 15/2/21.
 */
public class ToastHelper {
    private ToastHelper() {
    }

    public static void showCollected(Context context) {
        createToast(context, R.string.hint_collected, R.drawable.ic_hint_collect, Toast.LENGTH_SHORT).show();
    }

    public static void showNetworkError(Context context) {
        createToast(context, R.string.hint_network_error, R.drawable.ic_hint_message, Toast.LENGTH_SHORT).show();
    }

    public static void showCopyLinkSuccess(Context context) {
        createToast(context, "已复制到粘贴版", R.drawable.ic_hint_success, Toast.LENGTH_SHORT).show();
    }

    private static Toast createToast(Context context, int textID, int iconID, int duration) {
        return createToast(context, context.getResources().getString(textID), iconID, duration);
    }

    private static Toast createToast(Context context, String text, int iconID, int duration) {
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflate.inflate(R.layout.widget_toast, null);
        ((ImageView) contentView.findViewById(R.id.img_icon)).setImageResource(iconID);
        ((TextView) contentView.findViewById(R.id.label_msg)).setText(text);
        Toast toast = new Toast(context);
        toast.setView(contentView);
        toast.setDuration(duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        return toast;
    }
}
