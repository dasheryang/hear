package hear.app.views;

import hear.app.R;
import hear.app.models.Article;
import hear.app.helper.LogUtil;
import hear.app.widget.MasterLayout;

import java.text.SimpleDateFormat;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


/**
 * Activity For First Play
 */
public class PlayActivity extends BaseActivity {

    public static final String KEY_PAGE_NO = "pageno";

    public static final String KEY_FROM="from";

    private MasterLayout masterLayout;

    private MediaPlayer mediaPlayer=new MediaPlayer();

    private Handler mHandler=new Handler();

    SimpleDateFormat sm=new SimpleDateFormat("yyyy-MM-dd");

    private Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            int positon = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            int progress= (int)((positon/(float)duration)*100);
            if(progress>10){
                if(!TextUtils.isEmpty(mSoundDate)){
                    Article.setPlayed(mSoundDate);
                }
            }
            if(progress>=100){
                progress=100;
            }
            masterLayout.cusview.setupprogress(progress);
            if(duration>positon) {
                mHandler.postDelayed(this, 1000);
            }
        }
    };



    /**
     * 获取传入的pageno
     * @return
     */
    private int getInPageNo(){
        return getIntent().getIntExtra(KEY_PAGE_NO,-1);
    }

    private String getFrom(){
        return getIntent().getStringExtra(KEY_FROM);
    }



    /**
     *  跳转去文章
     */
    private void startArticleActivity(){

    }

    private void playUsingURL(String url){
        //url ="http://static.dbmeizi.com/1973.mp3";
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            LogUtil.d("media player start prepare");
            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    LogUtil.d("on prepared listener");
                    mediaPlayer.start();
                    mHandler.postDelayed(mRunnable,1000);
                    masterLayout.startPlay();

                }
            });


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    LogUtil.d("on complete");
                    onMediaPlayComplete();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    LogUtil.d("what:"+what);
                    return false;
                }
            });

            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    //LogUtil.d("on bufferring in percent"+percent);
                }
            });


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if(mediaPlayer!=null){
            mHandler.removeCallbacks(mRunnable);
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    /**
     *
     */
    private void onMediaPlayComplete(){
        if(isFromSplash()){
            gotoHistory();
        }
        else{
            finish();
        }
    }

    private void gotoHistory(){
        Intent i=new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }




    private String mSoundUrl;
    private String mSoundDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.play);
        masterLayout= (MasterLayout) findViewById(R.id.MasterLayout01);

        //Onclick listener of the progress button
        masterLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //masterLayout.animation();   //Need to call this method for animation and progression

                switch (masterLayout.flg_frmwrk_mode) {
                    case 1:
                        //Start state. Call your method
                        if(!TextUtils.isEmpty(mSoundUrl)){
                            playUsingURL(mSoundUrl);

                            masterLayout.startLoading();
                        }
                        break;
                    case 2:
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                            masterLayout.setPlayIcon();
                        }
                        else{
                            masterLayout.setPauseIcon();
                            mediaPlayer.start();
                        }
                        //LogUtil.d("case 2");
                        //Running state. Call your method
                        break;
                    case 3:
                        LogUtil.d("结束了");
                        //End state. Call your method
                        break;
                }
            }
        });

        int pageNo=getInPageNo();

        //设置URL
        Article a=Article.getArticleByPageNo(pageNo);
        mSoundUrl=a.soundurl;
        mSoundDate=a.getSimpleDate();
    }

    private boolean isFromSplash(){
        return "splash".equals(getFrom());
    }


}
