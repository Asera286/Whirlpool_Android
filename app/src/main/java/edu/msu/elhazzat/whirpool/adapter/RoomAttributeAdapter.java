package edu.msu.elhazzat.whirpool.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.model.RoomAttributeModel;

/**
 * Created by christianwhite on 10/25/15.
 */
public class RoomAttributeAdapter extends BaseAdapter {

    private Context mContext;
    private List<RoomAttributeModel> mAttributes = new ArrayList<>();

    public RoomAttributeAdapter(Context context, List<RoomAttributeModel> attributes) {
        mContext = context;
        mAttributes = attributes;
    }

    @Override
    public int getCount() {
        return mAttributes.size();
    }

    @Override
    public RoomAttributeModel getItem(int position) {
        return mAttributes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        // current menu type
        return position % 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext,
                    R.layout.room_simple_list_item, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        RoomAttributeModel item = getItem(position);

        holder.attribute_key.setText(item.getAttributeType());
        holder.attribute_value.setText(item.getmAttributeValue());

        return convertView;
    }

    class ViewHolder {
        TextView attribute_key;
        TextView attribute_value;

        public ViewHolder(View view) {
            attribute_key = (TextView) view.findViewById(R.id.attribute_key);
            attribute_value = (TextView) view.findViewById(R.id.attribute_value);
            view.setTag(this);
        }
    }
}