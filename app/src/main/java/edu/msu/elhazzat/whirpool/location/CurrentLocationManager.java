package edu.msu.elhazzat.whirpool.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.LocationCallback;

/**
 * Created by Christian White on 9/30/15.
 */
public abstract class CurrentLocationManager {

    private static final String LOG_TAG = CurrentLocationManager.class.getSimpleName();

    public abstract void handleLocationUpdate(Location location);


    private Context mContext;
    private LocationManager mLocationManager;
    private String mProvider;
    private Location mLastLocation;
    private LocationListener mLocationListener;
    private LocationCallback mDelegate;

    public CurrentLocationManager(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        setProvider();
    }

    private void setProvider() {
        Criteria criteria = new Criteria();
        mProvider = mLocationManager.getBestProvider(criteria, true);
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                handleLocationUpdate(location);
            }
            public void onStatusChanged(String str, int val, Bundle state) {}
            public void onProviderEnabled(String str) {}
            public void onProviderDisabled(String str) {}
        };

        if(mLastLocation!=null){
            handleLocationUpdate(mLastLocation);
        }
        mLocationManager.requestLocationUpdates(mProvider, 0, 0, mLocationListener);
    }

   /* public Location getCurrentLocation() {
        return mLocationManager.getLastKnownLocation(mProvider);
    }*/
}