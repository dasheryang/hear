package hear.app.views;

import android.app.Activity;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import hear.app.engine.BaseHttpAsyncTask;
import hear.app.helper.AppContext;
import hear.app.helper.ArrayUtils;
import hear.app.helper.DeviceUtil;
import hear.app.helper.LogUtil;
import hear.app.models.Article;
import hear.app.models.ArticleLike;
import hear.app.models.JsonRespWrapper;

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
        Article[] allArticle = Article.getAllArticles();
        if (ArrayUtils.isEmpty(allArticle)) {
            return false;
        }
        final Date now = new Date(System.currentTimeMillis());

        String datestr = sm.format(now);
        LogUtil.d("datestr:" + datestr);
        return Article.isPlayed(datestr);
    }

    SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");

    protected void initEntranceActivity() {
        LogUtil.d("come from splash activity");
        //今天是否已经播放过，并且时间是十点之后
        if (isTodayPlayed()) {
            LogUtil.d("today is played:" + sm.format(new Date(System.currentTimeMillis())));
            gotoHistory();
        } else {
            LogUtil.d("today is not played and get remote articles");
            getRemoteActicles(1);
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
                    Article.saveArtilcleList(AppContext.getGSON().toJson(wrapper.data));
                    ArrayUtils.each(ArrayUtils.from(wrapper.data), new ArrayUtils.Processor<Article, Void>() {
                        @Override
                        public Void process(Article src) {
                            //和服务器保持一致
                            ArticleLike.setLikeCount(src.pageno, src.likenum);
                            ArticleLike.setLikeArticle(src.pageno, src.haslike);

                            return null;
                        }
                    });
                    final Date now = new Date(System.currentTimeMillis());
                    if (isTodayPlayed()) {
                        if (!isHistory) {
                            gotoHistory();
                        }
                    } else {

                        Article todayArticle = ArrayUtils.findFirst(ArrayUtils.from(wrapper.data), new ArrayUtils.EqualeOP<Article>() {
                            @Override
                            public boolean test(Article src) {
                                return sm.format(new Date(src.showtime)).equals(sm.format(now));
                            }
                        });

                        if (todayArticle != null) {
                            LogUtil.d("find today's article");
                            startEnterActivity(todayArticle.pageno);
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

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("PhoneId", DeviceUtil.getPhoneId());
        asyncTask.get(params).execute();
    }

    private void startEnterActivity(int pageno) {
        Intent i = new Intent(mActivity, PlayActivity.class);
        i.putExtra(PlayActivity.KEY_FROM, "splash");
        i.putExtra(PlayActivity.KEY_PAGE_NO, pageno);
        mActivity.startActivity(i);
        mActivity.finish();
    }
}
