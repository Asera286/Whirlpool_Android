package edu.msu.elhazzat.whirpool.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;

import java.util.Calendar;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarFreeBusyReader;
import edu.msu.elhazzat.whirpool.crud.RelevantRoomDbHelper;
import edu.msu.elhazzat.whirpool.geojson.AsyncParseGeoJsonFromResource;
import edu.msu.elhazzat.whirpool.geojson.GeoJson;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonFeature;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonPolygon;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.utils.CalendarServiceHolder;

/**
 * Created by Stephanie on 10/1/2015.
 */
public class RoomActivity extends BaseGoogleMapsActivity {
    private String roomName;
    private String roomEmail;
    private String[] roomsDummyInfo = {"Capacity 26", "Projector", "Whiteboard", "Television" };
    private ListView roomListView;
    private ArrayAdapter arrayAdapter;
    TextView roomTextView;

    private ImageView mDirectionsButton;
    private ImageView mBookRoomButton;
    private ImageView mFavoritesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            roomName = b.getString("ROOM_ID");
            roomEmail = b.getString("ROOM_EMAIL");
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 2015);
        c.set(Calendar.MONTH, Calendar.OCTOBER);
        c.set(Calendar.DAY_OF_MONTH, 10);

        Calendar d = Calendar.getInstance();
        d.set(Calendar.YEAR, 2015);
        d.set(Calendar.MONTH, Calendar.NOVEMBER);
        d.set(Calendar.DAY_OF_MONTH, 10);

        new AsyncCalendarFreeBusyReader(CalendarServiceHolder.getInstance().getService(),
                roomEmail, new DateTime(c.getTime()), new DateTime(d.getTime())) {
            @Override
            public void handleTimePeriods(List<TimePeriod> time) {
                for(TimePeriod period : time) {
                    period.getStart();
                    period.getEnd();
                }
            }
        }.execute();

        mDirectionsButton = (ImageView)findViewById(R.id.directions);
        mDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent directionsIntent = new Intent(getApplicationContext(), DirectionsActivity.class);
                startActivity(directionsIntent);
            }
        });

        mBookRoomButton = (ImageView)findViewById(R.id.bookmark);
        mBookRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(getApplicationContext(), CreateEventActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ROOM_ID", roomName);
                bundle.putString("ROOM_EMAIL", roomEmail);
                eventIntent.putExtras(bundle);
                startActivity(eventIntent);
            }
        });

        mFavoritesButton = (ImageView)findViewById(R.id.favorite);
        mFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelevantRoomDbHelper helper = new RelevantRoomDbHelper(getApplicationContext());
                RoomModel model = new RoomModel();
                model.setRoomName(roomName);
                model.setEmail(roomEmail);
                helper.addRelevantRoom(model);
                Toast.makeText(getApplicationContext(), "Successfully favorited!", Toast.LENGTH_LONG).show();
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
            //
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);

            if (mMap != null) {
                final GeoJsonMap jsonMap = new GeoJsonMap(mMap);
                final GeoJsonMapLayer layer = new GeoJsonMapLayer();
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        new AsyncParseGeoJsonFromResource(getApplicationContext(), R.raw.riverviewfloor1) {
                            @Override
                            public void handleGeoJson(GeoJson json) {
                                layer.setGeoJson(json);
                                layer.draw(mMap, Color.rgb(255, 249, 236), Color.rgb(108, 122, 137), 3);
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(42.11256185048872, -86.46901868283749), 20);
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