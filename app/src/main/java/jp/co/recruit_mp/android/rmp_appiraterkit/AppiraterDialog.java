package jp.co.recruit_mp.android.rmp_appiraterkit;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sample.android.newsreader.app.R;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class AppiraterDialog extends Dialog implements DialogInterface {

    AppiraterDialog(Context context) {
        super(context);
    }

    public static final class Builder extends DialogBuilder<AppiraterDialog> {

        private CharSequence mTitle = null;

        private CharSequence mMessage = null;

        private Map<CharSequence, View.OnClickListener> mButtonDataMap = null;

        public Builder(@NonNull Context context) {
            super(context);

            mButtonDataMap = new HashMap<>();
        }

        public Builder setTitle(@NonNull CharSequence title) {
            mTitle = title;
            return this;
        }

        public Builder setTitle(int resourceId) {
            mTitle = getContext().getString(resourceId);
            return this;
        }

        public Builder setMessage(@NonNull CharSequence message) {
            mMessage = message;
            return this;
        }

        public Builder setMessage(int resourceId) {
            mMessage = getContext().getString(resourceId);
            return this;
        }

        public Builder addButton(@NonNull CharSequence text, @Nullable View.OnClickListener listener) {
            mButtonDataMap.put(text, listener);
            return this;
        }

        public Builder addButton(int resourceId, @Nullable View.OnClickListener listener) {
            String text = getContext().getString(resourceId);
            mButtonDataMap.put(text, listener);
            return this;
        }

        public Builder clearButton() {
            mButtonDataMap.clear();
            return this;
        }

        @Override
        public AppiraterDialog create() {
            AppiraterDialog dialog = new AppiraterDialog(getContext());
            dialog.setCancelable(true);

            LayoutInflater inflater = LayoutInflater.from(getContext());
            @SuppressLint("InflateParams") LinearLayout parent =
                    (LinearLayout) inflater.inflate(R.layout.rmp_appiraterkit_dialog, null, false);

            TextView titleView = (TextView) parent.findViewById(R.id.title);
            if (titleView != null) {
                if (TextUtils.isEmpty(mTitle)) {
                    titleView.setVisibility(View.GONE);
                } else {
                    titleView.setText(mTitle);
                }
            }

            TextView messageView = (TextView) parent.findViewById(R.id.message);
            if (messageView != null) {
                if (TextUtils.isEmpty(mMessage)) {
                    messageView.setVisibility(View.GONE);
                } else {
                    messageView.setText(mMessage);
                }
            }

            for (Map.Entry<CharSequence, View.OnClickListener> entry : mButtonDataMap.entrySet()) {
                @SuppressLint("InflateParams") View buttonLayout =
                        inflater.inflate(R.layout.rmp_appiraterkit_dialog_button, null);
                final Button button = (Button) buttonLayout.findViewById(R.id.button);
                if (entry != null) {
                    CharSequence text = entry.getKey();
                    final View.OnClickListener listener = entry.getValue();

                    button.setText(text);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (listener != null) {
                                listener.onClick(button);
                            }
                        }
                    });
                }
                parent.addView(buttonLayout);
            }

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(parent);

            return dialog;
        }
    }
}
