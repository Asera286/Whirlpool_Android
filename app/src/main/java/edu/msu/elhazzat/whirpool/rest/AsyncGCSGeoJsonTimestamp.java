package edu.msu.elhazzat.whirpool.rest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by christianwhite on 12/8/15.
 */

/************************************************************************************
 * Pull timestamp - most recent edit on geojson file for given building
 ************************************************************************************/
abstract public class AsyncGCSGeoJsonTimestamp extends AsyncTask<Void, Void, Integer> {
    public static final String LOG_TAG = AsyncGCSBuildingInfoReader.class.getSimpleName();

    public abstract void handleTimestamp(Integer time);

    // get param
    public static final String GET_PARAM_BUILDING = "building_name=";
    public static final String GET_PARAM_TIME_STAMP = "time=";

    // json response key
    private static final String SUCCESS_KEY = "success";
    private static final String TIME_STAMP_KEY = "time";

    public String mBuildingName;

    public AsyncGCSGeoJsonTimestamp(String buildingName) {
        mBuildingName = buildingName;
    }

    @Override
    public Integer doInBackground(Void... params) {
        try {
            // build get request url
            StringBuilder builder = new StringBuilder()
                    .append(GCSRestConstants.GCS_BLOBSTORE_BASE_URL)
                    .append(GET_PARAM_BUILDING)
                    .append(mBuildingName)
                    .append("&")
                    .append(GET_PARAM_TIME_STAMP)
                    .append("true");

            // send/acquire response
            String tmpUrl = builder.toString();
            URL url = new URL(tmpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            // read response into building models
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));
                String line = null;
                StringBuilder responseBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                JSONObject timeJson = new JSONObject(responseBuilder.toString());

                return timeJson.getInt(TIME_STAMP_KEY);
            }
        } catch (ProtocolException e) {
            Log.e(LOG_TAG, "Error :", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error :", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error :", e);
        }

        return null;
    }

    @Override
    public void onPostExecute(Integer time) {
        handleTimestamp(time);
    }
}