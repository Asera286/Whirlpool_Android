package edu.msu.elhazzat.whirpool;

import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class PolylineCoordinates extends BaseCoordinates {
    List<List<Double>> mPolyline;
    public void setPolyline(List<List<Double>> polyline) {
        mPolyline = polyline;
    }
    public List<List<Double>> getPolyline() {
        return mPolyline;
    }
}
