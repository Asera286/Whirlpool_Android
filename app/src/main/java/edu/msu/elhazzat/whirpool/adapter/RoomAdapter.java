package edu.msu.elhazzat.whirpool.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.activity.RoomActivity;
import edu.msu.elhazzat.whirpool.model.RoomModel;


public class RoomAdapter extends BaseListAdapter {
    private RoomModel mTempValue;

    public RoomAdapter(Activity activity, ArrayList data, Resources resLocal) {
        super(activity, data, resLocal);
    }

    public static class ViewHolder {
        public TextView text;
        public TextView text1;
        public ImageView image;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        ViewHolder holder;

        if(convertView == null) {
            listItemView = mInflater.inflate(R.layout.tabitem, null);

            holder = new ViewHolder();
            holder.text = (TextView)listItemView.findViewById(R.id.text);
            holder.text1 = (TextView)listItemView.findViewById(R.id.text1);
            holder.image = (ImageView)listItemView.findViewById(R.id.img1);

            listItemView.setTag(holder);
        }
        else {
            holder = (ViewHolder) listItemView.getTag();
        }

        if(mData.size() <= 0) {
            holder.text.setText("No Data");
        }
        else {
            mTempValue = null;
            mTempValue = (RoomModel) mData.get(position);

            holder.text.setText(Integer.toString(position));
            holder.text1.setText(mTempValue.getName());
         //   holder.image.setImageResource(mResources.getIdentifier("com.example.testui:drawable/marker",null,null));
            setListItemOnClickListener(listItemView, position);
        }

        return listItemView;
    }

    public void setListItemOnClickListener(View view, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent roomIntent = new Intent(mActivity, RoomActivity.class);
                RoomModel roomModel = (RoomModel) mData.get(position);
                roomIntent.putExtra("ROOM_ID", roomModel.getName());
                //roomIntent.putExtra("ROOM_ID", "109");
                //roomIntent.putExtra("RESOURCE_EMAIL", roomModel.getEmail());
                mActivity.startActivity(roomIntent);
            }
        });
    }
}