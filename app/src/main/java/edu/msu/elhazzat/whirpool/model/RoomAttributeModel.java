package edu.msu.elhazzat.whirpool.model;

/**
 * Created by christianwhite on 10/25/15.
 */
public class RoomAttributeModel {
    public String mAttributeType;
    public String mAttributeValue;

    public RoomAttributeModel() {

    }

    public RoomAttributeModel(String type, String value) {
        mAttributeType = type;
        mAttributeValue = value;
    }

    public void setAttributeType(String type) {
        mAttributeType = type;
    }

    public String getAttributeType() {
        return mAttributeType;
    }

    public void setAttributeValue(String value) {
        mAttributeValue = value;
    }

    public String getmAttributeValue() {
        return mAttributeValue;
    }
}
