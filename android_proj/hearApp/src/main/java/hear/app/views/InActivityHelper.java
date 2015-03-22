package hear.app.views;

import android.app.Activity;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import hear.app.engine.BaseHttpAsyncTask;
import hear.app.helper.ArrayUtils;
import hear.app.helper.DeviceUtil;
import hear.app.helper.LogUtil;
import hear.app.models.Article;
import hear.app.models.JsonRespWrapper;
import hear.app.store.ArticleStore;

/**
 * Created by power on 14-8-28.
 */
public class InActivityHelper {

    protected Activity mActivity;

    public InActivityHelper(Activity a) {
        this.mActivity = a;
    }

    private void gotoHistory() {
        Intent i = new Intent(mActivity, MainActivity.class);
        mActivity.startActivity(i);
        mActivity.finish();
    }

    public boolean isTodayPlayed() {
        List<Article> allArticle = ArticleStore.getInstance().getArticleSet();
        if (ArrayUtils.isEmpty(allArticle)) {
            return false;
        }
        String datestr = sm.format(new Date());
        return Article.isPlayedWithDate(datestr);
    }

    SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");

    protected void initEntranceActivity() {
        LogUtil.e("come from splash activity");
        //今天是否已经播放过，并且时间是十点之后或者最新的文章没有播放过
        if (isTodayPlayed()) {
            LogUtil.e("today is played:" + sm.format(new Date(System.currentTimeMillis())));
            gotoHistory();
        } else {
            Article latestArticle = ArticleStore.getInstance().firstArticle();
            if (latestArticle != null && !Article.isPlayedWithArticle(latestArticle)) {
                LogUtil.e("the latest article not played");
                startEnterActivity(latestArticle.pageno);
            } else {
                LogUtil.e("today is not played and get remote articles");
                getRemoteActicles(1);
            }
        }
    }

    /**
     * articlelist
     */
    public class ArticleListWrapper extends JsonRespWrapper {
        public Article[] data;
    }

    public void getRemoteActicles(final int retryCount) {
        getRemoteActicles(retryCount, null);
    }

    public static interface OnFinishListener {
        public void onFinish();
    }

    /**
     * 获取所有的文章
     */
    public void getRemoteActicles(final int retryCount, final OnFinishListener listener) {

        final boolean isHistory = (listener != null);

        String url = "http://www.hearheart.com/getlist";

        BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

            @Override
            public Class getRespClass() {
                return ArticleListWrapper.class;
            }

            @Override
            protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {

                if (jsonRespWrapper.ret == 0) {
                    ArticleListWrapper wrapper = (ArticleListWrapper) jsonRespWrapper;
                    LogUtil.d("resp data:" + wrapper.data);
                    ArticleStore.getInstance().setArticleSet(wrapper.data);
                    final Date now = new Date(System.currentTimeMillis());
                    if (isTodayPlayed()) {
                        if (!isHistory) {
                            gotoHistory();
                        }
                    } else {
//                        Article todayArticle = ArrayUtils.findFirst(ArrayUtils.from(wrapper.data), new ArrayUtils.EqualeOP<Article>() {
//                            @Override
//                            public boolean test(Article src) {
//                                return sm.format(new Date(src.showtime)).equals(sm.format(now));
//                            }
//                        });

                        Article latestArticle = ArticleStore.getInstance().firstArticle();

                        if (latestArticle != null && !Article.isPlayedWithArticle(latestArticle)) {
                            LogUtil.d("find today's article");
                            startEnterActivity(latestArticle.pageno);
                            if (isHistory) {
                                listener.onFinish();
                            }
                        } else {
                            LogUtil.d("can't find today's article and go act_main");
                            if (!isHistory) {
                                gotoHistory();
                            }
                        }
                    }

                } else {
                    if (retryCount > 0) {
                        LogUtil.d("retry ");
                        getRemoteActicles(retryCount - 1);
                    } else {
                        LogUtil.d("get response fail:" + jsonRespWrapper.ret);
                        gotoHistory();
                    }
                }
            }
        };

        HashMap<String, String> params = new HashMap<>();
        params.put("PhoneId", DeviceUtil.getPhoneId());
        asyncTask.get(params).execute();
    }

    private void startEnterActivity(int pageno) {
        PlayActivityV2.show(mActivity, ArticleStore.getInstance().getArticleWithPageNo(pageno));
        mActivity.finish();
//        Intent i = new Intent(mActivity, PlayActivity.class);
//        i.putExtra(PlayActivity.KEY_FROM, "splash");
//        i.putExtra(PlayActivity.KEY_PAGE_NO, pageno);
//        mActivity.startActivity(i);
//        mActivity.finish();
    }
}
