package edu.msu.elhazzat.wim.model;

/**
 * Created by Stephanie on 9/29/2015.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.DateTime;

/************************************************************************************
 * Abstraction of calendar event
 ************************************************************************************/
public class EventModel implements Parcelable {

    private String mStartTime;
    private String mEndTime;
    private String mLocation;
    private String mDescription;
    private String mSummary;
    private String mEmail;
    private String mId;

    private DateTime mStartDateTime;
    private DateTime mEndDateTime;

    public EventModel(String id, String email, String description,
                      String location, String startTime, String endTime) {
        mId = id;
        mEmail = email;
        mDescription = description;
        mLocation = location;
        mStartTime = startTime;
        mEndTime = endTime;
    }

    /**
     * Allow for passing in bundle
     * @param in
     */
    public EventModel(Parcel in){
        String[] data = new String[6];

        in.readStringArray(data);
        mId = data[0];
        mEmail = data[1];
        mDescription = data[2];
        mLocation = data[3];
        mStartTime = data[4];
        mEndTime = data[5];
    }

    public EventModel() {

    }

    public void setStartTime(String start) {
        mStartTime = start;
    }

    public void setEndTime(String end) {
        mEndTime = end;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public void setEmail(String email) { mEmail = email; }

    public void setId(String id) { mId = id; }

    public String getStartTime() {
        return mStartTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getDesription() {
        return mDescription;
    }

    public String getSummary() {
        return mSummary;
    }

    public String getEmail() { return mEmail; }

    public String getId() { return mId; }

    public DateTime getStartDateTime() {
        return mStartDateTime;
    }

    public DateTime getEndDateTime() {
        return mEndDateTime;
    }

    public void setStartDateTime(DateTime time) {
        mStartDateTime = time;
    }

    public void setEndDateTime(DateTime time) {
        mEndDateTime = time;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                mId, mEmail,
                mDescription,
                mLocation,
                mStartTime,
                mEndTime});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public EventModel createFromParcel(Parcel in) {
            return new EventModel(in);
        }

        public EventModel[] newArray(int size) {
            return new EventModel[size];
        }
    };
}