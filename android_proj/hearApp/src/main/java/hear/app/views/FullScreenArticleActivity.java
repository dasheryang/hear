package hear.app.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;

import hear.app.R;
import hear.app.models.Article;
import hear.app.store.ArticleStore;

/**
 * Created by ZhengYi on 15/2/16.
 */
public class FullScreenArticleActivity extends BaseFragmentActivity implements ShareFragmentDelegate {
    private static final String KEY_PAGE_NO = "page_no";
    private Fragment mShareFragment;

    public static void show(Context context, Article article) {
        Intent intent = new Intent(context, FullScreenArticleActivity.class);
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
        setContentView(R.layout.act_fullscreen);
        getSupportFragmentManager().beginTransaction().add(R.id.container_fragment, FullScreenArticleFragment.newInstance(getArticle(), false)).commit();

        findViewById(R.id.container_introduction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.container_introduction).setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mShareFragment != null)
            mShareFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.remain, R.anim.slide_out_to_bottom);
    }

    @Override
    public void onFragmentPerformShare(Fragment fragment) {
        mShareFragment = fragment;
    }

    private Article getArticle() {
        return ArticleStore.getInstance().getArticleWithPageNo(getIntent().getIntExtra(KEY_PAGE_NO, -1));
    }
}
