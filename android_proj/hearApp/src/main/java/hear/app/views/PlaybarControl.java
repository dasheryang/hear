package hear.app.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hear.app.R;
import hear.app.helper.SDCardUtils;
import hear.app.media.PlayListener;
import hear.app.media.Player;
import hear.app.models.Article;
import hear.app.widget.ProgressWheel;

/**
 * Created by ZhengYi on 15/2/20.
 */
public class PlaybarControl {
    @InjectView(R.id.label_volume)
    TextView mVolumeLabel;
    @InjectView(R.id.label_author)
    TextView mAuthorLabel;
    @InjectView(R.id.img_thumb)
    ImageView mThumbImage;
    @InjectView(R.id.img_play)
    ImageView mPlayImage;
    @InjectView(R.id.img_loading)
    ImageView mLoadingImage;
    @InjectView(R.id.pb_duration)
    ProgressWheel mProgressWheel;

    private boolean mIsPrepared = false;
    private Animation mAnimation;
    private Article mDefaultArticle;
    private Context mContext;
    private Handler mHandler;

    private Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            Player player = Player.getInstance();
            if (player.isPlaying() || player.isPause()) {
                if (player.getMax() > 0 && player.getMax() >= player.getCurrentPos() + 500) {
                    mProgressWheel.setProgress(player.getCurrentPos() * 360 / player.getMax());
                    mProgressWheel.setContourColor(Color.parseColor("#33ffffff"));
                    mProgressWheel.setRimColor(Color.parseColor("#33ffffff"));
                    mHandler.postDelayed(this, 1000L);
                } else {
                    mProgressWheel.setContourColor(Color.parseColor("#ffffff"));
                    mProgressWheel.setRimColor(Color.parseColor("#ffffff"));
                    mProgressWheel.setProgress(0);
                }
            } else {
                mProgressWheel.setContourColor(Color.parseColor("#ffffff"));
                mProgressWheel.setRimColor(Color.parseColor("#ffffff"));
                mProgressWheel.setProgress(0);
            }
        }
    };

    private PlayListener mPlayListener = new PlayListener() {
        @Override
        public void onOtherStart() {
            update();
        }

        @Override
        public void onComplete() {
            update();
        }

        @Override
        public void onLoadingEnd() {
            update();
        }
    };

    public PlaybarControl(Context context) {
        mContext = context;
        mHandler = new Handler(context.getMainLooper());
    }

    public void prepare(View rootView) {
        if (!mIsPrepared) {
            mIsPrepared = true;
            ButterKnife.inject(this, rootView.findViewById(R.id.playbar));
            update();
        }
    }

    public void onActivityResume() {
        mHandler.removeCallbacks(mUpdateProgressTask);
        mUpdateProgressTask.run();
    }

    public void onActivityPause() {
        mHandler.removeCallbacks(mUpdateProgressTask);
    }

    public void setDefaultArticle(Article article) {
        mDefaultArticle = article;
        update();
    }

    public void playArticle(Article article) {
        Player player = Player.getInstance();
        String url = getPlayUrl(article.soundurl);
        if (player.isPlaying(url)) {
            player.pause();
            update();
            mHandler.removeCallbacks(mUpdateProgressTask);
        } else if (player.isPause(url)) {
            player.resume();
            update();
            mHandler.removeCallbacks(mUpdateProgressTask);
            mUpdateProgressTask.run();
        } else {
            player.play(article, url, mPlayListener);
            update();
            mHandler.removeCallbacks(mUpdateProgressTask);
            mUpdateProgressTask.run();
        }
    }

    public void update() {
        if (!mIsPrepared)
            return;

        Article article = Player.getInstance().getLastPlayArticle();
        if (article == null) {
            article = mDefaultArticle;
        }

        if (article == null) {
            mVolumeLabel.setText("");
            mAuthorLabel.setText("");
            mThumbImage.setImageBitmap(null);
            mPlayImage.setImageResource(R.drawable.ic_playbar_play);
            mPlayImage.setVisibility(View.VISIBLE);
            mLoadingImage.setVisibility(View.GONE);
        } else {
            mVolumeLabel.setText("VOL. " + article.pageno);
            mAuthorLabel.setText(article.showauthor);
            ImageLoader.getInstance().displayImage(article.getImageURL(mContext), mThumbImage);

            String url = getPlayUrl(article.soundurl);
            Player player = Player.getInstance();
            boolean isLoading = player.isLoading(url);
            mLoadingImage.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            mPlayImage.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            if (isLoading) {
                if (mLoadingImage.getAnimation() == null)
                    mLoadingImage.startAnimation(getAnimation(mContext));
            } else if (player.isPlaying()) {
                mLoadingImage.clearAnimation();
                mPlayImage.setImageResource(R.drawable.ic_playbar_pause);
            } else if (player.isPause()) {
                mLoadingImage.clearAnimation();
                mPlayImage.setImageResource(R.drawable.ic_playbar_play);
            } else {
                mLoadingImage.clearAnimation();
                mPlayImage.setImageResource(R.drawable.ic_playbar_play);
                mProgressWheel.setContourColor(Color.parseColor("#ffffff"));
                mProgressWheel.setRimColor(Color.parseColor("#ffffff"));
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.container_play)
    void onPlayImageClick() {
        Player player = Player.getInstance();

        Article article = player.getLastPlayArticle();
        if (article == null)
            article = mDefaultArticle;

        if (article == null)
            return;

        playArticle(article);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.playbar)
    void onPlaybarClick() {
        Player player = Player.getInstance();

        Article article = player.getLastPlayArticle();
        if (article == null)
            article = mDefaultArticle;

        if (article == null)
            return;

        FullScreenArticleActivity.show(mContext, article);
    }

    private Animation getAnimation(Context context) {
        if (mAnimation == null) {
            mAnimation = AnimationUtils.loadAnimation(context,
                    R.anim.tip);
            LinearInterpolator lin = new LinearInterpolator();
            mAnimation.setInterpolator(lin);
        }
        return mAnimation;
    }

    private String getPlayUrl(String soundUrl) {
        String mediaName = soundUrl.substring(soundUrl.lastIndexOf("/") + 1);

        File file = new File(SDCardUtils.getMediaCachePath(), mediaName);

        if (file.exists()) {
            return file.getPath();
        }

        return soundUrl;

    }
}
