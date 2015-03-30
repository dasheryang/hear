package hear.lib.share.models;

import android.support.annotation.NonNull;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.update.UmengUpdateAgent;

/**
 * Created by ZhengYi on 15/2/9.
 */
public class ShareContent {
    public String articleID;
    public String title;
    public String text;
    public String imageURL;
    private String targetURL;

    public ShareContent() {
    }

    public ShareContent init(String articleID, String title, String text, String imageURL) {
        this.articleID = articleID;
        this.title = title;
        this.text = text;
        this.imageURL = imageURL;
        this.targetURL = null;
        return this;
    }

    public String getTargetURL(@NonNull SHARE_MEDIA media) {
        return "http://101.66.255.114:8080/share.php?aid=" + articleID + "&plantform=" + media.name();
    }
}
