package edu.msu.elhazzat.whirpool.geojson;

import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;

/**
 * Created by christianwhite on 10/8/15.
 */

/**
 * A "geo json map" will represent multiple geojson layers
 */
public class GeoJsonMap {

    private GoogleMap mMap;
    private HashMap<Integer, GeoJsonMapLayer> mLayers = new HashMap<>();
    private int mCurrentLayer;

    public GeoJsonMap(GoogleMap map) {
        mMap = map;
    }
    public GeoJsonMap() {

    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }

    // store layer in a hash map
    public void addLayer(int floor, GeoJsonMapLayer layer) {
        mLayers.put(floor, layer);
    }

    public void drawLayer(int floor, int fillColor, int strokeColor, int strokeWidth) {
        mLayers.get(floor).draw(mMap, fillColor, strokeColor, strokeWidth);
        mCurrentLayer = floor;
    }

    public GeoJsonMapLayer getCurrentLayer() {
        return mLayers.get(mCurrentLayer);
    }
    public void setCurrentLayer(int layer) { mCurrentLayer = layer; }

    public void removeLayer(int floor) {
        mLayers.get(floor).remove();
    }

    public void hideLayer(int floor, boolean toHide) {
        mLayers.get(floor).hide(toHide);
    }

}
