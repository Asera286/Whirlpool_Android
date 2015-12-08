package edu.msu.elhazzat.whirpool.rest;

/**
 * Created by christianwhite on 11/7/15.
 */

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

import edu.msu.elhazzat.whirpool.model.RoomModel;

/**
 * Created by christianwhite on 11/5/15.
 */
public abstract class AsyncGCSMultiRoomInfoReader extends AsyncTask<Void, Void, List<RoomModel>> {
    public static final String LOG_TAG = AsyncGCSRoomInfoReader.class.getSimpleName();

    public abstract void handleRooms(List<RoomModel> rooms);

    public static final String GET_PARAM_BUILDING = "building_name=";

    public String mBuildingName;

    public AsyncGCSMultiRoomInfoReader(String buildingName) {
        mBuildingName = buildingName;
    }

    @Override
    public List<RoomModel> doInBackground(Void... params) {
        try {

            StringBuilder builder = new StringBuilder()
                    .append(GCSRestConstants.GCS_ROOM_BASE_URL)
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

                JSONObject roomJson = new JSONObject(responseBuilder.toString());

                if(!roomJson.getBoolean(GCSRestConstants.SUCCESS_KEY)) {
                    return null;
                }

                JSONArray rooms = roomJson.getJSONArray(GCSRestConstants.ROOMS_KEY);
                List<RoomModel> roomModels = new ArrayList<>();

                for(int i = 0; i < rooms.length(); i++) {
                    JSONObject roomObj = (JSONObject) rooms.get(i);
                    String roomName = roomObj.getString(GCSRestConstants.ROOM_NAME_KEY);
                    String occupancyStatus = roomObj.getString(GCSRestConstants.OCC_STATUS_KEY);
                    int capacity = roomObj.getInt(GCSRestConstants.CAPACITY_KEY);
                    String extension = roomObj.getString(GCSRestConstants.EXT_KEY);
                    String roomType = roomObj.getString(GCSRestConstants.ROOM_TYPE_KEY);
                    String email = roomObj.getString(GCSRestConstants.EMAIL_KEY);
                    String resourceName = roomObj.getString(GCSRestConstants.RESOURCE_NAME_KEY);

                    RoomModel model = new RoomModel(roomName, mBuildingName, extension, roomType,
                            capacity, occupancyStatus, null, email, resourceName);
                    roomModels.add(model);
                }

                return roomModels;
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
    public void onPostExecute(List<RoomModel> rooms) {
        handleRooms(rooms);
    }
}