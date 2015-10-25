package edu.msu.elhazzat.whirpool.calendar;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;

/**
 * Created by christianwhite on 10/25/15.
 */
public class AsyncCalendarEventUpdater extends AsyncTask<Void, Void, Void> {
    public static final String LOG_TAG = AsyncCalendarEventDeleter.class.getSimpleName();

    private Calendar mService;
    private String mEventId;
    private Event mUpdatedEvent;

    public AsyncCalendarEventUpdater(Calendar service, String eventId, Event update) {
        mService = service;
        mEventId = eventId;
        mUpdatedEvent = update;
    }

    @Override
    public Void doInBackground(Void... params) {
        try {
            mService.events().update("primary", mEventId, mUpdatedEvent);
        }
        catch(IOException e) {
            Log.e(LOG_TAG, "Error :", e);
        }
        return null;
    }
}
