package hear.app.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.umeng.socialize.bean.SnsAccount;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import hear.app.helper.AppContext;
import hear.app.helper.FileUtils;

/**
 * Created by ZhengYi on 15/2/15.
 */
public class CollectedArticleStore {
    private static CollectedArticleStore sInstance;
    private LinkedList<Article> mCollectedDataSet;
    private LinkedList<Article> mCacheDataSet;
    @SuppressWarnings("FieldCanBeLocal")
    private SNSAccountStore.LoginStateListener mListener = new SNSAccountStore.LoginStateListener() {
        @Override
        public void onLogin(@NonNull SnsAccount account) {
            reloadData();
        }

        @Override
        public void onLogout(@Nullable SnsAccount account) {
            reloadData();
        }
    };

    private CollectedArticleStore() {
        reloadData();
        SNSAccountStore.getInstance().addLoginStateListener(mListener);
    }

    public static CollectedArticleStore getInstance() {
        if (sInstance == null)
            sInstance = new CollectedArticleStore();

        return sInstance;
    }

    public List<Article> getArticles() {
        return mCollectedDataSet;
    }

    public void add(final Article article) {
        if (article == null)
            return;

        if (isLogin()) {
            if (!mCollectedDataSet.contains(article)) {
                mCollectedDataSet.addFirst(article);
                archiveByAccountID();
            }
        } else {
            if (!mCacheDataSet.contains(article)) {
                mCacheDataSet.addFirst(article);
            }
        }
    }

    public void remove(final Article article) {
        if (article == null)
            return;

        if (isLogin()) {
            if (mCollectedDataSet.contains(article)) {
                mCollectedDataSet.remove(article);
                archiveByAccountID();
            }
        } else if (mCacheDataSet.contains(article)) {
            mCacheDataSet.remove(article);
        }
    }

    private void reloadData() {
        if (isLogin()) {
            unarchiveByAccountID();
            if (!mCacheDataSet.isEmpty()) {
                for (Article article : mCacheDataSet) {
                    if (!mCollectedDataSet.contains(article)) {
                        mCollectedDataSet.addFirst(article);
                    }
                }
                mCacheDataSet.clear();
                archiveByAccountID();
            }
        } else {
            mCollectedDataSet = new LinkedList<>();
            mCacheDataSet = new LinkedList<>();
        }
    }


    private void archiveByAccountID() {
        Article[] articles = new Article[mCollectedDataSet.size()];
        mCollectedDataSet.toArray(articles);
        String data = new Gson().toJson(articles, Article[].class);
        FileUtils.writeStringToFile(new File(getArchiveFilePath()), data, true);
    }

    private void unarchiveByAccountID() {
        String data = FileUtils.readStringFromFile(new File(getArchiveFilePath()));
        if (TextUtils.isEmpty(data)) {
            mCollectedDataSet = new LinkedList<>();
        } else {
            Article[] articles = new Gson().fromJson(data, Article[].class);
            if (articles != null && articles.length > 0) {
                mCollectedDataSet = new LinkedList<>();
                mCollectedDataSet.addAll(Arrays.asList(articles));
            } else {
                mCollectedDataSet = new LinkedList<>();
            }
        }
    }

    private String getArchiveFilePath() {
        return AppContext.getContext().getCacheDir().getAbsolutePath() + File.separator + "collection" + File.separator + getAccountID() + "articles.dat";
    }

    private String getAccountID() {
        return SNSAccountStore.getInstance().getLoginAccount().getUsid();
    }

    private boolean isLogin() {
        return SNSAccountStore.getInstance().isLogin();
    }
}
