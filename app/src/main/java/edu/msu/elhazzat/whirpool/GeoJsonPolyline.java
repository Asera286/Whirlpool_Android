package edu.msu.elhazzat.whirpool;

import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonPolyline extends Geometry<List<List<Double>>> {
    GeoJsonPolyline(List<List<Double>> points) {
        mPoints = points;
    }
}
