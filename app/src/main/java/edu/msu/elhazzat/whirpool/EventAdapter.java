package edu.msu.elhazzat.whirpool;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Stephanie on 10/2/2015.
 */
public class EventAdapter extends BaseAdapter {

    private Activity mActivity;
    private ArrayList mData;
    private ListModel mTempValues;
    private LayoutInflater mInflater;
    private Resources mResources;

    public EventAdapter(Activity activity, ArrayList list, Resources resLocal) {
        mActivity = activity;
        mData = list;
        mResources = resLocal;

        mInflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if(mData.size() <= 0) {
            return 1;
        }

        return mData.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
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
            mTempValues = null;
            mTempValues = (ListModel) mData.get(position);

            holder.text.setText(mTempValues.getStartTime());
            holder.text1.setText(mTempValues.getSummary());
            holder.image.setImageResource(mResources.getIdentifier("com.example.testui:drawable/marker",null,null));

            listItemView.setOnClickListener(new OnItemClickListener(position));
        }

        return listItemView;
    }

    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position ){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            // MainActivity2 sct = (MainActivity2) activity;

            /****  Call  onItemClick Method inside MainActivity2 Class ****/

            //sct.onItemClick(mPosition);


        }
    }
}