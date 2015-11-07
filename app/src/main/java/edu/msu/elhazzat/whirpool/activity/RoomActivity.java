package edu.msu.elhazzat.whirpool.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.util.HashMap;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.RoomAttributeAdapter;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonFeature;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonPolygon;
import edu.msu.elhazzat.whirpool.model.EventModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSRoomInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncParseGeoJsonGCS;
import edu.msu.elhazzat.whirpool.utils.RoomNameRegexMapper;
import edu.msu.elhazzat.whirpool.utils.WIMAppConstants;

/**
 * Created by Stephanie on 10/1/2015.
 */
public class RoomActivity extends BaseGoogleMapsActivity {
    private ListView roomListView;
    private HashMap<String, List<RoomAttributeModel>> mAttributes = new HashMap<>();
    private RoomAttributeAdapter arrayAdapter;
    TextView roomTextView;

    private GeoJsonMap mGeoJsonMap;

    private Marker mCurrentMarker;

    private EventModel mEvent;
    private String mBuildingName;
    private String mRoomName;
    private String mGeoJsonRoomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mEvent = (EventModel) b.getParcelable("EVENT");
        }

        if(mEvent != null && mEvent.getLocation() != null) {
            String location = mEvent.getLocation();
            mBuildingName = RoomNameRegexMapper.getBuildingNameFromResource(location);
            mRoomName = RoomNameRegexMapper.getRoomNameFromResource(location);
            mGeoJsonRoomName = RoomNameRegexMapper.getGeoJsonRoomNameFromMap(mBuildingName, mRoomName);
        }

        new AsyncParseGeoJsonGCS(WIMAppConstants.WHIRLPOOL_ABBRV_MAP.get(mBuildingName)) {
            public void handleGeoJson(GeoJsonMap map) {
                mGeoJsonMap = map;
                setUpMap();
            }
        }.execute();


        roomTextView = (TextView) findViewById(R.id.roomNameText);
        roomTextView.setTextColor(Color.BLUE);
        roomTextView.setText(mRoomName);

        roomListView = (ListView) findViewById(R.id.roomInfoList);}

    public void inflateRoomAttributes(String geoJsonRoomName) {
        String buildingAbbr = WIMAppConstants.WHIRLPOOL_ABBRV_MAP.get(mBuildingName);
        AsyncGCSRoomInfoReader reader = new AsyncGCSRoomInfoReader(buildingAbbr, geoJsonRoomName) {
            @Override
            public void handleRoom(RoomModel room) {
                if(room != null) {
                    String[] attributes = room.getmAmenities();
                }
            }
        };
        reader.execute();
    }

    public void setUpMap() {
        if (mMap == null) {
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);

            if (mMap != null) {
                mGeoJsonMap.setMap(mMap);
                loadMap();
                mapListen();
            }
        }
    }

    private void loadMap() {
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
                mGeoJsonMap.drawLayer(2, Color.rgb(255, 249, 236), Color.rgb(108, 122, 137), 3);

                LatLng center = getRoomCenter(layer, mGeoJsonRoomName);
                LatLng centerRev = new LatLng(center.longitude, center.latitude);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(centerRev,
                        WIMAppConstants.DEFAULT_MAP_CAMERA_ZOOM);

                for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                    if (feature.getGeoJsonGeometry().getType().equals("Polygon")) {
                        GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                        if (polygon.contains(centerRev)) {
                            Polygon gmsPoly = polygon.getGMSPolygon();
                            gmsPoly.setFillColor(WIMAppConstants.MAP_SELECTED_ROOM_COLOR);
                        }
                    }
                }
                mMap.moveCamera(cameraUpdate);
                mCurrentMarker = mMap.addMarker(new MarkerOptions().position(centerRev));
            }
        });
    }

    private LatLng getRoomCenter(GeoJsonMapLayer layer, String roomId) {
        for(GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
            if(feature.getGeoJsonGeometry().getType().equals("Polygon") &&
                    feature.getProperty("room").equals(roomId)) {

                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                return polygon.getCentroid();
            }
        }
        return null;
    }

    private void mapListen() {
        final GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for(GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                    if(feature.getGeoJsonGeometry().getType().equals("Polygon")) {
                        GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                        if(polygon.contains(latLng)) {

                            mCurrentMarker.remove();

                            Polygon gmsPoly = polygon.getGMSPolygon();
                            int color1 = WIMAppConstants.MAP_SELECTED_ROOM_COLOR;

                            gmsPoly.setFillColor(color1);
                            LatLng center1 = polygon.getCentroid();

                            mCurrentMarker = mMap.addMarker(new MarkerOptions().position(
                                    new LatLng(center1.longitude, center1.latitude)
                            ));

                            roomTextView.setText(feature.getProperty("room"));

                            String key = feature.getProperty("room");

                            if(key != null) {
                                List<RoomAttributeModel> attributes = mAttributes.get(key);
                                if(attributes != null && attributes.size() > 0) {
                                    arrayAdapter = new RoomAttributeAdapter(getApplicationContext(), attributes); //new ArrayAdapter(getApplicationContext(), R.layout.room_simple_text, mCurrentRoomAttributes);
                                    roomListView.setAdapter(arrayAdapter);
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}