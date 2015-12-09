package edu.msu.elhazzat.wim.calendar;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.calendar.Calendar;

import java.io.IOException;

/**
 * Created by christianwhite on 10/25/15.
 */
public abstract class AsyncCalendarEventDeleter extends AsyncTask<Void, Void, Void>  {

    public static final String LOG_TAG = AsyncCalendarEventDeleter.class.getSimpleName();

    private Calendar mService;
    private String mEventId;

    public AsyncCalendarEventDeleter(Calendar service, String eventId) {
        mService = service;
        mEventId = eventId;
    }

    public abstract void handleDelete();

    @Override
    public Void doInBackground(Void... params) {
        if(mService != null) {
            try {
                Calendar.Events.Delete delete = mService.events().delete("primary", mEventId);
                delete.execute();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error :", e);
            }
        }
        return null;
    }

    @Override
    public void onPostExecute(Void result) {
        handleDelete();
    }
}
