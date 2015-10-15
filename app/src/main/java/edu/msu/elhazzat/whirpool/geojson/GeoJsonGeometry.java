package edu.msu.elhazzat.whirpool.geojson;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonGeometry {
    private String mGeometryType;

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
