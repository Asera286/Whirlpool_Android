package edu.msu.elhazzat.whirpool.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 11/11/15.
 */
public class BuildingModel {
    private int mFloors;
    private int mWings;
    private String mBuildingName;
    private List<RoomModel> mRooms = new ArrayList<>();

    public BuildingModel(String buildingName, int floors, int wings) {
        mBuildingName = buildingName;
        mFloors = floors;
        mWings = wings;
    }

    public BuildingModel(String buildingName, List<RoomModel> rooms) {
        mBuildingName = buildingName;
        mRooms = rooms;
    }

    public int getFloors() {
        return mFloors;
    }

    public void setFloors(int floors) {
        mFloors = floors;
    }

    public int getWings() {
        return mWings;
    }

    public void setWings(int wings) {
        mWings = wings;
    }

    public String getBuildingName() {
        return mBuildingName;
    }

    public void setBuildingName(String name) {
        mBuildingName = name;
    }

    public List<RoomModel> getRooms() {
        return mRooms;
    }

    public void setRooms(List<RoomModel> rooms) {
        mRooms = rooms;
    }
}
