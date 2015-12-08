package edu.msu.elhazzat.whirpool.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import edu.msu.elhazzat.whirpool.R;

/**********************************************************************
 * Used by room activity to display room amenities and capacity
 **********************************************************************/

/**
 * Created by christianwhite on 11/15/15.
 */
public class AmenityAdapter extends ArrayAdapter<String> {
    private String[] mAmenitites;
    private int mCapacity;

    // There two type of items - the capacity and a list of amenities
    private static final int TYPE_ITEM1 = 0;
    private static final int TYPE_ITEM2 = 1;

    public AmenityAdapter(Context context, int resource, int capacity, String[] amenities) {
        super(context, resource, amenities);
        mAmenitites = amenities;
        mCapacity = capacity;
    }

    private static class ViewHolder{
        private TextView itemView;
        private ImageView imageView;
    }

    /**
     * Determine the type of view holder to use based on index
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_ITEM1;
        }
        else  {
            return TYPE_ITEM2;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder();

        LayoutInflater inflator = null;
        int layoutType = getItemViewType(position);

        if (convertView == null) {
            switch(layoutType) {
                case TYPE_ITEM1:
                    convertView = LayoutInflater.from(this.getContext())
                            .inflate(R.layout.room_attr_layout2, parent, false);
                    viewHolder.itemView = (TextView) convertView.findViewById(R.id.capacity);
                    viewHolder.itemView.setTextColor(Color.BLACK);
                    break;
                case TYPE_ITEM2:
                    convertView = LayoutInflater.from(this.getContext())
                            .inflate(R.layout.room_attr_layout, parent, false);

                    viewHolder.itemView = (TextView) convertView.findViewById(R.id.attribute_key);
                    viewHolder.itemView.setTextColor(Color.BLACK);
                    break;
            }
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        switch(layoutType) {
            case TYPE_ITEM1:
                viewHolder.itemView.setText(Integer.toString(mCapacity));
                break;
            case TYPE_ITEM2:
                viewHolder.itemView.setText(mAmenitites[position]);
        }
        return convertView;
    }

    public String getAmenity(int position) {
        return getItem(position);
    }

}