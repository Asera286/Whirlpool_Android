package edu.msu.elhazzat.whirpool;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by christianwhite on 10/12/15.
 */
public class AsyncRoomParseFromResource extends AsyncTask<Void, Void, List<Room>> {
    public static final int mResourceId = R.raw.bentonharborresources;
    private Context mContext;
    private AsyncRoomParseFromResourceDelegate mDelegate;

    public interface AsyncRoomParseFromResourceDelegate {
        public void handleRoomList(List<Room> rooms);
    }

    AsyncRoomParseFromResource(Context context, AsyncRoomParseFromResourceDelegate delegate) {
        mContext = context;
        mDelegate = delegate;
    }

    @Override
    public List<Room> doInBackground(Void... params) {
        List<Room> rooms = new ArrayList<>();
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
                    rooms.add(new Room(roomName, email, false));
                }
            }
        } catch (IOException e) {

        }

        return rooms;
    }

    @Override
    public void onPostExecute(List<Room> rooms) {
        mDelegate.handleRoomList(rooms);
    }
}
