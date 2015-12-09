package edu.msu.elhazzat.whirpool.geojson;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by christianwhite on 10/11/15.
 */

/**
 * Given a raw resource id, parse geojson into geojson object
 */
public abstract class AsyncParseGeoJsonFromFile extends AsyncTask<Void, Void, GeoJsonMap> {
    private static final String LOG_TAG = AsyncParseGeoJsonFromFile.class.getSimpleName();

    public abstract void handleGeoJson(GeoJsonMap json, ProgressDialog dialog);

    private static final String COUNT_KEY = "count";
    private static final String FLOORS_KEY = "floors";
    private static final String FLOOR_NUM_KEY = "floor_num";
    private static final String GEOJSON_KEY = "geojson";

    private Context mContext;
    private String mFileName;
    private ProgressDialog mDialog;

    public AsyncParseGeoJsonFromFile(Context context, String fileName) {
        mContext = context;
        mFileName= fileName;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // create a ProgressDialog instance, with a specified theme:
        mDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_DARK);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setTitle("Please wait");
        mDialog.setMessage("Downloading building map...");
        mDialog.show();
    }


    @Override
    public GeoJsonMap doInBackground(Void... params) {
        try {
            FileInputStream fis = new FileInputStream(new File(mFileName));

            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(fis));
            String line = null;
            StringBuilder responseBuilder = new StringBuilder();

            while((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            fis.close();

            GeoJsonMap map = new GeoJsonMap();

            String responseString = responseBuilder.toString();
            JSONObject jsonObj = new JSONObject(responseString);

            int count = jsonObj.getInt(COUNT_KEY);
            if(count == 0) {
                return null;
            }

            // parse geojson for each floor
            JSONArray floors = jsonObj.getJSONArray(FLOORS_KEY);
            for(int i = 0; i < count; i++) {
                JSONObject floor = floors.getJSONObject(i);
                int floorNum = floor.getInt(FLOOR_NUM_KEY);

                String geoJsonStr = floor.getString(GEOJSON_KEY);

                // build geojson object
                GeoJson floorGeoJson = readGeoJson(geoJsonStr);
                GeoJsonMapLayer layer = new GeoJsonMapLayer(floorGeoJson);

                layer.setFloorNum(floorNum);
                map.addLayer(floorNum, layer);
            }

            return map;
        }
        catch(JSONException e) {
                Log.e(LOG_TAG, "Error :", e);
        }
        catch(IOException e) {
            Log.e(LOG_TAG, "Error :", e);
        }

        return null;
    }

    /**
     * parse / serialize geosjon response
     * @param geoJson
     * @return
     */
    public GeoJson readGeoJson(String geoJson) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(GeoJsonGeometry.class,
                new GeoJsonGeometryDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(geoJson, GeoJson.class);
    }

    @Override
    public void onPostExecute(GeoJsonMap json) {
        handleGeoJson(json, mDialog);
    }
}