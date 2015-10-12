package edu.msu.elhazzat.whirpool;

import com.google.gson.annotations.SerializedName;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonGeometry {
    @SerializedName("type")
    private String mGeometryType;

    @SerializedName("coordinates")
    private BaseGeoJsonShape mCoordinates;

    public BaseGeoJsonShape getCoordinates() {
        return mCoordinates;
    }

    public String getType() {
        return mGeometryType;
    }
}
