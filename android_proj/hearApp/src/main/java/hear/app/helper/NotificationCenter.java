package hear.app.helper;

import android.support.annotation.NonNull;

import java.util.LinkedList;

import hear.app.models.Article;

/**
 * Created by ZhengYi on 15/3/8.
 */
public class NotificationCenter {
    private static NotificationCenter sInstance;
    private LinkedList<ICallbackListener> mListenerSet = new LinkedList<>();
    private Dispatcher mDispatcher = new Dispatcher();

    public static NotificationCenter getInstance() {
        if (sInstance == null)
            sInstance = new NotificationCenter();
        return sInstance;
    }

    private NotificationCenter() {
    }

    public Dispatcher getDispatcher() {
        return mDispatcher;
    }

    public synchronized void addListener(@NonNull ICallbackListener listener) {
        if (!mListenerSet.contains(listener))
            mListenerSet.add(listener);
    }

    public synchronized void removeListener(@NonNull ICallbackListener listener) {
        if (mListenerSet.contains(listener))
            mListenerSet.remove(listener);
    }

    public static interface ICallbackListener {
        void onArticleDataChanged(Article article);
    }

    public static class SimpleCallbackListener implements ICallbackListener {
        @Override
        public void onArticleDataChanged(Article article) {
        }
    }

    public class Dispatcher implements ICallbackListener {
        private Dispatcher() {
        }

        @Override
        public void onArticleDataChanged(Article article) {
            for (ICallbackListener listener : mListenerSet)
                listener.onArticleDataChanged(article);
        }
    }
}
