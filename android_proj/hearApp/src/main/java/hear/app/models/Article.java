package hear.app.models;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;

import java.text.SimpleDateFormat;
import java.util.Date;

import hear.app.helper.AppContext;
import hear.app.helper.ArrayUtils;
import hear.app.helper.LogUtil;
import hear.app.helper.UIHelper;
import hear.app.service.CacheMediaService;

/**
 * Created by power on 14-8-11.
 * <p/>
 * "pageno": 1, "txt": "那时我们有梦/关于文学，关于爱情/关于穿越世界的旅行/如今我们深夜饮酒/杯子碰到一起 /都是梦破碎的声音 ",
 * "imgurl":
 * "http://118.192.73.182:3000/images/2014-08-1050037365d9d5076cb6006d911378b2e1.jpg"
 * , "soundurl":
 * "http://118.192.73.182:3000/sounds/2014-08-10e4479bc306bc46a06e5a9ce08659b30b.mp3"
 * , "showtime": 1407628800000, "likenum": -2, "showauthor": "by 北岛", "haslike":
 * 0
 */
public class Article {

    /**
     * 南泉 10:21:24 管理地址http://118.192.73.182:3000/ 南泉 10:21:58
     * client查询：http://118.192.73.182:3000/getlist 南泉 10:22:38
     * client点击：http://118.192.73.182:3000/clicklike？加参数
     */
    public String name;
    public int pageno;
    public String txt;
    public String imgurl;
    public String soundurl;
    public long showtime;
    public int likenum;
    public String showauthor;
    public int haslike;

    public String getShowTime() {
        Date d = new Date(showtime);
        return new SimpleDateFormat("MM/dd/yyyy").format(d);
    }

    public String getSimpleDate() {
        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
        return sm.format(new Date(showtime));
    }

    @Override
    public boolean equals(Object rObject) {
        return rObject instanceof Article && hashCode() == rObject.hashCode();
    }

    @Override
    public int hashCode() {
        return pageno;
    }

    public final static String KEY_ALL_ARTICLE = "all_article";

    /**
     *
     */
    public static void saveArtilcleList(String json) {
        AppContext.getSharedPrefernce().put(KEY_ALL_ARTICLE, json);
        Intent service = new Intent(AppContext.getContext(),
                CacheMediaService.class);
        service.putExtra(KEY_ALL_ARTICLE, json);
        AppContext.getContext().startService(service);
    }

    /**
     * 返回所有的article
     */
    public static Article[] getAllArticles() {
        String json = AppContext.getSharedPrefernce().get(KEY_ALL_ARTICLE);
        Article[] result = AppContext.getGSON().fromJson(json, Article[].class);
        return result;
    }

    public static Article getArticleByPageNo(final int pageno) {
        Article[] articles = getAllArticles();
        return ArrayUtils.findFirst(ArrayUtils.from(articles),
                new ArrayUtils.EqualeOP<Article>() {
                    @Override
                    public boolean test(Article src) {
                        return src.pageno == pageno;
                    }
                });
    }

    public static final String KEY_PLAYED_PREFIX = "played_";

    /**
     * 第x页是播放过的
     *
     * @return
     */
    public static boolean isPlayed(String date) {
        return AppContext.getSharedPrefernce().get(KEY_PLAYED_PREFIX + date,
                false);
    }

    public static void setPlayed(String date) {
        AppContext.getSharedPrefernce().put(KEY_PLAYED_PREFIX + date, true);
    }

    public static int[] imgWidth = {978, 652, 435};
    public static int[] imgHeight = {812, 542, 340};

    public String getImageURL(Activity act) {
        Point p = UIHelper.getDisplaySize(act);
        int idx = 0;
        if (p.x < imgWidth[1]) {
            idx = 1;
        }
        if (p.x < imgWidth[2]) {
            idx = 2;
        }
        LogUtil.d("imgurl:" + imgurl);
        String[] parts = imgurl.split("\\.");
        StringBuilder sb = new StringBuilder();
        parts[parts.length - 2] = parts[parts.length - 2] + "_" + imgWidth[idx]
                + "x" + imgHeight[idx];
        return ArrayUtils.join(ArrayUtils.from(parts), ".");
    }
}
