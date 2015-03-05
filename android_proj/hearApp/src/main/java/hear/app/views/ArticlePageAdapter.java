package hear.app.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import hear.app.helper.ArrayUtils;
import hear.app.models.Article;

/**
 * Pager adapter
 */
public class ArticlePageAdapter extends FragmentPagerAdapter {
    private List<Article> mArticles;

    /**
     * Create pager adapter
     */
    public ArticlePageAdapter(final FragmentManager fragmentManager, List<Article> articles) {
        super(fragmentManager);
        this.mArticles = new ArrayList<>(articles);
    }

    @Override
    public int getCount() {
        return ArrayUtils.size(mArticles);
    }

    @Override
    public Fragment getItem(final int position) {
        final Fragment result;
        Article article = mArticles.get(position);
        if (article != null) {
            result = new ArticleFragment();
            Bundle args = new Bundle();
            args.putInt(ArticleFragment.KEY_ARTICLE, article.pageno);
            result.setArguments(args);
            return result;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        Article c = this.mArticles.get(position);
        if (c != null) {
            return c.name;
        }
        return null;
    }

}

