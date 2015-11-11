package com.sample.android.newsreader.app.provider

import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.BaseColumns

class NewsDataProvider : ContentProvider() {

    private var mOpenHelper: SQLiteOpenHelper? = null

    override fun onCreate(): Boolean {
        mOpenHelper = Helper(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return mOpenHelper!!.readableDatabase.query(Contract.TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder)
    }

    override fun getType(uri: Uri): String? {
        return Contract.CONTENT_TYPE
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val id = mOpenHelper!!.writableDatabase.insert(Contract.TABLE, null, values)
        if (id > 0) {
            try {
                return ContentUris.withAppendedId(uri, id)
            } finally {
                if (context != null) {
                    context!!.contentResolver.notifyChange(uri, null)
                }
            }
        } else {
            throw SQLException("Failed to insert row into " + uri)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        try {
            return mOpenHelper!!.writableDatabase.delete(Contract.TABLE, selection, selectionArgs)
        } finally {
            if (context != null) {
                context!!.contentResolver.notifyChange(uri, null)
            }
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        try {
            return mOpenHelper!!.writableDatabase.update(Contract.TABLE, values, selection, selectionArgs)
        } finally {
            if (context != null) {
                context!!.contentResolver.notifyChange(uri, null)
            }
        }
    }

    private class Helper(context: Context) : SQLiteOpenHelper(context, DATABASE, null, VERSION_INT) {

        companion object {
            val CREATE_TABLE: String = """
                CREATE TABLE ${Contract.TABLE} (
                    ${Contract._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${Contract.COLUMN_HEADLINE} TEXT NOT NULL,
                    ${Contract.COLUMN_DESC} TEXT NOT NULL,
                    ${Contract.COLUMN_LINK} TEXT NOT NULL,
                    ${Contract.COLUMN_DATE} INTEGER );
            """

            val DROP_TABLE = """
                DROP TABLE IF EXISTS ${Contract.TABLE};
            """
        }

        override fun onCreate(db: SQLiteDatabase) {
            createDatabase(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            dropDatabase(db)
            createDatabase(db)
        }

        private fun createDatabase(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        private fun dropDatabase(db: SQLiteDatabase) {
            db.execSQL(DROP_TABLE)
        }
    }

    interface Contract : BaseColumns {
        companion object {
            val _ID = BaseColumns._ID

            val AUTHORITY = "com.sample.android.newsreader.app"

            val CONTENT_URI = Uri.parse("content://" + AUTHORITY)

            val CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY

            val TABLE = "news"

            val COLUMN_HEADLINE = "headline"

            val COLUMN_DESC = "description"

            val COLUMN_LINK = "link"

            val COLUMN_DATE = "date"
        }
    }

    companion object {

        private val DATABASE = "news.db"

        private val VERSION_INT = 1
    }

}
