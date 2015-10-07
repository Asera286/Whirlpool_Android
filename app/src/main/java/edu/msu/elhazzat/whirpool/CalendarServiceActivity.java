package edu.msu.elhazzat.whirpool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;

/**
 * Created by christianwhite on 10/6/15.
 */
public abstract class CalendarServiceActivity extends Activity {

    public static final int COMPLETE_AUTHORIZATION_REQUEST_CODE = 1001;
    public static final String APPLICATION_NAME = "Whirlpool Indoor Maps";

    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory mJsonFactory = JacksonFactory.getDefaultInstance();

    protected com.google.api.services.calendar.Calendar mService;
    protected GoogleAccountCredential mCredential;

    public abstract void onCalendarPermissionAuthorized();
    public abstract void onCalendarPermissionDenied();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case COMPLETE_AUTHORIZATION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    onCalendarPermissionAuthorized();
                } else {
                    onCalendarPermissionDenied();
                }
                break;
        }
    }

    protected void buildCalendarService() {
        mService = new com.google.api.services.calendar.Calendar.Builder (
                mTransport, mJsonFactory, mCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    protected void buildCredential(String accountName) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                this, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(accountName);
    }
}
