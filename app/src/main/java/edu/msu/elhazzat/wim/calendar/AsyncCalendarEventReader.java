package edu.msu.elhazzat.wim.calendar;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by christianwhite on 9/20/15.
 */

/**
 * Use this class to fetch the primary account calendar events
 */
public abstract class AsyncCalendarEventReader extends AsyncTask<Void, Void, List<Event>> {

    private static final String LOG_TAG = AsyncCalendarEventWriter.class.getSimpleName();

    public abstract void onAsyncFinished(List<com.google.api.services.calendar.model.Event> events);
    public abstract void handleUserRecoverableAuthIOException(UserRecoverableAuthIOException e);

    private com.google.api.services.calendar.Calendar mCalendarService;
    private DateTime mTime;

    public AsyncCalendarEventReader(com.google.api.services.calendar.Calendar service, DateTime time) {
        mCalendarService = service;
        mTime = time;
    }

    @Override
    protected List<Event> doInBackground(Void... params) {
        try {
            return getCalendarEvents();
        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            Log.e(LOG_TAG, "Error: ", availabilityException);
        }
        catch(IOException e) {
            Log.e(LOG_TAG, "Error: ", e);
        }
        return null;
    }

    private List<Event> getCalendarEvents() throws IOException {
        if(mCalendarService != null) {
            try {
                Events events = mCalendarService.events().list("primary")
                        .setTimeMin(mTime)
                        .setTimeMax(getEndOfDay(new Date(mTime.getValue())))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                return events.getItems();
            } catch (UserRecoverableAuthIOException e) {
                handleUserRecoverableAuthIOException(e);
            }
        }
        return null;
    }

    private DateTime getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new DateTime(calendar.getTime());
    }

    @Override
    public void onPostExecute(List<Event> events) {
        onAsyncFinished(events);
    }
}
