package jp.co.recruit_mp.android.rmp_appiraterkit;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

@SuppressWarnings("unused")
abstract class DialogBuilder<DialogType extends Dialog> {

    private WeakReference<Context> mContext = null;

    protected DialogBuilder(@NonNull Context context) {
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
