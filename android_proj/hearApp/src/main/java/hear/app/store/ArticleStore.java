package hear.app.store;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hear.app.helper.AppContext;
import hear.app.helper.FileUtils;
import hear.app.models.Article;

/**
 * Created by ZhengYi on 15/3/8.
 */
public class ArticleStore {
    private static ArticleStore sInstance;
    private List<Article> mArticleSet;
    private boolean mArticleSetExpired = false;

    public static ArticleStore getInstance() {
        if (sInstance == null)
            sInstance = new ArticleStore();
        return sInstance;
    }

    public void setArticleSet(Article[] articleArray) {
        if (articleArray == null || articleArray.length == 0)
            return;

        mArticleSet = Arrays.asList(articleArray);
        saveArticleSetToLocal(articleArray);
    }

    public List<Article> getArticleSet() {
        if (mArticleSet == null || mArticleSetExpired) {
            mArticleSet = loadArticleSetFromLocal();
        }
        return mArticleSet;
    }

    public Article getArticleWithPageNo(int pageNo) {
        for (Article article : mArticleSet)
            if (article.pageno == pageNo)
                return article;

        return null;
    }

    private void saveArticleSetToLocal(Article[] articleArray) {
        if (articleArray == null || articleArray.length == 0)
            return;

        String dataSet = new Gson().toJson(articleArray);
        FileUtils.writeStringToFile(new File(getCacheFilePath()), dataSet, true);
    }

    private List<Article> loadArticleSetFromLocal() {
        File file = new File(getCacheFilePath());
        if (file.exists()) {
            String dataSet = FileUtils.readStringFromFile(file);
            if (dataSet != null && dataSet.length() != 0) {
                Article[] articleArray = new Gson().fromJson(dataSet, Article[].class);
                if (articleArray != null && articleArray.length > 0)
                    return Arrays.asList(articleArray);
            }
        }

        return new ArrayList<>(0);
    }

    private String getCacheFilePath() {
        return AppContext.getContext().getCacheDir().getAbsolutePath() + File.separator + "remote_article.dat";
    }
}
