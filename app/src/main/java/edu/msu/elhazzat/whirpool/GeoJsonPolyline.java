package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonPolyline extends BaseGeoJsonShape {
    private List<List<Double>> mPolylineCoordinates = new ArrayList<>();
    private List<LatLng> mPolylineLatLng = new ArrayList<>();
    private Polyline mPolyline;

    public void setPolylineCoordinates(List<List<Double>> coordinates) {
        mPolylineCoordinates = coordinates;
    }

    public List<List<Double>> getPolylineCoordinates() {
        return mPolylineCoordinates;
    }

    public void setPolylineLatLng(List<LatLng> coordinates) {
        mPolylineLatLng = coordinates;
    }

    public List<LatLng> getPolylineLatLng() {
        return mPolylineLatLng;
    }

    public void setGMSPolyline(Polyline polyline) {
        mPolyline = polyline;
    }

    public Polyline getGMSPolyline() {
        return mPolyline;
    }

    public void setLatLngFromCoordinates() {
        if(mPolylineCoordinates != null && mPolylineCoordinates.size() > 0) {
            for (List<Double> coordinate : mPolylineCoordinates) {
                mPolylineLatLng.add(new LatLng(coordinate.get(1), coordinate.get(0)));
            }
        }
    }

}
