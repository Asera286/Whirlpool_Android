package edu.msu.elhazzat.whirpool.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

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
 * Created by christianwhite on 10/18/15.
 */
public abstract class AsyncResourceReader extends AsyncTask<Void, Void, List<RoomModel>> {
    public static final String LOG_TAG = AsyncResourceReader.class.getSimpleName();
    public static final String mUrl = "https://webdev.cse.msu.edu/~elhazzat/wim/room-load.php";

    public abstract void handleRooms(List<RoomModel> rooms);

    @Override
    public List<RoomModel> doInBackground(Void... params) {
        try {

            URL url = new URL(mUrl);
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

                List<RoomModel> roomModels = new ArrayList<>();
                JSONArray jArray = new JSONArray(responseBuilder.toString());
                for (int i = 0; i < jArray.length(); i++) {
                    JSONArray row = jArray.getJSONArray(i);
                    RoomModel room = new RoomModel();
                    room.setBuildingName(row.getString(0));
                    room.setRoomName(row.getString(1));

                    room.addAttribute(new RoomAttributeModel("Capacity", row.getString(2)));
                    room.addAttribute(new RoomAttributeModel(row.getString(4), "true"));
                    room.addAttribute(new RoomAttributeModel(row.getString(5), "true"));

                    room.setEmail(row.getString(row.length() - 1));
                    roomModels.add(room);
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
