package edu.msu.elhazzat.whirpool.geojson;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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
            for (GeoJsonFeature feature : mGeoJson.getGeoJsonFeatures()) {
                final GeoJsonGeometry jsonGeometry = feature.getGeoJsonGeometry();
                if (jsonGeometry == null) {
                    continue;
                }
                switch (jsonGeometry.getType()) {
                    case GeoJsonConstants.POINT:
                        break;
                    case GeoJsonConstants.LINESTRING:
                        GeoJsonPolyline lineString = (GeoJsonPolyline) jsonGeometry.getGeometry();
                        List<LatLng> latLngList = Geometry.geoJsonCoordinateListToLatLng(lineString.getPoints(), false);
                        Polyline polyline = map.addPolyline(new PolylineOptions()
                                .addAll(latLngList).color(strokeColor).width(strokeWidth));
                        mPolylines.add(polyline);
                        break;
                    case GeoJsonConstants.POLYGON:
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
                        }
                        catch(NullPointerException e) {
                            Log.d(LOG_TAG, e.getMessage());
                        }

                        Polygon poly2 = map.addPolygon(new PolygonOptions()
                                .addAll(latLngPoly).fillColor(color).strokeWidth(strokeWidth));
                        poly1.setGMSPolygon(poly2);
                        mPolygons.add(poly2);
                        break;
                }
            }
            mIsDrawn = true;
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
}