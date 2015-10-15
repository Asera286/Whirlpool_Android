package edu.msu.elhazzat.whirpool.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.whirpool.model.RoomModel;

/**
 * Created by christianwhite on 10/12/15.
 */
public abstract class AsyncRoomParseFromResource extends AsyncTask<Void, Void, List<RoomModel>> {
    public int mResourceId;
    private Context mContext;

    public abstract void handleRoomList(List<RoomModel> roomModels);

    AsyncRoomParseFromResource(Context context, int resourceId) {
        mContext = context;
        mResourceId = resourceId;
    }

    @Override
    public List<RoomModel> doInBackground(Void... params) {
        List<RoomModel> roomModels = new ArrayList<>();
        try {
            InputStream stream = mContext.getResources().openRawResource(mResourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder out = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split(",");
                if(splitLine.length >= 2) {
                    String email = splitLine[0];
                    String roomName = splitLine[1];
                    if(email.endsWith(" ")) {
                       email = email.substring(0, email.length() - 1);
                    }
                    if(roomName.endsWith("\\")) {
                        roomName = roomName.substring(0, roomName.length() - 1);
                    }
                    roomModels.add(new RoomModel(roomName, email, false));
                }
            }
        } catch (IOException e) {

        }

        return roomModels;
    }

    @Override
    public void onPostExecute(List<RoomModel> roomModels) {
        handleRoomList(roomModels);
    }
}
