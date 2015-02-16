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
import hear.app.models.CollectedArticleStore;

/**
 * Created by ZhengYi on 15/2/16.
 */
public class CollectionFragment extends Fragment {
    @InjectView(R.id.label_noData)
    View mNoDataLabel;
    @InjectView(R.id.recyclerView_collection)
    RecyclerView mRecyclerView;

    private UIControl mUIControl;

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
        initContentView();
    }

    private void initContentView() {
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        if (mUIControl.getArticles().isEmpty()) {
            mNoDataLabel.setVisibility(View.VISIBLE);
        } else {
            mNoDataLabel.setVisibility(View.GONE);
            mRecyclerView.setAdapter(new CollectionAdapter(mUIControl.getArticles()));
        }
    }

    private class UIControl {
        private List<Article> getArticles() {
            return CollectedArticleStore.getInstance().getArticles();
        }
    }
}
