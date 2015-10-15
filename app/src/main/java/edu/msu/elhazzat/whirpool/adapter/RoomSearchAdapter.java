package edu.msu.elhazzat.whirpool.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.msu.elhazzat.whirpool.model.RoomModel;

public class RoomSearchAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mValues;
    private ArrayList<RoomModel> mRoomModelArrayList = new ArrayList<>();
    private RoomModel[] mRoomModels;

    public RoomSearchAdapter(Context context, int resource, String[] values) {
        super(context, resource, values);
        mContext = context;
        mValues = values;
        for (String s : mValues) {
            mRoomModelArrayList.add(new RoomModel(s, "", false));
        }
    }

    public void sort() {
        Collections.sort(mRoomModelArrayList, new RoomComparator());
    }

    private class RoomComparator implements Comparator<RoomModel> {
        public int compare(RoomModel lhs, RoomModel rhs) {
            String lName = lhs.getName();
            String rName = rhs.getName();
            return lName.compareToIgnoreCase(rName);
        }
    }
}