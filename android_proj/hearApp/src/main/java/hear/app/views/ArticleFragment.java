package hear.app.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.listener.SocializeListeners;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import hear.app.R;
import hear.app.engine.BaseHttpAsyncTask;
import hear.app.helper.DeviceUtil;
import hear.app.helper.SDCardUtils;
import hear.app.helper.ToastUtil;
import hear.app.media.PlayListener;
import hear.app.media.Player;
import hear.app.models.Article;
import hear.app.models.ArticleLike;
import hear.app.models.CollectedArticleStore;
import hear.app.models.JsonRespWrapper;
import hear.lib.share.SocialServiceWrapper;
import hear.lib.share.models.ShareContent;

/**
 * Created by power on 14-8-11.
 */
public class ArticleFragment extends Fragment implements PlayListener {

    private LinearLayout mLikeContainer;
    private ImageView mPlayIconView = null;
    private ImageView playLoading = null;
    private ImageView likeIcon = null;
    private TextView mLikeCountLabel = null;
    private TextView mArticleContentLabel = null;
    private TextView mAuthorLabel = null;
    private ImageView mCoverImageView = null;
    private TextView mVolumeLabel = null;
    private TextView mDateLabel = null;
    private SeekBar mSeekBar;
    private TextView mMusicCurrentTimeTextView = null;
    private TextView mMusicTotalTimeTextView = null;
    private ProgressBar pb;

    private Animation animation;
    private Handler mHandler = new Handler();
    private SeekBarHandler mSeekBarHandler = new SeekBarHandler(this);
    private LogicControl mLogicControl = new LogicControl();
    private SocialServiceWrapper mShareService;
    private boolean mIsFirstResume = true;

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

                if (Player.getInstance().isPlaying()) {
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
        setHasOptionsMenu(true);
        bindViews(view);
        initContentView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirstResume) {
            mIsFirstResume = false;
        } else {
            if (Player.getInstance().isPlaying(mLogicControl.getPlayUrl(mLogicControl.getArticle().soundurl))) {
                setPlayIconLevel(LEVEL_PAUSE);
            } else {
                setPlayIconLevel(LEVEL_PLAY);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_frag_article, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.item_share) {
            mLogicControl.performShare();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mShareService != null) {
            mShareService.handleOnActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onOtherStart() {
        setPlayIconLevel(LEVEL_PLAY);
    }

    private void setPlayIconLevel(final int level) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mPlayIconView != null) {
                    playLoading.setVisibility(View.GONE);
                    playLoading.clearAnimation();
                    mPlayIconView.setVisibility(View.VISIBLE);
                    LevelListDrawable bg = (LevelListDrawable) mPlayIconView
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
                mPlayIconView.setVisibility(View.GONE);
                playLoading.setVisibility(View.VISIBLE);
                playLoading.startAnimation(getAnimation());
            }
        });
    }

    private int getPlayIconLeve() {
        LevelListDrawable bg = (LevelListDrawable) mPlayIconView.getBackground();
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
        mDateLabel = (TextView) rootView.findViewById(R.id.date);
        mVolumeLabel = (TextView) rootView.findViewById(R.id.vol_id);
        mCoverImageView = (ImageView) rootView.findViewById(R.id.cover_picture);
        mArticleContentLabel = (TextView) rootView.findViewById(R.id.content);
        mAuthorLabel = (TextView) rootView.findViewById(R.id.author);
        mLikeCountLabel = (TextView) rootView.findViewById(R.id.like_count);
        likeIcon = (ImageView) rootView.findViewById(R.id.like_icon);
        mLikeContainer = (LinearLayout) rootView.findViewById(R.id.id_like_contaner);
        mPlayIconView = (ImageView) rootView.findViewById(R.id.play);
        playLoading = (ImageView) rootView.findViewById(R.id.play_loading);

        mMusicCurrentTimeTextView = (TextView) rootView
                .findViewById(R.id.play_current_time);
        mMusicTotalTimeTextView = (TextView) rootView
                .findViewById(R.id.play_total_time);
    }

    private void initContentView() {
        final Article article = mLogicControl.getArticle();
        mDateLabel.setText(article.getShowTime());
        mVolumeLabel.setText("VOL." + article.pageno);

        mAuthorLabel.setText(article.showauthor);

        ImageLoader.getInstance().displayImage(
                article.getImageURL(getActivity()), mCoverImageView,
                new ImageShowerListener());
        mCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullScreenArticleActivity.show(getActivity(), mLogicControl.getArticle());
            }
        });

        mArticleContentLabel.setText(article.txt);
        mArticleContentLabel.setMovementMethod(new ScrollingMovementMethod());

        mPlayIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getPlayIconLeve() == LEVEL_PLAY) {

                    String playUrl = mLogicControl.getPlayUrl(article.soundurl);

                    if (Player.getInstance().isPause(playUrl)) {
                        Player.getInstance().resume();
                        setPlayIconLevel(LEVEL_PAUSE);
                    } else {
                        Player.getInstance().play(playUrl, ArticleFragment.this);
                        setLoading();
                    }

                } else {
                    Player.getInstance().pause();
                    setPlayIconLevel(LEVEL_PLAY);
                }
            }
        });

        final LevelListDrawable drawable = (LevelListDrawable) likeIcon
                .getBackground();

        int isLikeInt = ArticleLike.getLikeArticle(mLogicControl.getArticle().pageno);
        isLikeInt = (isLikeInt == -1) ? mLogicControl.getArticle().haslike : isLikeInt;

        if (isLikeInt == 0) {
            drawable.setLevel(UN_LIKE_LEVEL);
        } else {
            drawable.setLevel(LIKE_LEVEL);
        }

        mLikeCountLabel.setText("" + ArticleLike.getLikeCount(article.pageno));
        mLikeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int level = drawable.getLevel();
                if (level == UN_LIKE_LEVEL) {
                    drawable.setLevel(LIKE_LEVEL);
                    mLogicControl.likeArticle(mLogicControl.getArticle().pageno);
                    incrLikeCount();
                } else {
                    drawable.setLevel(UN_LIKE_LEVEL);
                    mLogicControl.unlikeArticle(mLogicControl.getArticle().pageno);
                    ArticleLike.descLikeCount(mLogicControl.getArticle().pageno);
                }
            }
        });

        if (Player.getInstance().isPlaying(mLogicControl.getPlayUrl(article.soundurl))) {
            setPlayIconLevel(LEVEL_PAUSE);
        } else {
            setPlayIconLevel(LEVEL_PLAY);
        }

    }

    private void updateSeekBarProgress() {
        mSeekBar.setMax(Player.getInstance().getMax());
        mSeekBar.setProgress(Player.getInstance().getCurrentPos());
        SimpleDateFormat df = new SimpleDateFormat("mm:ss");
        Date currentDate = new Date(Player.getInstance().getCurrentPos());
        Date totalDate = new Date(Player.getInstance().getMax());
        mMusicCurrentTimeTextView.setText(df.format(currentDate));
        mMusicTotalTimeTextView.setText(df.format(totalDate));
    }

    private void incrLikeCount() {
        int val = Integer.parseInt(mLikeCountLabel.getText().toString());
        mLikeCountLabel.setText(String.valueOf(val + 1));
    }

    private void descLikeCount() {
        int val = Integer.parseInt(mLikeCountLabel.getText().toString());
        if (val > 0) {
            mLikeCountLabel.setText(String.valueOf(val - 1));
        }
    }

    private class LogicControl {
        private Article mArticle;

        private Article getArticle() {
            if (mArticle == null) {
                int pageno = getArguments().getInt(KEY_ARTICLE);
                mArticle = Article.getArticleByPageNo(pageno);
            }

            return mArticle;
        }

        private void likeArticle(int pageno) {
            ArticleLike.setLikeArticle(mLogicControl.getArticle().pageno, 1);
            ArticleLike.incLikeCount(mLogicControl.getArticle().pageno);

            CollectedArticleStore.getInstance().add(getArticle());

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

        private void unlikeArticle(int pageno) {
            ArticleLike.setLikeArticle(mLogicControl.getArticle().pageno, 0);
            descLikeCount();

            CollectedArticleStore.getInstance().remove(getArticle());

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

        private void performShare() {
            mShareService = new SocialServiceWrapper(getActivity());
            Article article = mLogicControl.getArticle();
            mShareService.setShareContent(new ShareContent().init(article.name, article.txt, article.imgurl, "http://www.baidu.com"));
            mShareService.showShareBoard(new SocializeListeners.SnsPostListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onComplete(SHARE_MEDIA media, int i, SocializeEntity socializeEntity) {
                    mShareService = null;
                }
            });
            Activity parent = getActivity();
            if (parent != null && parent instanceof ShareFragmentDelegate) {
                ((ShareFragmentDelegate) parent).onFragmentPerformShare(ArticleFragment.this);
            }
        }
    }
}
