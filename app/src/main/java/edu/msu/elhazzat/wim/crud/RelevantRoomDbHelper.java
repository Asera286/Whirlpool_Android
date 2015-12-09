package edu.msu.elhazzat.wim.crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.wim.model.RoomModel;

/**
 * Created by christianwhite on 10/17/15.
 */

public class RelevantRoomDbHelper extends SQLiteOpenHelper {

    // Database Version
    public static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "WhirlpoolRoomModelDB.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";

    public static final String TABLE_NAME = "room_entry";
    public static final String ROOM_NAME = "room_id";
    public static final String ROOM_BUILDING_NAME = "building_name";
    public static final String ROOM_EXTENSION = "extension";
    public static final String ROOM_TYPE = "room_type";
    public static final String ROOM_CAPACITY = "room_cap";
    public static final String ROOM_EMAIL = "email";
    public static final String ROOM_RESOURCE_NAME = "resource_name";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" + ROOM_NAME + TEXT_TYPE + COMMA_SEP
                + ROOM_BUILDING_NAME + TEXT_TYPE + COMMA_SEP
                + ROOM_EXTENSION + TEXT_TYPE + COMMA_SEP
                + ROOM_TYPE + TEXT_TYPE + COMMA_SEP
                + ROOM_CAPACITY + INT_TYPE + COMMA_SEP
                + ROOM_EMAIL + TEXT_TYPE + COMMA_SEP + ROOM_RESOURCE_NAME
                    + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public RelevantRoomDbHelper(Context context) {
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
    public void addRelevantRoom(RoomModel room) {
        SQLiteDatabase db = this.getWritableDatabase();

        RoomModel checkExists = getRoomModel(room.getBuildingName(), room.getRoomName());

        if(checkExists == null) {
            ContentValues values = new ContentValues();
            values.put(ROOM_NAME, room.getRoomName());
            values.put(ROOM_BUILDING_NAME, room.getBuildingName());
            values.put(ROOM_EXTENSION, room.getExtension());
            values.put(ROOM_TYPE, room.getRoomType());
            values.put(ROOM_CAPACITY, room.getCapacity());
            values.put(ROOM_EMAIL, room.getEmail());
            values.put(ROOM_RESOURCE_NAME, room.getResourceName());

            // Inserting Row
            db.insert(TABLE_NAME, null, values);
        }
        db.close(); // Closing database connection
    }

    // Getting single room model
    RoomModel getRoomModel(String buildingName, String roomId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{
                        ROOM_NAME, ROOM_BUILDING_NAME,
                        ROOM_EXTENSION, ROOM_TYPE,
                        ROOM_CAPACITY, ROOM_EMAIL, ROOM_RESOURCE_NAME}, ROOM_BUILDING_NAME + " =? " + "AND " + ROOM_NAME + " =? ",
                new String[]{buildingName, roomId}, null, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return new RoomModel(
                    cursor.getString(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3), cursor.getInt(4),
                    cursor.getString(5), null, cursor.getString(6), null);
        }

        // return room
        return null;
    }

    // Getting All rooms
    public List<RoomModel> getAllRelevantRooms() {
        List<RoomModel> roomList = new ArrayList<RoomModel>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RoomModel room = new RoomModel();
                room.setRoomName(cursor.getString(0));
                room.setBuildingName(cursor.getString(1));
                room.setExtension(cursor.getString(2));
                room.setRoomType(cursor.getString(3));
                room.setCapacity(cursor.getInt(4));
                room.setEmail(cursor.getString(5));
                room.setResourceName(cursor.getString(6));
                roomList.add(room);
            } while (cursor.moveToNext());
        }

        // return room list
        return roomList;
    }

    // Updating single room model
    public int updateRoomModel(RoomModel room) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(ROOM_NAME, room.getRoomName());
        values.put(ROOM_BUILDING_NAME, room.getBuildingName());
        values.put(ROOM_EXTENSION, room.getExtension());
        values.put(ROOM_TYPE, room.getRoomType());
        values.put(ROOM_CAPACITY, room.getCapacity());
        values.put(ROOM_EMAIL, room.getEmail());
        values.put(ROOM_RESOURCE_NAME, room.getResourceName());

        // updating row
        return db.update(TABLE_NAME, values, ROOM_BUILDING_NAME + " =? AND " + ROOM_NAME + " =?",
                new String[] { room.getBuildingName(), room.getRoomName() });
    }

    // Deleting single room
     public void deleteRoomModel(RoomModel room) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ROOM_BUILDING_NAME + " =? AND " + ROOM_NAME + " =?",
                new String[]{room.getBuildingName(), room.getRoomName()});
        db.close();
    }
}