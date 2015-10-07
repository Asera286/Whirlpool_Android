package edu.msu.elhazzat.whirpool;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geojson.GeoJsonLayer;

import org.json.JSONException;

import java.io.IOException;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private CurrentLocationManager mCurrentLocationManager;
    private static final String MARKER_NAME = "ME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);
            //mCurrentLocationManager = new CurrentLocationManager(this, new MapLocationUpdater());
            if (mMap != null) {
             //   Location current = mCurrentLocationManager.getCurrentLocation();
                GeoJsonLayer layer = null;
                try {
                    layer = new GeoJsonLayer(mMap, R.raw.geojsonrooms, getApplicationContext());
                }
                catch(JSONException e) {
                    //TODO: LOG
                }
                catch(IOException e) {
                    //TODO: LOG
                }
                layer.addLayerToMap();
                boolean test = layer.isLayerOnMap();
                Toast.makeText(this, Boolean.toString(test), Toast.LENGTH_LONG).show();
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current.getLatitude(), current.getLongitude()), 15));
              //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-84.481922381275,42.730473287925), 15));
            }
        }
    }

    public class MapLocationUpdater implements CurrentLocationManager.LocationCallback {
        public void handleLocationUpdate(Location location) {
            mMap.clear();
            LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(MARKER_NAME));
        }
    }

}
