package com.sample.android.newsreader.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.sample.android.newsreader.app.data.FetchNewsTask;
import com.sample.android.newsreader.app.data.FetchTask;
import com.sample.android.newsreader.app.data.NewsDataAdapter;
import com.sample.android.newsreader.app.provider.NewsDataProvider;

import java.lang.ref.WeakReference;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER = 1001;

    public interface ColumnIndex {
        int _ID = 0;
        int HEADLINE = 1;
        int DATE = 2;
    }
    private static final String[] LOADER_PROJECTION = new String[3];
    static {
            LOADER_PROJECTION[ColumnIndex._ID] = NewsDataProvider.Contract._ID;
            LOADER_PROJECTION[ColumnIndex.HEADLINE] = NewsDataProvider.Contract.COLUMN_HEADLINE;
            LOADER_PROJECTION[ColumnIndex.DATE] = NewsDataProvider.Contract.COLUMN_DATE;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    private CursorAdapter mCursorAdapter = null;

    private FetchTask.Callback<Void> mCallback = null;

    private FetchTask.OnStatusListener mOnStatusListener = null;

    private Handler mHandler = null;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mHandler = new Handler();

        mCursorAdapter = new NewsDataAdapter(getActivity(), null, 0);
        setListAdapter(mCursorAdapter);

        mCallback = new FetchTaskCallback();
        mOnStatusListener = new FetchTaskOnStatusListener(mHandler);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.red);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        Log.d(MainFragment.class.getSimpleName(), "onListItemClick");
        Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        int _id = cursor.getInt(ColumnIndex._ID);

        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(DetailFragment.EXTRA_QUERY_ID, _id);
        getActivity().startActivity(intent);
    }

    void updateNews() {
        FetchNewsTask task = new FetchNewsTask(getContext());
        task.setCallback(mCallback);
        task.setOnStatusListener(mOnStatusListener);
        task.execute(BuildConfig.NEWS_RSS_URL);
    }

    @Override
    public void onRefresh() {
        updateNews();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                NewsDataProvider.Contract.CONTENT_URI,
                LOADER_PROJECTION,
                null,
                null,
                NewsDataProvider.Contract.COLUMN_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private class FetchTaskCallback implements FetchTask.Callback<Void> {
        @Override
        public void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        public void onPostExecute(Void result) {
            getLoaderManager().restartLoader(LOADER, null, MainFragment.this);

            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private class FetchTaskOnStatusListener implements FetchTask.OnStatusListener {

        private final ToastRunnable mToastComplete;

        private final ToastRunnable mToastError;

        private final Handler mHandler;

        public FetchTaskOnStatusListener(Handler handler) {
            if (handler == null) {
                mHandler = new Handler();
            } else {
                mHandler = handler;
            }

            mToastComplete = new ToastRunnable(getContext(), R.string.refresh_complete, Toast.LENGTH_SHORT);
            mToastError = new ToastRunnable(getContext(), R.string.refresh_error, Toast.LENGTH_SHORT);
        }

        public FetchTaskOnStatusListener() {
            this(null);
        }

        @Override
        public void onComplete() {
            mToastComplete.post(mHandler);
       }

        @Override
        public void onError(String message, Throwable exception) {
            Log.e("Status", message, exception);
            mToastError.post(mHandler);
        }
    }

    private static class ToastRunnable implements Runnable {

        private final WeakReference<Context> mContext;

        private final int mResourceId;

        private final int mDuration;

        public ToastRunnable(Context context, int resourceId, int duration) {
            mContext = new WeakReference<>(context);
            mResourceId = resourceId;
            mDuration = duration;
        }

        @Override
        public void run() {
            Toast.makeText(mContext.get(), mResourceId, mDuration).show();
        }

        public void post(@NonNull Handler handler) {
            handler.post(this);
        }
    }
}
