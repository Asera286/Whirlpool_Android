package edu.msu.elhazzat.whirpool;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by christianwhite on 10/8/15.
 */
abstract class BaseListAdapter extends BaseAdapter {
    protected Activity mActivity;
    protected ArrayList mData;
    protected static LayoutInflater mInflater;
    protected Resources mResources;

    public BaseListAdapter(Activity activity, ArrayList data, Resources resLocal) {
        mActivity = activity;
        mData = data;
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

    abstract public View getView(int position, View convertView, ViewGroup parent);
    abstract public void setListItemOnClickListener(View view, int position);
}