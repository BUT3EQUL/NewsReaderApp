package com.sample.android.newsreader.app.data

import android.content.Context
import android.database.Cursor
import android.support.v4.widget.CursorAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sample.android.newsreader.app.BuildConfig
import com.sample.android.newsreader.app.R
import java.text.SimpleDateFormat
import java.util.*

class NewsDataAdapter(context: Context, cursor: Cursor?, flags: Int) : CursorAdapter(context, cursor, flags) {

    companion object {
        val HEADLINE = com.sample.android.newsreader.app.MainFragment.ColumnIndex.HEADLINE
        val DATE = com.sample.android.newsreader.app.MainFragment.ColumnIndex.DATE
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_news, parent, false)
        val viewHolder = ViewHolder(view)
        view.tag = viewHolder
        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val viewHolder = view.tag as ViewHolder
        viewHolder.headline.text = cursor.getString(HEADLINE)
        viewHolder.publishDate.text = convertPublishDate(cursor.getLong(DATE))
    }

    private fun convertPublishDate(publishDate: Long): String {
        // Tue, 29 Sep 2015 11:00:01 +0900
        val output = SimpleDateFormat(BuildConfig.OUTPUT_DATE_FORMAT, Locale.getDefault())
        return output.format(publishDate)
    }

    private class ViewHolder(view: View) {
        internal val headline: TextView
        internal val publishDate: TextView

        init {
            headline = view.findViewById(android.R.id.text1) as TextView
            publishDate = view.findViewById(android.R.id.text2) as TextView
        }
    }
}
