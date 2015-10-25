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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import edu.msu.elhazzat.whirpool.utils.AsyncResourceReader;
import edu.msu.elhazzat.whirpool.utils.CalendarServiceHolder;

/**
 * Created by Stephanie on 10/1/2015.
 */
public class RoomActivity extends BaseGoogleMapsActivity {
    private String roomName;
    private String roomEmail;
    private ListView roomListView;
    private HashMap<String, List<String>> mAttributes = new HashMap<>();
    private ArrayAdapter arrayAdapter;
    TextView roomTextView;
    private String[] mCurrentRoomAttributes;

    private ImageView mDirectionsButton;
    private ImageView mBookRoomButton;
    private ImageView mFavoritesButton;

    private GeoJsonMap mGeoJsonMap;
    private String mCurrentRoomId;

    private Marker mCurrentMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            roomName = b.getString("ROOM_ID");
            roomEmail = b.getString("ROOM_EMAIL");
        }

        mCurrentRoomId = roomName;

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
                Bundle bundle = new Bundle();
                bundle.putString("ROOM_ID", roomName);
                bundle.putString("ROOM_EMAIL", roomEmail);
                directionsIntent.putExtras(bundle);
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
        AsyncResourceReader reader = new AsyncResourceReader() {
            @Override
            public void handleRooms(List<RoomModel> rooms) {
                if(rooms != null) {
                    for (RoomModel roomModel : rooms) {
                        Pattern pattern = Pattern.compile("(B\\d{3})");
                        Matcher matcher = pattern.matcher(roomModel.getRoomName());
                        String key = roomModel.getRoomName();
                        if(matcher.find()) {
                            key = matcher.group(1);
                        }
                        mAttributes.put(key, roomModel.getAttributes());
                    }
                }
            }
        };
        reader.execute();
      //  roomListView.setAdapter(arrayAdapter);
    }

    public void setUpMap() {
        if (mMap == null) {
            //
            SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mMap.setMyLocationEnabled(true);

            if (mMap != null) {

                mGeoJsonMap = new GeoJsonMap(mMap);

                // get map using building name...
                // get map layers...

                // add layers to map...

                loadMap();
                mapListen();

            }
        }
    }

    private void loadMap() {
        final GeoJsonMapLayer layer = new GeoJsonMapLayer();
        mGeoJsonMap.addLayer(0, layer);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                new AsyncParseGeoJsonFromResource(getApplicationContext(), R.raw.riverviewfloor1) {
                    @Override
                    public void handleGeoJson(GeoJson json) {
                        layer.setGeoJson(json);
                        layer.draw(mMap, Color.rgb(255, 249, 236), Color.rgb(108, 122, 137), 3);
                        LatLng center = getRoomCenter(layer, mCurrentRoomId);
                        LatLng centerRev = new LatLng(center.longitude, center.latitude);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(centerRev, 20);

                        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                            if (feature.getGeoJsonGeometry().getType().equals("Polygon")) {
                                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                                if(polygon.contains(centerRev)) {
                                    Polygon gmsPoly = polygon.getGMSPolygon();
                                    int color1 = Color.rgb(137, 196, 244);
                                    gmsPoly.setFillColor(color1);
                                }
                            }
                        }
                        mMap.moveCamera(cameraUpdate);
                        mCurrentMarker = mMap.addMarker(new MarkerOptions().position(centerRev));
                    }
                }.execute();
            }
        });
    }

    private LatLng getRoomCenter(GeoJsonMapLayer layer, String roomId) {
        Pattern pattern = Pattern.compile("(B\\d{3})");
        Matcher matcher = pattern.matcher(roomId);
        String match = null;
        if(matcher.find()) {
            match = matcher.group(1);
        }
        for(GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {

            if(feature.getGeoJsonGeometry().getType().equals("Polygon") &&
                    feature.getProperty("room").equals(match)) {

                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                return polygon.getCentroid();
            }
        }
        return null;
    }

    private String getRoomSuffix(String str) {
        int index = 0;
        String result = null;
        for(Character ch : str.toCharArray()) {
            if(Character.isDigit(ch)) {
                result = str.substring(index);
                break;
            }
            ++index;
        }
        return result;
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
                            for(GeoJsonFeature feature2 : layer.getGeoJson().getGeoJsonFeatures()) {
                                if(feature.getGeoJsonGeometry().getType().equals("Polygon")) {
                                    GeoJsonPolygon polygon2 = (GeoJsonPolygon) feature2.getGeoJsonGeometry().getGeometry();
                                    Polygon gmsPoly2 = polygon2.getGMSPolygon();
                                    int color = Color.rgb(255, 246, 236);
                                    if (feature2.getProperty("room").equals("B250") || feature.getProperty("room").equals("B205") ||
                                            feature.getProperty("room").equals("B218") || feature.getProperty("room").equals("B217")) {
                                        color = Color.rgb(234, 230, 245);
                                    } else if (feature2.getProperty("room").equals("B241") ||
                                            feature2.getProperty("room").equals("B234") ||
                                            feature2.getProperty("room").equals("B219") ||
                                            feature2.getProperty("room").equals("B251") ||
                                            feature2.getProperty("room").equals("B230")) {

                                        color = Color.WHITE;
                                    } else if (feature2.getProperty("room").equals("B236") ||
                                            feature2.getProperty("room").equals("B232") ||
                                            feature2.getProperty("room").equals("B223") ||
                                            feature2.getProperty("room").equals("B247") ||
                                            feature2.getProperty("room").equals("B233-229") ||
                                            feature2.getProperty("room").equals("B235-238") ||
                                            feature2.getProperty("room").equals("B245-248") ||
                                            feature2.getProperty("room").equals("B222-220")) {
                                        color = Color.WHITE;
                                    }
                                    gmsPoly2.setFillColor(color);
                                }
                            }
                            Polygon gmsPoly = polygon.getGMSPolygon();
                            int color1 = Color.rgb(137, 196, 244);
                            gmsPoly.setFillColor(color1);
                            LatLng center1 = polygon.getCentroid();
                            mCurrentMarker = mMap.addMarker(new MarkerOptions().position(
                                    new LatLng(center1.longitude, center1.latitude)
                            ));

                            roomTextView.setText(feature.getProperty("room"));
                            String key = feature.getProperty("room");
                            if(key != null) {
                                List<String> attributes = mAttributes.get(key);
                                if(attributes != null && attributes.size() > 0) {
                                    mCurrentRoomAttributes = attributes.toArray(new String[attributes.size() - 1]);
                                    arrayAdapter = new ArrayAdapter(getApplicationContext(), R.layout.room_simple_text, mCurrentRoomAttributes);
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