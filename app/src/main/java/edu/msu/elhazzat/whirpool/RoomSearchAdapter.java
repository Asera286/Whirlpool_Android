package edu.msu.elhazzat.whirpool;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RoomSearchAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mValues;
    private ArrayList<Room> mRoomArrayList = new ArrayList<>();
    private Room[] mRooms;

    public RoomSearchAdapter(Context context, int resource, String[] values) {
        super(context, resource, values);
        mContext = context;
        mValues = values;
        for (String s : mValues) {
            mRoomArrayList.add(new Room(s, "", false));
        }
    }

    public void sort() {
        Collections.sort(mRoomArrayList, new RoomComparator());
    }

    private class RoomComparator implements Comparator<Room> {
        public int compare(Room lhs, Room rhs) {
            String lName = lhs.getName();
            String rName = rhs.getName();
            return lName.compareToIgnoreCase(rName);
        }
    }
}