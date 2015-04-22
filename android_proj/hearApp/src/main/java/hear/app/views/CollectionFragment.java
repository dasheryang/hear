package hear.app.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hear.app.R;
import hear.app.models.Article;
import hear.app.store.CollectedArticleStore;

/**
 * Created by ZhengYi on 15/2/16.
 */
public class CollectionFragment extends Fragment {
    @InjectView(R.id.container_noData)
    View mNoDataContainer;
    @InjectView(R.id.recyclerView_collection)
    RecyclerView mRecyclerView;

    private UIControl mUIControl;
    private int mLastDataSetCount = -1;

    public static CollectionFragment newInstance() {
        CollectionFragment ret = new CollectionFragment();
        ret.setRetainInstance(true);
        return ret;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_collection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mUIControl = new UIControl();
    }

    @Override
    public void onResume() {
        super.onResume();
        initContentView();
    }

    private void initContentView() {
        if (mLastDataSetCount == -1 || mLastDataSetCount != mUIControl.getArticles().size()) {
            mLastDataSetCount = mUIControl.getArticles().size();
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

            if (mUIControl.getArticles().isEmpty()) {
                mNoDataContainer.setVisibility(View.VISIBLE);
            } else {
                mNoDataContainer.setVisibility(View.GONE);

                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(new CollectionAdapter(getActivity(), mUIControl.getArticles()));
            }
        }
    }

    private class UIControl {
        private List<Article> getArticles() {
            return CollectedArticleStore.getInstance().getArticles();
        }
    }
}
