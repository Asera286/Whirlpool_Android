package edu.msu.elhazzat.whirpool.utils;

import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

/**
 * Created by christianwhite on 10/14/15. AsyncTokenFromGoogleAccountCredential
 */
public abstract class AsyncTokenFromGoogleAccountCredential extends AsyncTask<Void, Void, String> {
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

        }
        catch(IOException e) {

        }
        return null;
    }

    @Override
    public void onPostExecute(String token) {
        handleToken(token);
    }
}