package hear.app.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.listener.SocializeListeners;

import hear.app.R;
import hear.app.helper.StatHelper;
import hear.app.helper.ToastHelper;
import hear.app.helper.ToastUtil;
import hear.app.models.Article;
import hear.app.store.ArticleStore;
import hear.app.store.CollectedArticleStore;
import hear.app.store.SNSAccountStore;
import hear.lib.share.SocialServiceWrapper;
import hear.lib.share.models.ShareContent;

/**
 * Created by power on 14-8-11.
 */
public class ArticleFragment extends Fragment {

    private View mLikeImage = null;
    private TextView mLikeLabel = null;
    private TextView mContentLabel = null;
    private TextView mAuthorLabel = null;
    private ImageView mCoverImageView = null;
    private TextView mVolumeLabel = null;
    private TextView mDateLabel = null;
    private ProgressBar pb;

    private UILogic mUILogic = new UILogic();
    private SocialServiceWrapper mShareService;

    public static final String KEY_ARTICLE = "article";

    private class ImageShowerListener extends SimpleImageLoadingListener {

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
        return inflater.inflate(R.layout.frag_article, container, false);
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
        updateLikeContainer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
            mUILogic.performShare();
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

    void onLikeContainerClick() {
        Article article = mUILogic.getArticle();
        if (SNSAccountStore.getInstance().isLogin()) {
            if (!article.hasLiked()) {
                ToastHelper.showCollected(getActivity());
                mUILogic.toggleLikeState();
                updateLikeContainer();
            } else {
                new UncollectDialog(getActivity()).setDelegate(new UncollectDialog.Delegate() {
                    @Override
                    public void onConfirmButtonClick() {
                        mUILogic.toggleLikeState();
                        updateLikeContainer();
                    }
                }).show();
            }
        } else {
            mUILogic.toggleLikeState();
            updateLikeContainer();
        }
    }

    private void updateLikeContainer() {
        Article article = mUILogic.getArticle();
        mLikeLabel.setText("" + article.likeNum());
        mLikeImage.setSelected(article.hasLiked());
    }

    private void bindViews(View rootView) {
        pb = (ProgressBar) rootView.findViewById(R.id.img_loading);
        mDateLabel = (TextView) rootView.findViewById(R.id.label_date);
        mVolumeLabel = (TextView) rootView.findViewById(R.id.label_volume);
        mCoverImageView = (ImageView) rootView.findViewById(R.id.img_cover);
        mContentLabel = (TextView) rootView.findViewById(R.id.label_content);
        mAuthorLabel = (TextView) rootView.findViewById(R.id.label_author);
        mLikeLabel = (TextView) rootView.findViewById(R.id.label_like_count);
        mLikeImage = rootView.findViewById(R.id.img_like);
        LinearLayout mLikeContainer = (LinearLayout) rootView.findViewById(R.id.container_like);
        mLikeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikeContainerClick();
            }
        });
    }

    private void initContentView() {
        final Article article = mUILogic.getArticle();
        mDateLabel.setText(article.getShowTime());
        mVolumeLabel.setText("VOL." + article.pageno);

        mAuthorLabel.setText(article.showauthor);

        ImageLoader.getInstance().displayImage(
                article.getImageURL(getActivity()), mCoverImageView,
                new ImageShowerListener());

        mCoverImageView.setOnTouchListener(new View.OnTouchListener() {
            private boolean mNeedToCheck = false;
            private long mLastFingerDownTime;
            private PointF mPoint;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mNeedToCheck = true;
                    mLastFingerDownTime = System.currentTimeMillis();
                    mPoint = new PointF(event.getX(), event.getY());
                } else if (action == MotionEvent.ACTION_UP && mNeedToCheck) {
                    mNeedToCheck = false;
                    float deltaX = Math.abs(event.getX() - mPoint.x);
                    float deltaY = Math.abs(event.getY() - mPoint.y);
                    long passedTime = System.currentTimeMillis() - mLastFingerDownTime;
                    if (passedTime <= 100 && deltaX < 8 && deltaY < 8) {
                        playArticle();
                    }
                }
                return true;
            }

            private void playArticle() {
                if (getActivity() instanceof ArticleFragmentDelegate) {
                    ArticleFragmentDelegate delegate = (ArticleFragmentDelegate) getActivity();
                    delegate.onRequestPlayArticle(mUILogic.getArticle());
                }
            }
        });

        mContentLabel.setText(article.txt);
        mContentLabel.setMovementMethod(new ScrollingMovementMethod());
        updateLikeContainer();
    }

    private class UILogic {
        private Article mArticle;

        private Article getArticle() {
            if (mArticle == null) {
                reloadArticle();
            }

            return mArticle;
        }

        public void reloadArticle() {
            int pageno = getArguments().getInt(KEY_ARTICLE);
            mArticle = ArticleStore.getInstance().getArticleWithPageNo(pageno);
        }

        public void toggleLikeState() {
            Article article = getArticle();
            if (article.hasLiked()) {
                article.toggleLikeState();
                CollectedArticleStore.getInstance().remove(article);
            } else {
                article.toggleLikeState();
                CollectedArticleStore.getInstance().add(article);
            }
        }

        private void performShare() {
            mShareService = new SocialServiceWrapper(getActivity());
            final Article article = mUILogic.getArticle();
            mShareService.setShareContent(new ShareContent().init("" + article.pageno, "VOL. " + article.pageno, article.txt + article.showauthor, article.getImageURL(getActivity())));
            mShareService.showShareBoard(new SocializeListeners.SnsPostListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onComplete(SHARE_MEDIA media, int i, SocializeEntity socializeEntity) {
                    if (i == 200) {
                        StatHelper.onArticleShare(getActivity(), article, media);

                        if (media == SHARE_MEDIA.SMS) {
                            ToastHelper.showCopyLinkSuccess(getActivity());
                        }
                    }
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
