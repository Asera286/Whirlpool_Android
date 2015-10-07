package edu.msu.elhazzat.whirpool;

import android.os.AsyncTask;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;

/**
 * Created by christianwhite on 10/6/15.
 */
public class AsyncCalendarEventWriter extends AsyncTask<Void, Void, Boolean> {

    public interface AsyncCalendarEventWriterDelegate {
        public void handleEventWrite(Boolean success);
    }

    private com.google.api.services.calendar.Calendar mService;
    private AsyncCalendarEventWriterDelegate mDelegate;
    private Event mEvent;

    AsyncCalendarEventWriter(AsyncCalendarEventWriterDelegate delegate,
                             Calendar service, Event event) {
        mDelegate = delegate;
        mService = service;
        mEvent = event;
    }

    @Override
    public Boolean doInBackground(Void... params) {
        try {
            mService.events().insert("primary", mEvent).execute();
            return true;
        }
        catch(IOException e) {
            //TODO: LOG
        }

        return false;
    }

    @Override
    public void onPostExecute(Boolean success) {
        mDelegate.handleEventWrite(success);
    }

}
