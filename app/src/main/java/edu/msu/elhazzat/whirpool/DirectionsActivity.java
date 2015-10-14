package edu.msu.elhazzat.whirpool;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.maps.SupportMapFragment;

public class DirectionsActivity extends BaseGoogleMapsActivity {
    private String mRoomName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        Bundle b = getIntent().getExtras();
        if(b!=null) {
            mRoomName = b.getString("ROOM_ID");
        }

        setUpMap();
    }

    public void setUpMap() {
        if (mMap == null) {
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);

            if (mMap != null) {
                mCurrentLocationManager = new CurrentLocationManager(this, new CurrentLocationManager.LocationCallback() {
                    @Override
                    public void handleLocationUpdate(Location location) {

                    }
                });
                //get current location
                //use directions to map to building
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
