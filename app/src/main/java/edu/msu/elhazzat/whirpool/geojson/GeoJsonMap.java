package edu.msu.elhazzat.whirpool.geojson;

import com.google.android.gms.maps.GoogleMap;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by christianwhite on 10/8/15.
 */

/****************************************************************************
 * A "geo json map" will represent multiple geojson layers
 ****************************************************************************/
public class GeoJsonMap {

    private GoogleMap mMap; // map context to draw layers on
    public HashMap<Integer, GeoJsonMapLayer> mLayers = new HashMap<>(); // floor -> map
    private int mCurrentLayer; // currently drawn map layer

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

    /**
     * Call draw on select map layer if the index exists
     * @param floor
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     */
    public void drawLayer(int floor, int fillColor, int strokeColor, int strokeWidth) {
        if(mLayers.containsKey(floor)) {
            mLayers.get(floor).draw(mMap, fillColor, strokeColor, strokeWidth);

            // change current layer
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

    /**
     * Layer is already drawn - toggle visibility if the floor exists
     * @param floor
     * @param toShow
     */
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
