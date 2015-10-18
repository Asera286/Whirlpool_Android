package edu.msu.elhazzat.whirpool.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarEventWriter;
import edu.msu.elhazzat.whirpool.utils.CalendarServiceHolder;

/**
 * Created by christianwhite on 10/15/15.
 */
public class CreateEventActivity extends Activity {

    private String mRoomId;
    private String mRoomEmail;

    private String mDescription;

    private int mSelectedYear;
    private int mSelectedMonth;
    private int mSelectedDay;

    private int mSelectedBeginHour;
    private int mSelectedBeginMinute;
    private int mSelectedEndHour;
    private int mSelectedEndMinute;

    private EditText mSummaryEditText;
    private EditText mDescriptionEditText;
    private EditText mDateEditText;
    private EditText mBeginTimeEditText;
    private EditText mEndTimeEditText;

    private Button mCancelButton;
    private Button mSubmitButton;

    private Calendar mBeginTime = Calendar.getInstance();
    private Calendar mEndTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Bundle content = getIntent().getExtras();
        if(content != null) {
            mRoomId = content.getString("ROOM_ID");
            mRoomEmail = content.getString("ROOM_EMAIL");
        }

        mSummaryEditText = (EditText) findViewById(R.id.event_summary_text);
        mDescriptionEditText = (EditText) findViewById(R.id.event_description_text);

        mDateEditText = (EditText) findViewById(R.id.event_date_text);
        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateSelected();
            }
        });

        mBeginTimeEditText = (EditText) findViewById(R.id.event_begin_time_text);
        mBeginTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBeginTimeSelected();
            }
        });

        mEndTimeEditText = (EditText) findViewById(R.id.event_end_time_text);
        mEndTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEndTimeSelected();
            }
        });

        mCancelButton = (Button) findViewById(R.id.btnCancel);
        mSubmitButton = (Button) findViewById(R.id.btnSubmit);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent roomIntent = new Intent(getApplicationContext(), RoomActivity.class);
                roomIntent.putExtra("ROOM_ID", mRoomId);
                startActivity(roomIntent);
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.google.api.services.calendar.Calendar service = CalendarServiceHolder.getInstance().getService();

                Event event = new Event();

                event.setSummary(mSummaryEditText.getText().toString());

                event.setDescription(mDescriptionEditText.getText().toString());

                Calendar cal = Calendar.getInstance();
                TimeZone tz = cal.getTimeZone();

                DateTime startDateTime = new DateTime(mBeginTime.getTime());//, TimeZone.getDefault());
                DateTime endDateTime = new DateTime(mEndTime.getTime());//, TimeZone.getDefault());
                EventDateTime startEventDateTime = new EventDateTime()
                        .setDateTime(startDateTime);

  //              startEventDateTime.setTimeZone(TimeZone.getDefault().getDisplayName());

                EventDateTime endEventDateTime = new EventDateTime()
                        .setDateTime(endDateTime);

//                endEventDateTime.setTimeZone(TimeZone.getDefault().getDisplayName());

                event.setStart(startEventDateTime);
                event.setEnd(endEventDateTime);

                EventAttendee attendee = new EventAttendee();
                attendee.setEmail(mRoomEmail);
//                attendee.setDisplayName(mRoomId);

                List<EventAttendee> attendees = new ArrayList<>();
                attendees.add(attendee);

                event.setAttendees(attendees);

                new AsyncCalendarEventWriter(service, event) {
                    @Override
                    public void handleEventWrite(boolean success) {
                        Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(homeIntent);
                    }
                }.execute();
            }
        });
    }

    private void onDateSelected() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mSelectedYear = year;
                mSelectedMonth = monthOfYear;
                mSelectedMonth = dayOfMonth;

                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                String date = new SimpleDateFormat("MM/dd/yyyy").format(c.getTime());
                mDateEditText.setText(date);

                mBeginTime.set(Calendar.YEAR, year);
                mBeginTime.set(Calendar.MONTH, monthOfYear);
                mBeginTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mEndTime.set(Calendar.YEAR, year);
                mEndTime.set(Calendar.MONTH, monthOfYear);
                mEndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        };

        DatePickerDialog dialog = new DatePickerDialog(this, dateListener, year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void onBeginTimeSelected() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mSelectedBeginHour = hourOfDay;
                mSelectedBeginMinute = minute;

                Calendar c = Calendar.getInstance();
                c.set(0,0,0, hourOfDay, minute);
                SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
                String time = formatDate.format(c.getTime());
                mBeginTimeEditText.setText(time);

                mEndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mEndTime.set(Calendar.MINUTE, minute);
            }
        };

        TimePickerDialog dialog = new TimePickerDialog(this, timeListener, hour, minute, false);
        dialog.show();
    }

    private void onEndTimeSelected() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mSelectedEndHour = hourOfDay;
                mSelectedEndMinute = minute;

                Calendar c = Calendar.getInstance();
                c.set(0,0,0, hourOfDay, minute);
                SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
                String time = formatDate.format(c.getTime());
                mEndTimeEditText.setText(time);

                mEndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mEndTime.set(Calendar.MINUTE, minute);
            }
        };

        TimePickerDialog dialog = new TimePickerDialog(this, timeListener, hour, minute, false);
        dialog.show();
    }
}
