package edu.msu.elhazzat.whirpool.crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by christianwhite on 12/8/15.
 */

/************************************************************************************
 * Store timestamp/building name - use to determine if geojson should
 * be pulled from internal storage or if a network request should be made
 ************************************************************************************/
public class CachedGeoJsonDataDbHelper extends SQLiteOpenHelper {
    // Database Version
    public static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "WhirlpoolCacheDB5.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String TIME_STAMP_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    public static final String TABLE_NAME = "file_entry";
    public static final String LAST_UPDATE = "last_update";
    public static final String BUILDING_NAME = "building_name";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" + BUILDING_NAME + TEXT_TYPE + COMMA_SEP
                    + LAST_UPDATE + TIME_STAMP_TYPE  + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public CachedGeoJsonDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    public void addCacheInformation(String buildingName, int time) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BUILDING_NAME, buildingName);
        values.put(LAST_UPDATE, time);

        // Inserting Row
        db.insertOrThrow(TABLE_NAME, null, values);

        db.close(); // Closing database connection
    }

    // Getting timestamp
    public Integer getTimestamp(String buildingName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{ LAST_UPDATE } ,
                BUILDING_NAME + " = ?",
                new String[]{buildingName}, null, null, null, null);

        if(cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex(LAST_UPDATE));
        }

        return null;
    }

    // Update cache info
    public int updateCache(String buildingName, int time) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(BUILDING_NAME, buildingName);
        values.put(LAST_UPDATE, time);

        // updating row
        return db.update(TABLE_NAME, values, BUILDING_NAME + " = ?",
                new String[]{buildingName});
    }

}