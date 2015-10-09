package edu.msu.elhazzat.whirpool;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.geojson.GeoJsonLayer;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by christianwhite on 9/30/15.
 */
public class MapsActivity extends FragmentActivity {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private CurrentLocationManager mCurrentLocationManager;
    private static final String MARKER_NAME = "ME";
    private HashMap<Integer, GeoJsonLayer> mLayers = new HashMap<>();
    private Switch mSwitch;
    private int mCurrentRoomKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    public void createLayerFromResource(int floorNumber, int resourceId) {
        GeoJsonLayer layer = null;
        try {
            layer = new GeoJsonLayer(mMap, R.raw.geojsonrooms, getApplicationContext());
        }
        catch(JSONException e) {
            Log.e(LOG_TAG, "Error: ", e);

        }
        catch(IOException e) {
            Log.e(LOG_TAG, "Error: ", e);
        }
        mLayers.put(floorNumber, layer);
        layer.setMap(mMap);
    }

    public void createLayerFromDrive() {
        return;
    }

    public void removeLayerByFloor(int floorNumber) {
        mLayers.get(floorNumber).removeLayerFromMap();
        boolean test = mLayers.get(floorNumber).isLayerOnMap();
        Log.i(LOG_TAG, "HERE");
    }

    public void addLayerByFloor(int floorNumber, int resourceId) {
        drawFeaturesFromResource(resourceId);
   //     mLayers.get(floorNumber).addLayerToMap();

    }

    public void setUpMapCamera(LatLng latLng, int zoomLevel) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        mMap.animateCamera(cameraUpdate);
    }

    public void drawFeaturesFromResource(int resourceId) {
        IndoorGeoJsonResponse gsonResponse = new IndoorGeoJsonResponse(this);
        IndoorGeoJson json = gsonResponse.getIndoorGeoJsonFromAsset(resourceId);
        List<IndoorGeoJsonFeature> features = json.getFeatures();
        for (IndoorGeoJsonFeature feature : features) {
            IndoorGeoJsonGeometry geometry = feature.getGeometry();
            if (geometry.getType().equals("Polygon")) {
                WpPolygon polygon = new WpPolygon(geometry.getCoordinates().get(0));
                Polygon polygon2 = mMap.addPolygon(new PolygonOptions()
                        .addAll(polygon.getLatLngList())
                        .strokeColor(Color.RED)
                        .strokeWidth(1));
            }
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);

            if (mMap != null) {
                createLayerFromResource(0, R.raw.geojsonrooms);
                createLayerFromResource(1, R.raw.geojson);
                mSwitch = (Switch)findViewById(R.id.switch1);
                mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                         //   removeLayerByFloor(1);
                            addLayerByFloor(1, R.raw.geojsonrooms);
                        }
                        else {
                         //   removeLayerByFloor(0);
                         //   addLayerByFloor(1);
                        }
                    }
                });
/*                IndoorGeoJsonResponse gsonResponse = new IndoorGeoJsonResponse(this);

                final IndoorGeoJson json = gsonResponse.getIndoorGeoJsonFromAsset(R.raw.geojsonrooms);
                final List<IndoorGeoJsonFeature> features = json.getFeatures();

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        for (IndoorGeoJsonFeature feature : features) {
                            IndoorGeoJsonGeometry geometry = feature.getGeometry();
                            if (geometry.getType().equals("Polygon")) {
                                WpPolygon polygon = new WpPolygon(geometry.getCoordinates().get(0));
                                if (polygon.contains(latLng)) {
                                    String room = feature.getProperty("room");
                                }
                            }
                        }
                    }
                });*/

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
