package com.sample.android.newsreader.app.data

import android.content.ContentValues
import android.content.Context
import android.util.Xml
import com.sample.android.newsreader.app.BuildConfig
import com.sample.android.newsreader.app.provider.NewsDataProvider
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FetchNewsTask(context: Context) : FetchTask<String, Void, Void>() {

    private interface ColumnIndex {
        companion object {
            val HEADLINE = 0
            val DESC = 1
            val DATE = 2
            val LINK = 3
        }
    }

    private var mContext: WeakReference<Context>? = null

    protected val context: Context
        get() = mContext!!.get()

    init {
        mContext = WeakReference(context)
    }

    @Throws(Exception::class)
    override fun onBackground(vararg params: String): Void? {
        val cursor = context.contentResolver.query(
                NewsDataProvider.Contract.CONTENT_URI, PROJECTIONS, null, null, null)
        var backupColumns: Array<ContentValues>? = null
        if (cursor != null && cursor.moveToFirst()) {
            try {
                backupColumns = Array(size = cursor.count, init = {flags -> ContentValues()})
                var index = 0
                do {
                    val values = ContentValues()
                    values.put(NewsDataProvider.Contract.COLUMN_HEADLINE, cursor.getString(ColumnIndex.HEADLINE))
                    values.put(NewsDataProvider.Contract.COLUMN_DESC, cursor.getString(ColumnIndex.DESC))
                    values.put(NewsDataProvider.Contract.COLUMN_DATE, cursor.getLong(ColumnIndex.DATE))
                    values.put(NewsDataProvider.Contract.COLUMN_LINK, cursor.getString(ColumnIndex.LINK))
                    backupColumns[index++] = values
                } while (cursor.moveToNext())
            } finally {
                cursor.close()
            }
        }

        context.contentResolver.delete(NewsDataProvider.Contract.CONTENT_URI, null, null)

        if (params.size == 0) {
            throw RuntimeException("parameters is empty")
        }

        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        try {
            val url = URL(params[0])
            connection = url.openConnection() as HttpURLConnection

            reader = BufferedReader(InputStreamReader(connection.inputStream))
            val buffer = StringBuffer()
            do {
                var line : String? = reader.readLine()
                if (line != null) {
                    buffer.append(line)
                }
            } while (line != null)

            parse(buffer.toString())

        } catch (e: MalformedURLException) {
            if (backupColumns != null) {
                context.contentResolver.bulkInsert(NewsDataProvider.Contract.CONTENT_URI, backupColumns)
            }
            throw RuntimeException("parameter is not URL", e)
        } catch (e: IOException) {
            if (backupColumns != null) {
                context.contentResolver.bulkInsert(NewsDataProvider.Contract.CONTENT_URI, backupColumns)
            }
            throw RuntimeException("HTTP error", e)
        } finally {
            if (reader != null) {
                closeStream(reader)
            }

            if (connection != null) {
                connection.disconnect()
            }
        }

        return null
    }

    private fun parse(source: String) {
        val insertValues = ArrayList<ContentValues>()
        val parser = Xml.newPullParser()
        try {
            parser.setInput(StringReader(source))
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    if ("item" == parser.name) {
                        val values = parseNewsData(parser)
                        insertValues.add(values)
                    }
                }
                parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (!insertValues.isEmpty()) {
            val values = insertValues.toArray<ContentValues>(arrayOfNulls<ContentValues>(insertValues.size))
            context.contentResolver.bulkInsert(NewsDataProvider.Contract.CONTENT_URI, values)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseNewsData(parser: XmlPullParser): ContentValues {
        val values = ContentValues()
        while (parser.eventType != XmlPullParser.END_TAG) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                if ("title" == parser.name) {
                    values.put(NewsDataProvider.Contract.COLUMN_HEADLINE, parser.nextText())
                } else if ("description" == parser.name) {
                    values.put(NewsDataProvider.Contract.COLUMN_DESC, parser.nextText())
                } else if ("link" == parser.name) {
                    values.put(NewsDataProvider.Contract.COLUMN_LINK, parser.nextText())
                } else if ("pubDate" == parser.name) {
                    val pubDate = parser.nextText()
                    val dateValue = parsePublishDate(pubDate)
                    if (dateValue > 0) {
                        values.put(NewsDataProvider.Contract.COLUMN_DATE, dateValue)
                    }
                }
            }
            parser.next()
        }

        return values
    }

    companion object {

        private val PROJECTIONS = arrayOfNulls<String>(4)

        init {
            PROJECTIONS[ColumnIndex.HEADLINE] = NewsDataProvider.Contract.COLUMN_HEADLINE
            PROJECTIONS[ColumnIndex.DESC] = NewsDataProvider.Contract.COLUMN_DESC
            PROJECTIONS[ColumnIndex.DATE] = NewsDataProvider.Contract.COLUMN_DATE
            PROJECTIONS[ColumnIndex.LINK] = NewsDataProvider.Contract.COLUMN_LINK
        }

        private fun parsePublishDate(s: String): Long {
            val formatter = SimpleDateFormat(BuildConfig.INPUT_DATE_FORMAT, Locale.US)
            var date: Date? = null
            try {
                date = formatter.parse(s)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                return calendar.timeInMillis
            }
            return -1
        }

        private fun closeStream(c: Closeable) {
            try {
                c.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}