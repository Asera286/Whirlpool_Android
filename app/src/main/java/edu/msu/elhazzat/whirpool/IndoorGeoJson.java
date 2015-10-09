package edu.msu.elhazzat.whirpool;

/**
 * Created by christianwhite on 10/8/15.
 */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class IndoorGeoJson {
    @SerializedName("name")
    private String mGeoJsonName;

    @SerializedName("type")
    private String mGeoJsonType;

    private List<IndoorGeoJsonFeature> features = new ArrayList<IndoorGeoJsonFeature>();

    public String getName() {
        return mGeoJsonName;
    }

    public String getType() {
        return mGeoJsonType;
    }

    public List<IndoorGeoJsonFeature> getFeatures() {
        return features;
    }
}