package edu.msu.elhazzat.whirpool;

import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class PolygonCoordinates extends BaseCoordinates {
    List<List<List<Double>>> mPolygon;
    public void setPolygon(List<List<List<Double>>> polygon) {
        mPolygon = polygon;
    }
    public List<List<List<Double>>> getPolygon() {
        return mPolygon;
    }
}