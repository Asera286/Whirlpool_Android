package edu.msu.elhazzat.wim.geojson;

/**
 * Created by christianwhite on 10/8/15.
 */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/*****************************************************************
 * GeoJson object built using serializer
 *****************************************************************/
public class GeoJson {
    @SerializedName("name")
    private String mGeoJsonName;    //name of collection

    @SerializedName("type")
    private String mGeoJsonType;    //type of collection

    // feature consists of geometry and a map of properties (room name etc.)
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