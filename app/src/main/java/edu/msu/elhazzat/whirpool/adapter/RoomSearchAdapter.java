package edu.msu.elhazzat.whirpool.adapter;

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

import edu.msu.elhazzat.whirpool.model.RoomModel;

public class RoomSearchAdapter extends ArrayAdapter<RoomModel> {
    private List<RoomModel> mRoomModelArrayList = new ArrayList<>();

    public RoomSearchAdapter(Context context, int resource, List<RoomModel> roomModels) {//String[] values) {
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
                    .inflate(android.R.layout.simple_list_item_1, parent, false);

            viewHolder.itemView = (TextView) convertView.findViewById(android.R.id.text1);
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
      /*  if(position < mRoomModelArrayList.size()) {
            return mRoomModelArrayList.get(position);
        }*/
        return getItem(position);
      //  return null;
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