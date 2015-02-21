package hear.app.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;

import java.io.File;

import hear.app.helper.LogUtil;
import hear.app.helper.ToastUtil;
import hear.app.models.Article;

/**
 * Created by power on 14-8-23.
 */
public class Player {
    public final static int STATE_PLAYING = 1;
    public final static int STATE_PAUSE = 2;

    private static Player sInstance;
    private MediaPlayer mMediaPlayer;
    private String mLastPlayURL = null;
    private Article mLastPlayArticle = null;
    private PlayListener mPlayerListener;
    private Handler mHandler = new Handler();
    private boolean mIsLoading = false;

    private Player() {
    }

    public static synchronized Player getInstance() {
        if (sInstance == null) {
            sInstance = new Player();
        }
        return sInstance;
    }

    public Runnable getProgressRunnable() {
        return null;
    }

    public void onMediaPlayComplete() {
        if (mPlayerListener != null) {
            mPlayerListener.onComplete();
        }
    }

    public boolean isLoading() {
        return !TextUtils.isEmpty(mLastPlayURL) && mMediaPlayer != null
                && mIsLoading;
    }

    public boolean isLoading(String url) {
        return isLoading() && mLastPlayURL.equals(url);
    }

    public boolean isPlaying() {
        return !TextUtils.isEmpty(mLastPlayURL) && isMediaPlaying();
    }

    public boolean isPlaying(String url) {
        return isPlaying() && mLastPlayURL.equalsIgnoreCase(url);
    }

    public boolean isPause(String url) {
        return !TextUtils.isEmpty(mLastPlayURL) && mLastPlayURL.equals(url);
    }

    public boolean play(Article article, String playURL, PlayListener listener) {
        if (!TextUtils.isEmpty(playURL)) {
            if (playURL.equals(mLastPlayURL)) {
                return true;
            }
            mLastPlayURL = playURL;
            mLastPlayArticle = article;
            playCode(playURL, listener);
            return true;
        }
        return false;
    }

    private void playCode(String playURL, PlayListener listener) {

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }

        if (mPlayerListener != null) {
            mPlayerListener.onOtherStart();
        }

        mPlayerListener = listener;
        // url ="http://static.dbmeizi.com/1973.mp3";
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        final String url = playURL;
        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync(); // might take long! (for buffering, etc)

            mIsLoading = true;
            mMediaPlayer
                    .setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mIsLoading = false;
                            mMediaPlayer.start();
                            mPlayerListener.onLoadingEnd();
                            Runnable r = getProgressRunnable();
                            if (r != null) {
                                mHandler.postDelayed(r, 1000);
                            }
                        }
                    });

            mMediaPlayer
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            onMediaPlayComplete();
                        }
                    });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    LogUtil.d("what:" + what);
                    if (!url.startsWith("http://")) {
                        File localFile = new File(url);
                        localFile.delete();
                    }
                    if (what == 1) {
                        ToastUtil.Short("播放错误，请确定网络是否联通");
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            // ToastUtil.Short("播放错误");
            if (!url.startsWith("http://")) {
                File localFile = new File(url);
                localFile.delete();
            }

            e.printStackTrace();
        }
    }

    public void resume() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        LogUtil.d("pause");
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public int getState() {
        if (mMediaPlayer != null
                && (mMediaPlayer.isPlaying() || mMediaPlayer.isLooping())) {
            return STATE_PLAYING;
        }
        return STATE_PAUSE;
    }

    public void onToggle() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    public int getMax() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPos() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public Article getLastPlayArticle() {
        return mLastPlayArticle;
    }

    private boolean isMediaPlaying() {
        try {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        } catch (IllegalStateException igonred) {
            return false;
        }
    }
}
