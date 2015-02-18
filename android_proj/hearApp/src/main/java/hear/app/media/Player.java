package hear.app.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;

import java.io.File;

import hear.app.helper.LogUtil;
import hear.app.helper.ToastUtil;

/**
 * Created by power on 14-8-23.
 */
public class Player {
    public final static int STATE_PLAYING = 1;
    public final static int STATE_PAUSE = 2;

    private static Player sInstance;
    private MediaPlayer mediaPlayer;
    private String mCurrentUrl = null;
    private PlayListener mPlayerListener;
    private Handler mHandler = new Handler();

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

    public boolean isPlaying() {
        return !TextUtils.isEmpty(mCurrentUrl) && mediaPlayer != null
                && mediaPlayer.isPlaying();
    }

    public boolean isPlaying(String url) {
        return isPlaying() && mCurrentUrl.equalsIgnoreCase(url);
    }

    public boolean isPause(String url) {
        return !TextUtils.isEmpty(mCurrentUrl) && mCurrentUrl.equals(url);
    }

    public void resume() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean play(String url, PlayListener listener) {
        if (!TextUtils.isEmpty(url)) {
            if (url.equals(mCurrentUrl)) {
                return true;
            }
            mCurrentUrl = url;
            playUsingURL(url, listener);
            return true;
        }
        return false;
    }

    public void pause() {
        LogUtil.d("pause");
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void playUsingURL(final String url, PlayListener listener) {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        if (mPlayerListener != null) {
            mPlayerListener.onOtherStart();
        }

        mPlayerListener = listener;
        // url ="http://static.dbmeizi.com/1973.mp3";
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {

            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)

            mediaPlayer
                    .setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                            mPlayerListener.onLoadingEnd();
                            Runnable r = getProgressRunnable();
                            if (r != null) {
                                mHandler.postDelayed(r, 1000);
                            }
                        }
                    });

            mediaPlayer
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            LogUtil.d("on complete");
                            onMediaPlayComplete();
                        }
                    });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
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

    public int getState() {
        if (mediaPlayer != null
                && (mediaPlayer.isPlaying() || mediaPlayer.isLooping())) {
            return STATE_PLAYING;
        }
        return STATE_PAUSE;
    }

    public void onToggle() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public int getMax() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPos() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }
}
