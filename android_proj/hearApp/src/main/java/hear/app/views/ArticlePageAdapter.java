

package hear.app.views;

import hear.app.models.Article;
import hear.app.helper.ArrayUtils;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Pager adapter
 */
public class ArticlePageAdapter extends FragmentPagerAdapter {

    private final Resources resources;

    private Context mContext;

    /**
     * Create pager adapter
     *
     * @param resources
     * @param fragmentManager
     */
    public ArticlePageAdapter(final Resources resources, final FragmentManager fragmentManager, List<Article> articles, Context mActivity) {
        super(fragmentManager);
        this.resources = resources;
        this.mCategories =articles;
        this.mContext=mActivity;
    }

    private List<Article> mCategories;

    @Override
    public int getCount() {
        return ArrayUtils.size(mCategories);
    }

    @Override
    public Fragment getItem(final int position) {
        final Fragment result;
        Article article= mCategories.get(position);
        if(article!=null){
            result=new ArticleFragment();
            Bundle args=new Bundle();
            args.putInt(ArticleFragment.KEY_ARTICLE,article.pageno);
            result.setArguments(args);
            return result;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        Article c=this.mCategories.get(position);
        if(c!=null){
            return c.name;
        }
        return null;
    }

}

