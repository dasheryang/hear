package hear.app.models;

import android.content.Context;
import android.graphics.Point;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import hear.app.engine.BaseHttpAsyncTask;
import hear.app.helper.AppContext;
import hear.app.helper.ArrayUtils;
import hear.app.helper.DeviceUtil;
import hear.app.helper.LogUtil;
import hear.app.helper.UIHelper;

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
    public final static String KEY_ALL_ARTICLE = "all_article";
    public static final String KEY_PLAYED_PREFIX = "played_";

    /**
     * 南泉 10:21:24 管理地址http://118.192.73.182:3000/ 南泉 10:21:58
     * client查询：http://118.192.73.182:3000/getlist 南泉 10:22:38
     * client点击：http://118.192.73.182:3000/clicklike？加参数
     */
    public String name;
    public int pageno;
    public String txt;
    public String soundurl;
    public long showtime;
    public String showauthor;
    private String imgurl;
    private int likenum;
    private int haslike;

    public String getShowTime() {
        Date d = new Date(showtime);
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
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

    public boolean hasLiked() {
        return haslike == 1;
    }

    public int likeNum() {
        return likenum;
    }

    public void toggleLikeState() {
        if (hasLiked()) {
            haslike = 0;
            likenum = Math.max(--likenum, 0);
            String url = "http://www.hearheart.com/cancellike";
            BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

                @Override
                protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {
                }
            };
            HashMap<String, String> params = new HashMap<>();
            params.put("PhoneId", DeviceUtil.getPhoneId());
            params.put("pageno", String.valueOf(pageno));
            asyncTask.get(params).execute();
        } else {
            haslike = 1;
            likenum++;
            String url = "http://www.hearheart.com/clicklike";
            BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

                @Override
                protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {
                }
            };
            HashMap<String, String> params = new HashMap<>();
            params.put("PhoneId", DeviceUtil.getPhoneId());
            params.put("pageno", String.valueOf(pageno));
            asyncTask.get(params).execute();
        }
    }

    public static boolean isPlayed(String date) {
        return AppContext.getSharedPrefernce().get(KEY_PLAYED_PREFIX + date,
                false);
    }

    public static void setPlayed(String date) {
        AppContext.getSharedPrefernce().put(KEY_PLAYED_PREFIX + date, true);
    }

    public static int[] imgWidth = {978, 652, 435};
    public static int[] imgHeight = {812, 542, 340};

    public String getImageURL(Context act) {
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
