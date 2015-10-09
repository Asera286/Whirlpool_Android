package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class WpPolygon {

    private List<LatLng> mListLatLng = new ArrayList<LatLng>();

    WpPolygon(List<List<Double>> coordinatesList) {
        for(List<Double> coordinates : coordinatesList) {
            mListLatLng.add(new LatLng(coordinates.get(1), coordinates.get(0)));
        }
    }

    public List<LatLng> getLatLngList() {
        return mListLatLng;
    }

    public boolean contains(LatLng point) {
        // ray casting alogrithm http://rosettacode.org/wiki/Ray-casting_algorithm
        int crossings = 0;

        // for each edge
        for (int i=0; i < mListLatLng.size(); i++) {
            LatLng a = mListLatLng.get(i);
            int j = i + 1;
            if (j >= mListLatLng.size()) {
                j = 0;
            }
            LatLng b = mListLatLng.get(j);
            if (rayCrossesSegment(point, a, b)) {
                ++crossings;
            }
        }
        return (crossings % 2 == 1);
    }

    public boolean rayCrossesSegment(LatLng point, LatLng a,LatLng b) {
        double px = point.longitude,
                py = point.latitude,
                ax = a.longitude,
                ay = a.latitude,
                bx = b.longitude,
                by = b.latitude;

        if (ay > by) {
            ax = b.longitude;
            ay = b.latitude;
            bx = a.longitude;
            by = a.latitude;
        }

        if (px < 0 || ax <0 || bx <0) {
            px += 360;
            ax+=360;
            bx+=360;
        }

        if (py == ay || py == by) {
            py += 0.00000001;
        }

        if ((py > by || py < ay) || (px > Math.max(ax, bx))){
            return false;
        }
        else if (px < Math.min(ax, bx)){
            return true;
        }
        else {
            double red = (ax != bx) ? ((by - ay) / (bx - ax)) : Double.POSITIVE_INFINITY;
            double blue = (ax != px) ? ((py - ay) / (px - ax)) : Double.POSITIVE_INFINITY;
            return (blue >= red);
        }

    }
}