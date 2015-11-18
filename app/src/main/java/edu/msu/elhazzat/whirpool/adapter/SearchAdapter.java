package edu.msu.elhazzat.whirpool.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.model.BuildingModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.utils.WIMAppConstants;

/**
 * Created by christianwhite on 11/11/15.
 */
public class SearchAdapter extends BaseExpandableListAdapter {

    Map<String, Integer> mBuildingMap = new HashMap<>();

    private static final class ViewHolder {
        TextView textLabel;

        TextView buildingLabel;
        ImageView imageView;
    }

    private List<BuildingModel> mBuildingModelsFiltered = new ArrayList<>();
    private List<BuildingModel> mBuildingModels = new ArrayList<>();
    private LayoutInflater inflater;
    private Context mContext;

    public SearchAdapter(Context context, List<BuildingModel> itemList) {
        mContext = context;
        inflater = LayoutInflater.from(context);
        mBuildingModels = itemList;

        mBuildingMap.put(WIMAppConstants.WHIRLPOOL_DEFAULT, R.drawable.whirlpool_default_trans3x);
        mBuildingMap.put(WIMAppConstants.BENSON_ROAD, R.drawable.benson_road_trans3x);
        mBuildingMap.put(WIMAppConstants.BHTC, R.drawable.benton_harbor_tech_center_trans3x);
        mBuildingMap.put(WIMAppConstants.EDGEWATER, R.drawable.edge_water_tech_center_trans3x);
        mBuildingMap.put(WIMAppConstants.GHQ, R.drawable.ghq_trans3x);
        mBuildingMap.put(WIMAppConstants.HARBORTOWN, R.drawable.harbor_town_trans3x);
        mBuildingMap.put(WIMAppConstants.HILLTOP_150, R.drawable.hilltop_150_south_trans3x);
        mBuildingMap.put(WIMAppConstants.HILLTOP_211, R.drawable.hilltop_211_north_trans3x);
        mBuildingMap.put(WIMAppConstants.MMC, R.drawable.mmc_trans3x);
        mBuildingMap.put(WIMAppConstants.R_AND_E, R.drawable.rande_trans3x);
        mBuildingMap.put(WIMAppConstants.RIVERVIEW, R.drawable.riverview_trans3x);
        mBuildingMap.put(WIMAppConstants.ST_JOE_TECH_CENTER, R.drawable.stjoetechcenter_trans3x);
    }

    @Override
    public RoomModel getChild(int groupPosition, int childPosition) {

        return mBuildingModels.get(groupPosition).getRooms().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(mBuildingModels.get(groupPosition).getRooms() != null) {
            return mBuildingModels.get(groupPosition).getRooms().size();
        }
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {
        View resultView = convertView;
        ViewHolder holder;


        if (resultView == null) {

            resultView = inflater.inflate(R.layout.exp_list_search_item, null);
            holder = new ViewHolder();
            holder.textLabel = (TextView) resultView.findViewById(R.id.room_name);
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }

        RoomModel item = getChild(groupPosition, childPosition);

        holder.textLabel.setText(item.getRoomName());

        return resultView;
    }

    @Override
    public BuildingModel getGroup(int groupPosition) {
        return mBuildingModels.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mBuildingModels.size();
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
            resultView = inflater.inflate(R.layout.exp_list_search_header, null);
            holder = new ViewHolder();
            holder.buildingLabel = (TextView) resultView.findViewById(R.id.building_name);
            holder.imageView = (ImageView) resultView.findViewById(R.id.building_img);
            resultView.setTag(holder);

        } else {
            holder = (ViewHolder) resultView.getTag();
        }


        BuildingModel item = getGroup(groupPosition);
        String buildingNameFull = "";
        for (Map.Entry<String, String> e : WIMAppConstants.WHIRLPOOL_ABBRV_MAP.entrySet()) {
            String full = e.getKey();
            String abbr = e.getValue();
            if(abbr.equals(item.getBuildingName())) {
                buildingNameFull = full;
                break;
            }
        }

        holder.buildingLabel.setText(buildingNameFull);

        if(mBuildingMap.containsKey(buildingNameFull)) {
            Integer resource = mBuildingMap.get(buildingNameFull);

            Drawable image = ContextCompat.getDrawable(mContext, resource);
            holder.imageView.setImageDrawable(image);

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