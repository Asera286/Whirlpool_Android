package edu.msu.elhazzat.whirpool.activity;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.maps.SupportMapFragment;

import edu.msu.elhazzat.whirpool.R;

/**
 * Created by christianwhite on 10/1/15.
 */
public class DirectionsActivity extends BaseGoogleMapsActivity {
    private String mRoomName = null;
    //private ImageView mCurrentLocationImageView = null;
    //private ImageView mFromLandmarkImageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        Bundle b = getIntent().getExtras();
        if(b!=null) {
            mRoomName = b.getString("ROOM_ID");
        }

      /*  mCurrentLocationImageView = (ImageView) findViewById(R.id.location2);
        mCurrentLocationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentLocationManager = new CurrentLocationManager(getApplicationContext()) {
                    @Override
                    public void handleLocationUpdate(Location location) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()), 15);
                        mMap.animateCamera(cameraUpdate);
                    }
                };
            }
        });

        mFromLandmarkImageView = (ImageView) findViewById(R.id.landmark);*/

        setUpMap();
    }

    public void setUpMap() {
        if (mMap == null) {
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
