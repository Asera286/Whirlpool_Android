package edu.msu.elhazzat.whirpool;

/**
 * Created by Stephanie on 9/29/2015.
 */
public class ListModel {

    private String mStartTime;
    private String mEndTime;
    private String mLocation;
    private String mDescription;
    private String mSummary;
    private String mImage;

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

    public void setImage(String image) {
        mImage = image;
    }

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

    public String getImage() {
        return mImage;
    }
}