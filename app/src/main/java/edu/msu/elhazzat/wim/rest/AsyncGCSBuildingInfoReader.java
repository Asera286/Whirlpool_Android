package edu.msu.elhazzat.wim.rest;

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

import edu.msu.elhazzat.wim.model.BuildingModel;

/**
 * Created by christianwhite on 11/11/15.
 */

/************************************************************************************
 * Pull all building data for a given building from backend
 ************************************************************************************/
public abstract class AsyncGCSBuildingInfoReader extends AsyncTask<Void, Void, BuildingModel> {
    public static final String LOG_TAG = AsyncGCSBuildingInfoReader.class.getSimpleName();

    public abstract void handleBuilding(BuildingModel building);

    // get param
    public static final String GET_PARAM_BUILDING = "building_name=";

    // json response key
    private static final String SUCCESS_KEY = "success";
    private static final String BUILDING_INFO_KEY = "building_info";
    private static final String NUM_WINGS_INFO = "num_wings";
    private static final String NUM_FLOORS_INFO = "num_floors";
    private static final String BUILDING_NAME_KEY = "building_name";

    public String mBuildingName;

    public AsyncGCSBuildingInfoReader(String buildingName) {
        mBuildingName = buildingName;
    }

    @Override
    public BuildingModel doInBackground(Void... params) {
        try {

            // build get request url
            StringBuilder builder = new StringBuilder()
                    .append(GCSRestConstants.GCS_BUILDING_BASE_URL)
                    .append(GET_PARAM_BUILDING)
                    .append(mBuildingName);

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
                while((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                JSONObject buildingJson = new JSONObject(responseBuilder.toString());

                if(!buildingJson.getBoolean(SUCCESS_KEY)) {
                    return null;
                }

                JSONObject buildingInfoJson = buildingJson.getJSONObject(BUILDING_INFO_KEY);
                int wings = buildingInfoJson.getInt(NUM_WINGS_INFO);
                int floors = buildingInfoJson.getInt(NUM_FLOORS_INFO);
                String name = buildingInfoJson.getString(BUILDING_NAME_KEY);

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