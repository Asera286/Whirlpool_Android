package edu.msu.elhazzat.whirpool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.util.List;

/**
 * Created by Christian on 9/27/2015.
 */
public class HomeActivity extends CalendarServiceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accountName = getIntent().getExtras().getString("accountName");

        super.buildCredential(accountName);
        super.buildCalendarService();
        Intent mapsIntent = new Intent(this, MapsActivity.class);
        startActivity(mapsIntent);

      //  new AsyncCalendarEventReader(new HandleAsyncCalendarReader(), mService, new DateTime(System.currentTimeMillis()), 10).execute();
    }

    public void onCalendarPermissionAuthorized() {
        new AsyncCalendarEventReader(new HandleAsyncCalendarReader(), mService, new DateTime(System.currentTimeMillis()), 10).execute();
    }

    public void onCalendarPermissionDenied() {

    }

    public class HandleAsyncCalendarReader implements AsyncCalendarEventReader.AsyncCalendarReaderDelegate {
        public void onAsyncFinished(List<Event> events) {
            if (events != null) {
                for(Event event: events) {
                    Toast.makeText(getApplicationContext(), event.getSummary(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void handleUserRecoverableAuthIOException(UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), COMPLETE_AUTHORIZATION_REQUEST_CODE);
        }
    }
}
