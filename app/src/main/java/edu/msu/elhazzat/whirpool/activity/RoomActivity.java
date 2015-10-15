package edu.msu.elhazzat.whirpool.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonFeature;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonPolygon;

/**
 * Created by Stephanie on 10/1/2015.
 */
public class RoomActivity extends BaseGoogleMapsActivity {
    private String roomName;
    private String[] roomsDummyInfo = {"Projector", "Fridge", "Blah", "More Blah", "Other Stuff", "More Stuff", "Just Stuff"};
    private ListView roomListView;
    private ArrayAdapter arrayAdapter;
    TextView roomTextView;

    private Button mDirectionsButton;
    private Button mBookRoomButton;
    private Button mFavoritesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        Bundle b = getIntent().getExtras();
        if(b!=null) {
            roomName = b.getString("ROOM_ID");
        }

        mDirectionsButton = (Button)findViewById(R.id.button);
        mDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent directionsIntent = new Intent(getApplicationContext(), DirectionsActivity.class);
                startActivity(directionsIntent);
            }
        });

        mBookRoomButton = (Button)findViewById(R.id.button2);
        mBookRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(getApplicationContext(), CreateEventActivity.class);
                startActivity(eventIntent);
            }
        });

        mFavoritesButton = (Button)findViewById(R.id.button3);
        mFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        setUpMap();

        roomTextView = (TextView) findViewById(R.id.roomNameText);
        roomTextView.setText(roomName);

        roomListView = (ListView) findViewById(R.id.roomInfoList);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, roomsDummyInfo);
        roomListView.setAdapter(arrayAdapter);
    }

    public void setUpMap() {
        if (mMap == null) {
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);

            if (mMap != null) {
                final GeoJsonMap jsonMap = new GeoJsonMap(mMap);
                final GeoJsonMapLayer layer = new GeoJsonMapLayer();

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                            if (feature.getGeoJsonGeometry().getType().equals("Polygon")) {
                                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                                if (polygon.contains(latLng)) {
                                    Polygon gmsPoly = polygon.getGMSPolygon();
                                    for(Polygon polygon2 : layer.getPolygons()) {
                                        polygon2.setFillColor(Color.GRAY);
                                    }
                                    gmsPoly.setFillColor(Color.RED);
                                }
                            }
                        }
                    }
                });
            }
        }
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