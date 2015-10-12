package edu.msu.elhazzat.whirpool;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by christianwhite on 9/30/15.
 */
public class MapsActivity extends FragmentActivity {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private CurrentLocationManager mCurrentLocationManager;
    private static final String MARKER_NAME = "ME";
    private Switch mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    public void setUpMapCamera(LatLng latLng, int zoomLevel) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        mMap.animateCamera(cameraUpdate);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);

            if (mMap != null) {
                final GeoJsonMap jsonMap = new GeoJsonMap(mMap);
                final GeoJsonMapLayer layer = new GeoJsonMapLayer();
                GeoJson json = new GeoJsonParser(this).getGeoJsonFromResource(R.raw.geojsonrooms);
                layer.setFeatures(json.getFeatures());
                jsonMap.addLayer(0, layer);
                jsonMap.drawLayer(0, 1, Color.BLACK);
    /*            WpGeoJsonResponse gsonResponse = new WpGeoJsonResponse(this);

                final GeoJson json = gsonResponse.getIndoorGeoJsonFromAsset(R.raw.geojsonrooms);
                final List<GeoJsonFeature> features = json.getFeatures();*/

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        for (GeoJsonFeature feature : jsonMap.getCurrentLayer().getFeatures()) {
                            GeoJsonGeometry geometry = feature.getGeometry();
                            if (geometry.getType().equals("Polygon")) {
                                PolygonCoordinates coordinates = (PolygonCoordinates)geometry.getCoordinates();
                                GeometryPolygon polygon = new GeometryPolygon(coordinates);
                                if (polygon.contains(latLng)) {
                                    String room = feature.getProperty("room");
                                    Toast.makeText(getApplicationContext(), room, Toast.LENGTH_SHORT).show();
                                }
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
