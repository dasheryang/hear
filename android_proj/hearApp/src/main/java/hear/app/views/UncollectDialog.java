package hear.app.views;

import android.app.Dialog;
import android.content.Context;

import butterknife.ButterKnife;
import butterknife.OnClick;
import hear.app.R;

/**
 * Created by ZhengYi on 15/2/21.
 */
public class UncollectDialog extends Dialog {
    private Delegate mDelegate = Delegate.NULL;

    public UncollectDialog(Context context) {
        super(context, android.R.style.Theme_Panel);
        setContentView(R.layout.dialog_uncollect);
        ButterKnife.inject(this);
    }

    public UncollectDialog setDelegate(Delegate delegate) {
        mDelegate = delegate == null ? Delegate.NULL : delegate;
        return this;
    }

    @OnClick(R.id.btn_confirm)
    void onConfirmButtonClick() {
        dismiss();
        mDelegate.onConfirmButtonClick();
    }

    @OnClick(R.id.btn_cancel)
    void onCancelButtonClick() {
        dismiss();
    }

    public static interface Delegate {
        void onConfirmButtonClick();

        public static Delegate NULL = new Delegate() {
            @Override
            public void onConfirmButtonClick() {
            }
        };
    }
}
