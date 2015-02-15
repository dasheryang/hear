package hear.app.views;

import android.graphics.Bitmap;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import hear.app.R;
import hear.app.engine.BaseHttpAsyncTask;
import hear.app.helper.DeviceUtil;
import hear.app.helper.LogUtil;
import hear.app.helper.SDCardUtils;
import hear.app.helper.ToastUtil;
import hear.app.media.PlayListener;
import hear.app.media.Player;
import hear.app.models.Article;
import hear.app.models.ArticleLike;
import hear.app.models.JsonRespWrapper;

/**
 * Created by power on 14-8-11.
 */
public class ArticleFragment extends Fragment implements PlayListener {

    private LinearLayout likeContainer;
    private ImageView playIcon = null;
    private ImageView playLoading = null;
    private ImageView likeIcon = null;
    private TextView likeCount = null;
    private TextView articleContent = null;
    private TextView articleAuthor = null;
    private ImageView coverPicture = null;
    private TextView articleVol = null;
    private TextView date = null;
    private SeekBar mSeekBar;
    private TextView mMusicCurrentTimeTextView = null;
    private TextView mMusicTotalTimeTextView = null;
    private ProgressBar pb;

    private Animation animation;
    private Handler mHandler = new Handler();
    private SeekBarHandler mSeekBarHandler = new SeekBarHandler(this);
    private UILogic mUILogic = new UILogic();

    public static final int UN_LIKE_LEVEL = 1;
    public static final int LIKE_LEVEL = 2;
    public static final String KEY_ARTICLE = "article";
    private static final String TAG = "ArticleFragment";
    public static int LEVEL_PLAY = 1;
    public static int LEVEL_PAUSE = 2;

    private static class SeekBarHandler extends Handler {

        private WeakReference<ArticleFragment> instance;

        public SeekBarHandler(ArticleFragment fragment) {
            instance = new WeakReference<>(fragment);
        }

        public void handleMessage(android.os.Message msg) {

            if (instance != null && instance.get() != null) {
                instance.get().updateSeekBarProgress();

                if (Player.instance().isPlaying()) {
                    sendEmptyMessageDelayed(0, 1000);
                }
            }
        }
    }

    public class ImageShowerListener extends SimpleImageLoadingListener {

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            pb.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view,
                                    FailReason failReason) {
            pb.setVisibility(View.GONE);
            ToastUtil.Short("拉取图片失败.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.article_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        initContentView();
    }

    @Override
    public void onOtherStart() {
        setPlayIconLevel(LEVEL_PLAY);

    }

    private void setPlayIconLevel(final int level) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (playIcon != null) {
                    playLoading.setVisibility(View.GONE);
                    playLoading.clearAnimation();
                    playIcon.setVisibility(View.VISIBLE);
                    LevelListDrawable bg = (LevelListDrawable) playIcon
                            .getBackground();
                    bg.setLevel(level);
                }
            }
        });
    }

    private void setLoading() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                playIcon.setVisibility(View.GONE);
                playLoading.setVisibility(View.VISIBLE);
                playLoading.startAnimation(getAnimation());
            }
        });
    }

    private int getPlayIconLeve() {
        LevelListDrawable bg = (LevelListDrawable) playIcon.getBackground();
        return bg.getLevel();
    }

    @Override
    public void onComplete() {
        setPlayIconLevel(LEVEL_PLAY);
    }

    @Override
    public void onLoadingEnd() {
        mSeekBarHandler.sendEmptyMessage(0);
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mMusicCurrentTimeTextView.setVisibility(View.VISIBLE);
                mMusicTotalTimeTextView.setVisibility(View.VISIBLE);
            }
        });
        setPlayIconLevel(LEVEL_PAUSE);
    }

    public Animation getAnimation() {
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(this.getActivity(),
                    R.anim.tip);
            LinearInterpolator lin = new LinearInterpolator();
            animation.setInterpolator(lin);
        }
        return animation;
    }

    private void bindViews(View rootView) {
        mSeekBar = (SeekBar) rootView.findViewById(R.id.play_seekbar);
        pb = (ProgressBar) rootView.findViewById(R.id.image_loading);
        date = (TextView) rootView.findViewById(R.id.date);
        articleVol = (TextView) rootView.findViewById(R.id.vol_id);
        coverPicture = (ImageView) rootView.findViewById(R.id.cover_picture);
        articleContent = (TextView) rootView.findViewById(R.id.content);
        articleAuthor = (TextView) rootView.findViewById(R.id.author);
        likeCount = (TextView) rootView.findViewById(R.id.like_count);
        likeIcon = (ImageView) rootView.findViewById(R.id.like_icon);
        likeContainer = (LinearLayout) rootView.findViewById(R.id.id_like_contaner);
        playIcon = (ImageView) rootView.findViewById(R.id.play);
        playLoading = (ImageView) rootView.findViewById(R.id.play_loading);

        mMusicCurrentTimeTextView = (TextView) rootView
                .findViewById(R.id.play_current_time);
        mMusicTotalTimeTextView = (TextView) rootView
                .findViewById(R.id.play_total_time);
    }

    private void initContentView() {
        final Article article = mUILogic.getArticle();
        date.setText(article.getShowTime());
        articleVol.setText("VOL." + article.pageno);

        articleAuthor.setText(article.showauthor);

        ImageLoader.getInstance().displayImage(
                article.getImageURL(getActivity()), coverPicture,
                new ImageShowerListener());
        articleContent.setText(article.txt);
        articleContent.setMovementMethod(new ScrollingMovementMethod());

        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getPlayIconLeve() == LEVEL_PLAY) {

                    String playUrl = mUILogic.getPlayUrl(article.soundurl);

                    if (Player.instance().isPause(playUrl)) {
                        Player.instance().resume();
                        setPlayIconLevel(LEVEL_PAUSE);
                    } else {
                        Player.instance().play(playUrl, ArticleFragment.this);
                        setLoading();
                    }

                } else {
                    Player.instance().pause();
                    setPlayIconLevel(LEVEL_PLAY);
                }
            }
        });

        final LevelListDrawable drawable = (LevelListDrawable) likeIcon
                .getBackground();

        int articleLike = ArticleLike.getLikeArticle(mUILogic.getArticle().pageno);
        articleLike = (articleLike == -1) ? mUILogic.getArticle().haslike : articleLike;

        if (articleLike == 0) {
            drawable.setLevel(UN_LIKE_LEVEL);
        } else {
            drawable.setLevel(LIKE_LEVEL);
        }

        likeCount.setText("" + ArticleLike.getLikeCount(article.pageno));
        likeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int level = drawable.getLevel();
                if (level == UN_LIKE_LEVEL) {
                    drawable.setLevel(LIKE_LEVEL);
                    ArticleLike.setLikeArticle(mUILogic.getArticle().pageno, 1);
                    mUILogic.likeArticleRemote(mUILogic.getArticle().pageno);
                    ArticleLike.incLikeCount(mUILogic.getArticle().pageno);
                    incrLikeCount();
                } else {
                    drawable.setLevel(UN_LIKE_LEVEL);
                    ArticleLike.setLikeArticle(mUILogic.getArticle().pageno, 0);
                    mUILogic.unLikeArticleRemote(mUILogic.getArticle().pageno);
                    descLikeCount();
                    ArticleLike.descLikeCount(mUILogic.getArticle().pageno);
                }
            }
        });

        if (Player.instance().isPlaying(mUILogic.getPlayUrl(article.soundurl))) {
            setPlayIconLevel(LEVEL_PAUSE);
        } else {
            setPlayIconLevel(LEVEL_PLAY);
        }

    }

    private void updateSeekBarProgress() {
        mSeekBar.setMax(Player.instance().getMax());
        mSeekBar.setProgress(Player.instance().getCurrentPos());
        SimpleDateFormat df = new SimpleDateFormat("mm:ss");
        Date currentDate = new Date(Player.instance().getCurrentPos());
        Date totalDate = new Date(Player.instance().getMax());
        mMusicCurrentTimeTextView.setText(df.format(currentDate));
        mMusicTotalTimeTextView.setText(df.format(totalDate));
    }

    private void incrLikeCount() {
        int val = Integer.parseInt(likeCount.getText().toString());
        likeCount.setText(String.valueOf(val + 1));
    }

    private void descLikeCount() {
        int val = Integer.parseInt(likeCount.getText().toString());
        if (val > 0) {
            likeCount.setText(String.valueOf(val - 1));
        }
    }

    private class UILogic {
        private Article getArticle() {
            int pageno = getArguments().getInt(KEY_ARTICLE);
            return Article.getArticleByPageNo(pageno);
        }

        private void likeArticleRemote(int pageno) {

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

        private void unLikeArticleRemote(int pageno) {

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
        }

        private String getPlayUrl(String soundUrl) {
            String mediaName = soundUrl.substring(soundUrl.lastIndexOf("/") + 1);

            File file = new File(SDCardUtils.getMediaCachePath(), mediaName);

            if (file.exists()) {
                Log.d(TAG, mediaName + " 在本地已经有缓存");
                return file.getPath();
            }

            return soundUrl;

        }
    }
}
