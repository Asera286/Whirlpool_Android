package edu.msu.elhazzat.whirpool.model;


/**
 * Created by Asera on 10/9/2015.
 */
public class RoomModel {

    private String mBuildingName = null;
    private String mRoomName = null;
    private int mCapacity;
    private String mExtension;
    private String mRoomType;
    private String mOccupancyStatus;
    private String[] mAmenities;

    public RoomModel() {

    }

    public RoomModel(String buildingName, String roomName, int capacity, String occupancyStatus,
                     String extension, String roomType, String[] amenities) {
        mBuildingName = buildingName;
        mRoomName = roomName;
        mCapacity = capacity;
        mOccupancyStatus = occupancyStatus;
        mExtension = extension;
        mRoomType = roomType;
        mAmenities = amenities;

    }

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

    public String[] getmAmenities() { return mAmenities; }
}
