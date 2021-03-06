package hear.app.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SnsAccount;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.SocializeUser;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.exception.SocializeException;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hear.app.R;
import hear.app.helper.NotificationCenter;
import hear.app.helper.SDCardUtils;
import hear.app.helper.StatHelper;
import hear.app.helper.ToastHelper;
import hear.app.media.PlayListener;
import hear.app.media.Player;
import hear.app.models.Article;
import hear.app.store.CollectedArticleStore;
import hear.app.store.SNSAccountStore;
import hear.lib.share.SocialServiceWrapper;
import hear.lib.share.models.ShareContent;

/**
 * Created by ZhengYi on 15/2/16.
 */
public class FullScreenArticleFragment extends Fragment {
    private static final int STATE_PLAYING = 1;
    private static final int STATE_LOADING = 2;
    private static final int STATE_PAUSE = 3;
    private static final long UPDATE_PROGRESSBAR_INTERVAL = 1000L;

    @InjectView(R.id.image_play)
    ImageView mPlayImage;
    @InjectView(R.id.img_loading)
    ImageView mLoadingImage;
    @InjectView(R.id.pb_play)
    SeekBar mProgressBar;
    //    @InjectView(R.id.pb_duration)
//    ProgressWheel mProgressWheel;
    @InjectView(R.id.label_like_count)
    TextView mLikeLabel;
    @InjectView(R.id.img_like)
    View mLikeImage;

    private LogicControl mLogicControl = new LogicControl();
    private Animation mRotateAnimation;
    private SocialServiceWrapper mSocialService;
    private Handler mHandler;
    private boolean mPlayNow = false;

    private PlayListener mPlayListener = new PlayListener() {
        @Override
        public void onOtherStart() {
            if (mLogicControl.isLoading()) {
                updatePlayImage(STATE_LOADING);
            } else if (mLogicControl.isPlaying()) {
                updatePlayImage(STATE_PLAYING);
            } else if (mLogicControl.isPause()) {
                updatePlayImage(STATE_PAUSE);
            } else {
                updatePlayImage(STATE_PAUSE);
            }
        }

        @Override
        public void onComplete() {
            if (mLogicControl.isLoading()) {
                updatePlayImage(STATE_LOADING);
            } else if (mLogicControl.isPlaying()) {
                updatePlayImage(STATE_PLAYING);
            } else if (mLogicControl.isPause()) {
                updatePlayImage(STATE_PAUSE);
            } else {
                updatePlayImage(STATE_PAUSE);
            }
        }

        @Override
        public void onLoadingEnd() {
            if (mLogicControl.isLoading()) {
                updatePlayImage(STATE_LOADING);
            } else if (mLogicControl.isPlaying()) {
                updatePlayImage(STATE_PLAYING);
            } else if (mLogicControl.isPause()) {
                updatePlayImage(STATE_PAUSE);
            } else {
                updatePlayImage(STATE_PAUSE);
            }
        }
    };

    private Runnable mUpdateProgressBarTask = new Runnable() {
        @Override
        public void run() {
            if (mLogicControl.isPlaying() || mLogicControl.isPause()) {
                if (mLogicControl.getDuration() > 0 && mLogicControl.getDuration() >= mLogicControl.getCurrentPosition() + 500) {
                    mProgressBar.setProgress(mLogicControl.getCurrentPosition());
                    mProgressBar.setMax(mLogicControl.getDuration());
                }
            } else {
                mProgressBar.setProgress(0);
            }
            mHandler.postDelayed(this, UPDATE_PROGRESSBAR_INTERVAL);
        }
    };

    public static FullScreenArticleFragment newInstance(Article article, boolean playNow) {
        FullScreenArticleFragment ret = new FullScreenArticleFragment();
        ret.mLogicControl.mArticle = article;
        ret.mPlayNow = playNow;
        ret.setRetainInstance(true);
        return ret;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_fullscreen_article, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mHandler = new Handler();
        ButterKnife.inject(this, view);
        initContentView();
        updateLikeContainer();
        if (mPlayNow && !mLogicControl.isPlaying()) {
            onPlayImageClick();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        performUpdateProgressBarTask();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateProgressBarTask);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSocialService != null)
            mSocialService.handleOnActivityResult(requestCode, resultCode, data);
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

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.image_play)
    protected void onPlayImageClick() {
        if (mLogicControl.isPlaying()) {
            updatePlayImage(STATE_PAUSE);
            mLogicControl.pause();
            mHandler.removeCallbacks(mUpdateProgressBarTask);
        } else if (mLogicControl.isPause()) {
            updatePlayImage(STATE_PLAYING);
            mLogicControl.resume();
            performUpdateProgressBarTask();
        } else {
            updatePlayImage(STATE_LOADING);
            if (!mLogicControl.isLoading()) {
                mLogicControl.play();
            }
            performUpdateProgressBarTask();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.container_like)
    protected void onLikeContainerClick() {
        Article article = mLogicControl.getArticle();
        boolean isLogin = SNSAccountStore.getInstance().isLogin();

        if (isLogin) {
            if (article.hasLiked()) {
                new UncollectDialog(getActivity()).setDelegate(new UncollectDialog.Delegate() {
                    @Override
                    public void onConfirmButtonClick() {
                        mLogicControl.toggleLikeState();
                        updateLikeContainer();
                    }
                }).show();
            } else {
                ToastHelper.showCollected(getActivity());
                mLogicControl.toggleLikeState();
                updateLikeContainer();
            }
        } else {
            if (mSocialService == null) {
                mSocialService = new SocialServiceWrapper(getActivity());
            }
            mSocialService.showLoginBoard(new SocializeListeners.UMAuthListener() {
                @Override
                public void onStart(SHARE_MEDIA media) {
                }

                @Override
                public void onComplete(Bundle bundle, final SHARE_MEDIA media) {
                    mSocialService.getUserInfo(new SocializeListeners.FetchUserListener() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onComplete(int i, SocializeUser socializeUser) {
                            String platform = media.name();
                            if (media == SHARE_MEDIA.SINA) {
                                platform = "sina";
                            } else if (media == SHARE_MEDIA.WEIXIN) {
                                platform = "wxsession";
                            } else if (media == SHARE_MEDIA.QQ) {
                                platform = "qq";
                            }
                            for (SnsAccount account : socializeUser.mAccounts) {
                                if (account.getPlatform().equalsIgnoreCase(platform)) {
                                    SNSAccountStore.getInstance().setLoginAccountAndType(socializeUser.mAccounts.get(0), media).synchronize();
                                    return;
                                }
                            }
                            mSocialService = null;
                        }
                    });
                }

                @Override
                public void onError(SocializeException e, SHARE_MEDIA media) {
                    mSocialService = null;
                }

                @Override
                public void onCancel(SHARE_MEDIA media) {
                    mSocialService = null;
                }
            });
        }
    }

    private void performUpdateProgressBarTask() {
        if (mLogicControl.isPlaying() || mLogicControl.isPause()) {
            mProgressBar.setMax(mLogicControl.getDuration());
            mProgressBar.setProgress(mLogicControl.getCurrentPosition());
        }
        mHandler.removeCallbacks(mUpdateProgressBarTask);
        mUpdateProgressBarTask.run();
    }

    private void updateLikeContainer() {
        Article article = mLogicControl.getArticle();
        mLikeLabel.setText("" + article.likeNum());
        mLikeImage.setSelected(article.hasLiked());
    }

    private void initContentView() {
        Article article = mLogicControl.getArticle();
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.image_bg);
        ImageLoader.getInstance().displayImage(article.getImageURL(getActivity()), imageView);

        //update actionbar
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("VOL." + article.pageno);

        mProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        if (mLogicControl.isLoading()) {
            updatePlayImage(STATE_LOADING);
        } else if (mLogicControl.isPlaying()) {
            updatePlayImage(STATE_PLAYING);
        } else if (mLogicControl.isPause()) {
            updatePlayImage(STATE_PAUSE);
        } else {
            updatePlayImage(STATE_PAUSE);
        }
    }

    private void updatePlayImage(int state) {
        mPlayImage.setVisibility(state == STATE_LOADING ? View.GONE : View.VISIBLE);
        mLoadingImage.setVisibility(state == STATE_LOADING ? View.VISIBLE : View.GONE);

        if (state == STATE_PLAYING) {
            mLoadingImage.clearAnimation();
            mPlayImage.setImageResource(R.drawable.ic_pause);
        } else if (state == STATE_PAUSE) {
            mLoadingImage.clearAnimation();
            mPlayImage.setImageResource(R.drawable.ic_play);
        } else if (state == STATE_LOADING) {
            if (mLoadingImage.getAnimation() == null)
                mLoadingImage.startAnimation(getRoateAnimation());
        }
    }

    private Animation getRoateAnimation() {
        if (mRotateAnimation == null) {
            mRotateAnimation = AnimationUtils.loadAnimation(getActivity(),
                    R.anim.tip);
            mRotateAnimation.setInterpolator(new LinearInterpolator());
        }

        return mRotateAnimation;
    }

    private class LogicControl {
        private Article mArticle;

        public Article getArticle() {
            return mArticle;
        }

        public boolean isLoading() {
            return Player.getInstance().isLoading(getPlayUrl());
        }

        public boolean isPlaying() {
            return Player.getInstance().isPlaying(getPlayUrl());
        }

        public boolean isPause() {
            return Player.getInstance().isPause(getPlayUrl());
        }

        public int getCurrentPosition() {
            return Player.getInstance().getCurrentPos();
        }

        public int getDuration() {
            return Player.getInstance().getMax();
        }

        public void play() {
            Player.getInstance().play(getArticle(), getPlayUrl(), mPlayListener);
        }

        public void pause() {
            Player.getInstance().pause();
        }

        public void resume() {
            Player.getInstance().resume();
        }

        public void toggleLikeState() {
            Article article = mLogicControl.getArticle();
            if (article.hasLiked()) {
                article.toggleLikeState();
                CollectedArticleStore.getInstance().remove(article);
            } else {
                article.toggleLikeState();
                CollectedArticleStore.getInstance().add(article);
            }
            NotificationCenter.getInstance().getDispatcher().onArticleDataChanged(article);
        }

        public void performShare() {
            final Article article = getArticle();
            mSocialService = new SocialServiceWrapper(getActivity());
            mSocialService.setShareContent(new ShareContent().init("" + article.pageno, article.name, article.txt, article.getImageURL(getActivity())));
            mSocialService.showShareBoard(new SocializeListeners.SnsPostListener() {
                @Override
                public void onStart() {
                    if (getActivity() instanceof ShareFragmentDelegate) {
                        ((ShareFragmentDelegate) getActivity()).onFragmentPerformShare(FullScreenArticleFragment.this);
                    }
                }

                @Override
                public void onComplete(SHARE_MEDIA media, int i, SocializeEntity socializeEntity) {
                    if (i == 200) {
                        StatHelper.onArticleShare(getActivity(), article, media);
                        if (media == SHARE_MEDIA.SMS) {
                            ToastHelper.showCopyLinkSuccess(getActivity());
                        }
                    }
                    mSocialService = null;
                }
            });
        }

        private String getPlayUrl() {
            String mediaName = mArticle.soundurl.substring(mArticle.soundurl.lastIndexOf("/") + 1);

            File file = new File(SDCardUtils.getMediaCachePath(), mediaName);

            if (file.exists()) {
                return file.getPath();
            }

            return mArticle.soundurl;
        }
    }
}
