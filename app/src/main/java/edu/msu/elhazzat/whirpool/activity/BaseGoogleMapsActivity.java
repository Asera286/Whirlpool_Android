package edu.msu.elhazzat.whirpool.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import edu.msu.elhazzat.whirpool.location.CurrentLocationManager;

/**
 * Created by christianwhite on 9/30/15.
 */
abstract class BaseGoogleMapsActivity extends FragmentActivity {
    protected GoogleMap mMap;
    protected CurrentLocationManager mCurrentLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setUpMapCamera(LatLng latLng, int zoomLevel) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        mMap.animateCamera(cameraUpdate);
    }

    public abstract void setUpMap();
}
