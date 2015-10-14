package edu.msu.elhazzat.whirpool;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonGeometry {
    private String mGeometryType;

    private Geometry mGeometry;

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
