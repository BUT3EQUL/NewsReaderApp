package jp.co.recruit_mp.android.rmp_appiraterkit;

import android.app.Dialog;
import android.content.Context;

import java.lang.ref.WeakReference;

@SuppressWarnings("unused")
abstract class DialogBuilder<DialogType extends Dialog> {

    private WeakReference<Context> mContext = null;

    protected DialogBuilder(Context context) {
        mContext = new WeakReference<>(context);
    }

    protected Context getContext() {
        return mContext.get();
    }

    public abstract DialogType create();

    public DialogType show() {
        DialogType dialog = create();
        dialog.show();
        return dialog;
    }
}
