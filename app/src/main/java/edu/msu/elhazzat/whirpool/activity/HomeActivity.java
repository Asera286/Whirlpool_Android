package edu.msu.elhazzat.whirpool.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.EventAdapter;
import edu.msu.elhazzat.whirpool.adapter.FavoritesAdapter;
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
public class HomeActivity extends CalendarServiceActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final int SWIPE_VIEW_OFFSET = 0;
    private static final int SWIPE_VIEW_DELAY = 500;

    private SwipeListView mCalendarListView;
    private EventAdapter mCalendarAdapter;
    private ArrayList<EventModel> mCalendarListViewValues = new ArrayList<>();

    private DrawerLayout mDrawerLayout;
    private boolean mDrawerOpen = false;
    private ActionBarDrawerToggle mDrawerToggle;

    private ExpandableListView mFavoritesView;
    private FavoritesAdapter mFavoritesAdapter;

    private GoogleApiClient mGoogleApiClient;

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();

        mCalendarListView = (SwipeListView) findViewById(R.id.example_swipe_lv_list);

        ImageView addEventButton = (ImageView) findViewById(R.id.eventButton);
        addEventButton. setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createEventIntent = new Intent(getApplicationContext(), CreateEventActivity.class);
                startActivity(createEventIntent);
            }
        });

        mFavoritesView = (ExpandableListView) findViewById(R.id.exp_favorites_list);
        mFavoritesView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_LONG).show();
                return false;
            }
        });


        buildDrawer();
        buildSwipeView();
        inflateEventAdapter();
        inflateFavoritesAdapter();
    }

    private void buildDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        if(ab != null) {
    //        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0000ff")));
        }

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.hello_world, R.string.hello_world)  {

                public void onDrawerClosed(View view) {
                    mDrawerOpen = false;
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    supportInvalidateOptionsMenu();
                    //drawerOpened = false;
                }

                public void onDrawerOpened(View drawerView) {
                    //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);

                    mDrawerLayout.bringToFront();
                    mDrawerLayout.requestLayout();
                    supportInvalidateOptionsMenu();
                    //drawerOpened = true;
                }
            };

            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

    }

    private void buildSwipeView() {
        mCalendarListView = (SwipeListView) findViewById(R.id.example_swipe_lv_list);

        mCalendarListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mCalendarListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                      long id, boolean checked) {
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return true;
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                 /*   MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.menu_choice_items, menu);*/
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    mCalendarListView.unselectedChoiceStates();
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }
            });
        }


        mCalendarListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
            }

            @Override
            public void onStartClose(int position, boolean right) {
            }

            @Override
            public void onClickFrontView(int position) {
                EventModel model = mCalendarAdapter.getItem(position);
                if (model.getLocation() != null) {
                    Intent roomIntent = new Intent(getApplicationContext(), RoomActivity.class);
                    roomIntent.putExtra("EVENT", model);
                    startActivity(roomIntent);
                }
            }

            @Override
            public void onClickBackView(int position) {
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    mCalendarListViewValues.remove(position);
                }
                mCalendarAdapter.notifyDataSetChanged();
            }

        });

        mCalendarListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        mCalendarListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        mCalendarListView.setSwipeOpenOnLongPress(false);
        mCalendarListView.setOffsetLeft(SWIPE_VIEW_OFFSET);
        mCalendarListView.setAnimationTime(SWIPE_VIEW_DELAY);
        
        mCalendarListView.setAdapter(mCalendarAdapter);
    }
    
    private void inflateEventAdapter() {

        //get user calendar events for the date starting now
        new AsyncCalendarEventReader(mService, new DateTime(System.currentTimeMillis()), 10) {

            // populate calendar event view
            @Override
            public void onAsyncFinished(List<Event> events) {
                mCalendarListViewValues.clear();
                if(events != null) {
                    for (Event event : events) {
                        final EventModel sched = new EventModel();
                        sched.setId(event.getId());
                        sched.setLocation(event.getLocation());
                        sched.setSummary(event.getSummary());

                        sched.setStartTime(event.getStart().getDateTime().toString());
                        sched.setEndTime(event.getEnd().getDateTime().toString());

                        sched.setStartDateTime(event.getStart().getDateTime());
                        sched.setEndDateTime(event.getEnd().getDateTime());

                        sched.setDescription(event.getDescription());
                        mCalendarListViewValues.add(sched);
                    }
                    mCalendarAdapter = new EventAdapter(getApplicationContext(), mCalendarListViewValues);
                    mCalendarListView.setAdapter(mCalendarAdapter);
                }
            }

            // start request authorization process if authorization fails
            @Override
            public void handleUserRecoverableAuthIOException(UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), COMPLETE_AUTHORIZATION_REQUEST_CODE);
            }
        }.execute();
    }

    private void inflateFavoritesAdapter() {
        RelevantRoomDbHelper helper = new RelevantRoomDbHelper(this);
        List<RoomModel> rooms = helper.getAllRelevantRooms();
        mFavoritesAdapter = new FavoritesAdapter(this, rooms);
        mFavoritesView.setAdapter(mFavoritesAdapter);
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
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if(!mDrawerOpen) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mDrawerOpen = true;
            }
            return true;
        }
        else {
            int id = item.getItemId();
            switch (id) {
                case R.id.action_search:
                    Intent searchIntent = new Intent(this, SearchActivity.class);
                    startActivity(searchIntent);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

        @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onConnected(Bundle arg0) {
        //
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        mGoogleApiClient.connect();
        // updateUI(false);
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

}


