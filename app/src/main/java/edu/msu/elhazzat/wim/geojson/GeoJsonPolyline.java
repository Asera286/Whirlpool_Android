package edu.msu.elhazzat.wim.geojson;

import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonPolyline extends Geometry<List<List<Double>>> {
    public GeoJsonPolyline(List<List<Double>> points) {
        mPoints = points;
    }
}
