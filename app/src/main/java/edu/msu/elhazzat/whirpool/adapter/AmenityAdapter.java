package edu.msu.elhazzat.whirpool.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import edu.msu.elhazzat.whirpool.R;

/**
 * Created by christianwhite on 11/15/15.
 */
public class AmenityAdapter extends ArrayAdapter<String> {
    private String[] mAmenitites;

    public AmenityAdapter(Context context, int resource, String[] amenities) {
        super(context, resource, amenities);
        mAmenitites = amenities;
    }

    private static class ViewHolder {
        private TextView itemView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.room_attr_layout, parent, false);

            viewHolder.itemView = (TextView) convertView.findViewById(R.id.attribute_key);
            viewHolder.itemView.setTextColor(Color.BLACK);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // My layout has only one TextView
        // do whatever you want with your string and long
        viewHolder.itemView.setText(mAmenitites[position]);
        return convertView;
    }

    public String getAmenity(int position) {
        return getItem(position);
    }

}