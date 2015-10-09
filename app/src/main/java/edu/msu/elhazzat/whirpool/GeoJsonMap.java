package edu.msu.elhazzat.whirpool;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.geojson.GeoJsonLayer;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by christianwhite on 10/8/15.
 */
public class GeoJsonMap {
    private Context mContext;
    private HashMap<Integer, GeoJsonLayer> mLayers = new HashMap<>();

    GeoJsonMap(Context context) {
        mContext = context;
    }

    public void createLayerFromResource(GoogleMap map, int floorNumber, int resourceId) {
        GeoJsonLayer layer = null;
        try {
            layer = new GeoJsonLayer(map, R.raw.geojsonrooms, );
        }
        catch(JSONException e) {
            Log.e(LOG_TAG, "Error: ", e);

        }
        catch(IOException e) {
            Log.e(LOG_TAG, "Error: ", e);
        }
        mLayers.put(floorNumber, layer);
        layer.setMap(mMap);
    }
}
