package com.sample.android.newsreader.app

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.sample.android.newsreader.app.provider.NewsDataProvider
import com.sample.android.newsreader.app.widget.AsyncImageView
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class DetailFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private interface ColumnIndex {
        companion object {
            val HEADLINE = 0
            val DESCRIPTION = 1
            val DATE = 2
            val LINK = 3
        }
    }

    private var mId = DEFAULT_QUERY_ID

    private var mTitleView: TextView? = null

    private var mSummaryView: TextView? = null

    private var mPublishDate: TextView? = null

    private var mImageView: AsyncImageView? = null

    private var mNewsLinkButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val intent = activity.intent
        if (intent != null) {
            mId = intent.getIntExtra(EXTRA_QUERY_ID, DEFAULT_QUERY_ID)
        }
        if (mId == DEFAULT_QUERY_ID) {
            activity.finish()
        }

        val rootView = inflater!!.inflate(R.layout.fragment_detail, container, false)
        mTitleView = rootView.findViewById(android.R.id.title) as TextView
        mSummaryView = rootView.findViewById(android.R.id.summary) as TextView
        mPublishDate = rootView.findViewById(R.id.publishDate) as TextView
        mImageView = rootView.findViewById(R.id.image) as AsyncImageView
        mNewsLinkButton = rootView.findViewById(android.R.id.button1) as Button
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        loaderManager.initLoader(LOADER_ID, null, this)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        return CursorLoader(context,
                NewsDataProvider.Contract.CONTENT_URI,
                QUERY_PEOJECTION,
                NewsDataProvider.Contract._ID + " = ?",
                toStringArray(mId),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        if (cursor.moveToFirst()) {
            mImageView!!.visibility = View.GONE

            val title = cursor.getString(ColumnIndex.HEADLINE)
            mTitleView!!.text = title

            val summary = cursor.getString(ColumnIndex.DESCRIPTION)
            mSummaryView!!.text = removeHtmlTags(summary, TagHandler())

            val publishDate = cursor.getLong(ColumnIndex.DATE)
            val output = SimpleDateFormat(BuildConfig.OUTPUT_DATE_FORMAT, Locale.getDefault())
            mPublishDate!!.text = output.format(publishDate)

            val link = cursor.getString(ColumnIndex.LINK)
            mNewsLinkButton!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse(link))
                    context.startActivity(intent)
                }
            })
        }
    }

    private fun removeHtmlTags(source: String, handler: TagHandler?): String {
        var removed = source
        do {
            var start: Int = removed.indexOf("<")
            if (start != -1) {
                var end = removed.indexOf("/>", start + 1)
                if (end == -1) {
                    break
                } else {
                    end += "/>".length
                    if (handler != null) {
                        val tag = removed.substring(start, end)
                        handler.handleTag(tag)
                    }
                    if (start > 0) {
                        removed = removed.substring(0, start) + removed.substring(end)
                    } else {
                        removed = removed.substring(end)
                    }
                }
            }
        } while (start != -1)
        return removed
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mNewsLinkButton!!.setOnClickListener(null)
    }

    private inner class TagHandler {

        fun handleTag(tag: String) {
            val parser = Xml.newPullParser()
            val reader = StringReader(tag)
            try {
                parser.setInput(reader)

                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG) {
                        if ("img" == parser.name) {
                            mImageView!!.visibility = View.VISIBLE

                            val urlString = parser.getAttributeValue(null, "src")
                            if (urlString != null) {
                                mImageView!!.imageURL = URL(urlString)
                            }

                            val width = parser.getAttributeValue(null, "width")
                            if (width != null) {
                                mImageView!!.maxWidth = Integer.parseInt(width)
                            }
                        }
                    }
                    parser.next()
                }
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    companion object {

        private val LOADER_ID = 2001

        internal val EXTRA_QUERY_ID = "extra._id"

        private val DEFAULT_QUERY_ID = -1

        private val QUERY_PEOJECTION = arrayOfNulls<String>(4)

        init {
            QUERY_PEOJECTION[ColumnIndex.HEADLINE] = NewsDataProvider.Contract.COLUMN_HEADLINE
            QUERY_PEOJECTION[ColumnIndex.DESCRIPTION] = NewsDataProvider.Contract.COLUMN_DESC
            QUERY_PEOJECTION[ColumnIndex.DATE] = NewsDataProvider.Contract.COLUMN_DATE
            QUERY_PEOJECTION[ColumnIndex.LINK] = NewsDataProvider.Contract.COLUMN_LINK
        }

        private fun toStringArray(vararg objects: Any): Array<String>? {
            if (objects.size == 0) {
                return null
            } else {
                val array = Array(objects.size, {flags -> ""})
                for (i in objects.indices) {
                    array[i] = objects[i].toString()
                }
                return array
            }
        }
    }
}
