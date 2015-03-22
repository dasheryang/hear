package hear.app.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;

import hear.app.R;
import hear.app.models.Article;
import hear.app.store.ArticleStore;

/**
 * Created by ZhengYi on 15/2/16.
 */
public class PlayActivityV2 extends BaseFragmentActivity implements ShareFragmentDelegate {
    private static final String KEY_PAGE_NO = "page_no";
    private Fragment mShareFragment;

    public static void show(Context context, Article article) {
        Intent intent = new Intent(context, PlayActivityV2.class);
        intent.putExtra(KEY_PAGE_NO, article.pageno);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.remain);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_container);
        getSupportFragmentManager().beginTransaction().add(R.id.container_fragment, FullScreenArticleFragment.newInstance(getArticle(), true)).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mShareFragment != null)
            mShareFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        Article.setPlayedWithArticle(getArticle());
        super.finish();
        overridePendingTransition(R.anim.remain, R.anim.slide_out_to_bottom);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == android.R.id.home) {
            MainActivity.show(this);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MainActivity.show(this);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFragmentPerformShare(Fragment fragment) {
        mShareFragment = fragment;
    }

    private Article getArticle() {
        return ArticleStore.getInstance().getArticleWithPageNo(getIntent().getIntExtra(KEY_PAGE_NO, -1));
    }
}
