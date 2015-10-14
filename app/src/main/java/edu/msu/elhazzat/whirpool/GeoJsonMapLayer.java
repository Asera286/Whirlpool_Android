package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
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
    private GeoJson mGeoJson = null;
    private List<Polygon> mPolygons = new ArrayList<>();
    private List<Polyline> mPolylines = new ArrayList<>();
    private List<Point> mPoints = new ArrayList<>();
    private boolean mIsVisible = false;
    private boolean mIsDrawn = false;

    public List<Polygon> getPolygons() {
        return mPolygons;
    }

    public void setGeoJson(GeoJson json) {
        mGeoJson = json;
    }

    public GeoJson getGeoJson() {
        return mGeoJson;
    }

    public void draw(final GoogleMap map, final int fillColor,
                     final int strokeColor, final int strokeWidth) {
        if(!mIsDrawn && mGeoJson != null) {
            for (GeoJsonFeature feature : mGeoJson.getGeoJsonFeatures()) {
                final GeoJsonGeometry jsonGeometry = feature.getGeoJsonGeometry();
                if (jsonGeometry == null) {
                    continue;
                }
                switch(jsonGeometry.getType()) {
                    case "Point":
                        GeoJsonPoint point = (GeoJsonPoint) jsonGeometry.getGeometry();
                        LatLng latLng = Geometry.geoJsonCoordinateToLatLng(point.getPoints(), false);
                        Polygon polygon = map.addPolygon(new PolygonOptions()
                                .add(latLng).fillColor(fillColor).strokeWidth(strokeWidth));
                        mPolygons.add(polygon);
                        break;
                    case "LineString":
                        GeoJsonPolyline lineString = (GeoJsonPolyline) jsonGeometry.getGeometry();
                        List<LatLng> latLngList = Geometry.geoJsonCoordinateListToLatLng(lineString.getPoints(), false);
                        Polyline polyline  = map.addPolyline(new PolylineOptions()
                                .addAll(latLngList).color(strokeColor).width(strokeWidth));
                        mPolylines.add(polyline);
                        break;
                    case "Polygon":
                        GeoJsonPolygon poly1 = (GeoJsonPolygon) jsonGeometry.getGeometry();
                        List<LatLng> latLngPoly = Geometry.geoJsonCoordinateListToLatLng(poly1.getPoints().get(0), true);
                        Polygon poly2  = map.addPolygon(new PolygonOptions()
                                .addAll(latLngPoly).fillColor(fillColor).strokeWidth(strokeWidth));
                        poly1.setGMSPolygon(poly2);
                        mPolygons.add(poly2);
                        break;
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
}