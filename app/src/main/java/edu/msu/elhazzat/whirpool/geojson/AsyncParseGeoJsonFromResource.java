package edu.msu.elhazzat.whirpool.geojson;

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

/**
 * Given a raw resource id, parse geojson into geojson object
 */
public abstract class AsyncParseGeoJsonFromResource extends AsyncTask<Void, Void, GeoJson> {
    public abstract void handleGeoJson(GeoJson json);

    private Context mContext;
    private int mResourceId;

    public AsyncParseGeoJsonFromResource(Context context, int resourceId) {
        mContext = context;
        mResourceId = resourceId;
    }

    @Override
    public GeoJson doInBackground(Void... params) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(GeoJsonGeometry.class,
                new GeoJsonGeometryDeserializer());
        Gson gson = gsonBuilder.create();

        InputStream stream = mContext.getResources().openRawResource(mResourceId);
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        return gson.fromJson(reader, GeoJson.class);
    }

    @Override
    public void onPostExecute(GeoJson json) {
        handleGeoJson(json);
    }
}