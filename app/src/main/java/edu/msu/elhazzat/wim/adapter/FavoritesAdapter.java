package edu.msu.elhazzat.wim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.wim.R;
import edu.msu.elhazzat.wim.model.RoomModel;



/**
 * Created by christianwhite on 11/12/15.
 */
public class FavoritesAdapter extends BaseExpandableListAdapter {

    private static final class ViewHolder {
        TextView roomLabel;
        TextView favoritesLabel;
    }

    private List<RoomModel> mRoomModels = new ArrayList<>();
    private LayoutInflater inflater;

    public FavoritesAdapter(Context context, List<RoomModel> rooms) {
        inflater = LayoutInflater.from(context);
        mRoomModels = rooms;
    }

    @Override
    public RoomModel getChild(int groupPosition, int childPosition) {

        return mRoomModels.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition)  {
        return mRoomModels.size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {
        View resultView = convertView;
        ViewHolder holder;


        if (resultView == null) {

            resultView = inflater.inflate(R.layout.exp_list_favorites_item, null);
            holder = new ViewHolder();
            holder.roomLabel = (TextView) resultView.findViewById(R.id.room_name);
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }

        RoomModel item = getChild(groupPosition, childPosition);

        holder.roomLabel.setText(item.getRoomName());

        return resultView;
    }

    @Override
    public List<RoomModel> getGroup(int groupPosition) {
        return mRoomModels;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View theConvertView, ViewGroup parent) {
        View resultView = theConvertView;
        ViewHolder holder;

        if (resultView == null) {
            resultView = inflater.inflate(R.layout.exp_list_favorites_header, null);
            holder = new ViewHolder();
            holder.favoritesLabel = (TextView) resultView.findViewById(R.id.favorites_label);
            resultView.setTag(holder);

        } else {
            holder = (ViewHolder) resultView.getTag();
        }

        return resultView;
    }


    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}