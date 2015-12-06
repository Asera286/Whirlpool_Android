package edu.msu.elhazzat.whirpool.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Timer;
import java.util.TimerTask;

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
import edu.msu.elhazzat.whirpool.utils.MapConstants;
import edu.msu.elhazzat.whirpool.utils.WIMConstants;

/**
 * Created by christianwhite on 10/05/15.
 */
public class RoomActivity extends AppCompatActivity {

    private static final String LOG_TAG = RoomActivity.class.getSimpleName();

    // Bundle keys
    private static final String ROOM_MODEL_PARSIBLE_KEY = "ROOM_MODEL";

    // Marker dimensions
    private static final int IMAGE_VIEW_MARKER_HEIGHT_DP = 25;
    private static final int IMAGE_VIEW_MARKER_WIDTH_DP = 20;

    // animation constants
    private static final long SLIDE_DOWN_ANIM_DURATION = 250;
    private static final long SLIDE_UP_ANIM_DURATION = 400;
    private static final float THRESHOLD_SLIDE_DOWN_PRECENTAGE = .70f;
    private static final float THRESHOLD_SLIDE_UP_PERCENTAGE = .30f;
    private static final float ATTRIBUTE_MAX_VIEW_PERCENTAGE = .95f;

    private static final String ACTION_BAR_COLOR =  "#8286F8";

    private static final float FLOOR_TEXT_SIZE = 25f;
    private static final int FLOOR_TEXT_WIDTH = 60;

    private Polyline mPolyLine;
    private LatLng mNavStartPosition;
    private LatLng mNavEndPosition;
    private int mStartFloorNum = 0;
    private int mEndFloorNum = 0;
    private int mCurrentFloorNum = 1;

    private ImageView mEndLocationImageView;
    private ImageView mStartLocationImageView;

    private Marker mEndLocationMarker;

    private AmenityAdapter mAmenitiesAdapter;
    private ListView mRoomAttributeListView;
    private TextView mRoomNameTextView;

    private GeoJsonMap mGeoJsonMap;
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

    private List<RoomModel> mRooms = new ArrayList<>();

    private Map<Integer, List<Marker>> mRoomMarkerMap = new HashMap<>();

    private RoomModel mCurrentRoom;

    private ImageView mBookRoomImageView;

    private Map<String, Integer> mMapTop = new HashMap<>();

    private ImageView mGoButton;
    private ImageView mInfoButton;

    private Map<String, GeoJsonFeature> mFeatureMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        //TODO: Check if room exists
        //TODO: Add default building coordinates
        //TODO: Make all bundle keys public static in calling class
        //TODO: ADD yes button for floor select
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get room/building information if a calendar event or favorite room
        // has been selected
        Bundle b = getIntent().getExtras();
        if(b != null) {
            mBuildingName = b.getString(WIMConstants.BUNDLE_BUILDING_NAME_KEY);
            mRoomName = b.getString(WIMConstants.BUNDLE_ROOM_NAME_KEY);
        }

        // If neither a building or room has been selected, a carousel button has been clicked
        if(b != null && mBuildingName == null && mRoomName == null) {
            mBuildingName = b.getString(HomeActivity.BUILDING_CAROUSEL_KEY);
        }

        new AsyncGCSBuildingInfoReader(mBuildingName) {
            public void handleBuilding(BuildingModel model) {
                if(model == null) {
                    buildingUnderConstructionDialog();
                }
                else {
                    buildMap();
                    inflateRoomAttributes(mRoomName, mBuildingName);
                }
            }
        }.execute();

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


        // initialize interactive elements of activity UI
        initFavorites();
        initBookRoom();
        initNavigation();
        initStartLocation();
        buildSlideView();
        getLayoutDimensions();
        buildFloorPicker();

        mGoButton = (ImageView)findViewById(R.id.go_button);
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawNavRoute();
            }
        });

        mGoButton.setVisibility(View.INVISIBLE);

        mInfoButton = (ImageView)findViewById(R.id.info_button);
        mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScrollViewDirectChild.getVisibility() == View.VISIBLE) {
                    mScrollViewDirectChild.setVisibility(View.INVISIBLE);
                } else {
                    mScrollViewDirectChild.setVisibility(View.VISIBLE);
                }
            }
        });

        mInfoButton.setVisibility(View.INVISIBLE);
        mScrollViewDirectChild.setVisibility(View.GONE);
    }

    /**
     * Favorite room and add to SQLite DB
     */
    private void initFavorites() {
        ImageView favoritesImageView = (ImageView) findViewById(R.id.add_to_favorites);

        // Insert into room table upon favoriting a selected room
        favoritesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RelevantRoomDbHelper helper = new RelevantRoomDbHelper(getApplicationContext());
                AsyncGCSRoomInfoReader reader = new AsyncGCSRoomInfoReader(mBuildingName,
                        mRoomNameTextView.getText().toString()) {
                    @Override
                    public void handleRoom(RoomModel room) {
                        if (room != null) {
                            helper.addRelevantRoom(room);
                            Toast.makeText(getApplicationContext(), mRoomNameTextView.getText().toString()
                                    + " added to favorites!", Toast.LENGTH_LONG).show();
                        }
                    }
                };
                reader.execute();
            }
        });
    }

    /**
     * Add ability to book room
     */
    private void initBookRoom() {
        mBookRoomImageView = (ImageView) findViewById(R.id.edit_event);
        mBookRoomImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // start create event activity using current room information
                Intent eventIntent = new Intent(getApplicationContext(), CreateEventActivity.class);
                eventIntent.putExtra(ROOM_MODEL_PARSIBLE_KEY, mCurrentRoom);
                startActivity(eventIntent);
            }
        });
    }

    /**
     * Add ability to start navigation
     */
    private void initNavigation() {
        TextView startNavigation = (TextView)findViewById(R.id.start_navigation);
        startNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNavigationOn) {
                    drawNavRoute();
                }
            }
        });

        findViewById(R.id.nav_info_layout).setVisibility(View.GONE);
        startNavigation.setVisibility(View.GONE);
    }

    /**
     * Create imageview for "start" location - start in this
     * context means the starting navigation point
     */
    private void initStartLocation() {
        mStartLocationImageView = (ImageView) findViewById(R.id.start_location);
        mStartLocationImageView.setVisibility(View.GONE);
    }

    /**
     * Create a bitmap from string
     * @param text
     * @return
     */
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

        canvas.translate(width / 2f, height);
        canvas.drawText(text, 0, 0, paint);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(image);
        return icon;
    }

    /**
     * Draw a navigation route
     */
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

    /**
     * Half of the room activity screen is made up of room stats - allow
     * the user to slide this view done and reveal more of the map
     */
    private void buildSlideView() {

        // Make the slide view height a function of user's phone dimensions
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float heightdp = displayMetrics.heightPixels / displayMetrics.density;

        mAttributeLayout = (LinearLayout) findViewById(R.id.attribute_list_view);
        mAttributeLayout.getLayoutParams().height = (int) (heightdp * ATTRIBUTE_MAX_VIEW_PERCENTAGE);

        mRoomAttributeListView = (ListView) findViewById(R.id.roomInfoList);

        mRoomNameTextView = (TextView) findViewById(R.id.roomNameText);
        mRoomNameTextView.setText(mRoomName);
        mRoomNameTextView.setTextColor(Color.WHITE);

        // Slide action
        mRoomNameTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float currentY = event.getY();
                final float delta = currentY - mStartY;
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

    private void updateRoomOccupancyStatuses() {

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
            mScrollViewDirectChild.setVisibility(View.GONE);
            mInfoButton.setVisibility(View.GONE);

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

            mInfoButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Fetch room names of this building
     */
    private void getRooms() {
        new AsyncGCSMultiRoomInfoReader(mBuildingName) {
            public void handleRooms(List<RoomModel> rooms) {
                if (rooms != null) {
                    mRooms = rooms;
                    drawRoomNames();
                    for (Marker m : mRoomMarkerMap.get(mGeoJsonMap.getCurrentLayer().getFloorNum())) {
                        m.setVisible(true);
                    }
                }
            }
        }.execute();
    }

    /**
     * Draw Room names on map
     */
    private void drawRoomNames() {

        // map room names to a floor
        Map<Integer, List<String>> roomNamesMap = new HashMap<>();
        for(RoomModel model : mRooms) {
            Integer roomNameKey = getInitialFloor(model.getRoomName());
            if(roomNamesMap.containsKey(roomNameKey)) {
                roomNamesMap.get(roomNameKey).add(model.getRoomName());
            }
            else {
                List<String> names = new ArrayList<>();
                names.add(model.getRoomName());
                roomNamesMap.put(roomNameKey, names);
            }
        }

        for (Map.Entry<Integer, List<String>> entry : roomNamesMap.entrySet()) {
            GeoJsonMapLayer layer = mGeoJsonMap.getLayer(entry.getKey());
            if(layer != null) {
                for (String roomNameInMap : entry.getValue()) {
                    for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                        String roomName = feature.getProperty("room");
                        if (roomName != null) {
                            if (roomName.equals(roomNameInMap)) {
                                GeoJsonPolygon poly = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                                BitmapDescriptor text = RoomActivity.getTextMarker(roomName);
                                Marker m = mMap.addMarker(new MarkerOptions()
                                        .position(poly.getCentroid())
                                        .icon(text));
                                m.setVisible(false);
                                if (mRoomMarkerMap.containsKey(layer.getFloorNum())) {
                                    mRoomMarkerMap.get(layer.getFloorNum()).add(m);
                                } else {
                                    List<Marker> markers = new ArrayList<>();
                                    markers.add(m);
                                    mRoomMarkerMap.put(layer.getFloorNum(), markers);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * On clicking a floor number select and draw
     * @param floor
     */
    private void setOnFloorSelectListener(TextView floor) {
        // set floor picker on click listener
        floor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                //tv.setTextColor(Color.BLUE);
                tv.setBackgroundColor(Color.parseColor("#F2A440"));
                int tmpFloorNum = tv.getText().equals("T") ? mMapTop.get("T")
                        : Integer.parseInt(tv.getText().toString());

                // don't redraw or show if we are currently on this floor
                if(tmpFloorNum != mCurrentFloorNum) {

                    unselectLastFloor(tv);

                    selectNextFloor(tmpFloorNum);

                    if(mNavigationOn) {
                        if(mPolyLine != null) {
                            mPolyLine.remove();
                        }

                        // if navigating between floors, draw the next route
                        if((mCurrentFloorNum == mEndFloorNum || mCurrentFloorNum == mStartFloorNum) && mNavigationOn) {
                            MapRoute mRoute = new MapRoute(getApplicationContext(), mMap, mBuildingName, mCurrentFloorNum);
                            mPolyLine = mRoute.drawRoute(mNavStartPosition, mStartFloorNum, mNavEndPosition, mEndFloorNum);
                        }
                    }
                }
            }
        });
    }

    /**
     * A new floor has been selected
     * @param lastFloor
     */
    private void unselectLastFloor(TextView lastFloor) {
        // remove conference/huddle labels from last floor
        if(mRoomMarkerMap.containsKey(mCurrentFloorNum)) {
            for (Marker m : mRoomMarkerMap.get(mCurrentFloorNum)) {
                m.setVisible(false);
            }
        }

        // change unselected floor from blue to black
        for(int i = 0; i < mScrollViewDirectChild.getChildCount(); i++) {
            TextView child = (TextView) mScrollViewDirectChild.getChildAt(i);
            if(!child.getText().toString().equals(lastFloor.getText().toString())) {
                child.setBackgroundColor(Color.WHITE);
            }
        }
    }

    /**
     * A new floor has been selected - draw it and color in the floor
     * picker number
     * @param newFloorNum
     */
    private void selectNextFloor(int newFloorNum) {
        // change current floor
        mCurrentFloorNum = newFloorNum;

        // draw new layer
        mGeoJsonMap.drawLayer(mCurrentFloorNum,
                WIMConstants.MAP_DEFAULT_FILL_COLOR,
                WIMConstants.MAP_DEFAULT_STROKE_COLOR,
                WIMConstants.MAP_DEFAULT_STROKE_WIDTH);

        // draw conference/huddle labels for selected floor
        if(mRoomMarkerMap.containsKey(mCurrentFloorNum)) {
            for (Marker m : mRoomMarkerMap.get(mCurrentFloorNum)) {
                m.setVisible(true);
            }
        }
    }

    /**
     * Create floor number for floor picker
     * @param floorText
     * @return
     */
    private TextView createFloorNumberTextView(String floorText) {
        TextView tv = new TextView(getApplicationContext());
        tv.setWidth(FLOOR_TEXT_WIDTH);
        tv.setTextSize(FLOOR_TEXT_SIZE);
        tv.setBackgroundColor(Color.WHITE);
        tv.setText(floorText);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);

        return tv;
    }

    /**
     * Create a floor picker based on the number of available floors
     */
    private void buildFloorPicker() {
        // Make the floor picker scrollable to handle many floors vs small screen size
        mScrollViewDirectChild = (LinearLayout) findViewById(R.id.scroll_view_direct_child);
        mScrollViewDirectChild.setVisibility(View.INVISIBLE);

        new AsyncGCSBuildingInfoReader(mBuildingName) {
            public void handleBuilding(BuildingModel model) {
                if(model != null) {

                    for (int i = model.getFloors() + 1; i >= 1; i--) {
                        String text = null;
                        if(model.getFloors() + 1 == i) {
                            text = "T";
                            mMapTop.put("T", model.getFloors() + 1);
                        }
                        else {
                            text = Integer.toString(i);
                        }
                        TextView floor = createFloorNumberTextView(text);
                        setOnFloorSelectListener(floor);

                        mScrollViewDirectChild.addView(floor);
                    }
                }
            }
        }.execute();
    }

    /**
     * Start navigation interface
     */
    private void startNavigation() {
        if(!mNavigationOn) {

            mGoButton.setVisibility(View.VISIBLE);

            // if the slide view is not collapsed, collapse it
            if (!mCollapsed) {
                mViewCurrentHeight = mViewMinHeight;

                LinearLayout view = (LinearLayout) findViewById(R.id.attribute_list_view);
                HeightAnimation heightAnim = new HeightAnimation(view, view.getHeight(), mViewMinHeight);
                heightAnim.setDuration(SLIDE_DOWN_ANIM_DURATION);
                dispatchAnimationLock(SLIDE_DOWN_ANIM_DURATION);
                view.startAnimation(heightAnim);

                mCollapsed = true;
            }

            mStartLocationImageView.setVisibility(View.VISIBLE);

            /**
             * Listen for camera position changes - record the center point -
             * this will be the lat/lng point of the nav pin
             */
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0) {
                    LatLng tmp = mMap.getCameraPosition().target;
                    mLocationMarkerLatLng = new LatLng(tmp.latitude, tmp.longitude);
                    setNavInformationView();
                }
            });

            mNavigationOn = true;
            findViewById(R.id.nav_info_layout).setVisibility(View.VISIBLE);
        }
        else {
            mStartLocationImageView.setVisibility(View.GONE);
            mNavigationOn = false;

            if(mPolyLine != null) mPolyLine.remove();

            findViewById(R.id.start_navigation).setVisibility(View.GONE);
            findViewById(R.id.nav_info_layout).setVisibility(View.GONE);
            mGoButton.setVisibility(View.INVISIBLE);
            mMap.setOnCameraChangeListener(null);

        }
    }

    private void setNavInformationView() {
        GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
            if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {
                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                if (polygon.contains(mLocationMarkerLatLng)) {
                    ((TextView)findViewById(R.id.navigation_text)).setText(
                            new StringBuilder()
                                    .append("Building ")
                                    .append(mBuildingName)
                                    .append(", Floor ")
                                    .append(mCurrentFloorNum)
                                    .append(", Room ")
                                    .append(feature.getProperty("room")).toString()
                    );
                }
            }
        }
    }

    private void buildFeatureMap() {

    }

    /**
     * Get location of navigation pin
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
                if(timePeriods != null) {
                    for (TimePeriod period : timePeriods) {
                        if (now.getValue() >= period.getStart().getValue() && now.getValue() <=
                                period.getEnd().getValue()) {
                            isBusy = true;
                            break;
                        }
                    }

                    mAmenitiesAdapter = new AmenityAdapter(getApplicationContext(), 0, occupancy, isBusy, amenities);
                    mRoomAttributeListView.setAdapter(mAmenitiesAdapter);
                }
            }
        }.execute();
    }

    /**
     * Pull room data on clicking a particular room - populate room attribute list view
     * @param roomName
     * @param buildingName
     */
    private void inflateRoomAttributes(String roomName, String buildingName) {
        AsyncGCSRoomInfoReader reader = new AsyncGCSRoomInfoReader(buildingName, roomName) {
            @Override
            public void handleRoom(RoomModel room) {
                if (room != null) {
                    String[] amenities = room.getAmenities();
                    int occupancy = room.getCapacity();
                    setRoomAvailable(amenities, occupancy, room.getEmail());
                    mCurrentRoom = room;
                    if(room.getRoomType() != null && room.getRoomType().equals("C")) {
                        mBookRoomImageView.setVisibility(View.VISIBLE);
                    }
                    else {
                        mBookRoomImageView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    mRoomAttributeListView.setAdapter(null);
                    mBookRoomImageView.setVisibility(View.INVISIBLE);
                }
            }
        };
        reader.execute();
    }

    /**
     * Map is not available - show dialog and return to home page
     */
    public void buildingUnderConstructionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Building")
                .setMessage("Building map under construction.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(homeIntent);
                    }
                }).show();
    }

    /**
     * Room is not available on map- show dialog and return to home page
     */
    public void roomUnderConstructionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Room")
                .setMessage("Room not available.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(homeIntent);
                    }
                }).show();
    }

    /**
     * Build map
     */
    private void buildMap() {

        // Acquire the map data
        new AsyncParseGeoJsonGCS(this, mBuildingName) {
            public void handleGeoJson(GeoJsonMap map, ProgressDialog dialog) {
                if(map != null) {
                    // set the map
                    mGeoJsonMap = map;

                    //set the inital floor based on the searched room
                    int initialFloor = MapConstants.INIT_FLOOR;

                    if(mRoomName != null) {
                        initialFloor = getInitialFloor(mRoomName);
                    }

                    mGeoJsonMap.setCurrentLayer(initialFloor);

                    // floors set for navigation
                    mCurrentFloorNum = initialFloor;
                    mEndFloorNum = initialFloor;
                    mStartFloorNum = initialFloor;

                    buildGoogleMap(dialog);
                    getRooms();
                }
            }
        }.execute();

    }

    public void colorByOccupiedStatus() {

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        new AsyncGCSMultiRoomInfoReader(mBuildingName) {
                            public void handleRooms(List<RoomModel> rooms) {
                                if (rooms != null) {
                                    GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
                                    for(RoomModel model : rooms) {
                                        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                                            if (model.getRoomName().equals(feature.getProperty("room"))) {
                                                if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {
                                                    GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                                                    Polygon gmsPoly = polygon.getGMSPolygon();

                                                    if (model.getOccupancyStatus().equals("Y")) {
                                                        gmsPoly.setFillColor(Color.GREEN);
                                                    }
                                                    else if(model.getOccupancyStatus().equals("N")){
                                                        gmsPoly.setFillColor(Color.RED);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }.execute();
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1200000);
    }

    public void buildGoogleMap(ProgressDialog dialog) {
        if (mMap == null) {
            mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();

            if (mMap != null) {
                mGeoJsonMap.setMap(mMap);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                    public boolean onMarkerClick(Marker marker) {
                        mapListenCallback(marker.getPosition());
                        return true;
                    }
                });

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mapListenCallback(latLng);
                    }
                });

                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
                        mGeoJsonMap.drawLayer(layer.getFloorNum(), WIMConstants.MAP_DEFAULT_FILL_COLOR,
                                WIMConstants.MAP_DEFAULT_STROKE_COLOR,
                                WIMConstants.MAP_DEFAULT_STROKE_WIDTH);

                        LatLng center = Geometry.getRoomCenter(layer, mRoomName);
                        if (center == null) {
                            roomUnderConstructionDialog();
                        }

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center,
                                WIMConstants.DEFAULT_MAP_CAMERA_ZOOM);

                        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                            if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {
                                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();

                                if (polygon.contains(center)) {
                                    Polygon gmsPoly = polygon.getGMSPolygon();
                                    gmsPoly.setFillColor(WIMConstants.MAP_DEFAULT_SELECTED_ROOM_COLOR);
                                }
                            }
                        }
                        mMap.moveCamera(cameraUpdate);
                        mEndLocationMarker = mMap.addMarker(new MarkerOptions().position(center)
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("location_end",
                                        IMAGE_VIEW_MARKER_WIDTH_DP, IMAGE_VIEW_MARKER_HEIGHT_DP))));

                        mNavEndPosition = mEndLocationMarker.getPosition();
                    }
                });

                colorByOccupiedStatus();

                if (dialog.isShowing()) {
                    dialog.dismiss();
                    findViewById(R.id.loading_overlay).setVisibility(View.GONE);
                }
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


    private void mapListenCallback(LatLng latLng) {
        GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
            if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {
                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                if (polygon.contains(latLng)) {

                    mEndLocationMarker.remove();

                    Polygon gmsPoly = polygon.getGMSPolygon();
                    int color1 = WIMConstants.MAP_DEFAULT_SELECTED_ROOM_COLOR;

                    gmsPoly.setFillColor(color1);
                    LatLng center1 = polygon.getCentroid();

                    mEndLocationMarker = mMap.addMarker(new MarkerOptions().position(center1).
                            icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("location_end",
                                    IMAGE_VIEW_MARKER_WIDTH_DP, IMAGE_VIEW_MARKER_HEIGHT_DP))));

                    if(mPolyLine != null) {
                        mPolyLine.remove();
                    }

                    mNavEndPosition = mEndLocationMarker.getPosition();

                    mEndFloorNum = mCurrentFloorNum;

                    String selectedRoomName = feature.getProperty(GeoJsonConstants.ROOM_TAG);
                    mRoomNameTextView.setText(selectedRoomName);

                    inflateRoomAttributes(selectedRoomName, mBuildingName);


                } else {
                    Polygon gmsPoly = polygon.getGMSPolygon();
                    int color1 = WIMConstants.MAP_DEFAULT_FILL_COLOR;

                    String roomName = feature.getProperty("room");
                    if(roomName != null) {
                        switch (feature.getProperty("room")) {
                            case "HW":
                                color1 = Color.WHITE;
                                break;
                            case "WB":
                                color1 = Color.rgb(234, 230, 245);
                                break;
                            case "MB":
                                color1 = Color.rgb(234, 230, 245);
                                break;

                        }
                    }

                    gmsPoly.setFillColor(color1);

                }
            }
        }
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

    private Bitmap resizeMapIcons(String iconName, int widthDip, int heightDip){
        int widthPx = (int)dipToPixels(widthDip);
        int heightPx = (int)dipToPixels(heightDip);

        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),
                getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, widthPx, heightPx, false);
        return resizedBitmap;
    }

    private float dipToPixels(float dipValue) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public class FeatureMap {
        private Map<Integer, Map<String, GeoJsonFeature>> mFeatureMaps = new HashMap<>();

        public FeatureMap(List<RoomModel> models, GeoJsonMap map) {
            if(models != null) {
                for(RoomModel model : models) {
                    int floorNum = getInitialFloor(model.getRoomName());
                    if(floorNum != -1) {
                        if(mFeatureMaps.containsKey(floorNum)) {
                            GeoJsonMapLayer layer = map.getLayer(floorNum);
                            if(layer != null) {
                                for(GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                                    try {
                                        if (feature.getProperty("room").equals(model.getRoomName())) {
                                            if(mFeatureMaps.containsKey(floorNum)) {
                                                mFeatureMaps.get(floorNum).put(model.getRoomName(), feature);
                                            }
                                            else {
                                                Map<String, GeoJsonFeature> featureMap = new HashMap<>();
                                                featureMap.put(model.getRoomName(), feature);
                                                mFeatureMaps.put(floorNum, featureMap);
                                            }
                                        }
                                    }catch(NullPointerException e) {
                                        Log.e(LOG_TAG, e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        public GeoJsonFeature getFeature(int floorNum, String roomName) {
            if(mFeatureMaps.containsKey(floorNum)) {
                Map<String, GeoJsonFeature> featureMap = mFeatureMaps.get(floorNum);
                if(featureMap.containsKey(roomName)) {
                    return featureMap.get(roomName);
                }
            }
        }

    }
}