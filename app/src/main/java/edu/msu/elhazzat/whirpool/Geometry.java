package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/13/15.
 */
public class Geometry<T> {
    protected T mPoints;
    public T getPoints() {
        return mPoints;
    }
    public void setPoints(T points) {
        mPoints = points;
    }

    public static LatLng geoJsonCoordinateToLatLng(List<Double> coordinate, boolean longitudeFirst) {
        if(coordinate == null || coordinate.size() < 2) {
            return null;
        }
        if(longitudeFirst) {
            return new LatLng(coordinate.get(1), coordinate.get(0));
        }
        else {
            return new LatLng(coordinate.get(0), coordinate.get(1));
        }
    }

    public static List<LatLng> geoJsonCoordinateListToLatLng(List<List<Double>> coordinates, boolean longitudeFirst) {
        if(coordinates == null || coordinates.size() == 0) {
            return null;
        }
        List<LatLng> latLngCoordinates = new ArrayList<>();
        for(List<Double> coordinate : coordinates) {
            LatLng latLngCoordinate = Geometry.geoJsonCoordinateToLatLng(coordinate, longitudeFirst);
            if(latLngCoordinate != null) {
                latLngCoordinates.add(latLngCoordinate);
            }
        }

        return latLngCoordinates;
    }
}