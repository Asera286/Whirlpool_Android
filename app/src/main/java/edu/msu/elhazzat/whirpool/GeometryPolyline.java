package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeometryPolyline {
    private Polyline mPolyline;
    private List<LatLng> mPolylineCoordinates = new ArrayList<LatLng>();

    GeometryPolyline(List<LatLng> coordinates) {
        mPolylineCoordinates = coordinates;
    }

    GeometryPolyline(PolylineCoordinates polyline) {
        if(polyline != null) {
            List<List<Double>> polylineCoordinates = polyline.getPolyline();
            if(polylineCoordinates.size() > 0) {
                for(List<Double> coordinate : polylineCoordinates) {
                    mPolylineCoordinates.add(new LatLng(coordinate.get(1), coordinate.get(0)));
                }
            }
        }
    }

    public Polyline getGMSPolyline() {
        return mPolyline;
    }

    public void setGMSPolyline(Polyline polyline) {
        mPolyline = polyline;
    }

    public List<LatLng> getPolylineCoordinates() {
        return mPolylineCoordinates;
    }
}
