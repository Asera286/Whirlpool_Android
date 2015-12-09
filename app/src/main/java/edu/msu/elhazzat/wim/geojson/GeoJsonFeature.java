package edu.msu.elhazzat.wim.geojson;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by christianwhite on 10/8/15.
 */

/********************************************************************
 * Describes a the geometry and properties of the geometry
 ******************************************************************/
public class GeoJsonFeature {
    @SerializedName("type")
    private String mFeatureType; // type i.e polygon, polyline etc.

    @SerializedName("geometry")
    private GeoJsonGeometry mGeoJsonGeometry; // describes geometry

    @SerializedName("properties")
    private HashMap<String, String> mProperties; // map of properties i.e room name

    public String getType() {
        return mFeatureType;
    }

    public GeoJsonGeometry getGeoJsonGeometry() {
        return mGeoJsonGeometry;
    }

    public String getProperty(String key) {
        if(mProperties.containsKey(key)){
            return mProperties.get(key);
        }
        return null;
    }
}