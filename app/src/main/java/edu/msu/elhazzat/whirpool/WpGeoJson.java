package edu.msu.elhazzat.whirpool;

/**
 * Created by christianwhite on 10/8/15.
 */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class WpGeoJson {
    @SerializedName("name")
    private String mGeoJsonName;

    @SerializedName("type")
    private String mGeoJsonType;

    private List<WpGeoJsonFeature> features = new ArrayList<WpGeoJsonFeature>();

    public String getName() {
        return mGeoJsonName;
    }

    public String getType() {
        return mGeoJsonType;
    }

    public List<WpGeoJsonFeature> getFeatures() {
        return features;
    }
}