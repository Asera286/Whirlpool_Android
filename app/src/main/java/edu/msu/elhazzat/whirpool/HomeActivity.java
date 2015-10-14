package edu.msu.elhazzat.whirpool;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian on 9/27/2015.
 */
public class HomeActivity extends CalendarServiceActivity implements View.OnClickListener{

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    private ListView mCalendarList;
    private EventAdapter mCalendarAdapter;
    private ArrayList<ListEventModel> mCalendarListValues = new ArrayList<>();

    private ListView mRoomList;
    private RoomAdapter mRoomAdapter;
    private ArrayList<Room> mRoomListValues = new ArrayList<>();

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

        mRoomList = (ListView)findViewById(R.id.roomList);

        new Test().execute();
    }

    public void onCalendarPermissionAuthorized() {
        new AsyncCalendarEventReader(new HandleAsyncCalendarReader(), mService,
                new DateTime(System.currentTimeMillis()), 10).execute();
    }

    public void onCalendarPermissionDenied() {
        //TODO Allow user to continue without this functionality
    }

    public class HandleAsyncCalendarReader implements AsyncCalendarEventReader.AsyncCalendarReaderDelegate {
        public void onAsyncFinished(List<Event> events) {
            if(events != null) {
                Resources res = getResources();
                for (Event event : events) {
                    final ListEventModel sched = new ListEventModel();
                    sched.setSummary(event.getSummary());
                    mCalendarListValues.add(sched);
                }
                mCalendarAdapter = new EventAdapter(HomeActivity.this, mCalendarListValues, res);
                mCalendarList.setAdapter(mCalendarAdapter);
            }
        }

        public void handleUserRecoverableAuthIOException(UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), COMPLETE_AUTHORIZATION_REQUEST_CODE);
        }
    }

    public class HandleAsyncRoomReader implements AsyncRoomParseFromResource.AsyncRoomParseFromResourceDelegate {
        public void handleRoomList(List<Room> rooms) {
            if(rooms != null) {
                Resources res = getResources();
                for (Room room : rooms) {
                    mRoomListValues.add(room);
                }
                mRoomAdapter = new RoomAdapter(HomeActivity.this, mRoomListValues, res);
                mRoomList.setAdapter(mRoomAdapter);
            }
        }
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

    public class Test extends AsyncTask<Void, Void, String> {
        @Override
        public String doInBackground(Void... params) {
            try {
                return mCredential.getToken();
            }
            catch(GoogleAuthException e) {

            }
            catch(IOException e) {

            }
            return null;
        }

        @Override
        public void onPostExecute(String token) {
            new AsyncCalendarResourceReader( "https://apps-apis.google.com/a/feeds/calendar/resource/2.0/whirlpool.com/", token) {
                @Override
                public void handleRooms(List<Room> rooms) {
                    String test = "1";
                }
            }.execute();
        }
    }
}
