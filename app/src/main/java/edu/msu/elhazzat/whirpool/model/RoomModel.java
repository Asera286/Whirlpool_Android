package edu.msu.elhazzat.whirpool.model;


/**
 * Created by Asera on 10/9/2015.
 */
public class RoomModel {

    private String mName = null;
    private String mBuildingName = null;
    private String mRoomName = null;
    private String mEmail = null;
    private Boolean mOccupied = false;

    public RoomModel(String name, String email, Boolean occupied) {
        mName = name;
        mEmail = email;
        mOccupied = occupied;
    }

    public RoomModel() {

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

    public String getRoomName() {
        return mRoomName;
    }

    public void setRoomName(String roomName) {
        mRoomName = roomName;
    }

    public String getBuildingName() {
        return mBuildingName;
    }

    public void setBuildingName(String buildingName) {
        mBuildingName = buildingName;
    }

    public String toString() {
        return this.getRoomName();
    }
}
