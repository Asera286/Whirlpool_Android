package edu.msu.elhazzat.whirpool;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class WpGeoJsonGeometry {

    @SerializedName("type")
    private String mGeometryType;

    @SerializedName("coordinates")
    private List<List<List<Double>>> mCoordinates = new ArrayList<List<List<Double>>>();

    private List<Point> mListLatLng = new ArrayList<Point>();

    public String getType() {
        return mGeometryType;
    }

    public List<Point> getCoordinates() {
        if(mListLatLng == null) {
            setLatLngListFromCoordinates();
        }
        return mListLatLng;
    }

    private void setLatLngListFromCoordinates() {
        for(List<List<Double>> coordinatesList : mCoordinates) {
            if(mListLatLng == null) {
                mListLatLng = new ArrayList<>();
            }
            for(List<Double> coordinates : coordinatesList) {
                mListLatLng.add(new Point(coordinates.get(0), coordinates.get(1)));
            }
        }
    }

    public boolean contains(Point p) {
        if(mListLatLng == null) {
            setLatLngListFromCoordinates();
        }
        int n = mListLatLng.size() / 2;
        double angle=0;
        Point p1 = new Point();
        Point p2 = new Point();

        for(int i = 0; i < n; i++) {
            p1.x = mListLatLng.get(i).x - p.x;
            p1.y = mListLatLng.get(i).y - p.y;
            p2.x = mListLatLng.get((i+1)%n).x - p.x;
            p2.y = mListLatLng.get((i+1)%n).y - p.y;
            angle += angle2D(p1.x, p1.y, p2.x, p2.y);
        }

        if (Math.abs(angle) < Math.PI) {
            return false;
        }
        else {
            return (true);
        }
    }

    private double angle2D(double x1, double y1, double x2, double y2) {
        double dtheta,theta1,theta2;

        theta1 = Math.atan2(y1,x1);
        theta2 = Math.atan2(y2,x2);
        dtheta = theta2 - theta1;

        while (dtheta > Math.PI) {
            dtheta -= 2 * Math.PI;
        }

        while (dtheta < -1 * Math.PI) {
            dtheta += 2 * Math.PI;
        }

        return dtheta;
    }
}
