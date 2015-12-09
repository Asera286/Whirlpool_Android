package edu.msu.elhazzat.wim.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;

import edu.msu.elhazzat.wim.utils.CalendarServiceHolder;

/**
 * Created by christianwhite on 10/1/15.
 */
public abstract class CalendarServiceActivity extends AppCompatActivity {
    private static final String LOG_TAG = CalendarServiceActivity.class.getSimpleName();

    public static final String APPLICATION_NAME = "Whirlpool Indoor Maps";
    protected static final int COMPLETE_AUTHORIZATION_REQUEST_CODE = 1001;

    public static final String RESOURCE_SCOPE = "https://apps-apis.google.com/a/feeds/calendar/resource/";

    private static final String[] SCOPES = { CalendarScopes.CALENDAR , RESOURCE_SCOPE };

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

    /**
     * Called to resolve authorization on google calendar api scopes
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    /**
     * Builds a google calendar service from a valid oauth2 credential
     *
     */
    protected void buildCalendarService() {
        mService = new com.google.api.services.calendar.Calendar.Builder (
                mTransport, mJsonFactory, mCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Set service for application wide use
        CalendarServiceHolder.getInstance().setService(mService);
    }

    /**
     * Acquire oauth2 credential from current user
     *
     * @param accountName
     */
    protected void buildCredential(String accountName) {
        mCredential = GoogleAccountCredential.usingOAuth2(
                this, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(accountName);
    }
}
