package edu.msu.elhazzat.whirpool;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by christianwhite on 10/11/15.
 */
public class AsyncCalendarResourceReader extends AsyncTask<Void, Void, Document> {

    public interface AsyncCalendarResourceReaderDelegate {
        public void handleResourceXml(Document xml);
    }

    private static final String LOG_TAG = AsyncCalendarResourceReader.class.getSimpleName();

    private AsyncCalendarResourceReaderDelegate mDelegate;
    private String mUrl;
    private String mToken;

    AsyncCalendarResourceReader(AsyncCalendarResourceReaderDelegate delegate, String url, String token) {
        mDelegate = delegate;
        mUrl = url;
        mToken = token;
    }

    @Override
    public Document doInBackground(Void... params) {
        try {
            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + mToken);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));
                String responseXml = reader.readLine();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document d1 = builder.parse(new InputSource(new StringReader(responseXml)));
                    return d1;
                }catch(ParserConfigurationException e) {
                    Log.e(LOG_TAG, "Error: ", e);
                }
                catch(SAXException e) {
                    Log.e(LOG_TAG, "Error: ", e);
                }
            }
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error: ", e);
        }

        return null;
    }

    @Override
    public void onPostExecute(Document xmlDoc) {
        mDelegate.handleResourceXml(xmlDoc);
    }
}