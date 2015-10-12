package edu.msu.elhazzat.whirpool;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by christianwhite on 10/11/15.
 */
public class GeoJsonParser {
    private Context mContext;

    GeoJsonParser(Context context) {
        mContext = context;
    }

    public GeoJson getGeoJsonFromResource(int resourceId) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BaseGeoJsonShape.class,
                new GeoJsonCoordinatesDeserializer());
        Gson gson = gsonBuilder.create();

        InputStream stream = mContext.getResources().openRawResource(resourceId);
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        return gson.fromJson(reader, GeoJson.class);
    }
}
