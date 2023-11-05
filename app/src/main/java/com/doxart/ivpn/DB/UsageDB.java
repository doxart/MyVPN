package com.doxart.ivpn.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.doxart.ivpn.Model.UsageModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsageDB {
    private static final String DATABASE_NAME = "VPNUsageDatabase";
    private static final String TABLE_NAME = "VPNUsage";
    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DATE_TIME = "datetime";
    private static final String COLUMN_USAGE_MINUTES = "usage_minutes";

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yy ", Locale.US);

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_DATE_TIME + " LONG DEFAULT 0, " +
                    COLUMN_USAGE_MINUTES + " INTEGER);";

    private SQLiteDatabase database;
    private final DatabaseHelper dbHelper;

    public UsageDB(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public UsageDB open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long insertUsage(String date, long dateTime, int usageMinutes) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DATE_TIME, dateTime);
        values.put(COLUMN_USAGE_MINUTES, usageMinutes);
        return database.insert(TABLE_NAME, null, values);
    }

    public List<UsageModel> getAllUsageData() {
        Cursor cursor = database.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_DATE, COLUMN_DATE_TIME, COLUMN_USAGE_MINUTES}, null, null, null, null, null);
        List<UsageModel> list = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new UsageModel(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                        cursor.getLong(cursor.getColumnIndex(COLUMN_DATE_TIME)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_USAGE_MINUTES))));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public UsageModel getUsageDataByDate(String date) {
        Cursor cursor = database.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_DATE, COLUMN_DATE_TIME, COLUMN_USAGE_MINUTES}, COLUMN_DATE + "=?", new String[]{date}, null, null, null);
        UsageModel m = null;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            String dateUsage = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
            long dateTimeUsage = cursor.getLong(cursor.getColumnIndex(COLUMN_DATE_TIME));
            int usageMinutes = cursor.getInt(cursor.getColumnIndex(COLUMN_USAGE_MINUTES));

            m = new UsageModel(id, dateUsage, dateTimeUsage, usageMinutes);
        }

        cursor.close();
        return m;
    }

    public boolean updateUsage(String date, int usageMinutes) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USAGE_MINUTES, usageMinutes);

        int rowsAffected = database.update(TABLE_NAME, values, COLUMN_DATE + "=?", new String[]{date});
        return rowsAffected > 0;
    }

    public boolean deleteUsage(int id) {
        return database.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean isExist(String date) {
        Cursor cursor = database.query(TABLE_NAME, new String[]{COLUMN_ID}, COLUMN_DATE + "=?", new String[]{date}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
