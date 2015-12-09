package edu.msu.elhazzat.wim.calendar;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;

/**
 * Created by christianwhite on 10/6/15.
 */
public abstract class AsyncCalendarEventWriter extends AsyncTask<Void, Void, Boolean> {

    private static final String LOG_TAG = AsyncCalendarEventWriter.class.getSimpleName();

    private com.google.api.services.calendar.Calendar mService;
    private Event mEvent;

    public abstract void handleEventWrite(boolean success);

    public AsyncCalendarEventWriter(Calendar service, Event event) {
        mService = service;
        mEvent = event;
    }

    @Override
    public Boolean doInBackground(Void... params) {
        if(mService != null) {
            try {
                mService.events().insert("primary", mEvent).execute();
                return true;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error :", e);
            }
        }
        return false;
    }

    @Override
    public void onPostExecute(Boolean success) {
        handleEventWrite(success);
    }

}
