package edu.msu.elhazzat.whirpool.rest;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import edu.msu.elhazzat.whirpool.geojson.GeoJson;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonGeometry;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonGeometryDeserializer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;
import edu.msu.elhazzat.whirpool.utils.AsyncResourceReader;

/**
 * Created by christianwhite on 11/5/15.
 */
public abstract class AsyncParseGeoJsonGCS extends AsyncTask<Void, Void, GeoJsonMap> {
    public static final String LOG_TAG = AsyncResourceReader.class.getSimpleName();

    public abstract void handleGeoJson(GeoJsonMap map);

    public static final String GET_PARAM = "building_name=";
    public String mBuildingName;

    public AsyncParseGeoJsonGCS(String buildingName) {
        mBuildingName = buildingName;
    }

    @Override
    public GeoJsonMap doInBackground(Void... params) {
        try {
            String tmpUrl = GCSRestConstants.GCS_BLOGSTORE_BASE_URL + GET_PARAM + mBuildingName;
            URL url = new URL(tmpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));
                String line = null;
                StringBuilder responseBuilder = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                GeoJsonMap map = new GeoJsonMap();

                String responseString = responseBuilder.toString();
                JSONObject jsonObj = new JSONObject(responseString);
                int count = jsonObj.getInt("count");
                JSONArray floors = jsonObj.getJSONArray("floors");
                for(int i = 0; i < count; i++) {
                    JSONObject floor = floors.getJSONObject(i);
                    int floorNum = floor.getInt("floor_num");
                  //  String win = floor.getString("wing");
                    String geoJsonStr = floor.getString("geojson");
                    GeoJson floorGeoJson = readGeoJson(geoJsonStr);
                    GeoJsonMapLayer layer = new GeoJsonMapLayer(floorGeoJson);
                    map.addLayer(floorNum, layer);
                }

                return map;
            }
        }
        catch(ProtocolException e) {
            Log.e(LOG_TAG, "Error :", e);
        }
        catch(JSONException e) {
            Log.e(LOG_TAG, "Error :", e);
        }
        catch(IOException e) {
            Log.e(LOG_TAG, "Error :", e);
        }

        return null;
    }

    public GeoJson readGeoJson(String geoJson) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(GeoJsonGeometry.class,
                new GeoJsonGeometryDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(geoJson, GeoJson.class);
    }


    @Override
    public void onPostExecute(GeoJsonMap map) {
        handleGeoJson(map);
    }
}