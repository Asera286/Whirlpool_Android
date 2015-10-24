package edu.msu.elhazzat.whirpool.crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.whirpool.model.RoomModel;

/**
 * Created by christianwhite on 10/17/15.
 */

public class RelevantRoomDbHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "RoomModelDB.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    public static final String TABLE_NAME = "room_entry";
    public static final String ROOM_ENTRY_ID = "entry_id";
    public static final String ROOM_RESOURCE_EMAIL = "email";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" + ROOM_ENTRY_ID + TEXT_TYPE + COMMA_SEP + ROOM_RESOURCE_EMAIL + TEXT_TYPE +
                    " )";

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

        ContentValues values = new ContentValues();
        values.put(ROOM_ENTRY_ID, room.getRoomName()); // Contact Name
        values.put(ROOM_RESOURCE_EMAIL, room.getEmail()); // Contact Phone

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
  /*  RoomModel getRoomModel(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { _ID,
                        KEY_NAME, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        // return contact
        return contact;
    }*/

    // Getting All Contacts
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
                room.setEmail(cursor.getString(1));
                // Adding contact to list
                roomList.add(room);
            } while (cursor.moveToNext());
        }

        // return contact list
        return roomList;
    }

    // Updating single contact
  /*  public int updateRoomModel(RoomModel room) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ROOM_ENTRY_ID, room.getName());
        values.put(ROOM_RESOURCE_EMAIL, room.getPhoneNumber());

        // updating row
        return db.update(TABLE_NAME, values, _ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
    }*/

    // Deleting single contact
 /*   public void deleteContact(RoomModel room) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
        db.close();
    }


    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }*/

}