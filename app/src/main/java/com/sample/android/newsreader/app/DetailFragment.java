package com.sample.android.newsreader.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sample.android.newsreader.app.provider.NewsDataProvider;
import com.sample.android.newsreader.app.widget.AsyncImageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 2001;

    static final String EXTRA_QUERY_ID = "extra._id";

    private static final int DEFAULT_QUERY_ID = -1;

    private interface ColumnIndex {
        int HEADLINE = 0;
        int DESCRIPTION = 1;
        int DATE = 2;
        int LINK = 3;
    }

    private static final String[] QUERY_PEOJECTION = new String[4];
    static {
        QUERY_PEOJECTION[ColumnIndex.HEADLINE] = NewsDataProvider.Contract.COLUMN_HEADLINE;
        QUERY_PEOJECTION[ColumnIndex.DESCRIPTION] = NewsDataProvider.Contract.COLUMN_DESC;
        QUERY_PEOJECTION[ColumnIndex.DATE] = NewsDataProvider.Contract.COLUMN_DATE;
        QUERY_PEOJECTION[ColumnIndex.LINK] = NewsDataProvider.Contract.COLUMN_LINK;
    }

    private int mId = DEFAULT_QUERY_ID;

    private TextView mTitleView = null;

    private TextView mSummaryView = null;

    private TextView mPublishDate = null;

    private AsyncImageView mImageView = null;

    private Button mNewsLinkButton = null;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mId = intent.getIntExtra(EXTRA_QUERY_ID, DEFAULT_QUERY_ID);
        }
        if (mId == DEFAULT_QUERY_ID) {
            getActivity().finish();
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitleView = (TextView) rootView.findViewById(android.R.id.title);
        mSummaryView = (TextView) rootView.findViewById(android.R.id.summary);
        mPublishDate = (TextView) rootView.findViewById(R.id.publishDate);
        mImageView = (AsyncImageView) rootView.findViewById(R.id.image);
        mNewsLinkButton = (Button) rootView.findViewById(android.R.id.button1);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                NewsDataProvider.Contract.CONTENT_URI,
                QUERY_PEOJECTION,
                NewsDataProvider.Contract._ID + " = ?",
                toStringArray(mId),
                null);
    }

    private static String[] toStringArray(Object... objects) {
        if (objects == null || objects.length == 0) {
            return null;
        } else {
            String[] array = new String[objects.length];
            for (int i = 0; i < objects.length; i++) {
                array[i] = String.valueOf(objects[i]);
            }
            return array;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            mImageView.setVisibility(View.GONE);

            String title = cursor.getString(ColumnIndex.HEADLINE);
            mTitleView.setText(title);

            String summary = cursor.getString(ColumnIndex.DESCRIPTION);
            mSummaryView.setText(removeHtmlTags(summary, new TagHandler()));

            long publishDate = cursor.getLong(ColumnIndex.DATE);
            SimpleDateFormat output = new SimpleDateFormat(BuildConfig.OUTPUT_DATE_FORMAT, Locale.getDefault());
            mPublishDate.setText(output.format(publishDate));

            final String link = cursor.getString(ColumnIndex.LINK);
            mNewsLinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(link));
                    getContext().startActivity(intent);
                }
            });
        }
    }

    private String removeHtmlTags(String source, TagHandler handler) {
        String removed = source;
        int start;
        while ((start = removed.indexOf("<")) != -1) {
            int end = removed.indexOf("/>", start + 1);
            if (end == -1) {
                break;
            } else {
                end += "/>".length();
                if (handler != null) {
                    String tag = removed.substring(start, end);
                    handler.handleTag(tag);
                }
                if (start > 0) {
                    removed = removed.substring(0, start) + removed.substring(end);
                } else {
                    removed = removed.substring(end);
                }
            }
        }
        return removed;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNewsLinkButton.setOnClickListener(null);
    }

    private class TagHandler {

        public void handleTag(String tag) {
            XmlPullParser parser = Xml.newPullParser();
            StringReader reader = new StringReader(tag);
            try {
                parser.setInput(reader);

                int eventType;
                while ((eventType = parser.getEventType()) != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if ("img".equals(parser.getName())) {
                            mImageView.setVisibility(View.VISIBLE);

                            String urlString = parser.getAttributeValue(null, "src");
                            if (urlString != null) {
                                mImageView.setImageURL(new URL(urlString));
                            }

                            String width = parser.getAttributeValue(null, "width");
                            if (width != null) {
                                mImageView.setMaxWidth(Integer.parseInt(width));
                            }
                        }
                    }
                    parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
