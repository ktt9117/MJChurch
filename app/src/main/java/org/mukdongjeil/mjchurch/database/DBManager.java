package org.mukdongjeil.mjchurch.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

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

    public List<SermonItem> getSermonList(int sermonType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DataHelper.TABLE_SERMON +
                " WHERE " + SermonColumn.SERMON_TYPE + " = " + sermonType;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            List<SermonItem> list = new ArrayList<>();
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                SermonItem item = new SermonItem();
                item.title = cursor.getString(cursor.getColumnIndex(SermonColumn.TITLE));
                item.preacher = cursor.getString(cursor.getColumnIndex(SermonColumn.PREACHER));
                item.content = cursor.getString(cursor.getColumnIndex(SermonColumn.CONTENT));
                item.contentUrl = cursor.getString(cursor.getColumnIndex(SermonColumn.CONTENT_URL));
                item.date = cursor.getString(cursor.getColumnIndex(SermonColumn.DATE));
                item.chapterInfo = cursor.getString(cursor.getColumnIndex(SermonColumn.CHAPTER));
                item.audioUrl = cursor.getString(cursor.getColumnIndex(SermonColumn.AUDIO_URL));
                item.docUrl =cursor.getString(cursor.getColumnIndex(SermonColumn.DOC_URL));
                item.bbsNo = cursor.getString(cursor.getColumnIndex(SermonColumn.BBS_NO));
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

    public int insertData(SermonItem item, int sermonType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int res = insertData(db, item, sermonType);
        db.close();
        return res;
    }

    public int insertData(List<SermonItem> items, int sermonType) {
        if (items == null) {
            return -1;
        }

        int res = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (SermonItem item : items) {
                res += insertData(db, item, sermonType);
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

    public interface SermonColumn {
        String ID = "_id";
        String TITLE = "title";
        String CONTENT = "content";
        String CONTENT_URL = "content_url";
        String DATE = "date";
        String PREACHER = "preacher";
        String CHAPTER = "chapter_info";
        String AUDIO_URL = "audio_url";
        String DOC_URL = "doc_url";
        String BBS_NO = "bbs_no";
        String SERMON_TYPE = "sermon_type";
    }

    private int insertData(SQLiteDatabase db, SermonItem item, int sermonType) {
        ContentValues values = new ContentValues();
        values.put(SermonColumn.TITLE, item.title);
        values.put(SermonColumn.PREACHER, item.preacher);
        values.put(SermonColumn.CONTENT, item.content);
        values.put(SermonColumn.CONTENT_URL, item.contentUrl);
        values.put(SermonColumn.DATE, item.date);
        values.put(SermonColumn.CHAPTER, item.chapterInfo);
        values.put(SermonColumn.AUDIO_URL, item.audioUrl);
        values.put(SermonColumn.DOC_URL, item.docUrl);
        values.put(SermonColumn.BBS_NO, item.bbsNo);
        values.put(SermonColumn.SERMON_TYPE, sermonType);

        long res = db.insert(DataHelper.TABLE_SERMON, null, values);
        return (int)res;
    }

    private class DataHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 1; // Version must be >= 1
        private static final String DB_NAME = "data.db";

        public static final String TABLE_SERMON = "sermon";

        public DataHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_SERMON + " (" +
                    SermonColumn.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SermonColumn.TITLE + " TEXT, " +
                    SermonColumn.CONTENT + " TEXT ," +
                    SermonColumn.CONTENT_URL + " TEXT ," +
                    SermonColumn.DATE + " DATE, " +
                    SermonColumn.PREACHER + " TEXT, " +
                    SermonColumn.CHAPTER + " TEXT, " +
                    SermonColumn.AUDIO_URL + " TEXT, " +
                    SermonColumn.DOC_URL + " TEXT, " +
                    SermonColumn.SERMON_TYPE + " INTEGER, " +
                    SermonColumn.BBS_NO + " TEXT);";

            Logger.d(TAG, "create table query : " + sql);
            sqLiteDatabase.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            String sql = "DROP TABLE IF EXIST " + TABLE_SERMON;
            sqLiteDatabase.execSQL(sql);
            onCreate(sqLiteDatabase);
        }
    }
}
