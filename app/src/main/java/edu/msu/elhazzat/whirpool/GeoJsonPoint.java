package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by christianwhite on 10/11/15.
 */
public class GeoJsonPoint extends BaseGeoJsonShape {
    private List<Double> mPointCoordinate;
    private LatLng mPoint;

    public void setPointCoordinate(List<Double> point) {
        mPointCoordinate = point;
    }
    public List<Double> getPointCoordinate() {
        return mPointCoordinate;
    }

    public void setPoint(LatLng point) { mPoint = point; }
    public LatLng getPoint() { return mPoint; }
}
