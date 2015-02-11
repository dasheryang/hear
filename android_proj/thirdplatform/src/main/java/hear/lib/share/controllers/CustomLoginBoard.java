package hear.lib.share.controllers;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;

import hear.lib.share.R;

/**
 * Created by ZhengYi on 15/2/9.
 */
public class CustomLoginBoard extends Dialog {
    private static final String[] sItemNameArray = new String[]{"微信", "QQ", "新浪"};
    private static final int[] sItemImageResArray = new int[]{R.drawable.umeng_socialize_wechat, R.drawable.umeng_socialize_qq_on, R
            .drawable.umeng_socialize_sina_on
    };
    private static final SHARE_MEDIA[] sMediaArray = new SHARE_MEDIA[]{SHARE_MEDIA.WEIXIN, SHARE_MEDIA.QQ, SHARE_MEDIA.SINA};

    private Activity mContext;
    private UMSocialService mSocialService;
    private SocializeListeners.UMAuthListener mAuthListener;

    public CustomLoginBoard(Activity context, UMSocialService socialService, SocializeListeners.UMAuthListener authListener) {
        super(context, R.style.Share_Dialog);
        mContext = context;
        mSocialService = socialService;
        mAuthListener = authListener;
        setContentView(R.layout.share__dialog_login);
        getWindow().setLayout(-1, -2);
        getWindow().setGravity(Gravity.BOTTOM);
        initContentView();
    }

    private void initContentView() {
        //setup items
        int count = sItemNameArray.length;
        TableLayout container = (TableLayout) findViewById(R.id.container_login);
        int currentRow = 0;
        TableRow tableRow = createTableRow(container);
        container.addView(tableRow);
        for (int i = 0; i < count; i++) {
            tableRow.addView(createItemWithInfo(sMediaArray[i], sItemNameArray[i], sItemImageResArray[i], tableRow));
        }
    }

    private TableRow createTableRow(ViewGroup container) {
        TableRow ret = new TableRow(mContext);
        ViewGroup.LayoutParams params = container.generateLayoutParams(null);
        params.width = -1;
        params.height = -2;
        return ret;
    }

    private View createItemWithInfo(final SHARE_MEDIA media, String name, int resID, ViewGroup container) {
        View cell = mContext.getLayoutInflater().inflate(R.layout.share__cell_share, container, false);

        ImageView imageView = (ImageView) cell.findViewById(R.id.img_share);
        imageView.setImageResource(resID);

        TextView textView = (TextView) cell.findViewById(R.id.label_share);
        textView.setText(name);

        cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mSocialService.doOauthVerify(mContext, media, mAuthListener);
            }
        });

        return cell;
    }
}
