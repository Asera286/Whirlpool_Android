package edu.msu.elhazzat.whirpool;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by christianwhite on 10/8/15.
 */
public class IndoorGeoJsonResponse {
    private Context mContext;

    IndoorGeoJsonResponse(Context context) {
        mContext = context;
    }

    public IndoorGeoJson getIndoorGeoJsonFromAsset(int resourceId) {
        Gson gson = new Gson();
        InputStream stream = mContext.getResources().openRawResource(resourceId);
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        return gson.fromJson(reader, IndoorGeoJson.class);
    }

    public IndoorGeoJson getIndoorGeoJsonFromDrive() {
        return null;
    }
}


