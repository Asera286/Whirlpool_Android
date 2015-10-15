package edu.msu.elhazzat.whirpool.geojson;

import java.util.List;

/**
 * Created by christianwhite on 10/11/15.
 */
public class GeoJsonPoint extends Geometry<List<Double>> {
    GeoJsonPoint(List<Double> points) {
        mPoints = points;
    }
}
