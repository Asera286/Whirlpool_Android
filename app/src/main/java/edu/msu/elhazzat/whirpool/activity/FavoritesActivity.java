package edu.msu.elhazzat.whirpool.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.EventAdapter;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarEventReader;
import edu.msu.elhazzat.whirpool.model.EventModel;
import edu.msu.elhazzat.whirpool.utils.CalendarServiceHolder;

public class FavoritesActivity extends AppCompatActivity {

    private SwipeListView mSwipeListView;
    private EventAdapter mAdapter;
    private List<EventModel> mData = new ArrayList<>();
    private AsyncCalendarEventReader mEventReader = null;

    private Calendar mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        mService = CalendarServiceHolder.getInstance().getService();

        mSwipeListView = (SwipeListView) findViewById(R.id.example_swipe_lv_list);

        mSwipeListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mSwipeListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                      long id, boolean checked) {
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                   /* switch (item.getItemId()) {
                        case R.id.menu_delete:
                            swipeListView.dismissSelected();
                            mode.finish();
                            return true;
                        default:
                            return false;
                    }*/
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
                    mSwipeListView.unselectedChoiceStates();
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }
            });
        }


        mSwipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
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
                Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d("swipe", String.format("onStartClose %d", position));
            }

            @Override
            public void onClickFrontView(int position) {
                Log.d("swipe", String.format("onClickFrontView %d", position));
            }

            @Override
            public void onClickBackView(int position) {
                Log.d("swipe", String.format("onClickBackView %d", position));
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    mData.remove(position);
                }
                mAdapter.notifyDataSetChanged();
            }

        });

        mSwipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mSwipeListView.setAdapter(mAdapter);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        reload();
        inflateEventAdapter();
    }

    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    private void reload() {
        mSwipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        mSwipeListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        mSwipeListView.setSwipeOpenOnLongPress(false);
        mSwipeListView.setOffsetLeft(0);
        mSwipeListView.setAnimationTime(500);
    }

    private void inflateEventAdapter() {

        //get user calendar events for the date starting now
        mEventReader = new AsyncCalendarEventReader(mService, new DateTime(System.currentTimeMillis()), 10) {

            // populate calendar event view
            @Override
            public void onAsyncFinished(List<Event> events) {
                mData.clear();
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
                        mData.add(sched);
                    }
                    mAdapter = new EventAdapter(getApplicationContext(), mData);
                    mSwipeListView.setAdapter(mAdapter);
                }
            }

            // start request authorization process if authorization fails
            @Override
            public void handleUserRecoverableAuthIOException(UserRecoverableAuthIOException e) {
                return;
            }
        };

        mEventReader.execute();
    }


}