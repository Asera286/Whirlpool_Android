package edu.msu.elhazzat.whirpool;

/**
 * Created by christianwhite on 10/8/15.
 */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GeoJson {
    @SerializedName("name")
    private String mGeoJsonName;

    @SerializedName("type")
    private String mGeoJsonType;

    @SerializedName("features")
    private List<GeoJsonFeature> mGeoJsonFeatures = new ArrayList<GeoJsonFeature>();

    public String getName() {
        return mGeoJsonName;
    }

    public String getType() {
        return mGeoJsonType;
    }

    public List<GeoJsonFeature> getGeoJsonFeatures() {
        return mGeoJsonFeatures;
    }
}