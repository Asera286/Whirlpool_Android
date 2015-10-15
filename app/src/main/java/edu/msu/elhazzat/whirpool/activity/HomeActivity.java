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
import edu.msu.elhazzat.whirpool.adapter.RoomAdapter;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarEventReader;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarResourceReader;
import edu.msu.elhazzat.whirpool.model.EventModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.utils.AsyncTokenFromGoogleAccountCredential;

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
    private RoomAdapter mRoomAdapter;
    private ArrayList<RoomModel> mRoomModelListValues = new ArrayList<>();
    private AsyncCalendarResourceReader mResourceReader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String accountName = getIntent().getExtras().getString("accountName");

        //build calendar service and acquire oauth2 credential
        super.buildCredential(accountName);
        super.buildCalendarService();

        mCalendarList = (ListView)findViewById(R.id.timeList);

        //get user calendar events for the date starting now
        mEventReader = new AsyncCalendarEventReader(mService, new DateTime(System.currentTimeMillis()), 10) {

            // populate calendar event view
            @Override
            public void onAsyncFinished(List<Event> events) {
                if(events != null) {
                    Resources res = getResources();
                    for (Event event : events) {
                        final EventModel sched = new EventModel();
                        sched.setSummary(event.getSummary());
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

        mRoomList = (ListView)findViewById(R.id.roomList);

        // The service class does not support the google calendar resource api
        // Get token from service credential
        new AsyncTokenFromGoogleAccountCredential(mCredential) {

            // after the token has been fetched, use it to grab the resource feed
            @Override
            public void handleToken(String token) {
                mResourceReader = new AsyncCalendarResourceReader(
                        "https://apps-apis.google.com/a/feeds/calendar/resource/2.0/whirlpool.com/",token) {

                    // populate room list view
                    @Override
                    public void handleRooms(List<RoomModel> roomModels) {
                        if(roomModels != null) {
                            Resources res = getResources();
                            for (RoomModel roomModel : roomModels) {
                                mRoomModelListValues.add(roomModel);
                            }
                            mRoomAdapter = new RoomAdapter(HomeActivity.this, mRoomModelListValues, res);
                            mRoomList.setAdapter(mRoomAdapter);
                        }
                    }
                };
                mResourceReader.execute();
            }
        }.execute();
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
