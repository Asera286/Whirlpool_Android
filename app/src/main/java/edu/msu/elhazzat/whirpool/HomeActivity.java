package edu.msu.elhazzat.whirpool;

import android.content.Intent;
import android.os.Bundle;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Christian on 9/27/2015.
 */
public class HomeActivity extends AccountServiceActivity implements AsyncCalendarReaderDelegate {

    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory mJsonFactory = JacksonFactory.getDefaultInstance();

    private com.google.api.services.calendar.Calendar mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorizeGoogleApiOauth(Arrays.asList(SCOPES));
        buildCalendarService();
        if(mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        }
        new AsyncCalendarEventReader(this, mService, new DateTime(System.currentTimeMillis()), 10).execute();
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    private void buildCalendarService() {
        mService = new com.google.api.services.calendar.Calendar.Builder(
                mTransport, mJsonFactory, mCredential)
                .setApplicationName(AccountServiceActivity.APPLICATION_NAME)
                .build();
    }

    public void onAsyncFinished(List<Event> events) {

    }
}
