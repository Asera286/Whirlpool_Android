package edu.msu.elhazzat.whirpool.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.activity.RoomActivity;
import edu.msu.elhazzat.whirpool.model.EventModel;
import edu.msu.elhazzat.whirpool.utils.WIMAppConstants;

/**
 * Created by Christian on 10/23/2015.
 */
public class EventAdapter extends BaseAdapter {

    private Context mContext;
    private List<EventModel> mCalendarListValues = new ArrayList<>();
    private Map<String, Integer> mBuildingMap = new HashMap<>();

    public EventAdapter(Context context, List<EventModel> eventModels) {
        super();
        mContext = context;
        mCalendarListValues = eventModels;

        mBuildingMap.put(WIMAppConstants.WHIRLPOOL_DEFAULT, R.drawable.whirlpool_default3x);
        mBuildingMap.put(WIMAppConstants.BENSON_ROAD, R.drawable.benson_road3x);
        mBuildingMap.put(WIMAppConstants.BHTC, R.drawable.benton_harbor_tech_center3x);
        mBuildingMap.put(WIMAppConstants.EDGEWATER, R.drawable.edge_water_tech_center3x);
        mBuildingMap.put(WIMAppConstants.GHQ, R.drawable.ghq3x);
        mBuildingMap.put(WIMAppConstants.HARBORTOWN, R.drawable.harbor_town3x);
        mBuildingMap.put(WIMAppConstants.HILLTOP_150, R.drawable.hilltop_150_south3x);
        mBuildingMap.put(WIMAppConstants.HILLTOP_211, R.drawable.hilltop_211_north);
        mBuildingMap.put(WIMAppConstants.MMC, R.drawable.us_benton_harbor_mmc3x);
        mBuildingMap.put(WIMAppConstants.R_AND_E, R.drawable.rande3x);
        mBuildingMap.put(WIMAppConstants.RIVERVIEW, R.drawable.riverview3x);
        mBuildingMap.put(WIMAppConstants.ST_JOE_TECH_CENTER, R.drawable.stjoetechcenter3x);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext,
                    R.layout.event_row, null);
            new  ViewHolder(convertView);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        final EventModel item = getItem(position);

        // front
        Integer resource = getBuildingImageResource(item);
        Drawable image = ContextCompat.getDrawable(mContext, resource);
        holder.room_icon.setImageDrawable(image);

        try {
            Date startDate = new Date(item.getStartDateTime().getValue());
            Date endDate = new Date(item.getEndDateTime().getValue());

            DateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            String startTime = dateFormat.format(startDate);
            String endTime = dateFormat.format(endDate);

            String interval = startTime + " - " + endTime;

            long seconds = (startDate.getTime() - System.currentTimeMillis()) / 1000;
            float minutesFloat = seconds / 60;
            float hours= minutesFloat / 60;
            float minutesFrac = (float) (hours - Math.floor(hours));
            int minutes = (int) (60 * minutesFrac);
            String timeUntil = "";
            if(hours >= 1) {
                timeUntil = Integer.toString((int) Math.floor(hours)) + "hrs "
                        + Integer.toString(minutes) + " min";
            }
            else {
                timeUntil = Integer.toString(minutes) + " min";
            }
            holder.event_time_until.setText(timeUntil);
            holder.event_start_end.setText(interval);

        }
        catch(android.net.ParseException e) {

        }

        holder.event_summary.setText(item.getSummary());

        //back

        if(item.getLocation() != null) {
            holder.nav_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent roomIntent = new Intent(mContext, RoomActivity.class);
                    roomIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    roomIntent.putExtra("EVENT", item);
                    mContext.startActivity(roomIntent);
                }
            });
        }
        else {
            holder.nav_icon.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {

        //front
        ImageView room_icon;
        TextView event_start_end;
        TextView event_summary;
        TextView event_time_until;

        //back
        ImageView nav_icon;
        ImageView delete_icon;
        ImageView edit_icon;


        public ViewHolder(View view) {
            room_icon = (ImageView) view.findViewById(R.id.room_icon);
            event_start_end = (TextView) view.findViewById(R.id.event_start_end);
            event_summary = (TextView) view.findViewById(R.id.event_summary);
            event_time_until = (TextView) view.findViewById(R.id.event_time_until);

            nav_icon = (ImageView) view.findViewById(R.id.nav1x);
            delete_icon = (ImageView) view.findViewById(R.id.delete1x);
            edit_icon = (ImageView) view.findViewById(R.id.edit1x);
            view.setTag(this);
        }
    }

    private Integer getBuildingImageResource(EventModel item) {

        String location = item.getLocation();
        if(location == null) {
            return mBuildingMap.get(WIMAppConstants.WHIRLPOOL_DEFAULT);
        }
        String[] resourceSplit = location.split("-");
        Integer png = null;
        if(resourceSplit.length > 2) {
            String locationName = resourceSplit[2].trim();
            png = mBuildingMap.get(locationName);
        }

        if(png == null) {
            return mBuildingMap.get(WIMAppConstants.WHIRLPOOL_DEFAULT);
        }
        else {
            return png;
        }
    }
}