package edu.msu.elhazzat.whirpool.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;

import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonPolygon extends Geometry<List<List<List<Double>>>> {
    private Polygon mPolygon;
    public GeoJsonPolygon(List<List<List<Double>>> points) {
        mPoints = points;
    }

    // GMS polygon - used for reference during map click events
    public Polygon getGMSPolygon() {
        return mPolygon;
    }

    public void setGMSPolygon(Polygon polygon) {
        mPolygon = polygon;
    }

    public LatLngBounds getBoundingBox() {
        if(mPoints == null) {
            return null;
        }
        List<LatLng> latLngPoints = Geometry.geoJsonCoordinateListToLatLng(mPoints.get(0), false);
        LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
        for(int i = 0; i < latLngPoints.size(); i++) {
            boundsBuilder.include(latLngPoints.get(i));
        }
        return boundsBuilder.build();
    }

    /**
     * Find center point of polygon
     * @return
     */
    public LatLng getCentroid() {
        LatLngBounds bounds = getBoundingBox();
        if(bounds != null) {
            LatLng center =  bounds.getCenter();
            return new LatLng(center.longitude, center.latitude);
        }
        return null;
    }

    /**
     * Determine if a point is contained within this polygon
     * @param point
     * @return
     */
    public boolean contains(LatLng point) {
        List<LatLng> latLngPoints = Geometry.geoJsonCoordinateListToLatLng(mPoints.get(0), true);
        int crossings = 0;
        for (int i=0; i < latLngPoints.size(); i++) {
            LatLng a = latLngPoints.get(i);
            int j = i + 1;
            if (j >= latLngPoints.size()) {
                j = 0;
            }
            LatLng b = latLngPoints.get(j);
            if (rayCrossesSegment(point, a, b)) {
                ++crossings;
            }
        }
        return (crossings % 2 == 1);
    }

    /**
     * Ray cast algorithm for checking contains method
     * @param point
     * @param a
     * @param b
     * @return
     */
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