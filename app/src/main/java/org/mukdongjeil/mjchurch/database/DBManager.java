package org.mukdongjeil.mjchurch.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.mukdongjeil.mjchurch.common.dao.BoardItem;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John Kim on 2015-08-19.
 */
public class DBManager  {
    private static final String TAG = DBManager.class.getSimpleName();

    private static DBManager instance;
    private DataHelper dbHelper;

    private DBManager(Context context) {
        dbHelper = new DataHelper(context);
    }

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        return instance;
    }

    /** Sermon Relate Query */
    public List<SermonItem> getSermonList(int sermonType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DataHelper.TABLE_SERMON +
                " WHERE " + SermonCols.SERMON_TYPE + " = " + sermonType;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            List<SermonItem> list = new ArrayList<>();
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                SermonItem item = new SermonItem();
                item._id = cursor.getInt(cursor.getColumnIndex(SermonCols.ID));
                item.title = cursor.getString(cursor.getColumnIndex(SermonCols.TITLE));
                item.preacher = cursor.getString(cursor.getColumnIndex(SermonCols.PREACHER));
                item.content = cursor.getString(cursor.getColumnIndex(SermonCols.CONTENT));
                item.contentUrl = cursor.getString(cursor.getColumnIndex(SermonCols.CONTENT_URL));
                item.date = cursor.getString(cursor.getColumnIndex(SermonCols.DATE));
                item.chapterInfo = cursor.getString(cursor.getColumnIndex(SermonCols.CHAPTER));
                item.audioUrl = cursor.getString(cursor.getColumnIndex(SermonCols.AUDIO_URL));
                item.docUrl =cursor.getString(cursor.getColumnIndex(SermonCols.DOC_URL));
                item.bbsNo = cursor.getString(cursor.getColumnIndex(SermonCols.BBS_NO));
                item.downloadQueryId = cursor.getLong(cursor.getColumnIndex(SermonCols.DOWNLOAD_QUERY_ID));
                list.add(item);
            }
            Logger.i(TAG, "getSermonList > item count : " + list.size());
            cursor.close();
            db.close();
            return list;
        } else {
            db.close();
            Logger.e(TAG, "getSermonList > cursor is null. query : " + sql);
            return null;
        }
    }

    public int insertSermon(SermonItem item, int sermonType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int res = insertSermon(db, item, sermonType);
        db.close();
        return res;
    }

    /*
    public int insertSermon(List<SermonItem> items, int sermonType) {
        if (items == null) {
            return -1;
        }

        int res = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (SermonItem item : items) {
                res += insertSermon(db, item, sermonType);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
            res = -1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return res;
    }
    */

    /** Thank Share Relate Query */
    public List<BoardItem> getThankShareList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DataHelper.TABLE_THANK_SHARE;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            List<BoardItem> list = new ArrayList<>();
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                BoardItem item = new BoardItem();
                item.title = cursor.getString(cursor.getColumnIndex(ThankShareCols.TITLE));
                item.writer = cursor.getString(cursor.getColumnIndex(ThankShareCols.WRITER));
                item.content = cursor.getString(cursor.getColumnIndex(ThankShareCols.CONTENT));
                item.date = cursor.getString(cursor.getColumnIndex(ThankShareCols.DATE));
                item.contentUrl = cursor.getString(cursor.getColumnIndex(ThankShareCols.CONTENT_URL));
                list.add(item);
            }
            Logger.i(TAG, "getThankShareList > item count : " + list.size());
            cursor.close();
            db.close();
            return list;
        } else {
            db.close();
            Logger.e(TAG, "getThankShareList > cursor is null. query : " + sql);
            return null;
        }
    }

    public int insertThankShare(BoardItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ThankShareCols.TITLE, item.title);
        values.put(ThankShareCols.WRITER, item.writer);
        values.put(ThankShareCols.CONTENT, item.content);
        values.put(ThankShareCols.DATE, item.date);
        values.put(ThankShareCols.CONTENT_URL, item.contentUrl);

        long res = db.insert(DataHelper.TABLE_THANK_SHARE, null, values);
        db.close();
        return (int) res;
    }

    private int insertSermon(SQLiteDatabase db, SermonItem item, int sermonType) {
        ContentValues values = new ContentValues();
        values.put(SermonCols.TITLE, item.title);
        values.put(SermonCols.PREACHER, item.preacher);
        values.put(SermonCols.CONTENT, item.content);
        values.put(SermonCols.CONTENT_URL, item.contentUrl);
        values.put(SermonCols.DATE, item.date);
        values.put(SermonCols.CHAPTER, item.chapterInfo);
        values.put(SermonCols.AUDIO_URL, item.audioUrl);
        values.put(SermonCols.DOC_URL, item.docUrl);
        values.put(SermonCols.BBS_NO, item.bbsNo);
        values.put(SermonCols.SERMON_TYPE, sermonType);

        long res = db.insert(DataHelper.TABLE_SERMON, null, values);
        return (int)res;
    }

    private class DataHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 3; // Version must be >= 1
        private static final String DB_NAME = "data.db";

        public static final String TABLE_SERMON = "sermon";
        public static final String TABLE_THANK_SHARE = "thank_share";

        public DataHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String createTableSermon = "CREATE TABLE IF NOT EXISTS " + TABLE_SERMON + " (" +
                    SermonCols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SermonCols.TITLE + " TEXT, " +
                    SermonCols.CONTENT + " TEXT ," +
                    SermonCols.CONTENT_URL + " TEXT ," +
                    SermonCols.DATE + " DATE, " +
                    SermonCols.PREACHER + " TEXT, " +
                    SermonCols.CHAPTER + " TEXT, " +
                    SermonCols.AUDIO_URL + " TEXT, " +
                    SermonCols.DOC_URL + " TEXT, " +
                    SermonCols.SERMON_TYPE + " INTEGER, " +
                    SermonCols.DOWNLOAD_QUERY_ID + " LONG, " +
                    SermonCols.BBS_NO + " TEXT);";
            Logger.d(TAG, "create table sermon query : " + createTableSermon);

            String createTableThankShare = "CREATE TABLE IF NOT EXISTS " + TABLE_THANK_SHARE + " (" +
                    ThankShareCols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ThankShareCols.TITLE + " TEXT, " +
                    ThankShareCols.CONTENT + " TEXT ," +
                    ThankShareCols.DATE + " DATE, " +
                    ThankShareCols.CONTENT_URL + " TEXT ," +
                    ThankShareCols.WRITER + " TEXT);";
            Logger.d(TAG, "create table thank_share query : " + createTableThankShare);

            sqLiteDatabase.execSQL(createTableSermon);
            sqLiteDatabase.execSQL(createTableThankShare);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            String dropTableSermon = "DROP TABLE IF EXISTS " + TABLE_SERMON;
            String dropTableThankShare = "DROP TABLE IF EXISTS " + TABLE_THANK_SHARE;
            sqLiteDatabase.execSQL(dropTableSermon);
            sqLiteDatabase.execSQL(dropTableThankShare);
            onCreate(sqLiteDatabase);
        }
    }
}
