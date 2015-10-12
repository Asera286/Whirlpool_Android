package edu.msu.elhazzat.whirpool;

import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonMap {

    private GoogleMap mMap;
    private HashMap<Integer, GeoJsonMapLayer> mLayers = new HashMap<>();
    private int mCurrentLayer;

    GeoJsonMap(GoogleMap map) {
        mMap = map;
    }

    public void addLayer(int floor, GeoJsonMapLayer layer) {
        mLayers.put(floor, layer);
    }

    public void drawLayer(int floor, int strokeWidth, int color) {
       // if(mCurrentLayer != floor) {
            mLayers.get(floor).draw(mMap, strokeWidth, color);
            mCurrentLayer = floor;
      //  }
    }

    public GeoJsonMapLayer getCurrentLayer() {
        return mLayers.get(mCurrentLayer);
    }

    public void removeLayer(int floor) {
        mLayers.get(floor).remove();
    }

    public void hideLayer(int floor, boolean toHide) {
        mLayers.get(floor).hide(toHide);
    }

}
