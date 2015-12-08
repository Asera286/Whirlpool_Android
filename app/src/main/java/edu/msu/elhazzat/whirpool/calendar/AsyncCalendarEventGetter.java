package edu.msu.elhazzat.whirpool.calendar;

/**
 * Created by christianwhite on 10/25/15.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;

/**
 * Created by christianwhite on 10/25/15.
 */
public abstract class AsyncCalendarEventGetter extends AsyncTask<Void, Void, Event> {

    public static final String LOG_TAG = AsyncCalendarEventDeleter.class.getSimpleName();

    private Calendar mService;
    private String mEventId;

    public AsyncCalendarEventGetter(Calendar service, String eventId) {
        mService = service;
        mEventId = eventId;
    }

    public abstract void handleEvent(Event event);

    @Override
    public Event doInBackground(Void... params) {
        if(mService != null) {
            try {
                Calendar.Events.Get getter = mService.events().get("primary", mEventId);
                Event event = getter.execute();
                return event;

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error :", e);
            }
        }
        return null;
    }

    @Override
    public void onPostExecute(Event event) {
        handleEvent(event);
    }
}

