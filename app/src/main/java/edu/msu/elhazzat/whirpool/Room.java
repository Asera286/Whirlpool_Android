package edu.msu.elhazzat.whirpool;


/**
 * Created by Asera on 10/9/2015.
 */
public class Room {

    String mName, mEmail;
    Boolean mOccupied = false;

    public Room(String name, String email, Boolean occupied) {
        mName = name;
        mEmail = email;
        mOccupied = occupied;
    }

    public String GetName() {
        return mName;
    }

    public String GetEmail() {
        return mEmail;
    }

    public Boolean GetOccupied() {
        return mOccupied;
    }
}
