package hear.app.widget;

import hear.app.R;
import hear.app.util.LogUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by power on 14-8-11.
 */
public class GuideFragment extends Fragment {

    public static final String KEY_POSITION = "position";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.guide_fragment, null);
    }

    private int[] images = new int[] { R.drawable.guide01, R.drawable.guide02, R.drawable.guide03, R.drawable.guide04 };

    /**
     * 获取article
     * @return
     */
    public int getPosition(){
        int position
                = getArguments().getInt(KEY_POSITION);
        return position;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        View view=getView();

        ImageView image_view= (ImageView) view.findViewById(R.id.image_view);

        int positon=getPosition();

        LogUtil.d("position: "+positon);

        //image_view.setBackgroundResource(images[positon]);
        image_view.setImageResource(images[positon]);

        LogUtil.d("showd image");

    }
}
