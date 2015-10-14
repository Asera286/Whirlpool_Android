package edu.msu.elhazzat.whirpool;


/**
 * Created by Asera on 10/9/2015.
 */
public class Room {

    private String mName = null;
    private String mEmail = null;
    private Boolean mOccupied = false;

    Room(String name, String email, Boolean occupied) {
        mName = name;
        mEmail = email;
        mOccupied = occupied;
    }

    Room() {

    }

    public String getName() {
        return mName;
    }

    public void setName(String name) { mName = name; }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) { mEmail = email; }

    public Boolean getOccupied() {
        return mOccupied;
    }

    public void setOccupied(boolean occupied) { mOccupied = occupied; }
}
