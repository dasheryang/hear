package hear.app.helper;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;

import hear.app.models.Article;

/**
 * Created by ZhengYi on 15/2/23.
 * 统计的工具类
 */
public class StatHelper {
    private StatHelper() {
    }

    public static void onArticleLike(Context context, Article article) {
        HashMap<String, String> parmas = new HashMap<>();
        parmas.put("vol", "" + article.pageno);
        MobclickAgent.onEvent(context, "001", parmas);
    }

    public static void onArticleShare(Context context, Article article, SHARE_MEDIA media) {
        HashMap<String, String> parmas = new HashMap<>();
        parmas.put("vol", "" + article.pageno);
        parmas.put("platform", media.name());
        MobclickAgent.onEvent(context, "002", parmas);
    }
}
