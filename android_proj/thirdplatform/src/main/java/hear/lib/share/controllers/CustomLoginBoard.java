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
    private static final String[] sItemNameArray = new String[]{"微信登录", "QQ登录", "新浪登录"};
    private static final int[] sItemImageResArray = new int[]{
            R.drawable.ic_share_wx,
            R.drawable.ic_share_qq,
            R.drawable.ic_share_sina
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
        //setup cancelButton
        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //setup items
        int count = sItemNameArray.length;
        TableLayout container = (TableLayout) findViewById(R.id.container_login);
        TableRow tableRow = createTableRow(container);
        container.addView(tableRow);
        for (int i = 0; i < count; i++) {
            tableRow.addView(createItemWithInfo(sMediaArray[i], sItemNameArray[i], sItemImageResArray[i], tableRow));
        }
    }

    private TableRow createTableRow(ViewGroup container) {
        TableRow ret = new TableRow(mContext);
        TableLayout.LayoutParams params = (TableLayout.LayoutParams) container.generateLayoutParams(null);
        params.width = -1;
        params.height = -2;
        ret.setLayoutParams(params);
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
