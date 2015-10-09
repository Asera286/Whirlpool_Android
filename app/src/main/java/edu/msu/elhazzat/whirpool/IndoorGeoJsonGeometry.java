package edu.msu.elhazzat.whirpool;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class IndoorGeoJsonGeometry {
    @SerializedName("type")
    private String mGeometryType;

    @SerializedName("coordinates")
    private List<List<List<Double>>> mCoordinates = new ArrayList<List<List<Double>>>();

    public String getType() {
        return mGeometryType;
    }

    public List<List<List<Double>>> getCoordinates() {
        return mCoordinates;
    }
}
