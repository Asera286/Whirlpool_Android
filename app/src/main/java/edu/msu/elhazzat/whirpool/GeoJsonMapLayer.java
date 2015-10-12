package edu.msu.elhazzat.whirpool;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonMapLayer {
    private List<GeoJsonFeature> mFeatures;
    private List<Polygon> mPolygons = new ArrayList<>();
    private List<Polyline> mPolylines = new ArrayList<>();
    private List<Point> mPoints = new ArrayList<>();
    private boolean mIsVisible = false;
    private boolean mIsDrawn = false;

    public void setFeatures(List<GeoJsonFeature> features) {
        mFeatures = features;
    }
    public List<GeoJsonFeature> getFeatures() {
        return mFeatures;
    }

    public void draw(final GoogleMap map, final int strokeWidth, final int color) {
        if(!mIsDrawn) {
            for (GeoJsonFeature feature : mFeatures) {
                final GeoJsonGeometry geometry = feature.getGeometry();
                if (geometry == null) {
                    continue;
                }
                if (geometry.getType().equals("Polygon")) {
                    GeoJsonPolygon polygon = (GeoJsonPolygon) geometry.getCoordinates();
                    new AsyncPolygonDrawer(map, polygon, color, strokeWidth).execute();
                }
                else if (geometry.getType().equals("LineString")) {
                    GeoJsonPolyline polyline = (GeoJsonPolyline) geometry.getCoordinates();
                    new AsyncPolylineDrawer(map, polyline, color, strokeWidth).execute();
                }
                else if (geometry.getType().equals("Point")) {

                }
            }
            mIsDrawn = true;
        }
        else {
            hide(false);
        }
    }

    public void remove() {
        for(Polygon polygon : mPolygons) {
            polygon.remove();
        }
        for(Polyline polyline : mPolylines) {
            polyline.remove();
        }
    }

    public void hide(boolean toShow) {
        for(Polygon polygon : mPolygons) {
            polygon.setVisible(toShow);
        }
        for(Polyline polyline : mPolylines) {
            polyline.setVisible(toShow);
        }
        mIsVisible = toShow;
    }

    public class AsyncPolygonDrawer extends AsyncTask<Void, Void, PolygonOptions> {
        private GoogleMap mMap;
        private GeoJsonPolygon mPolygon;
        private int mColor;
        private int mStrokeWidth;

        AsyncPolygonDrawer(GoogleMap map, GeoJsonPolygon polygon, int color, int strokeWidth) {
            mMap = map;
            mPolygon = polygon;
            mColor = color;
            mStrokeWidth = strokeWidth;
        }

        @Override
        public PolygonOptions doInBackground(Void... params) {
            mPolygon.setLatLngFromCoordinates();
            PolygonOptions options = new PolygonOptions()
                    .addAll(mPolygon.getPolygonLatLng())
                    .strokeColor(mColor)
                    .strokeWidth(mStrokeWidth);
            return options;
        }

        @Override
        public void onPostExecute(PolygonOptions options) {
            Polygon polygon = mMap.addPolygon(options);
            mPolygon.setGMSPolygon(polygon);
            mPolygons.add(polygon);
        }
    }

    public class AsyncPolylineDrawer extends AsyncTask<Void, Void, PolylineOptions> {
        private GoogleMap mMap;
        private GeoJsonPolyline mPolyline;
        private int mColor;
        private int mStrokeWidth;

        AsyncPolylineDrawer(GoogleMap map, GeoJsonPolyline polyline, int color, int strokeWidth) {
            mMap = map;
            mPolyline = polyline;
            mColor = color;
            mStrokeWidth = strokeWidth;
        }

        @Override
        public PolylineOptions doInBackground(Void... params) {
            mPolyline.setLatLngFromCoordinates();
            PolylineOptions options = new PolylineOptions()
                    .addAll(mPolyline.getPolylineLatLng()).width(mStrokeWidth).color(mColor);
            return options;
        }

        @Override
        public void onPostExecute(PolylineOptions options) {
            Polyline polyline = mMap.addPolyline(options);
            mPolyline.setGMSPolyline(polyline);
            mPolylines.add(polyline);
        }
    }
}