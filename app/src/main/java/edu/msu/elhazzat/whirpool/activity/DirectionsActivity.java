package edu.msu.elhazzat.whirpool.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.geojson.AsyncParseGeoJsonFromResource;
import edu.msu.elhazzat.whirpool.geojson.GeoJson;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonFeature;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonPolygon;

/**
 * Created by christianwhite on 10/1/15.
 */
public class DirectionsActivity extends BaseGoogleMapsActivity {
    private String mRoomName = null;
    private Marker mDestinationMarker = null;
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

      /* mCurrentLocationImageView = (ImageView) findViewById(R.id.location2);
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
        SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = mMapFragment.getMap();
        if (mMap != null) {
            final GeoJsonMap jsonMap = new GeoJsonMap(mMap);
            final GeoJsonMapLayer layer = new GeoJsonMapLayer();
            mDestinationMarker = mMap.addMarker( new MarkerOptions().position(new LatLng(40.11256185048872, -80.46901868283749)));
            mDestinationMarker.setDraggable(true);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {

                    new AsyncParseGeoJsonFromResource(getApplicationContext(), R.raw.riverviewfloor1) {
                        @Override
                        public void handleGeoJson(GeoJson json) {
                            layer.setGeoJson(json);
                            layer.draw(mMap, Color.rgb(255, 249, 236), Color.rgb(108, 122, 137), 3);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(42.11256185048872, -86.46901868283749), 19);
                            for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                                if (feature.getGeoJsonGeometry().getType().equals("Polygon")) {
                                    GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                                    if(polygon.contains(new LatLng(42.11256185048872, -86.46901868283749))) {
                                        Polygon gmsPoly = polygon.getGMSPolygon();
                                        int color1 = Color.rgb(137, 196, 244);
                                        gmsPoly.setFillColor(color1);
                                    }
                                }
                            }
                            mMap.animateCamera(cameraUpdate);
                            //   Marker marker = mMap.addMarker(new MarkerOptions().
                            //         position(new LatLng(42.11256185048872, -86.46901868283749)));
                        }
                    }.execute();
                }
            });


            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                        if (feature.getGeoJsonGeometry().getType().equals("Polygon")) {
                            GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                            if (polygon.contains(latLng)) {
                                //      Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                                for (GeoJsonFeature feature2 : layer.getGeoJson().getGeoJsonFeatures()) {
                                    int color2 = Color.rgb(255, 246, 236);
                                    if (feature2.getProperty("room").equals("B250") || feature.getProperty("room").equals("B205") ||
                                            feature.getProperty("room").equals("B218") || feature.getProperty("room").equals("B217")) {
                                        color2 = Color.rgb(234, 230, 245);
                                    } else if (feature2.getProperty("room").equals("B241") ||
                                            feature2.getProperty("room").equals("B234") ||
                                            feature2.getProperty("room").equals("B219") ||
                                            feature2.getProperty("room").equals("B251") ||
                                            feature2.getProperty("room").equals("B230")) {

                                        color2 = Color.WHITE;
                                    } else if (feature2.getProperty("room").equals("B236") ||
                                            feature2.getProperty("room").equals("B232") ||
                                            feature2.getProperty("room").equals("B223") ||
                                            feature2.getProperty("room").equals("B247") ||
                                            feature2.getProperty("room").equals("B233-229") ||
                                            feature2.getProperty("room").equals("B235-238") ||
                                            feature2.getProperty("room").equals("B245-248") ||
                                            feature2.getProperty("room").equals("B222-220")) {
                                        color2 = Color.WHITE;
                                    }
                                    ((GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry()).getGMSPolygon().setFillColor(color2);
                                }
                                Polygon gmsPoly = polygon.getGMSPolygon();
                                int color1 = Color.rgb(137, 196, 244);
                                gmsPoly.setFillColor(color1);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
