package edu.msu.elhazzat.wim.calendar;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;

/**
 * Created by christianwhite on 10/25/15.
 */
public abstract class AsyncCalendarEventUpdater extends AsyncTask<Void, Void, Event> {
    public static final String LOG_TAG = AsyncCalendarEventDeleter.class.getSimpleName();

    private Calendar mService;
    private String mEventId;
    private Event mUpdatedEvent;

    public AsyncCalendarEventUpdater(Calendar service, String eventId, Event update) {
        mService = service;
        mEventId = eventId;
        mUpdatedEvent = update;
    }

    public abstract void handleEventUpdate(Event event);

    @Override
    public Event doInBackground(Void... params) {
        if(mService != null) {
            try {
                Calendar.Events.Update updater = mService.events()
                        .update("primary", mEventId, mUpdatedEvent);
                return updater.execute();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error :", e);
            }
        }
        return null;
    }

    @Override
    public void onPostExecute(Event event) {
        handleEventUpdate(event);
    }
}
