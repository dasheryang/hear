package hear.app.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import hear.app.R;
import hear.app.models.Article;
import hear.app.models.CollectedArticleStore;

/**
 * Created by power on 14-8-11.
 */
public class CollectionGalleryActivity extends BaseFragmentActivity implements ShareFragmentDelegate, ArticleFragmentDelegate {
    private static final String KEY_DEFAULT_PAGE_NO = "default_page_no";
    private UILogic mUILogic = new UILogic();
    private PlaybarControl mPlaybarControl;
    private WeakReference<Fragment> mSharingFragment;

    public static void show(Context context, Article defaultArticle) {
        Intent intent = new Intent(context, CollectionGalleryActivity.class);
        intent.putExtra(KEY_DEFAULT_PAGE_NO, defaultArticle.pageno);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mPlaybarControl = new PlaybarControl(this);
        setContentView(R.layout.history);
        initContentView();
        mPlaybarControl.prepare(findViewById(R.id.playbar));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlaybarControl.update();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSharingFragment != null && mSharingFragment.get() != null) {
            mSharingFragment.get().onActivityResult(requestCode, resultCode, data);
            mSharingFragment = null;
        }
    }

    @Override
    public void onFragmentPerformShare(Fragment fragment) {
        mSharingFragment = new WeakReference<>(fragment);
    }

    @Override
    public void onRequestPlayArticle(Article article) {
        mPlaybarControl.playArticle(article);
    }

    private void initContentView() {
        /** bind views **/
        ViewPager mViewPager = (ViewPager) findViewById(R.id.vp_pages);

        /** setup ViewPager **/
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                List<Article> articles = mUILogic.getCacheArticles();
                if (articles != null) {
                    mPlaybarControl.setDefaultArticle(articles.get(position));
                }
            }
        });
        List<Article> mArticles = mUILogic.getCacheArticles();
        ArticlePageAdapter firstCategoryAdapter = new ArticlePageAdapter(
                getSupportFragmentManager(),
                mArticles);
        mViewPager.setAdapter(firstCategoryAdapter);
        Log.e("Hear", "default_item_position:" + Math.max(0, mUILogic.getArticlePosition(getIntent().getIntExtra(KEY_DEFAULT_PAGE_NO, -1))));
        mViewPager.setCurrentItem(Math.max(0, mUILogic.getArticlePosition(getIntent().getIntExtra(KEY_DEFAULT_PAGE_NO, -1))));
        mViewPager.setVisibility(View.VISIBLE);
    }

    private class UILogic {
        private List<Article> mArticle;

        public List<Article> getCacheArticles() {
            if (mArticle == null) {
                mArticle = CollectedArticleStore.getInstance().getArticles();
            }

            return mArticle;
        }

        public int getArticlePosition(int pageNo) {
            Iterator<Article> iter = getCacheArticles().iterator();
            int index = 0;
            while (iter.hasNext()) {
                if (iter.next().pageno == pageNo)
                    return index;
                index++;
            }

            return -1;
        }
    }
}
