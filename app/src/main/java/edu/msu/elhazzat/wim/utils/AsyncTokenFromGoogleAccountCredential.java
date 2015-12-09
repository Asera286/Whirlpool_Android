package edu.msu.elhazzat.wim.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

/**
 * Created by christianwhite on 10/14/15.
 */

/*************************************************************************
 * Asynctask for extracting token from a google account credential-
 * this will be used for google api calls not available from google
 * play
 **************************************************************************/
public abstract class AsyncTokenFromGoogleAccountCredential extends AsyncTask<Void, Void, String> {
    private static final String LOG_TAG = AsyncTokenFromGoogleAccountCredential.class.getSimpleName();
    private GoogleAccountCredential mCredential;

    public AsyncTokenFromGoogleAccountCredential(GoogleAccountCredential credential) {
        mCredential = credential;
    }

    public abstract void handleToken(String token);

    @Override
    public String doInBackground(Void... params) {
        try {
            return mCredential.getToken();
        }
        catch(GoogleAuthException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        catch(IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return null;
    }

    @Override
    public void onPostExecute(String token) {
        handleToken(token);
    }
}