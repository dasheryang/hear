package hear.lib.share.controllers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateResponse;

import hear.lib.share.R;

/**
 * Created by ZhengYi on 15/3/2.
 */
public class CustomUpdateDialog extends Dialog {
    private TextView mContentLabel;
    private UpdateResponse mUpdateResponse;
    private Listener mListener = Listener.NULL;

    public CustomUpdateDialog(Context context) {
        super(context, R.style.Share_Dialog);
        if (!(context instanceof Activity)) {
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        setContentView(R.layout.share__dialog_update);
        setCancelable(false);
        initContentView();
    }

    public CustomUpdateDialog setUpdateResponse(UpdateResponse response) {
        mUpdateResponse = response;
        updateContentViewIfNeeded();
        return this;
    }

    public CustomUpdateDialog setListener(Listener listener) {
        mListener = listener == null ? Listener.NULL : listener;
        return this;
    }

    protected void onConfirmButtonClick() {
        if (mUpdateResponse != null)
            UmengUpdateAgent.startDownload(getContext(), mUpdateResponse);

        dismiss();
        mListener.onConfirmButtonClick();
    }

    protected void onCancelButtonClick() {
        dismiss();
        mListener.onCancelButtonClick();
    }

    private void updateContentViewIfNeeded() {
        if (mUpdateResponse != null) {
            mContentLabel.setText(mUpdateResponse.updateLog);
        }
    }

    private void initContentView() {
        mContentLabel = (TextView) findViewById(R.id.label_content);

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelButtonClick();
            }
        });

        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmButtonClick();
            }
        });
    }

    public static interface Listener {
        void onConfirmButtonClick();

        void onCancelButtonClick();

        public static Listener NULL = new Listener() {
            @Override
            public void onConfirmButtonClick() {
            }

            @Override
            public void onCancelButtonClick() {
            }
        };
    }
}
