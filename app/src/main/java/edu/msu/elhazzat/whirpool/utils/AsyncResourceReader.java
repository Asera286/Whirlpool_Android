package edu.msu.elhazzat.whirpool.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by christianwhite on 10/18/15.
 */
public class AsyncResourceReader extends AsyncTask<Void, Void, Void> {
    public static final String mUrl = "https://webdev.cse.msu.edu/~elhazzat/wim/room-load.php";


    @Override
    public Void doInBackground(Void... params) {
        try {

            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));
                String line = null;
                StringBuilder responseBuilder = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }
        }
        catch(ProtocolException e) {
            Log.e("TAG", "Error :", e);
        }
        catch(IOException e) {
            Log.e("TAG", "Error :", e);
        }

        return null;
    }
}
