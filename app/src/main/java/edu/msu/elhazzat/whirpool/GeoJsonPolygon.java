package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonPolygon extends BaseGeoJsonShape {
    private List<List<List<Double>>> mPolygonCoordinates = new ArrayList<>();
    private List<LatLng> mPolygonLatLng = new ArrayList<>();
    private Polygon mPolygon;

    public void setPolygonCoordinates(List<List<List<Double>>> coordinates) {
        mPolygonCoordinates = coordinates;
    }

    public List<List<List<Double>>> getPolygonCoordinates() {
        return mPolygonCoordinates;
    }

    public void setPolygonLatLng(List<LatLng> coordinates) {
        mPolygonLatLng = coordinates;
    }

    public List<LatLng> getPolygonLatLng() {
        return mPolygonLatLng;
    }

    public Polygon getGMSPolygon() {
        return mPolygon;
    }

    public void setGMSPolygon(Polygon polygon) {
        mPolygon = polygon;
    }

    public void setLatLngFromCoordinates() {
        if(mPolygonCoordinates != null && mPolygonCoordinates.size() > 0) {
            for (List<Double> coordinate : mPolygonCoordinates.get(0)) {
                mPolygonLatLng.add(new LatLng(coordinate.get(1), coordinate.get(0)));
            }
        }
    }

    public boolean contains(LatLng point) {
        if(mPolygonLatLng == null) {
            setLatLngFromCoordinates();
        }

        int crossings = 0;
        for (int i=0; i < mPolygonLatLng.size(); i++) {
            LatLng a = mPolygonLatLng.get(i);
            int j = i + 1;
            if (j >= mPolygonLatLng.size()) {
                j = 0;
            }
            LatLng b = mPolygonLatLng.get(j);
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
            ax += 360;
            bx += 360;
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