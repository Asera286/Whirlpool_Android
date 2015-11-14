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
    private String mResourceName;

    public RoomModel() {

    }

    public RoomModel(String roomName, String buildingName, String extension,
                     String roomType, int capacity, String occupancyStatus,
                     String[] amenities, String email, String resourceName) {
        mBuildingName = buildingName;
        mRoomName = roomName;
        mCapacity = capacity;
        mOccupancyStatus = occupancyStatus;
        mExtension = extension;
        mRoomType = roomType;
        mAmenities = amenities;
        mEmail = email;
        mResourceName = resourceName;
    }

    public RoomModel(Parcel in){
        mBuildingName = in.readString();
        mRoomName = in.readString();
        mCapacity = in.readInt();
        mOccupancyStatus = in.readString();
        mExtension = in.readString();
        mRoomType = in.readString();
        mEmail = in.readString();
        mResourceName = in.readString();
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

    public String getResourceName() { return mResourceName; }

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

    public void setResourceName(String resource) { mResourceName = resource;}

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
        dest.writeString(mResourceName);
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
