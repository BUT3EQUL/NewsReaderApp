package com.sample.android.newsreader.app.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sample.android.newsreader.app.BuildConfig;
import com.sample.android.newsreader.app.R;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.sample.android.newsreader.app.MainFragment.ColumnIndex.HEADLINE;
import static com.sample.android.newsreader.app.MainFragment.ColumnIndex.DATE;

public class NewsDataAdapter extends CursorAdapter {

    private WeakReference<Context> mContext = null;

    public NewsDataAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mContext = new WeakReference<>(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_news, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.headline.setText(cursor.getString(HEADLINE));
        viewHolder.publishDate.setText(convertPublishDate(cursor.getLong(DATE)));
    }

    protected Context getContext() {
        return mContext.get();
    }

    private static String convertPublishDate(long publishDate) {
        // Tue, 29 Sep 2015 11:00:01 +0900
        SimpleDateFormat output = new SimpleDateFormat(BuildConfig.OUTPUT_DATE_FORMAT, Locale.getDefault());
        return output.format(publishDate);
    }

    private static class ViewHolder {
        final TextView headline;
        final TextView publishDate;

        public ViewHolder(View view) {
            headline = (TextView) view.findViewById(android.R.id.text1);
            publishDate = (TextView) view.findViewById(android.R.id.text2);
        }
    }
}
