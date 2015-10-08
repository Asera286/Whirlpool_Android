package edu.msu.elhazzat.whirpool;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geojson.GeoJsonLayer;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private CurrentLocationManager mCurrentLocationManager;
    private static final String MARKER_NAME = "ME";
    private GeoJsonLayer mLayer = null;

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

            if (mMap != null) {
                try {
                    mLayer = new GeoJsonLayer(mMap, R.raw.geojsonrooms, getApplicationContext());
                }
                catch(JSONException e) {
                    //TODO: LOG
                }
                catch(IOException e) {
                    //TODO: LOG
                }

                mLayer.addLayerToMap();
                WpGeoJsonResponse gsonResponse = new WpGeoJsonResponse(this);

                final WpGeoJson json = gsonResponse.getWpGeoJsonFromAsset(R.raw.geojsonrooms);
                final List<WpGeoJsonFeature> features = json.getFeatures();

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Point point = new Point(latLng.longitude, latLng.latitude);
                        for (WpGeoJsonFeature feature : features) {
                            WpGeoJsonGeometry geometry = feature.getGeometry();
                            if (geometry.contains(point)) {
                                 String room = feature.getProperty("room");
                            }
                        }
                    }
                });
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
