package edu.msu.elhazzat.whirpool;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


public class WpGeoJsonResponse {
    private Context mContext;

    WpGeoJsonResponse(Context context) {
        mContext = context;
    }

    public WpGeoJson getWpGeoJsonFromAsset(int resourceId) {
        Gson gson = new Gson();
        InputStream stream = mContext.getResources().openRawResource(resourceId);
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        return gson.fromJson(reader, WpGeoJson.class);
    }
}


