package com.sample.android.newsreader.app

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ListFragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.widget.CursorAdapter
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast

import com.sample.android.newsreader.app.data.FetchNewsTask
import com.sample.android.newsreader.app.data.FetchTask
import com.sample.android.newsreader.app.data.NewsDataAdapter
import com.sample.android.newsreader.app.provider.NewsDataProvider

/**
 * A placeholder fragment containing a simple view.
 */
class MainFragment : ListFragment(), SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    interface ColumnIndex {
        companion object {
            val _ID = 0
            val HEADLINE = 1
            val DATE = 2
        }
    }

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private var mCursorAdapter: CursorAdapter? = null

    private var mCallback: FetchTask.Callback<Void>? = null

    private var mOnStatusListener: FetchTask.OnStatusListener? = null

    private var mHandler: Handler? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mHandler = Handler()

        mCursorAdapter = NewsDataAdapter(activity, null, 0)
        listAdapter = mCursorAdapter

        mCallback = FetchTaskCallback()
        mOnStatusListener = FetchTaskOnStatusListener()

        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        mSwipeRefreshLayout!!.setColorSchemeResources(R.color.blue, R.color.red)
        mSwipeRefreshLayout!!.setOnRefreshListener(this)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        loaderManager.initLoader(LOADER, null, this)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onListItemClick(listView: ListView?, v: View?, position: Int, id: Long) {
        Log.d(MainFragment::class.simpleName, "onListItemClick")
        val cursor = listView!!.getItemAtPosition(position) as Cursor
        val _id = cursor.getInt(ColumnIndex._ID)

        val intent = Intent(activity, DetailActivity::class.java)
        intent.setAction(Intent.ACTION_VIEW)
        intent.putExtra(DetailFragment.EXTRA_QUERY_ID, _id)
        activity.startActivity(intent)
    }

    internal fun updateNews() {
        val task = FetchNewsTask(context)
        task.setCallback(mCallback)
        task.setOnStatusListener(mOnStatusListener)
        task.execute(BuildConfig.NEWS_RSS_URL)
    }

    override fun onRefresh() {
        updateNews()
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        return CursorLoader(context,
                NewsDataProvider.Contract.CONTENT_URI,
                LOADER_PROJECTION,
                null,
                null,
                NewsDataProvider.Contract.COLUMN_DATE + " DESC")
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        mCursorAdapter!!.swapCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mCursorAdapter!!.swapCursor(null)
    }

    private inner class FetchTaskCallback : FetchTask.Callback<Void> {
        override fun onPreExecute() {
            mSwipeRefreshLayout!!.isRefreshing = true
        }

        override fun onPostExecute(result: Void) {
            loaderManager.restartLoader(LOADER, null, this@MainFragment)

            if (mSwipeRefreshLayout!!.isRefreshing) {
                mSwipeRefreshLayout!!.isRefreshing = false
            }
        }
    }

    private inner class FetchTaskOnStatusListener : FetchTask.OnStatusListener {
        override fun onComplete() {
            post(R.string.refresh_complete)
        }

        override fun onError(message: String?, exception: Throwable?) {
            Log.e("Status", message, exception)
            post(R.string.refresh_error)
        }

        private fun post(resourceId: Int) {
            if (mHandler != null) {
                mHandler!!.post(object : Runnable {
                    override fun run() {
                        Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    companion object {

        private val LOADER = 1001
        private val LOADER_PROJECTION = arrayOfNulls<String>(3)

        init {
            LOADER_PROJECTION[ColumnIndex._ID] = NewsDataProvider.Contract._ID
            LOADER_PROJECTION[ColumnIndex.HEADLINE] = NewsDataProvider.Contract.COLUMN_HEADLINE
            LOADER_PROJECTION[ColumnIndex.DATE] = NewsDataProvider.Contract.COLUMN_DATE
        }
    }
}
