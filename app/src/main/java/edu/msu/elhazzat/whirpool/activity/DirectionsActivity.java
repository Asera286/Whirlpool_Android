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
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarFreeBusyReader;
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
 * Created by christianwhite on 10/1/15.
 */
public class DirectionsActivity extends BaseGoogleMapsActivity {
    private String mRoomName = null;
    private String mCurrentRoomId;
    private Marker mCurrentMarker = null;
    private GeoJsonMap mGeoJsonMap = null;

    final HashMap<String, String> mEmailMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        Bundle b = getIntent().getExtras();
        if(b!=null) {
            mCurrentRoomId = b.getString("ROOM_ID");
        }

        setEmailMap();
        setUpMap();
    }

    public void setUpMap() {
        SupportMapFragment mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = mMapFragment.getMap();
        if (mMap != null) {
            mGeoJsonMap = new GeoJsonMap(mMap);
          loadMap();
            mapListen();
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
                                if (polygon.contains(centerRev)) {
                                    Polygon gmsPoly = polygon.getGMSPolygon();
                                    int color1 = Color.rgb(137, 196, 244);
                                    gmsPoly.setFillColor(color1);
                                }
                            }
                        }
                        mMap.moveCamera(cameraUpdate);
                        mCurrentMarker = mMap.addMarker(new MarkerOptions().position(centerRev));
                        setRoomsBusyStatus(layer);
                    }
                }.execute();
            }
        });
    }

    private void setRoomsBusyStatus(GeoJsonMapLayer layer) {
        for(GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
            String email = mEmailMap.get(feature.getProperty("room"));
            if(email != null) {
                Polygon polygon = ((GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry()).getGMSPolygon();
                setRoomBusy(polygon, email);
            }
        }
    }
    private void setEmailMap() {
        AsyncResourceReader resourceReader = new AsyncResourceReader() {
            @Override
            public void handleRooms(List<RoomModel> rooms) {
                if(rooms != null) {
                    for(RoomModel room : rooms) {
                        Pattern pattern = Pattern.compile("(B\\d{3})");
                        Matcher matcher = pattern.matcher(room.getRoomName());
                        String key = null;

                        if(matcher.find()) {
                            key = matcher.group(1);
                            mEmailMap.put(key, room.getEmail());
                        }
                    }
                }
            }
        };
        resourceReader.execute();
    }

    private void setRoomBusy(final Polygon gmsPoly, String email) {
        long nowLong = System.currentTimeMillis();
        Date nowDate = new Date(nowLong);
        final DateTime now = new DateTime(nowLong);
        DateTime end = new DateTime(getEndOfDay(nowDate));
        new AsyncCalendarFreeBusyReader(CalendarServiceHolder.getInstance().getService(),
                email, now, end) {
            @Override
            public void handleTimePeriods(List<TimePeriod> timePeriods) {
                boolean isBusy = false;
                for(TimePeriod period : timePeriods) {
                    if(now.getValue() >= period.getStart().getValue() && now.getValue() <=
                            period.getEnd().getValue()) {
                        gmsPoly.setFillColor(Color.RED);
                        isBusy = true;
                        break;
                    }
                }
                if(!isBusy) {
                    gmsPoly.setFillColor(Color.GREEN);
                }
            }
        }.execute();
    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
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

}
