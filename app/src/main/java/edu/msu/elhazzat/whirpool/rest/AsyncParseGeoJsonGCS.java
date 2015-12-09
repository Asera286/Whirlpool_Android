package edu.msu.elhazzat.whirpool.rest;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import edu.msu.elhazzat.whirpool.geojson.GeoJson;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonGeometry;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonGeometryDeserializer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;

/**
 * Created by christianwhite on 11/5/15.
 */
public abstract class AsyncParseGeoJsonGCS extends AsyncTask<Void, Void, GeoJsonMap> {
    public static final String LOG_TAG = AsyncParseGeoJsonGCS.class.getSimpleName();

    public abstract void handleGeoJson(GeoJsonMap map, ProgressDialog dialog);

    private static final String COUNT_KEY = "count";
    private static final String FLOORS_KEY = "floors";
    private static final String FLOOR_NUM_KEY = "floor_num";
    private static final String GEOJSON_KEY = "geojson";

    private ProgressDialog mDialog = null;

    public static final String GET_PARAM = "building_name=";
    public String mBuildingName;
    private Context mContext;

    public AsyncParseGeoJsonGCS(Context context, String buildingName) {
        mBuildingName = buildingName;
        mContext = context;
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
            // build get request
            String tmpUrl = GCSRestConstants.GCS_BLOBSTORE_BASE_URL + GET_PARAM + mBuildingName;
            URL url = new URL(tmpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            // get json response
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

                FileOutputStream fOut = mContext.openFileOutput(
                        mBuildingName + ".json", Context.MODE_PRIVATE);

                OutputStreamWriter osw = new OutputStreamWriter(fOut);

                // Write the string to the file
                osw.write(responseString);
                osw.close();

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
    public void onPostExecute(GeoJsonMap map) {
        handleGeoJson(map, mDialog);
    }
}