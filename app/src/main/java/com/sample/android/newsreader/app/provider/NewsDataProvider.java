package com.sample.android.newsreader.app.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class NewsDataProvider extends ContentProvider {

    private static final String DATABASE = "news.db";

    private static final int VERSION_INT = 1;

    private SQLiteOpenHelper mOpenHelper = null;

    @Override
    public boolean onCreate() {
        mOpenHelper = new Helper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(Contract.TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return Contract.CONTENT_TYPE;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long id = mOpenHelper.getWritableDatabase().insert(Contract.TABLE, null, values);
        if (id > 0) {
            try {
                return ContentUris.withAppendedId(uri, id);
            } finally {
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
            }
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        try {
            return mOpenHelper.getWritableDatabase().delete(Contract.TABLE, selection, selectionArgs);
        } finally {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        try {
            return mOpenHelper.getWritableDatabase().update(Contract.TABLE, values, selection, selectionArgs);
        } finally {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
    }

    private static class Helper extends SQLiteOpenHelper {

        public Helper(Context context) {
            super(context, DATABASE, null, VERSION_INT);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createDatabase(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            dropDatabase(db);
            createDatabase(db);
        }

        private static void createDatabase(SQLiteDatabase db) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("CREATE TABLE ").append(Contract.TABLE).append(" ( ");
            buffer.append(Contract._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
            buffer.append(Contract.COLUMN_HEADLINE).append(" TEXT NOT NULL, ");
            buffer.append(Contract.COLUMN_DESC).append(" TEXT NOT NULL, ");
            buffer.append(Contract.COLUMN_LINK).append(" TEXT NOT NULL, ");
            buffer.append(Contract.COLUMN_DATE).append(" INTEGER );");

            db.execSQL(buffer.toString());
        }

        private static void dropDatabase(SQLiteDatabase db) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("DROP TABLE IF EXISTS ").append(Contract.TABLE);

            db.execSQL(buffer.toString());
        }
    }

    public interface Contract extends BaseColumns {
        String AUTHORITY = "com.sample.android.newsreader.app";

        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

        String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY;

        String TABLE = "news";

        String COLUMN_HEADLINE = "headline";

        String COLUMN_DESC = "description";

        String COLUMN_LINK = "link";

        String COLUMN_DATE = "date";
    }

}
