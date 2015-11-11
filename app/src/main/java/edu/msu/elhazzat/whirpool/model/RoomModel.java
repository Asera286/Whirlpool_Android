package edu.msu.elhazzat.whirpool.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Asera on 10/9/2015.
 */
public class RoomModel implements Parcelable {

    private String mBuildingName = null;
    private String mRoomName = null;
    private int mCapacity;
    private String mExtension;
    private String mRoomType;
    private String mOccupancyStatus;
    private String mEmail;
    private String[] mAmenities;

    public static final String ROOM_NAME = "room_id";
    public static final String ROOM_BUILDING_NAME = "building_name";
    public static final String ROOM_EXTENSION = "extension";
    public static final String ROOM_TYPE = "room_type";
    public static final String ROOM_CAPACITY = "room_cap";
    public static final String ROOM_EMAIL = "email";

    public RoomModel() {

    }

    public RoomModel(String roomName, String buildingName, String extension,
                     String roomType, int capacity, String occupancyStatus,
                     String[] amenities, String email) {
        mBuildingName = buildingName;
        mRoomName = roomName;
        mCapacity = capacity;
        mOccupancyStatus = occupancyStatus;
        mExtension = extension;
        mRoomType = roomType;
        mAmenities = amenities;
        mEmail = email;
    }

    public RoomModel(Parcel in){
        mBuildingName = in.readString();
        mRoomName = in.readString();
        mCapacity = in.readInt();
        mOccupancyStatus = in.readString();
        mExtension = in.readString();
        mRoomType = in.readString();
        mEmail = in.readString();
        mAmenities = (String[]) in.readSerializable();
    }

    public String getRoomName() {
        return mRoomName;
    }

    public String getBuildingName() {
        return mBuildingName;
    }

    public int getCapacity() {
        return mCapacity;
    }

    public String getOccupancyStatus() {
        return mOccupancyStatus;
    }

    public String getExtension() {
        return mExtension;
    }

    public String getRoomType() {
        return mRoomType;
    }

    public String getEmail() { return mEmail; }

    public String[] getAmenities() { return mAmenities; }

    public void setAmenities(String[] amenities) {
        mAmenities = amenities;
    }

    public void setRoomName(String roomName) {
        mRoomName = roomName;
    }

    public void setBuildingName(String buildingName) {
        mBuildingName = buildingName;
    }

    public void setCapacity(int capacity) {
        mCapacity = capacity;
    }

    public void setOccupancyStatus(String occupancyStatus) {
        mOccupancyStatus = occupancyStatus;
    }

    public void setRoomType(String roomType) {
        mRoomType = roomType;
    }

    public void setEmail(String email) { mEmail = email; }

    public void setExtension(String ext) { mExtension = ext; }

    public String toString() {
        return this.getRoomName();
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mBuildingName);
        dest.writeString(mRoomName);
        dest.writeInt(mCapacity);
        dest.writeString(mOccupancyStatus);
        dest.writeString(mExtension);
        dest.writeString(mRoomType);
        dest.writeSerializable(mAmenities);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public RoomModel createFromParcel(Parcel in) {
            return new RoomModel(in);
        }

        public RoomModel[] newArray(int size) {
            return new RoomModel[size];
        }
    };
}
