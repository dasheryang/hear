package hear.app.views;

import hear.app.models.Article;

/**
 * Created by ZhengYi on 15/2/21.
 */
public interface ArticleFragmentDelegate {
    void onRequestPlayArticle(Article article);
}
