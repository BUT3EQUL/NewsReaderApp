package com.sample.android.newsreader.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Xml;

import com.sample.android.newsreader.app.BuildConfig;
import com.sample.android.newsreader.app.provider.NewsDataProvider;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FetchNewsTask extends FetchTask<String, Void, Void> {

    private interface ColumnIndex {
        int HEADLINE = 0;
        int DESC = 1;
        int DATE = 2;
        int LINK = 3;
    }

    private static final String[] PROJECTIONS = new String[4];
    static {
        PROJECTIONS[ColumnIndex.HEADLINE] = NewsDataProvider.Contract.COLUMN_HEADLINE;
        PROJECTIONS[ColumnIndex.DESC] = NewsDataProvider.Contract.COLUMN_DESC;
        PROJECTIONS[ColumnIndex.DATE] = NewsDataProvider.Contract.COLUMN_DATE;
        PROJECTIONS[ColumnIndex.LINK] = NewsDataProvider.Contract.COLUMN_LINK;
    }

    private WeakReference<Context> mContext = null;

    public FetchNewsTask(@NonNull Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    protected Void onBackground(@NonNull String... params) throws Exception {
        Cursor cursor = getContext().getContentResolver().query(
                NewsDataProvider.Contract.CONTENT_URI, PROJECTIONS, null, null, null);
        ContentValues[] backupColumns = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                backupColumns = new ContentValues[cursor.getCount()];
                int index = 0;
                do {
                    ContentValues values = new ContentValues();
                    values.put(NewsDataProvider.Contract.COLUMN_HEADLINE, cursor.getString(ColumnIndex.HEADLINE));
                    values.put(NewsDataProvider.Contract.COLUMN_DESC, cursor.getString(ColumnIndex.DESC));
                    values.put(NewsDataProvider.Contract.COLUMN_DATE, cursor.getLong(ColumnIndex.DATE));
                    values.put(NewsDataProvider.Contract.COLUMN_LINK, cursor.getString(ColumnIndex.LINK));
                    backupColumns[index++] = values;
                } while (cursor.moveToNext());
            } finally {
                cursor.close();
            }
        }

        getContext().getContentResolver().delete(NewsDataProvider.Contract.CONTENT_URI, null, null);

        if (params == null || params.length == 0) {
            throw new RuntimeException("parameters is empty");
        }

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            parse(buffer.toString());

        } catch (MalformedURLException e) {
            if (backupColumns != null) {
                getContext().getContentResolver().bulkInsert(NewsDataProvider.Contract.CONTENT_URI, backupColumns);
            }
            throw new RuntimeException("parameter is not URL", e);
        } catch (IOException e) {
            if (backupColumns != null) {
                getContext().getContentResolver().bulkInsert(NewsDataProvider.Contract.CONTENT_URI, backupColumns);
            }
            throw new RuntimeException("HTTP error", e);
        } finally {
            if (reader != null) {
                closeStream(reader);
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    protected Context getContext() {
        return mContext.get();
    }

    private void parse(@NonNull String source) {
        List<ContentValues> insertValues = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(source));
            int eventType;

            while ((eventType = parser.getEventType()) != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("item".equals(parser.getName())) {
                        ContentValues values = parseNewsData(parser);
                        insertValues.add(values);
                    }
                }
                parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        if (!insertValues.isEmpty()) {
            ContentValues[] values = insertValues.toArray(new ContentValues[insertValues.size()]);
            getContext().getContentResolver().bulkInsert(NewsDataProvider.Contract.CONTENT_URI, values);
        }
    }

    private ContentValues parseNewsData(@NonNull XmlPullParser parser)
            throws XmlPullParserException, IOException {
        ContentValues values = new ContentValues();
        int eventType;
        while ((eventType = parser.getEventType()) != XmlPullParser.END_TAG) {
            if (eventType == XmlPullParser.START_TAG) {
                if ("title".equals(parser.getName())) {
                    values.put(NewsDataProvider.Contract.COLUMN_HEADLINE, parser.nextText());
                } else if ("description".equals(parser.getName())) {
                    values.put(NewsDataProvider.Contract.COLUMN_DESC, parser.nextText());
                } else if ("link".equals(parser.getName())) {
                    values.put(NewsDataProvider.Contract.COLUMN_LINK, parser.nextText());
                } else if ("pubDate".equals(parser.getName())) {
                    String pubDate = parser.nextText();
                    long dateValue = parsePublishDate(pubDate);
                    if (dateValue > 0) {
                        values.put(NewsDataProvider.Contract.COLUMN_DATE, dateValue);
                    }
                }
            }
            parser.next();
        }

        return values;
    }

    private static long parsePublishDate(@NonNull String s) {
        SimpleDateFormat formatter = new SimpleDateFormat(BuildConfig.INPUT_DATE_FORMAT, Locale.US);
        Date date = null;
        try {
            date = formatter.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.getTimeInMillis();
        }
        return -1;
    }

    private static void closeStream(@NonNull Closeable c) {
        try {
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}