package edu.msu.elhazzat.whirpool.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.RoomSearchAdapter;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarEventReader;
import edu.msu.elhazzat.whirpool.crud.RelevantRoomDbHelper;
import edu.msu.elhazzat.whirpool.model.EventModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.utils.AsyncTokenFromGoogleAccountCredential;
import edu.msu.elhazzat.whirpool.utils.CalendarServiceHolder;
import edu.msu.elhazzat.whirpool.utils.TokenHolder;


/**
 * Created by Christian on 9/27/2015.
 */
public class HomeActivity extends CalendarServiceActivity implements View.OnClickListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    private SwipeMenuListView mCalendarList;
    private EventAdapter mCalendarAdapter;
    private ArrayList<EventModel> mCalendarListValues = new ArrayList<>();
    private AsyncCalendarEventReader mEventReader = null;

    private ListView mRoomList;
    private RoomSearchAdapter mRoomSearchAdapter;
    private List<RoomModel> mRoomModelListValues = new ArrayList<>();

    private DrawerLayout mDrawerLayout;

    private ImageView mHamburgerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(CalendarServiceHolder.getInstance().getService() == null) {
            String accountName = getIntent().getExtras().getString("accountName");

            //build calendar service and acquire oauth2 credential
            super.buildCredential(accountName);
            super.buildCalendarService();
        }
        else {
            mService = CalendarServiceHolder.getInstance().getService();
        }

        if(TokenHolder.getInstance().getToken() == null) {
            // The service class does not support the google calendar resource api
            // Get token from service credential
            new AsyncTokenFromGoogleAccountCredential(mCredential) {

                // after the token has been fetched, use it to grab the resource feed
                @Override
                public void handleToken(String token) {
                    TokenHolder.getInstance().setToken(token);

                }
            }.execute();
        }

        mCalendarList = (SwipeMenuListView) findViewById(R.id.swipeList);

        // mRoomList = (ListView)findViewById(R.id.roomList);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mHamburgerImage = (ImageView) findViewById(R.id.hamburger);

        buildSwipeView();
        inflateEventAdapter();
        buildDrawerLayout();
    }

    private void buildDrawerLayout() {

        mHamburgerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {}

            @Override
            public void onDrawerOpened(View view) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onDrawerClosed(View view) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerStateChanged(int i) {}
        });
    }

    private void buildSwipeView() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            private float dp2px(int dip, Context context){
                float scale = context.getResources().getDisplayMetrics().density;
                return dip * scale + 0.5f;
            }

            @Override
            public void create(SwipeMenu menu) {
                // create "nav" item
                SwipeMenuItem navItem = new SwipeMenuItem(
                        getApplicationContext());

                // set item width
                navItem.setWidth((int) dp2px(60, getApplicationContext()));

                navItem.setBackground(new ColorDrawable(Color.rgb(0x48,
                        0x87, 0xF0)));
                // set a icon
                navItem.setIcon(R.drawable.navigate);
                // add to menu
                menu.addMenuItem(navItem);

                // create "fav" item
                SwipeMenuItem favItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background

                // set item width
                favItem.setWidth((int) dp2px(60, getApplicationContext()));
                // set a icon
                favItem.setIcon(R.drawable.edit);
                // add to menu
                menu.addMenuItem(favItem);

                // create "calendar" item
                SwipeMenuItem calItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background

                // set item width
                calItem.setWidth((int) dp2px(60, getApplicationContext()));
                // set a icon
                calItem.setIcon(R.drawable.delete);
                // add to menu
                menu.addMenuItem(calItem);
            }
        };

        mCalendarList.setMenuCreator(creator);

        mCalendarList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        EventModel model = mCalendarListValues.get(position);
                        Intent roomIntent = new Intent(getApplicationContext(), RoomActivity.class);
                        roomIntent.putExtra("ROOM_ID", model.getLocation());
                        startActivity(roomIntent);
                        break;
                    case 1:
                        // fav
                        break;
                    case 2:
                        // calendar
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void inflateRoomAdapter() {
        mRoomModelListValues.clear();
        RelevantRoomDbHelper helper = new RelevantRoomDbHelper(this);
        mRoomModelListValues = helper.getAllRelevantRooms();
        mRoomSearchAdapter = new RoomSearchAdapter(this, android.R.layout.simple_list_item_1, mRoomModelListValues);
        mRoomList.setAdapter(mRoomSearchAdapter);
    }

    private void inflateEventAdapter() {

        //get user calendar events for the date starting now
        mEventReader = new AsyncCalendarEventReader(mService, new DateTime(System.currentTimeMillis()), 10) {

            // populate calendar event view
            @Override
            public void onAsyncFinished(List<Event> events) {
                mCalendarListValues.clear();
                if(events != null) {
                    Resources res = getResources();
                    for (Event event : events) {
                        final EventModel sched = new EventModel();
                        sched.setLocation(event.getLocation());
                        sched.setSummary(event.getSummary());
                        sched.setStartTime(event.getStart().getDateTime().toString());
                        mCalendarListValues.add(sched);
                    }
                    mCalendarAdapter = new EventAdapter();
                    mCalendarList.setAdapter(mCalendarAdapter);
                }
            }

            // start request authorization process if authorization fails
            @Override
            public void handleUserRecoverableAuthIOException(UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), COMPLETE_AUTHORIZATION_REQUEST_CODE);
            }
        };

        mEventReader.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        inflateEventAdapter();
   //     inflateRoomAdapter();
    }


    public void onCalendarPermissionAuthorized() {
        inflateEventAdapter();
    }

    public void onCalendarPermissionDenied() {
        //TODO Allow user to continue without this functionality
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_button:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.imageButton:
                Intent intent2 = new Intent(this, SearchActivity.class);
                startActivity(intent2);
                break;
        }
    }

    class EventAdapter extends BaseAdapter {

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
                convertView = View.inflate(getApplicationContext(),
                        R.layout.item_list_app, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            EventModel item = getItem(position);
            Integer resource = getBuildingImageResource(item);
            Drawable image = ContextCompat.getDrawable(getApplicationContext(), resource);

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
            Map<String, Integer> buildingMap = new HashMap<>();

            buildingMap.put("Benson Road", R.drawable.benson_road3x);
            buildingMap.put("whirlpoolDefault", R.drawable.whirlpool_default3x);
            buildingMap.put("BHTC", R.drawable.benton_harbor_tech_center3x);
            buildingMap.put("Edgewater", R.drawable.edge_water_tech_center3x);
            buildingMap.put("GHQ", R.drawable.ghq3x);
            buildingMap.put("Harbortown", R.drawable.harbor_town3x);
            buildingMap.put("Hilltop 150", R.drawable.hilltop_150_south3x);
            buildingMap.put("Hilltop 211", R.drawable.hilltop_211_north);
            buildingMap.put("MMC", R.drawable.us_benton_harbor_mmc3x);
            buildingMap.put("R&E", R.drawable.rande3x);
            buildingMap.put("Riverview", R.drawable.riverview3x);
            buildingMap.put("St, Joe Tech Center", R.drawable.stjoetechcenter3x);

            String location = item.getLocation();
            String[] resourceSplit = location.split("-");
            Integer png = null;
            if(resourceSplit.length > 2) {
                String locationName = resourceSplit[2].trim();
                png = buildingMap.get(locationName);
            }

            if(png == null) {
                return buildingMap.get("whirlpoolDefault");
            }
            else {
                return png;
            }

        }
    }
}
