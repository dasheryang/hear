package hear.app.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hear.app.R;
import hear.app.models.Article;

/**
 * Created by ZhengYi on 15/2/16.
 */
public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.MyViewHolder> {
    private List<Article> mItems;

    public CollectionAdapter(List<Article> items) {
        mItems = new LinkedList<>();
        mItems.addAll(items);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup container, int position) {
        View cell = LayoutInflater.from(container.getContext()).inflate(R.layout.cell_collection, container, false);
        return new MyViewHolder(cell);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        viewHolder.configureCell(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.img_cover)
        ImageView mCoverImage;
        @InjectView(R.id.label_volume)
        TextView mVolumeLabel;
        @InjectView(R.id.label_title)
        TextView mTitleLabel;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = (int) itemView.getTag();
            if (v == itemView) {
                onItemClick(itemView, mItems.get(position));
            }
        }

        public void configureCell(int position) {
            Article item = mItems.get(position);

            itemView.setTag(position);
            ImageLoader.getInstance().displayImage(item.imgurl, mCoverImage);
            mVolumeLabel.setText("VOL " + item.pageno);
            mTitleLabel.setText(item.name);
        }

        public void onItemClick(View cell, Article item) {
            FullScreenArticleActivity.show(cell.getContext(), item);
        }
    }
}
