package edu.msu.elhazzat.whirpool;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian on 9/27/2015.
 */
public class HomeActivity extends CalendarServiceActivity {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    private ListView mCalendarList;
    private EventAdapter mCalendarAdapter;
    private ArrayList<ListEventModel> mCalendarListValues = new ArrayList<>();

    private ListView mRoomList;
    private RoomAdapter mRoomAdapter;
    private ArrayList<ListRoomModel> mRoomListValues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String accountName = getIntent().getExtras().getString("accountName");

        super.buildCredential(accountName);
        super.buildCalendarService();

        Resources res = getResources();
        mCalendarList = (ListView)findViewById(R.id.timeList);

        new AsyncCalendarEventReader(new HandleAsyncCalendarReader(), mService,
                new DateTime(System.currentTimeMillis()), 10).execute();

        setRoomData();

        mRoomList = (ListView)findViewById(R.id.roomList);

        mRoomAdapter = new RoomAdapter(this, mRoomListValues, res);
        mRoomList.setAdapter(mRoomAdapter);
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void onCalendarPermissionAuthorized() {
        new AsyncCalendarEventReader(new HandleAsyncCalendarReader(), mService,
                new DateTime(System.currentTimeMillis()), 10).execute();
    }

    public void onCalendarPermissionDenied() {
        //TODO Allow user to continue without this functionality
    }

    public void setRoomData()
    {
        for(int i = 0; i < 7; i++){

            final ListEventModel sched2 = new ListEventModel();

/*            sched2.setTime("10:00 AM\n11:00 AM");
            sched2.setImage("marker");
            sched2.setMainText("Room" + i);
*/
            //mRoomListValues.add(sched2);
        }

    }

    public class HandleAsyncCalendarReader implements AsyncCalendarEventReader.AsyncCalendarReaderDelegate {
        public void onAsyncFinished(List<Event> events) {

            if(events != null) {
                Resources res = getResources();
                mCalendarAdapter = new EventAdapter(HomeActivity.this, mCalendarListValues, res);
                mCalendarList.setAdapter(mCalendarAdapter);
                for (Event event : events) {
                    final ListEventModel sched = new ListEventModel();
                    //add properties to sched
                    mCalendarListValues.add(sched);
                }
            }
        }

        public void handleUserRecoverableAuthIOException(UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), COMPLETE_AUTHORIZATION_REQUEST_CODE);
        }
    }
}
