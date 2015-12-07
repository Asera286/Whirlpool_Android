package edu.msu.elhazzat.whirpool.geojson;

import com.google.android.gms.maps.GoogleMap;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by christianwhite on 10/8/15.
 */

/**
 * A "geo json map" will represent multiple geojson layers
 */
public class GeoJsonMap {

    private GoogleMap mMap;
    public HashMap<Integer, GeoJsonMapLayer> mLayers = new HashMap<>();
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
        if(mLayers.containsKey(floor)) {
            mLayers.get(floor).draw(mMap, fillColor, strokeColor, strokeWidth);
            mCurrentLayer = floor;
        }
    }

    public void drawLayerAsync(int floor, int fillColor, int strokeColor, int strokeWidth) {
        if(mLayers.containsKey(floor)) {
            mLayers.get(floor).asyncDraw(mMap, fillColor, strokeColor, strokeWidth);
            mCurrentLayer = floor;
        }
    }

    public GeoJsonMapLayer getCurrentLayer() {
        return mLayers.get(mCurrentLayer);
    }
    public void setCurrentLayer(int layer) { mCurrentLayer = layer; }

    public void removeLayer(int floor) {
        if(mLayers.containsKey(floor)) {
            mLayers.get(floor).remove();
        }
    }

    public void showLayer(int floor, boolean toShow) {
        if(mLayers.containsKey(floor)) {
            if (toShow) {
                mCurrentLayer = floor;
            }
            mLayers.get(floor).show(toShow);
        }
    }

    public Collection<GeoJsonMapLayer> getLayers() {
        return mLayers.values();
    }

    public GeoJsonMapLayer getLayer(int floor) {
        if(mLayers.containsKey(floor)) {
            return mLayers.get(floor);
        }
        return null;
    }
}
