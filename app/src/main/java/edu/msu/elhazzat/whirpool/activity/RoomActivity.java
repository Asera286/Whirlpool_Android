package edu.msu.elhazzat.whirpool.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.AmenityAdapter;
import edu.msu.elhazzat.whirpool.calendar.AsyncCalendarFreeBusyReader;
import edu.msu.elhazzat.whirpool.crud.RelevantRoomDbHelper;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonConstants;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonFeature;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonPolygon;
import edu.msu.elhazzat.whirpool.geojson.Geometry;
import edu.msu.elhazzat.whirpool.model.BuildingModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSBuildingInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSMultiRoomInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSRoomInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncParseGeoJsonGCS;
import edu.msu.elhazzat.whirpool.routing.MapRoute;
import edu.msu.elhazzat.whirpool.utils.CalendarServiceHolder;
import edu.msu.elhazzat.whirpool.utils.WIMAppConstants;

public class RoomActivity extends AppCompatActivity {

    private Polyline mPolyLine;
    private LatLng mNavStartPosition;
    private LatLng mNavEndPosition;
    private int mStartFloorNum = 0;
    private int mEndFloorNum = 0;
    private int mCurrentFloorNum = 1;

    private static final String ACTION_BAR_COLOR =  "#8286F8";

    private static final float FLOOR_TEXT_SIZE = 25f;
    private static final int FLOOR_TEXT_WIDTH = 50;

    private static final long SLIDE_DOWN_ANIM_DURATION = 250;
    private static final long SLIDE_UP_ANIM_DURATION = 400;
    private static final float THRESHOLD_SLIDE_DOWN_PRECENTAGE = .70f;
    private static final float THRESHOLD_SLIDE_UP_PERCENTAGE = .30f;
    private static final float ATTRIBUTE_MAX_VIEW_PERCENTAGE = .95f;

    private AmenityAdapter mAmenitiesAdapter;
    private ListView mRoomAttributeListView;
    private TextView mRoomNameTextView;

    private GeoJsonMap mGeoJsonMap;
    private Marker mCurrentMarker;

    private ImageView mLocationMarker;
    private LatLng mLocationMarkerLatLng;

    private boolean mNavigationOn = false;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;

    private float mStartY = 0;

    private LinearLayout mAttributeLayout;

    private int mViewMaxHeight;
    private int mViewCurrentHeight;
    private int mViewMinHeight;
    private boolean mAnimationInEffect = false;
    private boolean mCollapsed = false;

    private LinearLayout mScrollViewDirectChild;

    private String mBuildingName;
    private String mRoomName;

    private static final String BUNDLE_BUILDING_NAME_KEY = "BUILDING_NAME";
    private static final String BUNDLE_ROOM_NAME = "ROOM_NAME";

    private Marker mNavIconFixed;

    private List<RoomModel> mRooms = new ArrayList<>();

    private Map<Integer, List<Marker>> mRoomNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mBuildingName = b.getString(BUNDLE_BUILDING_NAME_KEY);
            mRoomName = b.getString(BUNDLE_ROOM_NAME);
        }

        //temporary start values
        mNavEndPosition = new LatLng(42.15099619463289,-86.44287049770355);
        mNavStartPosition = new LatLng(42.150831643314156,-86.4429010078311);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor(ACTION_BAR_COLOR)));
        }

        ImageView favoritesImageView = (ImageView) findViewById(R.id.add_to_favorites);
        favoritesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelevantRoomDbHelper helper = new RelevantRoomDbHelper(getApplicationContext());
            }
        });

        ImageView editEventImageView = (ImageView) findViewById(R.id.edit_event);
        editEventImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(getApplicationContext(), CreateEventActivity.class);
                startActivity(eventIntent);
            }
        });

        TextView startNavigation = (TextView)findViewById(R.id.start_navigation);
        startNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNavigationOn) {
                    drawNavRoute();
                }
            }
        });

        ((RelativeLayout)findViewById(R.id.nav_info_layout)).setVisibility(View.GONE);

        startNavigation.setVisibility(View.GONE);

        mLocationMarker = (ImageView) findViewById(R.id.start_location);
        mLocationMarker.setVisibility(View.GONE);

        buildSlideView();
        getLayoutDimensions();
        buildFloorPicker();
        buildMap();
    }

    private float computeWalkingTime(LatLng start, LatLng end) {
        Location location1 = new Location("");
        location1.setLatitude(start.latitude);
        location1.setLongitude(start.longitude);

        Location location2 = new Location("");
        location2.setLatitude(end.latitude);
        location2.setLongitude(end.longitude);

        float distanceInMeters = location1.distanceTo(location2);

        int speed = 84;
        return distanceInMeters / speed;
    }

    public static BitmapDescriptor getTextMarker(String text) {

        Paint paint = new Paint();

      /* Set text size, color etc. as needed */
        paint.setTextSize(24);

        int width = (int)paint.measureText(text);
        int height = (int)paint.getTextSize();

        paint.setTextAlign(Paint.Align.CENTER);

        // Create a transparent bitmap as big as you need
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);

        // During development the following helps to see the full
        // drawing area:
        canvas.drawColor(0x00000000);
        // Start drawing into the canvas
        canvas.translate(width / 2f, height);
        canvas.drawText(text, 0, 0, paint);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(image);
        return icon;
    }

    private void drawNavRoute() {
        LatLng startLocation = getLocationMarkerCenter();
        if(startLocation != null) {
            if(mPolyLine != null) {
                mPolyLine.remove();
            }
            mStartFloorNum = mCurrentFloorNum;
            MapRoute mRoute = new MapRoute(getApplicationContext(), mMap, mBuildingName, mCurrentFloorNum);
            mPolyLine = mRoute.drawRoute(startLocation, mStartFloorNum, mNavEndPosition, mEndFloorNum);
        }
    }

    private void buildSlideView() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float heightdp = displayMetrics.heightPixels / displayMetrics.density;

        mAttributeLayout = (LinearLayout) findViewById(R.id.attribute_list_view);
        mAttributeLayout.getLayoutParams().height = (int) (heightdp * ATTRIBUTE_MAX_VIEW_PERCENTAGE);

        mRoomAttributeListView = (ListView) findViewById(R.id.roomInfoList);

        mRoomNameTextView = (TextView) findViewById(R.id.roomNameText);
        mRoomNameTextView.setText(mRoomName);
        mRoomNameTextView.setTextColor(Color.WHITE);

        mRoomNameTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float currentY = event.getY();
                float delta = currentY - mStartY;
                if (!mAnimationInEffect) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mStartY = event.getY();
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            float currentHeight = mViewCurrentHeight - delta;
                            if (mCollapsed && delta < 0) {
                                slideUp(currentHeight, delta);
                            } else if (delta > 0) {
                                animateSlideDown(currentHeight, delta);
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            animateSlideUp();
                            return true;
                    }
                }
                return false;
            }
        });
    }

    private void getRooms() {
        new AsyncGCSMultiRoomInfoReader(mBuildingName) {
            public void handleRooms(List<RoomModel> rooms) {
                mRooms = rooms;
                drawRoomNames();
                for(Marker m : mRoomNames.get(mGeoJsonMap.getCurrentLayer().getFloorNum())) {
                    m.setVisible(true);
                }
            }
        }.execute();
    }

    private void drawRoomNames() {
        for(RoomModel model : mRooms) {
            for(GeoJsonMapLayer layer : mGeoJsonMap.mLayers.values()) {
                for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                    if (feature.getProperty("room").equals(model.getRoomName())) {
                        GeoJsonPolygon poly = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                        BitmapDescriptor text = RoomActivity.getTextMarker(feature.getProperty("room"));
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(poly.getCentroid())
                                .icon(text));
                        m.setVisible(false);
                        if(mRoomNames.containsKey(layer.getFloorNum())) {
                            mRoomNames.get(layer.getFloorNum()).add(m);
                        }
                        else {
                            List<Marker> markers = new ArrayList<>();
                            markers.add(m);
                            mRoomNames.put(layer.getFloorNum(), markers);
                        }
                    }
                }
            }
        }
    }

    private void buildFloorPicker() {
        mScrollViewDirectChild = (LinearLayout) findViewById(R.id.scroll_view_direct_child);
        mScrollViewDirectChild.setVisibility(View.INVISIBLE);

        new AsyncGCSBuildingInfoReader(mBuildingName) {
            public void handleBuilding(BuildingModel model) {
                if(model != null) {
                    for (int i = 1; i < model.getFloors() + 1; i++) {

                        TextView tv = new TextView(getApplicationContext());
                        tv.setWidth(FLOOR_TEXT_WIDTH + 10);
                        tv.setTextSize(FLOOR_TEXT_SIZE);

                        String text = Integer.toString(i);

                        tv.setBackgroundColor(Color.WHITE);
                        tv.setText(text);
                   //     tv.setPadding(30, 30, 30, 30);
                        tv.setTextColor(Color.BLACK);
                        tv.setGravity(Gravity.CENTER);

                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TextView tv = (TextView) v;
                                tv.setTextColor(Color.BLUE);

                                int tmpFloorNum = Integer.parseInt(tv.getText().toString());
                                if(tmpFloorNum != mCurrentFloorNum) {
                                    if(mRoomNames.containsKey(mCurrentFloorNum)) {
                                        for (Marker m : mRoomNames.get(mCurrentFloorNum)) {
                                            m.setVisible(false);
                                        }
                                    }

                                    mCurrentFloorNum = tmpFloorNum;

                                    mGeoJsonMap.drawLayer(mCurrentFloorNum,
                                            WIMAppConstants.MAP_DEFAULT_FILL_COLOR,
                                            WIMAppConstants.MAP_DEFAULT_STROKE_COLOR,
                                            WIMAppConstants.MAP_DEFAULT_STROKE_WIDTH);

                                    if(mRoomNames.containsKey(mCurrentFloorNum)) {
                                        for (Marker m : mRoomNames.get(mCurrentFloorNum)) {
                                            m.setVisible(true);
                                        }
                                    }

                                    for(int i = 0; i < mScrollViewDirectChild.getChildCount(); i++) {
                                        TextView child = (TextView) mScrollViewDirectChild.getChildAt(i);
                                        if(!child.getText().toString().equals(tv.getText().toString())) {
                                            child.setTextColor(Color.BLACK);
                                        }
                                    }

                                    if(mNavigationOn) {
                                        if(mPolyLine != null) {
                                            mPolyLine.remove();
                                        }
                                    }
                                }
                            }
                        });
                        mScrollViewDirectChild.addView(tv);
                    }
                }
            }
        }.execute();
    }

    /**
     * Compute layout dimensions - need to know these before the view actually
     * loads
     */
    private void getLayoutDimensions() {
        final ViewTreeObserver vto = mAttributeLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mViewMaxHeight = mAttributeLayout.getHeight();
                mViewMinHeight = findViewById(R.id.roomNameText).getHeight();
                mViewCurrentHeight = mViewMaxHeight;
                mAttributeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /**
     * Lock slide down view while animating
     * @param timeInMilli
     */
    private void dispatchAnimationLock(long timeInMilli) {
        mAnimationInEffect = true;
        Runnable runnable = new Runnable() {
            public void run() {
                mAnimationInEffect = false;
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, timeInMilli);
    }

    /**
     * Slide view up on touch event
     * @param currentHeight
     * @param delta
     */
    private void slideUp(float currentHeight, float delta) {
        // Adjust y dimension
        if (currentHeight <= THRESHOLD_SLIDE_UP_PERCENTAGE * mViewMaxHeight) {
            ViewGroup.LayoutParams params = findViewById(R.id.attribute_list_view).getLayoutParams();
            params.height -= delta;

            findViewById(R.id.attribute_list_view).setLayoutParams(params);

            mViewCurrentHeight -= delta;
        }
        else {
            // past threshold - animate slide up
            mViewCurrentHeight = mViewMaxHeight;

            LinearLayout view = (LinearLayout) findViewById(R.id.attribute_list_view);
            HeightAnimation heightAnim = new HeightAnimation(view, view.getHeight(), mViewMaxHeight);
            heightAnim.setDuration(SLIDE_DOWN_ANIM_DURATION);
            dispatchAnimationLock(SLIDE_DOWN_ANIM_DURATION);
            view.startAnimation(heightAnim);

            mCollapsed = false;
            mScrollViewDirectChild.setVisibility(View.INVISIBLE);


            //remove previously drawn route and the navigation marker
            if(mPolyLine != null) mPolyLine.remove();
        }
    }

    /**
     * Animate sliding up to max height
     */
    private void animateSlideUp() {
        if(mViewCurrentHeight > THRESHOLD_SLIDE_DOWN_PRECENTAGE * mViewMaxHeight) {
            mViewCurrentHeight = mViewMaxHeight;

            LinearLayout view = (LinearLayout) findViewById(R.id.attribute_list_view);
            HeightAnimation heightAnim = new HeightAnimation(view, mViewCurrentHeight, mViewMaxHeight);
            heightAnim.setDuration(SLIDE_UP_ANIM_DURATION);
            dispatchAnimationLock(SLIDE_UP_ANIM_DURATION);
            view.startAnimation(heightAnim);
        }
    }

    /**
     * Animate slide down to min height
     * @param currentHeight
     * @param delta
     */
    private void animateSlideDown(float currentHeight, float delta) {
        if (currentHeight >= THRESHOLD_SLIDE_DOWN_PRECENTAGE * mViewMaxHeight) {
            ViewGroup.LayoutParams params = findViewById(R.id.attribute_list_view).getLayoutParams();
            params.height -= delta;

            findViewById(R.id.attribute_list_view).setLayoutParams(params);

            mViewCurrentHeight -= delta;
        }
        else {
            mViewCurrentHeight = mViewMinHeight;

            LinearLayout view = (LinearLayout) findViewById(R.id.attribute_list_view);
            HeightAnimation heightAnim = new HeightAnimation(view, view.getHeight(), mViewMinHeight);
            heightAnim.setDuration(SLIDE_DOWN_ANIM_DURATION);
            dispatchAnimationLock(SLIDE_DOWN_ANIM_DURATION);
            view.startAnimation(heightAnim);

            mCollapsed = true;
            mScrollViewDirectChild.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Start navigation interface
     */
    private void startNavigation() {
        if(!mNavigationOn) {
            // if the slide view is not collapsed, collapse it
            if (!mCollapsed) {
                mViewCurrentHeight = mViewMinHeight;

                LinearLayout view = (LinearLayout) findViewById(R.id.attribute_list_view);
                HeightAnimation heightAnim = new HeightAnimation(view, view.getHeight(), mViewMinHeight);
                heightAnim.setDuration(SLIDE_DOWN_ANIM_DURATION);
                dispatchAnimationLock(SLIDE_DOWN_ANIM_DURATION);
                view.startAnimation(heightAnim);

                mCollapsed = true;
                mScrollViewDirectChild.setVisibility(View.VISIBLE);
            }

            mLocationMarker.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.start_navigation)).setVisibility(View.VISIBLE);
            ((RelativeLayout)findViewById(R.id.nav_info_layout)).setVisibility(View.VISIBLE);

            /**
             * Listen for camera position changes - record the center point -
             * this will be the lat/lng point of the nav pin
             */
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0) {
                    LatLng tmp = mMap.getCameraPosition().target;
                    mLocationMarkerLatLng = new LatLng(tmp.latitude, tmp.longitude);
                }
            });
            mNavigationOn = true;
        }
        else {
            mLocationMarker.setVisibility(View.GONE);
            mNavigationOn = false;
            if(mPolyLine != null) mPolyLine.remove();
        }
    }

    /**
     * Get locaiton of navigation pin
     * @return
     */
    private LatLng getLocationMarkerCenter() {
        GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
            if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {
                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                if (polygon.contains(mLocationMarkerLatLng)) {
                    return polygon.getCentroid();
                }
            }
        }
        return null;
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

    /**
     * Determine if a room is available
     * @param email
     */
    private void setRoomAvailable(final String[] amenities, final int occupancy, String email) {
        long nowLong = System.currentTimeMillis();
        Date nowDate = new Date(nowLong);
        final DateTime now = new DateTime(nowLong);
        DateTime end = new DateTime(getEndOfDay(nowDate));

        // get free busy information
        new AsyncCalendarFreeBusyReader(CalendarServiceHolder.getInstance().getService(),
                email, now, end) {
            @Override
            public void handleTimePeriods(List<TimePeriod> timePeriods) {
                boolean isBusy = false;
                for(TimePeriod period : timePeriods) {
                    if(now.getValue() >= period.getStart().getValue() && now.getValue() <=
                            period.getEnd().getValue()) {
                        isBusy = true;
                        break;
                    }
                }

                mAmenitiesAdapter = new AmenityAdapter(getApplicationContext(), 0, occupancy, isBusy, amenities);
                mRoomAttributeListView.setAdapter(mAmenitiesAdapter);
            }
        }.execute();
    }

    private void inflateRoomAttributes(String roomName, String buildingName) {
        AsyncGCSRoomInfoReader reader = new AsyncGCSRoomInfoReader(buildingName, roomName) {
            @Override
            public void handleRoom(RoomModel room) {
                if (room != null) {
                    String[] amenities = room.getAmenities();
                    int occupancy = room.getCapacity();
                    setRoomAvailable(amenities, occupancy, room.getEmail());
                } else {
                    mRoomAttributeListView.setAdapter(null);
                }
            }
        };
        reader.execute();
    }

    /**
     * Map is not available - show dialog and return to home page
     */
    public void bulidingUnderConstructionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Building")
                .setMessage("Building under construction.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(homeIntent);
                    }
                }).show();
    }

    private void buildMap() {
        new AsyncParseGeoJsonGCS(mBuildingName) {
            public void handleGeoJson(GeoJsonMap map) {
                if(map != null) {
                    mGeoJsonMap = map;
                    int initialFloor = getInitialFloor(mRoomName);
                    mGeoJsonMap.setCurrentLayer(initialFloor);

                    mCurrentFloorNum = initialFloor;
                    mEndFloorNum = initialFloor;
                    mStartFloorNum = initialFloor;

                    setUpMap();
                    getRooms();
                    inflateRoomAttributes(mRoomName, mBuildingName);
                }
                else {
                    bulidingUnderConstructionDialog();
                }
            }
        }.execute();
    }

    public void setUpMap() {
        if (mMap == null) {
            mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            mGeoJsonMap.setMap(mMap);

            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                    public boolean onMarkerClick(Marker marker) {
                        mapListenCallback(marker.getPosition());
                        return true;
                    }
                });

                loadMap();
                mapListen();
            }
        }
    }

    private int getInitialFloor(String roomName) {
        for (Character ch : roomName.toCharArray()) {
            if (Character.isDigit(ch)) {
                return Integer.parseInt(Character.toString(ch));
            }
        }
        return -1;
    }

    private void loadMap() {
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
                mGeoJsonMap.drawLayer(layer.getFloorNum(), WIMAppConstants.MAP_DEFAULT_FILL_COLOR,
                        WIMAppConstants.MAP_DEFAULT_STROKE_COLOR,
                        WIMAppConstants.MAP_DEFAULT_STROKE_WIDTH);

                LatLng center = Geometry.getRoomCenter(layer, mRoomName);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center,
                        WIMAppConstants.DEFAULT_MAP_CAMERA_ZOOM);

                for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                    if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {
                        GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                        if (polygon.contains(center)) {
                            Polygon gmsPoly = polygon.getGMSPolygon();
                            gmsPoly.setFillColor(WIMAppConstants.MAP_DEFAULT_SELECTED_ROOM_COLOR);
                        }
                    }
                }
                mMap.moveCamera(cameraUpdate);
                mCurrentMarker = mMap.addMarker(new MarkerOptions().position(center));

                mNavEndPosition = mCurrentMarker.getPosition();
            }
        });
    }

    private void mapListenCallback(LatLng latLng) {
        GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
            if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {
                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                if (polygon.contains(latLng)) {

                    mCurrentMarker.remove();

                    Polygon gmsPoly = polygon.getGMSPolygon();
                    int color1 = WIMAppConstants.MAP_DEFAULT_SELECTED_ROOM_COLOR;

                    gmsPoly.setFillColor(color1);
                    LatLng center1 = polygon.getCentroid();

                    mCurrentMarker = mMap.addMarker(new MarkerOptions().position(center1));

                    mNavEndPosition = mCurrentMarker.getPosition();

                    mEndFloorNum = mCurrentFloorNum;

                    String selectedRoomName = feature.getProperty(GeoJsonConstants.ROOM_TAG);
                    mRoomNameTextView.setText(selectedRoomName);

                    inflateRoomAttributes(selectedRoomName, mBuildingName);

                } else {
                    Polygon gmsPoly = polygon.getGMSPolygon();
                    int color1 = WIMAppConstants.MAP_DEFAULT_FILL_COLOR;
                    gmsPoly.setFillColor(color1);
                }
            }
        }
    }

    private void mapListen() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapListenCallback(latLng);
            }
        });
    }

    public class HeightAnimation extends Animation {
        private final int mOriginalHeight;
        private final View mView;
        private float mPerValue;

        public HeightAnimation(View view, int fromHeight, int toHeight) {
            mView = view;
            mOriginalHeight = fromHeight;
            mPerValue = (toHeight - fromHeight);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mView.getLayoutParams().height = (int) (mOriginalHeight + mPerValue * interpolatedTime);
            mView.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.navigate:
                startNavigation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class CircleView extends View {

        private Paint paint;

        public CircleView(Context context) {
            super(context);

            // create the Paint and set its color
            paint = new Paint();
            paint.setColor(Color.GRAY);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.GRAY);
            canvas.drawCircle(200, 200, 100, paint);
        }

    }
}