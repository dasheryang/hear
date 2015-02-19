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
public class CustomShareBoard extends Dialog {
    private static final String[] sItemNameArray = new String[]{"微信好友", "朋友圈", "QQ好友", "QQ空间", "新浪微博", "复制链接"};
    private static final int[] sItemImageResArray = new int[]{R.drawable.umeng_socialize_wechat, R.drawable.umeng_socialize_wxcircle, R.drawable.umeng_socialize_qq_on, R.drawable.umeng_socialize_qzone_on, R.drawable.umeng_socialize_sina_on, R.drawable.umeng_socialize_google};
    private static final SHARE_MEDIA[] sMediaArray = new SHARE_MEDIA[]{SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.SINA, SHARE_MEDIA.EMAIL};

    private Activity mContext;
    private UMSocialService mSocialService;
    private SocializeListeners.SnsPostListener mPostListener;

    public CustomShareBoard(Activity context, UMSocialService socialService, SocializeListeners.SnsPostListener postListener) {
        super(context, R.style.Share_Dialog);
        mContext = context;
        mSocialService = socialService;
        mPostListener = postListener;
        setContentView(R.layout.share__dialog_share);
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
        TableLayout container = (TableLayout) findViewById(R.id.container_share);
        int currentRow = 0;
        TableRow tableRow = createTableRow(container);
        container.addView(tableRow);
        for (int i = 0; i < count; i++) {
            if (currentRow < i / 3) {
                currentRow++;
                tableRow = createTableRow(container);
                container.addView(tableRow);
            }
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
                mSocialService.postShare(mContext, media, mPostListener);
            }
        });

        return cell;
    }
}
