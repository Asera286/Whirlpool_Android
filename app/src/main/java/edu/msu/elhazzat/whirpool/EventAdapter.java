package edu.msu.elhazzat.whirpool;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Stephanie on 10/2/2015.
 */
public class EventAdapter extends BaseListAdapter {
    private ListEventModel mTempValues;

    public EventAdapter(Activity activity, ArrayList list, Resources resLocal) {
        super(activity, list, resLocal);
    }

    public static class ViewHolder {
        public TextView time;
        public TextView summary;
        public ImageView image;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        ViewHolder holder;

        if(convertView == null) {
            listItemView = mInflater.inflate(R.layout.tabitem, null);

            holder = new ViewHolder();
            holder.time = (TextView)listItemView.findViewById(R.id.text);
            holder.summary = (TextView)listItemView.findViewById(R.id.text1);
            holder.image = (ImageView)listItemView.findViewById(R.id.img1);

            listItemView.setTag(holder);
        }
        else {
            holder = (ViewHolder) listItemView.getTag();
        }

        if(mData.size() <= 0) {
            holder.time.setText("No Data");
        }
        else {
            mTempValues = null;
            mTempValues = (ListEventModel) mData.get(position);

            holder.time.setText(mTempValues.getStartTime());
            holder.summary.setText(mTempValues.getSummary());
            holder.image.setImageResource(mResources.getIdentifier("com.example.testui:drawable/marker",null,null));

            setListItemOnClickListener(listItemView, position);
        }

        return listItemView;
    }

    public void setListItemOnClickListener(View view, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListEventModel model = (ListEventModel) mData.get(position);
                Intent roomIntent = new Intent(mActivity, RoomActivity.class);
                roomIntent.putExtra("ROOM_ID", "116");
                mActivity.startActivity(roomIntent);
            }
        });
    }
}