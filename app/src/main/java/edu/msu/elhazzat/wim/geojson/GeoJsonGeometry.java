package edu.msu.elhazzat.wim.geojson;

/**
 * Created by christianwhite on 10/8/15.
 */

/****************************************************************
 * Describes geometry of a geojson object
 ****************************************************************/
public class GeoJsonGeometry {
    private String mGeometryType; // type - polygon/polyline etc.

    private Geometry mGeometry;

    // return actual shape - polygon, polyline, point etc.
    public Geometry getGeometry() {
        return mGeometry;
    }

    public void setGeometry(Geometry geometry) {
        mGeometry = geometry;
    }

    public String getType() {
        return mGeometryType;
    }

    public void setType(String type) {
        mGeometryType = type;
    }
}
