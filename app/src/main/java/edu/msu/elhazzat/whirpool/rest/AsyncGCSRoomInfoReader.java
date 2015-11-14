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

import javax.net.ssl.HttpsURLConnection;

import edu.msu.elhazzat.whirpool.model.RoomModel;

/**
 * Created by christianwhite on 11/5/15.
 */
public abstract class AsyncGCSRoomInfoReader extends AsyncTask<Void, Void, RoomModel> {
    public static final String LOG_TAG = AsyncGCSRoomInfoReader.class.getSimpleName();

    public abstract void handleRoom(RoomModel room);

    public static final String GET_PARAM_BUILDING = "building_name=";
    public static final String GET_PARAM_ROOM = "room_name=";
    public String mBuildingName;
    public String mRoomName;

    public AsyncGCSRoomInfoReader(String buildingName, String roomName) {

        mBuildingName = buildingName;
        mRoomName = roomName;
    }

    @Override
    public RoomModel doInBackground(Void... params) {
        try {

            StringBuilder builder = new StringBuilder()
                    .append(GCSRestConstants.GCS_ROOM_BASE_URL)
                    .append(GET_PARAM_BUILDING)
                    .append(mBuildingName)
                    .append('&')
                    .append(GET_PARAM_ROOM)
                    .append(mRoomName);

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

                JSONObject roomJson = new JSONObject(responseBuilder.toString());

                if(!roomJson.getBoolean("success")) {
                    return null;
                }

                JSONArray amenitiesJson = roomJson.getJSONArray("amenities");
                String[] amenities = new String[amenitiesJson.length()];
                for(int i = 0; i < amenities.length; i++) {
                    amenities[i] = amenitiesJson.getString(i);
                }
                JSONArray rooms = roomJson.getJSONArray("rooms");

                if(rooms.length() > 0) {
                    JSONObject roomObj = (JSONObject) rooms.get(0);
                    String occupancyStatus = roomObj.getString("occupancy_status");
                    int capacity = roomObj.getInt("capacity");
                    String extension = roomObj.getString("extension");
                    String roomType = roomObj.getString("room_type");
                    String email = roomObj.getString("email");
                    String resourceName = roomObj.getString("resource_name");

                    RoomModel model = new RoomModel(mRoomName, mBuildingName, extension, roomType,
                            capacity, occupancyStatus, amenities, email, resourceName);
                    return model;
                }
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
    public void onPostExecute(RoomModel room) {
        handleRoom(room);
    }
}