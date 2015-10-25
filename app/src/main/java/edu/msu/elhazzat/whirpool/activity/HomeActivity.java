package edu.msu.elhazzat.whirpool.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.EventAdapter;
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
    private RelativeLayout mDrawerPane;

    private ImageView mHamburgerImage;

    private ImageView mAddEventButton;

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

        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mRoomList = (ListView)findViewById(R.id.roomList);

        mRoomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomModel model = mRoomModelListValues.get(position);
                Bundle bundle = new Bundle();
                bundle.putString("ROOM_ID", model.getRoomName());
                bundle.putString("ROOM_EMAIL", model.getEmail());
                Intent roomIntent = new Intent(getApplicationContext(), RoomActivity.class);
                roomIntent.putExtras(bundle);
                startActivity(roomIntent);
            }
        });

        mAddEventButton = (ImageView) findViewById(R.id.eventButton);
        mAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createEventIntent = new Intent(getApplicationContext(), CreateEventActivity.class);
                startActivity(createEventIntent);
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mHamburgerImage = (ImageView) findViewById(R.id.hamburger);

        buildSwipeView();
        inflateEventAdapter();

        buildDrawerLayout();
        inflateRoomAdapter();
    }

    private void buildDrawerLayout() {

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
                inflateRoomAdapter();
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

    private float dp2px(int dip, Context context){
        float scale = context.getResources().getDisplayMetrics().density;
        return dip * scale + 0.5f;
    }

    private void buildSwipeView() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                buildNavigationSwipeMenuItem(menu);
                buildEditSwipeMenuItem(menu);
                buildDeleteSwipeMenuItem(menu);
            }
        };

        mCalendarList.setMenuCreator(creator);

        mCalendarList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // navigation
                        EventModel model = mCalendarListValues.get(position);
                        Intent roomIntent = new Intent(getApplicationContext(), RoomActivity.class);
                        roomIntent.putExtra("ROOM_ID", model.getLocation());
                        startActivity(roomIntent);
                        break;
                    case 1:
                        // edit
                        break;
                    case 2:
                        // delete
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void buildNavigationSwipeMenuItem(SwipeMenu menu) {
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
    }

    private void buildEditSwipeMenuItem(SwipeMenu menu) {
        // create "edit" item
        SwipeMenuItem favItem = new SwipeMenuItem(
                getApplicationContext());

        // set item width
        favItem.setWidth((int) dp2px(60, getApplicationContext()));

        // set a icon
        favItem.setIcon(R.drawable.edit);

        // add to menu
        menu.addMenuItem(favItem);
    }

    private void buildDeleteSwipeMenuItem(SwipeMenu menu) {
        // create "delete" item
        SwipeMenuItem calItem = new SwipeMenuItem(
                getApplicationContext());

        // set item width
        calItem.setWidth((int) dp2px(60, getApplicationContext()));

        // set a icon
        calItem.setIcon(R.drawable.delete);

        // add to menu
        menu.addMenuItem(calItem);
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
                    mCalendarAdapter = new EventAdapter(getApplicationContext(), mCalendarListValues);
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
            case R.id.imageButton:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                break;
        }
    }
}
