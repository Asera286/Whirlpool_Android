package edu.msu.elhazzat.whirpool.rest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import edu.msu.elhazzat.whirpool.model.BuildingModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;

/**
 * Created by christianwhite on 11/12/15.
 */

/************************************************************************************
 * Pull all room data for a given building from backend
 ************************************************************************************/
public abstract class AsyncGCSAllRooms extends AsyncTask<Void, Void, List<BuildingModel>> {
    public static final String LOG_TAG = AsyncGCSBuildingInfoReader.class.getSimpleName();

    public abstract void handleBuildings(List<BuildingModel> buildings);

    @Override
    public List<BuildingModel> doInBackground(Void... params) {
        try {

            // build https connection
            String tmpUrl = GCSRestConstants.GCS_ROOM_BASE_URL;
            URL url = new URL(tmpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));
                String line = null;
                StringBuilder responseBuilder = new StringBuilder();

                // read returned data
                while((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                // prepare room model response
                List<BuildingModel> buildingModels = new ArrayList<>();
                JSONArray roomsJson = new JSONArray(responseBuilder.toString());

                for(int i = 0; i < roomsJson.length(); i++) {
                    JSONObject building = roomsJson.getJSONObject(i);
                    JSONArray rooms = building.getJSONArray(GCSRestConstants.ROOMS_KEY);

                    List<RoomModel> roomModels = new ArrayList<>();
                    String buildingAbbrv = building.getString(GCSRestConstants.BUILDING_NAME_KEY);

                    for(int k = 0; k < rooms.length(); k++) {
                        JSONObject roomObj = (JSONObject) rooms.get(k);

                        String roomName = roomObj.getString(GCSRestConstants.ROOM_NAME_KEY);
                        String occupancyStatus = roomObj.getString(GCSRestConstants.OCC_STATUS_KEY);
                        int capacity = roomObj.getInt(GCSRestConstants.CAPACITY_KEY);
                        String extension = roomObj.getString(GCSRestConstants.EXT_KEY);
                        String roomType = roomObj.getString(GCSRestConstants.ROOM_TYPE_KEY);
                        String email = roomObj.getString(GCSRestConstants.EMAIL_KEY);
                        String resourceName = roomObj.getString(GCSRestConstants.RESOURCE_NAME_KEY);

                        RoomModel model = new RoomModel(roomName, buildingAbbrv, extension, roomType,
                                capacity, occupancyStatus, null, email, resourceName);
                        roomModels.add(model);
                    }

                    buildingModels.add(new BuildingModel(buildingAbbrv, roomModels));
                }

                return buildingModels;
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
    public void onPostExecute(List<BuildingModel> buildings) {
        handleBuildings(buildings);
    }
}