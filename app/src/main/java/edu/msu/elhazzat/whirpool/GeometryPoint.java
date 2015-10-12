package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeometryPoint {
    private LatLng mPoint;

    GeometryPoint(LatLng point) {
        mPoint = point;
    }

    GeometryPoint(List<Double> point) {
        if(point != null && point.size() > 0) {
            mPoint = new LatLng(point.get(1), point.get(0));
        }
    }

    public LatLng getPoint() {
        return mPoint;
    }
}
