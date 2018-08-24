package com.haram.firebasephototag.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.haram.firebasephototag.R;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "label.db";
    private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " +
            DBContract.DBEntry.TABLE_NAME + " (" +
            DBContract.DBEntry.COLUMN_NAME_LABEL + " TEXT, " +
            DBContract.DBEntry.COLUMN_NAME_IMAGES+ " TEXT)";
    private static final String DROP_SQL = "DROP TABLE IF EXISTS " + DBContract.DBEntry.TABLE_NAME;

    private static DBHelper instance;
    private SQLiteDatabase db;

    public static DBHelper getInstance(Context context) {
        if(instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    public SQLiteDatabase open() {
        if(db == null) {
            db = instance.getWritableDatabase();
        }
        return db;
    }

    public void close() {
        if(db.isOpen()) {
            db.close();
        }
    }


    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(DROP_SQL);
        onCreate(sqLiteDatabase);
    }
}
