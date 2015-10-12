package edu.msu.elhazzat.whirpool;

import java.util.List;

/**
 * Created by christianwhite on 10/11/15.
 */
public class PointCoordinates extends BaseCoordinates {
    List<Double> mPoint;
    public void setPoint(List<Double> point) {
        mPoint = point;
    }
    public List<Double> getPoint() {
        return mPoint;
    }
}
