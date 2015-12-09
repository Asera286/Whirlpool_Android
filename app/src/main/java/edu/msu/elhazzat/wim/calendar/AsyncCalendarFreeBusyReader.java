package edu.msu.elhazzat.wim.calendar;

/**
 * Created by christianwhite on 10/17/15.
 */
/**
 * Created by christianwhite on 9/20/15.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Use this class to fetch the primary account calendar events
 */
public abstract class AsyncCalendarFreeBusyReader extends AsyncTask<Void, Void, List<TimePeriod>> {

    private static final String LOG_TAG = AsyncCalendarFreeBusyReader.class.getSimpleName();

    public abstract void handleTimePeriods(List<TimePeriod> timePeriods);

    private com.google.api.services.calendar.Calendar mCalendarService;
    private String mEmail;
    private DateTime mTimeMin;
    private DateTime mTimeMax;

    public AsyncCalendarFreeBusyReader (com.google.api.services.calendar.Calendar service,
                                        String email, DateTime timeMin, DateTime timeMax) {
        mCalendarService = service;
        mEmail = email;
        mTimeMin = timeMin;
        mTimeMax = timeMax;
    }

    @Override
    protected List<TimePeriod> doInBackground(Void... params) {
        if(mCalendarService != null) {
            try {
                Calendar.Freebusy freebusy = mCalendarService.freebusy();
                FreeBusyRequest request = new FreeBusyRequest();

                List<FreeBusyRequestItem> items = new ArrayList<>();
                FreeBusyRequestItem emailItem = new FreeBusyRequestItem();
                emailItem.setId(mEmail);
                items.add(emailItem);

                request.setTimeMin(mTimeMin);
                request.setTimeMax(mTimeMax);
                request.setItems(items);

                Calendar.Freebusy.Query query = freebusy.query(request);

                FreeBusyResponse response = query.execute();
                Map<String, FreeBusyCalendar> responseItems = response.getCalendars();
                Iterator it = responseItems.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    FreeBusyCalendar calendar = (FreeBusyCalendar) pair.getValue();
                    return calendar.getBusy();
                }
            } catch (UserRecoverableAuthIOException e) {
                Log.e(LOG_TAG, "Error :", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error :", e);
            }
        }
        return null;
    }

    @Override
    public void onPostExecute(List<TimePeriod> timePeriods) {
        handleTimePeriods(timePeriods);
    }
}
