package hear.lib.share;

/**
 * Created by ZhengYi on 15/2/8.
 */
public class ShareComponent {
    private static ShareComponent sInstance;

    private ShareComponent() {
    }

    public static ShareComponent getInstance() {
        if (sInstance == null)
            sInstance = new ShareComponent();

        return sInstance;
    }
}
