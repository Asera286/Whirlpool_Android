package edu.msu.elhazzat.whirpool;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by christianwhite on 9/30/15.
 */
abstract class BaseGoogleMapsActivity extends FragmentActivity {
    protected GoogleMap mMap;
    protected CurrentLocationManager mCurrentLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setUpMap();
    }

    public void setUpMapCamera(LatLng latLng, int zoomLevel) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        mMap.animateCamera(cameraUpdate);
    }

    public abstract void setUpMap();
     /*   if (mMap == null) {
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);

            if (mMap != null) {
                final GeoJsonMap jsonMap = new GeoJsonMap(mMap);
                final GeoJsonMapLayer layer = new GeoJsonMapLayer();
                GeoJson json = new GeoJsonParser(this).getGeoJsonFromResource(R.raw.geojsonrooms);
                layer.setFeatures(json.getFeatures());
                jsonMap.addLayer(0, layer);
                jsonMap.drawLayer(0, 2, Color.BLACK);

                 mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        for (GeoJsonFeature feature : jsonMap.getCurrentLayer().getFeatures()) {
                            GeoJsonGeometry geometry = feature.getGeometry();
                            if (geometry.getType().equals("Polygon")) {
                                GeoJsonPolygon polygon = (GeoJsonPolygon)geometry.getCoordinates();
                                if (polygon.contains(latLng)) {
                                    Polygon gmsPoly = polygon.getGMSPolygon();
                                    gmsPoly.setFillColor(Color.RED);
                                    String room = feature.getProperty("room");
                                }
                            }
                        }
                    }
                });

            }
        }*/
    //}

 /*   public class MapLocationUpdater implements CurrentLocationManager.LocationCallback {
        public void handleLocationUpdate(Location location) {
            mMap.clear();
            LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(MARKER_NAME));
        }
    }*/
}
