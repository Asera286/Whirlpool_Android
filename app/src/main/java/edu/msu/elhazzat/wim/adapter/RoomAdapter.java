package edu.msu.elhazzat.wim.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.msu.elhazzat.wim.R;
import edu.msu.elhazzat.wim.model.RoomModel;

/************************************************************
 *  Used to display listview of room model objects
 ************************************************************/

/**
 * Created by christianwhite on 9/20/15.
 */
public class RoomAdapter extends ArrayAdapter<RoomModel> {
    private List<RoomModel> mRoomModelArrayList = new ArrayList<>();

    public RoomAdapter(Context context, int resource, List<RoomModel> roomModels) {//String[] values) {
        super(context, resource, roomModels);
        mRoomModelArrayList = roomModels;
    }

    private static class ViewHolder {
        private TextView itemView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.room_simple_list_item, parent, false);

            viewHolder.itemView = (TextView) convertView.findViewById(R.id.attribute_key);
            viewHolder.itemView.setTextColor(Color.BLACK);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        RoomModel item = getItem(position);
        if (item!= null) {
            // My layout has only one TextView
            // do whatever you want with your string and long
            viewHolder.itemView.setText(item.getRoomName());
        }

        return convertView;
    }

    public RoomModel getRoomModel(int position) {
        return getItem(position);
    }

    public void sort() {
        Collections.sort(mRoomModelArrayList, new RoomComparator());
    }

    public static class RoomComparator implements Comparator<RoomModel> {
        public int compare(RoomModel lhs, RoomModel rhs) {
            String lName = lhs.getRoomName();
            String rName = rhs.getRoomName();
            return lName.compareToIgnoreCase(rName);
        }
    }
}