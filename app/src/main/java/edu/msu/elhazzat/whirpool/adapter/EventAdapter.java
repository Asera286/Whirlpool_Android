package edu.msu.elhazzat.whirpool.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.model.EventModel;

/**
 * Created by Christian on 10/23/2015.
 */
public class EventAdapter extends BaseAdapter {

    private Context mContext;
    private List<EventModel> mCalendarListValues = new ArrayList<>();
    private Map<String, Integer> mBuildingMap = new HashMap<>();

    public static final String WHIRLPOOL_DEFAULT = "whirlpoolDefault";
    public static final String BENSON_ROAD = "Benson Road";
    public static final String BHTC = "BHTC";
    public static final String EDGEWATER = "Edgewater";
    public static final String GHQ = "GHQ";
    public static final String HARBORTOWN = "Harbortown";
    public static final String HILLTOP_150 = "Hilltop 150";
    public static final String HILLTOP_211 = "Hilltop 211";
    public static final String MMC = "MMC";
    public static final String R_AND_E = "R&E";
    public static final String RIVERVIEW = "Riverview";
    public static final String ST_JOE_TECH_CENTER = "St. Joe Tech Center";

    public EventAdapter(Context context, List<EventModel> eventModels) {
        super();
        mContext = context;
        mCalendarListValues = eventModels;

        mBuildingMap.put(WHIRLPOOL_DEFAULT, R.drawable.whirlpool_default3x);
        mBuildingMap.put(BENSON_ROAD, R.drawable.benson_road3x);
        mBuildingMap.put(BHTC, R.drawable.benton_harbor_tech_center3x);
        mBuildingMap.put(EDGEWATER, R.drawable.edge_water_tech_center3x);
        mBuildingMap.put(GHQ, R.drawable.ghq3x);
        mBuildingMap.put(HARBORTOWN, R.drawable.harbor_town3x);
        mBuildingMap.put(HILLTOP_150, R.drawable.hilltop_150_south3x);
        mBuildingMap.put(HILLTOP_211, R.drawable.hilltop_211_north);
        mBuildingMap.put(MMC, R.drawable.us_benton_harbor_mmc3x);
        mBuildingMap.put(R_AND_E, R.drawable.rande3x);
        mBuildingMap.put(RIVERVIEW, R.drawable.riverview3x);
        mBuildingMap.put(ST_JOE_TECH_CENTER, R.drawable.stjoetechcenter3x);
    }

    @Override
    public int getCount() {
        return mCalendarListValues.size();
    }

    @Override
    public EventModel getItem(int position) {
        return mCalendarListValues.get(position);
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
                    R.layout.item_list_app, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        EventModel item = getItem(position);
        Integer resource = getBuildingImageResource(item);
        Drawable image = ContextCompat.getDrawable(mContext, resource);

        holder.room_icon.setImageDrawable(image);

        holder.event_start_end.setText(item.getStartTime());
        holder.event_summary.setText(item.getSummary());

        return convertView;
    }

    class ViewHolder {
        ImageView room_icon;
        TextView event_start_end;
        TextView event_summary;

        public ViewHolder(View view) {
            room_icon = (ImageView) view.findViewById(R.id.room_icon);
            event_start_end = (TextView) view.findViewById(R.id.event_start_end);
            event_summary = (TextView) view.findViewById(R.id.event_summary);
            view.setTag(this);
        }
    }

    private Integer getBuildingImageResource(EventModel item) {

        String location = item.getLocation();
        String[] resourceSplit = location.split("-");
        Integer png = null;
        if(resourceSplit.length > 2) {
            String locationName = resourceSplit[2].trim();
            png = mBuildingMap.get(locationName);
        }

        if(png == null) {
            return mBuildingMap.get(WHIRLPOOL_DEFAULT);
        }
        else {
            return png;
        }
    }
}