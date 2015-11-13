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

import edu.msu.elhazzat.whirpool.model.BuildingModel;

/**
 * Created by christianwhite on 11/11/15.
 */
public abstract class AsyncGCSBuildingInfoReader extends AsyncTask<Void, Void, BuildingModel> {
    public static final String LOG_TAG = AsyncGCSBuildingInfoReader.class.getSimpleName();

    public abstract void handleBuilding(BuildingModel building);

    public static final String GET_PARAM_BUILDING = "building_name=";
    public String mBuildingName;

    public AsyncGCSBuildingInfoReader(String buildingName) {
        mBuildingName = buildingName;
    }

    @Override
    public BuildingModel doInBackground(Void... params) {
        try {

            StringBuilder builder = new StringBuilder()
                    .append(GCSRestConstants.GCS_BUILDING_BASE_URL)
                    .append(GET_PARAM_BUILDING)
                    .append(mBuildingName);

            String tmpUrl = builder.toString();
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

                JSONObject buildingJson = new JSONObject(responseBuilder.toString());

                if(!buildingJson.getBoolean("success")) {
                    return null;
                }

                JSONObject buildingInfoJson = buildingJson.getJSONObject("building_info");
                int wings = buildingInfoJson.getInt("num_wings");
                int floors = buildingInfoJson.getInt("num_floors");
                String name = buildingInfoJson.getString("building_name");

                return  new BuildingModel(name, floors, wings);
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

    @Override
    public void onPostExecute(BuildingModel building) {
        handleBuilding(building);
    }
}