package edu.msu.elhazzat.whirpool.geojson;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonMapLayer {

    private static final String LOG_TAG = GeoJsonMapLayer.class.getSimpleName();

    private GeoJson mGeoJson = null;

    private List<Polygon> mPolygons = new ArrayList<>(); // gms polygon
    private List<Polyline> mPolylines = new ArrayList<>(); // gms polyline

    private List<Marker> mRoomLabels;

    private int mFloorNum;

    private boolean mIsDrawn = false;
    private boolean mIsHidden = false;

    public GeoJsonMapLayer(GeoJson json) {
        mGeoJson = json;
    }

    public GeoJson getGeoJson() {
        return mGeoJson;
    }

    public void setGeoJson(GeoJson json) {
        mGeoJson = json;
    }

    public List<Polygon> getPolygons() {
        return mPolygons;
    }

    public void setPolygons(List<Polygon> polygons) {
        mPolygons = polygons;
    }

    public boolean isHidden() {
        return mIsHidden;
    }

    public boolean isDrawn() {
        return mIsDrawn;
    }

    public void showRoomLabels() {
        for(Marker marker : mRoomLabels) {
            marker.setVisible(true);
        }
    }

    public void hideRoomLabels() {
        for(Marker marker : mRoomLabels) {
            marker.setVisible(false);
        }
    }


    /**
     * Draws the map using uniform properties.
     * @param map
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     */
    public void draw(final GoogleMap map, final int fillColor,
                     final int strokeColor, final int strokeWidth) {

        if(!mIsDrawn && mGeoJson != null) {
            drawOnUiThread(map, fillColor, strokeColor, strokeWidth);
        }
        else {
            show(true);
        }
    }


    /**
     * Draws the map using uniform properties.
     * @param map
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     */
    public void asyncDraw(final GoogleMap map, final int fillColor,
                     final int strokeColor, final int strokeWidth) {

        if(!mIsDrawn && mGeoJson != null) {
            new AsyncDrawPolygon(map, fillColor, strokeColor, strokeWidth).execute();
        }
        else {
            show(true);
        }
    }

    /**
     * Removes layer. Will have to be redrawn to be visible again.
     */
    public void remove() {
        for(Polygon polygon : mPolygons) {
            polygon.remove();
        }
        for(Polyline polyline : mPolylines) {
            polyline.remove();
        }
    }

    /**
     * Hide the map. Does not have to be redrawn.
     * @param toShow
     */
    public void show(boolean toShow) {
        for(Polygon polygon : mPolygons) {
            polygon.setVisible(toShow);
        }
        for(Polyline polyline : mPolylines) {
            polyline.setVisible(toShow);
        }

        mIsHidden = !toShow;
    }


    public void setFloorNum(int floorNum) {
        mFloorNum = floorNum;
    }

    public int getFloorNum() {
        return mFloorNum;
    }

    public void drawOnUiThread(final GoogleMap map, final int fillColor,
                               final int strokeColor, final int strokeWidth) {
        for (GeoJsonFeature feature : mGeoJson.getGeoJsonFeatures()) {
            final GeoJsonGeometry jsonGeometry = feature.getGeoJsonGeometry();
            if (jsonGeometry == null) {
                continue;
            }
            if (jsonGeometry.getType().equals(GeoJsonConstants.POLYGON)) {
                GeoJsonPolygon poly1 = (GeoJsonPolygon) jsonGeometry.getGeometry();
                List<LatLng> latLngPoly = Geometry.geoJsonCoordinateListToLatLng(poly1.getPoints().get(0), true);
                int color = fillColor;
                try {
                    switch (feature.getProperty("room")) {
                        case "HW":
                            color = Color.WHITE;
                            break;
                        case "WB":
                            color = Color.rgb(234, 230, 245);
                            break;
                        case "MB":
                            color = Color.rgb(234, 230, 245);
                            break;
                        case "STR":
                            color = Color.parseColor("#F2A440");
                            break;
                    }
                } catch (NullPointerException e) {

                }

                Polygon poly2 = map.addPolygon(new PolygonOptions()
                        .addAll(latLngPoly)
                        .fillColor(color)
                        .strokeColor(strokeColor)
                        .strokeWidth(strokeWidth));
                mPolygons.add(poly2);
                poly1.setGMSPolygon(poly2);
            }
        }
    }

    public class PgonPoints {
        public List<LatLng> mPoints;
    }

    public class AsyncDrawPolygon extends AsyncTask<Void, PgonPoints, Void> {


        private GoogleMap mMap;
        private int mStrokeColor;
        private int mStrokeWidth;
        private int mFillColor;
        private GeoJsonPolygon mPolygon;
        private int mColor;

        public AsyncDrawPolygon(GoogleMap map, int fillColor,int strokeColor, int strokeWidth) {
            mMap = map;
            mStrokeColor = strokeColor;
            mStrokeWidth = strokeWidth;
            mFillColor = fillColor;
        }

        @Override
        public Void doInBackground(Void... param) {
            for (GeoJsonFeature feature : mGeoJson.getGeoJsonFeatures()) {
                final GeoJsonGeometry jsonGeometry = feature.getGeoJsonGeometry();
                if (jsonGeometry == null) {
                    continue;
                }
                if (jsonGeometry.getType().equals(GeoJsonConstants.POLYGON)) {
                    mPolygon = (GeoJsonPolygon) jsonGeometry.getGeometry();
                    List<LatLng> latLngPoly = Geometry.geoJsonCoordinateListToLatLng(mPolygon.getPoints().get(0), true);
                    mColor = mFillColor;
                    try {
                        switch (feature.getProperty("room")) {
                            case "HW":
                                mColor = Color.WHITE;
                                break;
                            case "WB":
                                mColor = Color.rgb(234, 230, 245);
                                break;
                            case "MB":
                                mColor = Color.rgb(234, 230, 245);
                                break;
                            case "STR":
                                mColor = Color.parseColor("#F2A440");
                                break;
                        }
                    } catch (NullPointerException e) {

                    }

                    PgonPoints points = new PgonPoints();
                    points.mPoints = latLngPoly;
                    publishProgress(points);
                }
            }
            return null;
        }

        protected void onProgressUpdate(PgonPoints... points) {
            Polygon poly2 = mMap.addPolygon(new PolygonOptions()
                    .addAll(points[0].mPoints)
                    .fillColor(mColor)
                    .strokeColor(mStrokeColor)
                    .strokeWidth(mStrokeWidth));
            mPolygons.add(poly2);
            mPolygon.setGMSPolygon(poly2);
        }
    }
}