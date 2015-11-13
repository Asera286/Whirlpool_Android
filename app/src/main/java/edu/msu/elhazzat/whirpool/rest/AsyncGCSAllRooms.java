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
public abstract class AsyncGCSAllRooms extends AsyncTask<Void, Void, List<BuildingModel>> {
    public static final String LOG_TAG = AsyncGCSBuildingInfoReader.class.getSimpleName();

    public abstract void handleBuildings(List<BuildingModel> buildings);

    @Override
    public List<BuildingModel> doInBackground(Void... params) {
        try {

            StringBuilder builder = new StringBuilder()
                    .append(GCSRestConstants.GCS_ROOM_BASE_URL);

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

                List<BuildingModel> buildingModels = new ArrayList<>();
                JSONArray roomsJson = new JSONArray(responseBuilder.toString());
                for(int i = 0; i < roomsJson.length(); i++) {
                    JSONObject building = roomsJson.getJSONObject(i);
                    JSONArray rooms = building.getJSONArray("rooms");
                    List<RoomModel> roomModels = new ArrayList<>();
                    String buildingAbbrv = building.getString("building_name");
                    for(int k = 0; k < rooms.length(); k++) {
                        JSONObject roomObj = (JSONObject) rooms.get(k);
                        String roomName = roomObj.getString("room_name");
                        String occupancyStatus = roomObj.getString("occupancy_status");
                        int capacity = roomObj.getInt("capacity");
                        String extension = roomObj.getString("extension");
                        String roomType = roomObj.getString("room_type");
                        String email = roomObj.getString("email");

                        RoomModel model = new RoomModel(roomName, buildingAbbrv, extension, roomType,
                                capacity, occupancyStatus, null, email);
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