package edu.msu.elhazzat.whirpool.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.RoomAdapter;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarEventGetter;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarEventUpdater;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarEventWriter;
import edu.msu.elhazzat.whirpool.model.BuildingModel;
import edu.msu.elhazzat.whirpool.model.EventModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSAllRooms;
import edu.msu.elhazzat.whirpool.utils.CalendarServiceHolder;

/**
 * Created by christianwhite on 10/15/15.
 */
public class CreateEventActivity extends AppCompatActivity {

    private EventModel mEvent;
    private RoomModel mRoom;

    private String mRoomId;
    private String mRoomEmail;

    private String mDescription;

    private EditText mSummaryEditText;
    private EditText mLocationEditText;
    private EditText mDescriptionEditText;
    private TextView mDateTextView;
    private TextView mBeginTimeTextView;
    private TextView mEndTimeTextView;

    private Calendar mBeginTime = Calendar.getInstance();
    private Calendar mEndTime = Calendar.getInstance();

    private List<RoomModel> mRooms = new ArrayList<>();
    private RoomAdapter mLocationAdapter;
    private ListView mLocationListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#73E3B3")));
        }

        Bundle content = getIntent().getExtras();
        if(content != null) {
            mEvent = content.getParcelable("EVENT");
        }

        mSummaryEditText = (EditText) findViewById(R.id.event_summary_text);
        mDescriptionEditText = (EditText) findViewById(R.id.description_text_view);
        mLocationEditText = (EditText) findViewById(R.id.location_text_view);

     //   setUpLocation();
        setUpPickers();

        if(mEvent != null) {
            populateViewFromEvent();
        }
    }

    private void setUpLocation() {
        mLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationListView.getVisibility() == View.GONE) {
                    mLocationListView.setVisibility(View.VISIBLE);
                } else {
                    mLocationListView.setVisibility(View.GONE);
                }
            }
        });

        mLocationEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                mLocationAdapter.getFilter().filter(arg0);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        mLocationListView = (ListView) findViewById(R.id.location_lv);
        mLocationListView.setVisibility(View.GONE);

        mLocationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomModel model = mLocationAdapter.getRoomModel(position);
                mLocationEditText.setText(model.getRoomName());
                mLocationListView.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mLocationEditText.getWindowToken(), 0);
                mRoom = model;
            }
        });

        new AsyncGCSAllRooms() {
            public void handleBuildings(List<BuildingModel> buildings) {
                if(buildings != null) {
                    for (BuildingModel model : buildings) {
                        List<RoomModel> rooms = model.getRooms();
                        mRooms.addAll(rooms);
                    }
                    mLocationAdapter = new RoomAdapter(getApplicationContext(), 0, mRooms);
                    mLocationListView.setAdapter(mLocationAdapter);
                }
            }
        }.execute();
    }

    /**
     * Create date and time pickers
     */
    private void setUpPickers() {
        mDateTextView = (TextView) findViewById(R.id.date_text_view);
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateSelected();
            }
        });

        mBeginTimeTextView = (TextView) findViewById(R.id.start_time_text_view);
        mBeginTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBeginTimeSelected();
            }
        });

        mEndTimeTextView = (TextView) findViewById(R.id.end_time_text_view);
        mEndTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEndTimeSelected();
            }
        });
    }

    /**
     * An event has been passed in - populate view using event values
     */
    private void populateViewFromEvent() {
        String id = mEvent.getId();
        new AsyncCalendarEventGetter(CalendarServiceHolder.getInstance().getService(), id) {
            public void handleEvent(Event event) {
                mSummaryEditText.setText(event.getSummary());
                mDescriptionEditText.setText(event.getDescription());
                long millisecondStart = event.getStart().getDateTime().getValue();
                String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecondStart)).toString();

                mDateTextView.setText(dateString);
                String timeStringStart = DateFormat.format("hh:ss", new Date(millisecondStart)).toString();
                mBeginTimeTextView.setText(timeStringStart);

                long millisecondEnd = event.getEnd().getDateTime().getValue();
                String timeStringEnd = DateFormat.format("hh:ss", new Date(millisecondEnd)).toString();

                mEndTimeTextView.setText(timeStringEnd);

                mLocationEditText.setText(mEvent.getLocation());
            }
        }.execute();
    }

    /**
     * Create a DatePickerDialog for a user to select a calendar date
     */
    private void onDateSelected() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                String date = new SimpleDateFormat("MM/dd/yyyy").format(c.getTime());
                mDateTextView.setText(date);

                mBeginTime.set(Calendar.YEAR, year);
                mBeginTime.set(Calendar.MONTH, monthOfYear);
                mBeginTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mEndTime.set(Calendar.YEAR, year);
                mEndTime.set(Calendar.MONTH, monthOfYear);
                mEndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        };

        DatePickerDialog dialog = new DatePickerDialog(this, DatePickerDialog.THEME_HOLO_LIGHT,
                dateListener, year, month, day);
        dialog.show();
    }

    private String getTime(int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(0,0,0, hourOfDay, minute);
        SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
        return  formatDate.format(c.getTime());
    }

    private void onBeginTimeSelected() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = getTime(hourOfDay, minute);
                mBeginTimeTextView.setText(time);

                mBeginTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mBeginTime.set(Calendar.MINUTE, minute);
            }
        };

        CustomTimePickerDialog dialog = new CustomTimePickerDialog(this, timeListener, hour, minute, false);
        dialog.show();
    }

    /**
     *
     */
    private void onEndTimeSelected() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = getTime(hourOfDay, minute);
                mEndTimeTextView.setText(time);

                mEndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mEndTime.set(Calendar.MINUTE, minute);
            }
        };

        CustomTimePickerDialog dialog = new CustomTimePickerDialog(this, timeListener, hour, minute, false);
        dialog.show();
    }

    /**
     * Custom time picker - use this to set time interval 10 rather than the default of 1
     */
    public class CustomTimePickerDialog extends TimePickerDialog {

        private final static int TIME_PICKER_INTERVAL = 10;
        private TimePicker mTimePicker;
        private final OnTimeSetListener mCallback;

        public CustomTimePickerDialog(Context context, OnTimeSetListener callBack,
                                      int hourOfDay, int minute, boolean is24HourView) {
            super(context, TimePickerDialog.THEME_HOLO_LIGHT, callBack, hourOfDay, minute / TIME_PICKER_INTERVAL,
                    is24HourView);
            mCallback = callBack;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mCallback != null && mTimePicker != null) {
                mTimePicker.clearFocus();
                mCallback.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(),
                        mTimePicker.getCurrentMinute() * TIME_PICKER_INTERVAL);
            }
        }

        @Override
        protected void onStop() {
        }

        @Override
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            try {
                Class<?> classForid = Class.forName("com.android.internal.R$id");
                Field timePickerField = classForid.getField("timePicker");
                mTimePicker = (TimePicker) findViewById(timePickerField
                        .getInt(null));
                Field field = classForid.getField("minute");

                NumberPicker mMinuteSpinner = (NumberPicker) mTimePicker
                        .findViewById(field.getInt(null));
                mMinuteSpinner.setMinValue(0);
                mMinuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
                List<String> displayedValues = new ArrayList<String>();
                for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                    displayedValues.add(String.format("%02d", i));
                }
                mMinuteSpinner.setDisplayedValues(displayedValues
                        .toArray(new String[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Create new event from user input
     * @return
     */
    private Event createEvent() {
        Event event = new Event();

        // Room parsible has been passed in
        if(mRoom != null && mLocationEditText.getText().toString().equals(mRoom.getRoomName())) {
            event.setLocation(mRoom.getResourceName());
            String email = mRoom.getEmail();
            EventAttendee attendee = new EventAttendee();
            attendee.setEmail(email);
            List<EventAttendee> attendees = new ArrayList<>();
            attendees.add(attendee);
            event.setAttendees(attendees);
        }
        else if(mEvent != null && mLocationEditText.getText().toString().equals(mEvent.getLocation())) {
            event.setLocation(mEvent.getLocation());
        }
        else {
            event.setLocation(mLocationEditText.getText().toString());
        }

        event.setSummary(mSummaryEditText.getText().toString());
        event.setDescription(mDescriptionEditText.getText().toString());

        Calendar cal = Calendar.getInstance();

        DateTime startDateTime = new DateTime(mBeginTime.getTime());
        DateTime endDateTime = new DateTime(mEndTime.getTime());

        EventDateTime startEventDateTime = new EventDateTime()
                .setDateTime(startDateTime);

        EventDateTime endEventDateTime = new EventDateTime()
                .setDateTime(endDateTime);

        event.setStart(startEventDateTime);
        event.setEnd(endEventDateTime);

        return event;
    }

    /**
     * Push new event or update current event
     */
    private void addEvent() {
        Event event = createEvent();

        com.google.api.services.calendar.Calendar service = CalendarServiceHolder.getInstance().getService();

        if(mEvent != null) {
            new AsyncCalendarEventUpdater(service, mEvent.getId()
                    , event) {
                public void handleEventUpdate(Event event) {
                    Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(homeIntent);
                }
            }.execute();
        }

        else {
            new AsyncCalendarEventWriter(service, event) {
                @Override
                public void handleEventWrite(boolean success) {
                    Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(homeIntent);
                }
            }.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_event:
                addEvent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
