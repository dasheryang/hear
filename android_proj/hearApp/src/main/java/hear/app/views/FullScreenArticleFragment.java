package hear.app.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.listener.SocializeListeners;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hear.app.R;
import hear.app.helper.SDCardUtils;
import hear.app.helper.StatHelper;
import hear.app.media.PlayListener;
import hear.app.media.Player;
import hear.app.models.Article;
import hear.lib.share.SocialServiceWrapper;
import hear.lib.share.models.ShareContent;

/**
 * Created by ZhengYi on 15/2/16.
 */
public class FullScreenArticleFragment extends Fragment {
    private static final int STATE_PLAYING = 1;
    private static final int STATE_LOADING = 2;
    private static final int STATE_PAUSE = 3;

    @InjectView(R.id.image_bg)
    ImageView mBGImage;
    @InjectView(R.id.image_play)
    ImageView mPlayImage;
    @InjectView(R.id.img_loading)
    ImageView mLoadingImage;

    private LogicControl mLogicControl = new LogicControl();
    private Animation mRotateAnimation;
    private SocialServiceWrapper mShareService;

    private PlayListener mPlayListener = new PlayListener() {
        @Override
        public void onOtherStart() {
            updatePlayImage(STATE_PAUSE);
        }

        @Override
        public void onComplete() {
            updatePlayImage(STATE_PAUSE);
        }

        @Override
        public void onLoadingEnd() {
            updatePlayImage(STATE_PLAYING);
        }
    };

    public static FullScreenArticleFragment newInstance(Article article) {
        FullScreenArticleFragment ret = new FullScreenArticleFragment();
        ret.mLogicControl.mArticle = article;
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
        ButterKnife.inject(this, view);
        initContentView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mShareService != null)
            mShareService.handleOnActivityResult(requestCode, resultCode, data);
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
        } else if (mLogicControl.isPause()) {
            updatePlayImage(STATE_PLAYING);
            mLogicControl.resume();
        } else {
            updatePlayImage(STATE_LOADING);
            mLogicControl.play();
        }
    }

    private void initContentView() {
        Article article = mLogicControl.getArticle();
        ImageLoader.getInstance().displayImage(article.imgurl, mBGImage);

        //update actionbar
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("VOL." + article.pageno);

        updatePlayImage(mLogicControl.isPlaying() ? STATE_PLAYING : STATE_PAUSE);
    }

    private void updatePlayImage(int state) {
        mPlayImage.setVisibility(state == STATE_LOADING ? View.GONE : View.VISIBLE);
        mLoadingImage.setVisibility(state == STATE_LOADING ? View.VISIBLE : View.GONE);

        if (state == STATE_PLAYING) {
            mLoadingImage.clearAnimation();
            mPlayImage.setImageResource(R.drawable.pause_icon);
        } else if (state == STATE_PAUSE) {
            mLoadingImage.clearAnimation();
            mPlayImage.setImageResource(R.drawable.play_icon);
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

        public boolean isPlaying() {
            return Player.getInstance().isPlaying(getPlayUrl());
        }

        public boolean isPause() {
            return Player.getInstance().isPause(getPlayUrl());
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

        public void performShare() {
            final Article article = getArticle();
            mShareService = new SocialServiceWrapper(getActivity());
            mShareService.setShareContent(new ShareContent().init(article.name, article.txt, article.imgurl, "http://www.baidu.com"));
            mShareService.showShareBoard(new SocializeListeners.SnsPostListener() {
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
                    }
                    mShareService = null;
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
