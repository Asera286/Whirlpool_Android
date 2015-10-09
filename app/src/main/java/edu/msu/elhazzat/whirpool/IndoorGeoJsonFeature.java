package edu.msu.elhazzat.whirpool;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by christianwhite on 10/8/15.
 */
public class IndoorGeoJsonFeature {
    @SerializedName("type")
    private String mFeatureType;

    @SerializedName("geometry")
    private IndoorGeoJsonGeometry mGeometry;

    @SerializedName("properties")
    private HashMap<String, String> mProperties;

    public String getType() {
        return mFeatureType;
    }

    public IndoorGeoJsonGeometry getGeometry() {
        return mGeometry;
    }

    public String getProperty(String key) {
        return mProperties.get(key);
    }
}