package hear.lib.share.models;

import com.umeng.update.UmengUpdateAgent;

/**
 * Created by ZhengYi on 15/2/9.
 */
public class ShareContent {
    public String title;
    public String text;
    public String imageURL;
    public String targetURL;

    public ShareContent() {
    }

    public ShareContent init(String title, String text, String imageURL, String targetURL) {
        this.title = title;
        this.text = text;
        this.imageURL = imageURL;
        this.targetURL = targetURL;
        return this;
    }
}
