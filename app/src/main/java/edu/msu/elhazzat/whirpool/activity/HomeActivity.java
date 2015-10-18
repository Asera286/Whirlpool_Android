package edu.msu.elhazzat.whirpool.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

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
public class HomeActivity extends CalendarServiceActivity implements View.OnClickListener{

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    private ListView mCalendarList;
    private EventAdapter mCalendarAdapter;
    private ArrayList<EventModel> mCalendarListValues = new ArrayList<>();
    private AsyncCalendarEventReader mEventReader = null;

    private ListView mRoomList;
    private RoomSearchAdapter mRoomSearchAdapter;
    private List<RoomModel> mRoomModelListValues = new ArrayList<>();

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

        mCalendarList = (ListView)findViewById(R.id.timeList);
        mRoomList = (ListView)findViewById(R.id.roomList);

        inflateEventAdapter();
        inflateRoomAdapter();
    }

    private void inflateRoomAdapter() {
        mRoomModelListValues.clear();
        RelevantRoomDbHelper helper = new RelevantRoomDbHelper(this);
        mRoomModelListValues = helper.getAllRelevantRooms();
        mRoomSearchAdapter = new RoomSearchAdapter(this, android.R.layout.simple_list_item_1, mRoomModelListValues);
        mRoomList.setAdapter(mRoomSearchAdapter);
    }

    private void inflateEventAdapter() {
        mCalendarListValues.clear();

        //get user calendar events for the date starting now
        mEventReader = new AsyncCalendarEventReader(mService, new DateTime(System.currentTimeMillis()), 10) {

            // populate calendar event view
            @Override
            public void onAsyncFinished(List<Event> events) {
                if(events != null) {
                    Resources res = getResources();
                    for (Event event : events) {
                        final EventModel sched = new EventModel();
                        sched.setLocation(event.getLocation());
                        sched.setSummary(event.getSummary());
                        sched.setStartTime(event.getStart().getDateTime().toString());
                        mCalendarListValues.add(sched);
                    }
                    mCalendarAdapter = new EventAdapter(HomeActivity.this, mCalendarListValues, res);
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
   //     inflateEventAdapter();
        inflateRoomAdapter();
    }


    public void onCalendarPermissionAuthorized() {
        mEventReader.execute();
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

}
