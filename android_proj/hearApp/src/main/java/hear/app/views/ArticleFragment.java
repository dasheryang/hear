package hear.app.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.util.HashMap;

import hear.app.R;
import hear.app.engine.BaseHttpAsyncTask;
import hear.app.helper.DeviceUtil;
import hear.app.helper.StatHelper;
import hear.app.helper.ToastUtil;
import hear.app.models.Article;
import hear.app.models.ArticleLike;
import hear.app.models.CollectedArticleStore;
import hear.app.models.JsonRespWrapper;
import hear.app.models.SNSAccountStore;
import hear.lib.share.SocialServiceWrapper;
import hear.lib.share.models.ShareContent;

/**
 * Created by power on 14-8-11.
 */
public class ArticleFragment extends Fragment {

    private LinearLayout mLikeContainer;
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

    public static final int UN_LIKE_LEVEL = 1;
    public static final int LIKE_LEVEL = 2;
    public static final String KEY_ARTICLE = "article";

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
            if (article.haslike == 0) {
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
        mLikeLabel.setText("" + article.likenum);
        mLikeImage.setBackgroundResource(article.haslike == 1 ? R.drawable.like_item_full : R.drawable.like_item);
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
        mLikeContainer = (LinearLayout) rootView.findViewById(R.id.container_like);
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
        mCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                int pageno = getArguments().getInt(KEY_ARTICLE);
                mArticle = Article.getArticleByPageNo(pageno);
            }

            return mArticle;
        }

        public void toggleLikeState() {
            Article article = getArticle();
            if (article.haslike == 1) {
                article.haslike = 0;
                article.likenum = article.likenum - 1;
                ArticleLike.setLikeArticle(article.pageno, 0);
                ArticleLike.descLikeCount(article.pageno);
                CollectedArticleStore.getInstance().remove(article);
                String url = "http://www.hearheart.com/cancellike";
                BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

                    @Override
                    protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {
                    }
                };
                HashMap<String, String> params = new HashMap<>();
                params.put("PhoneId", DeviceUtil.getPhoneId());
                params.put("pageno", String.valueOf(getArticle()));
                asyncTask.get(params).execute();
            } else {
                article.haslike = 1;
                article.likenum = article.likenum + 1;
                ArticleLike.setLikeArticle(article.pageno, 1);
                ArticleLike.incLikeCount(article.pageno);
                CollectedArticleStore.getInstance().add(article);
                String url = "http://www.hearheart.com/clicklike";
                BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

                    @Override
                    protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {
                    }
                };
                HashMap<String, String> params = new HashMap<>();
                params.put("PhoneId", DeviceUtil.getPhoneId());
                params.put("pageno", String.valueOf(getArticle().pageno));
                asyncTask.get(params).execute();
            }
        }

        private void performShare() {
            mShareService = new SocialServiceWrapper(getActivity());
            final Article article = mUILogic.getArticle();
            mShareService.setShareContent(new ShareContent().init(article.name, article.txt, article.imgurl, "http://www.baidu.com"));
            mShareService.showShareBoard(new SocializeListeners.SnsPostListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onComplete(SHARE_MEDIA media, int i, SocializeEntity socializeEntity) {
                    if (i == 200)
                        StatHelper.onArticleShare(getActivity(), article, media);
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
