package edu.msu.elhazzat.whirpool;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by christianwhite on 10/11/15.
 */
public class AsyncParseGeoJsonFromResource extends AsyncTask<Void, Void, GeoJson> {

    public interface AsyncParseGeoJsonFromResourceDelegate {
        public void handleGeoJson(GeoJson geoJson);
    }

    private Context mContext;
    private AsyncParseGeoJsonFromResourceDelegate mDelegate;
    private int mResourceId;

    AsyncParseGeoJsonFromResource(Context context, AsyncParseGeoJsonFromResourceDelegate delegate,
                                  int resourceId) {
        mContext = context;
        mDelegate = delegate;
        mResourceId = resourceId;
    }

    @Override
    public GeoJson doInBackground(Void... params) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BaseCoordinates.class,
                new GeoJsonCoordinatesDeserializer());
        Gson gson = gsonBuilder.create();

        InputStream stream = mContext.getResources().openRawResource(mResourceId);
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        return gson.fromJson(reader, GeoJson.class);
    }

    @Override
    public void onPostExecute(GeoJson json) {
        mDelegate.handleGeoJson(json);
    }

}