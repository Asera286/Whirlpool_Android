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
                    new AsyncPolygonDrawer(map, geometry, color, strokeWidth).execute();
                }
                else if (geometry.getType().equals("LineString")) {
                    new AsyncPolylineDrawer(map, geometry, color, strokeWidth).execute();
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
        private GeoJsonGeometry mGeometry;
        private int mColor;
        private int mStrokeWidth;

        AsyncPolygonDrawer(GoogleMap map, GeoJsonGeometry geometry, int color, int strokeWidth) {
            mMap = map;
            mGeometry = geometry;
            mColor = color;
            mStrokeWidth = strokeWidth;
        }

        @Override
        public PolygonOptions doInBackground(Void... params) {
            PolygonCoordinates coordinates = (PolygonCoordinates) mGeometry.getCoordinates();
            GeometryPolygon polygon = new GeometryPolygon(coordinates);
            PolygonOptions options = new PolygonOptions()
                    .addAll(polygon.getPolygonCoordinates())
                    .strokeColor(mColor)
                    .strokeWidth(mStrokeWidth);
            return options;
        }

        @Override
        public void onPostExecute(PolygonOptions options) {
            Polygon polygon = mMap.addPolygon(options);
            mPolygons.add(polygon);
        }
    }

    public class AsyncPolylineDrawer extends AsyncTask<Void, Void, PolylineOptions> {
        private GoogleMap mMap;
        private GeoJsonGeometry mGeometry;
        private int mColor;
        private int mStrokeWidth;

        AsyncPolylineDrawer(GoogleMap map, GeoJsonGeometry geometry, int color, int strokeWidth) {
            mMap = map;
            mGeometry = geometry;
            mColor = color;
            mStrokeWidth = strokeWidth;
        }

        @Override
        public PolylineOptions doInBackground(Void... params) {
            PolylineCoordinates coordinates = (PolylineCoordinates) mGeometry.getCoordinates();
            GeometryPolyline polyline = new GeometryPolyline(coordinates);
            PolylineOptions options = new PolylineOptions()
                    .addAll(polyline.getPolylineCoordinates()).width(mStrokeWidth).color(mColor);
            return options;
        }

        @Override
        public void onPostExecute(PolylineOptions options) {
            Polyline polyline = mMap.addPolyline(options);
            mPolylines.add(polyline);
        }
    }
}