package org.mukdongjeil.mjchurch.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by John Kim on 2015-08-19.
 */
public class DataHelper extends SQLiteOpenHelper {
    private static final String TAG = DataHelper.class.getSimpleName();

    private static final int DB_VERSION = 0;
    private static final String DB_NAME = "data.db";

    private static final String TABLE_DATA = "data";
    private static final String COL_ID = "_id";

    public DataHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE IF NOT EXIST " + TABLE_DATA +
                " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ");";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXIST " + TABLE_DATA;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
}
