package edu.msu.elhazzat.whirpool.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.AmenityAdapter;
import edu.msu.elhazzat.whirpool.crud.CachedGeoJsonDataDbHelper;
import edu.msu.elhazzat.whirpool.crud.RelevantRoomDbHelper;
import edu.msu.elhazzat.whirpool.geojson.AsyncParseGeoJsonFromFile;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonConstants;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonFeature;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonPolygon;
import edu.msu.elhazzat.whirpool.geojson.Geometry;
import edu.msu.elhazzat.whirpool.model.BuildingModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSBuildingInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSGeoJsonTimestamp;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSMultiRoomInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSRoomInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncParseGeoJsonGCS;
import edu.msu.elhazzat.whirpool.routing.MapRoute;
import edu.msu.elhazzat.whirpool.utils.BitmapUtil;
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
    private static final int IMAGE_VIEW_MARKER_HEIGHT_DP = 30;
    private static final int IMAGE_VIEW_MARKER_WIDTH_DP = 21;
    private static final int IMAGE_VIEW_ROOM_ICON_HEIGHT_DP = 12;
    private static final int IMAGE_VIEW_ROOM_ICON_WIDTH_DP = 12;

    // animation constants
    private static final long SLIDE_DOWN_ANIM_DURATION = 250;
    private static final long SLIDE_UP_ANIM_DURATION = 400;
    private static final float THRESHOLD_SLIDE_DOWN_PRECENTAGE = .70f;
    private static final float THRESHOLD_SLIDE_UP_PERCENTAGE = .30f;

    private static final String ACTION_BAR_COLOR =  "#8286F8";

    private static final float FLOOR_TEXT_SIZE = 25f;
    private static final int FLOOR_TEXT_WIDTH = 60;

    private Polyline mPolyLine;
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
    private LatLng mNavStartLocation;

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

    private TimerTask mAsyncColorOccupiedRoomsTask;

    private Marker mFixedNavIcon;

    private boolean mDirectRouteExists = true;
    private boolean mThreeStageRoute = false;

    private GeoJsonPolygon mCurrentSelectedPoly;
    private int mLastFillColor;

    private boolean mBlockNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

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
                    loadMapData();
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
        buildSwitchFloorOnNavButton();
        initStartLocation();

        buildSlideView();
        getLayoutDimensions();
        buildFloorPicker();
        buildGoButton();
        buildInfoButton();

        findViewById(R.id.nav_info_layout).setVisibility(View.GONE);
    }

    /**********************************************************************************
     * Build UI buttons and set onclick handlers
     **********************************************************************************/

    /**
     * If navigation is active and multifloor, ask user to confirm that
     * they are on the destination floor - switch floors and draw
     * the destination route if confirmed
     */
    private void buildSwitchFloorOnNavButton() {
        TextView v = (TextView) findViewById(R.id.switch_floor_button);
        v.setVisibility(View.INVISIBLE);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDirectRouteExists) {
                    unselectLastFloor(Integer.toString(mEndFloorNum));
                    selectNextFloor(mEndFloorNum);
                    setNavDistanceView(mNavEndPosition, mNavStartLocation);
                    findViewById(R.id.switch_floor_button).setVisibility(View.GONE);
                    setCurrentFloorColor();
                    mThreeStageRoute = false;

                } else {
                    unselectLastFloor(Integer.toString(1));
                    selectNextFloor(1);
                    setCurrentFloorColor();
                    mThreeStageRoute = true;

                    ((TextView) findViewById(R.id.navigation_text)).setText("Are you on floor "
                            + Integer.toString(mEndFloorNum) + "?");
                }
            }
        });
    }

    /**
     * Populate nav info view with distance and time to reach destination
     */
    private void setNavDistanceView(LatLng start, LatLng end) {
        int distance = getLatLngDistance(start, end);
        int distanceInFeet = metersToFeet(distance);
        String minutes = getWalkingTimeInMinutes(distance);
        ((TextView) findViewById(R.id.navigation_text)).setText(
                minutes + " (" + Integer.toString(distanceInFeet) + " ft)");
    }

    /**
     * Build go button - used to draw route during navigation
     */
    private void buildGoButton() {
        mGoButton = (ImageView)findViewById(R.id.go_button);
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNavigationOn && mBlockNavigation) {
                    Toast.makeText(getApplicationContext(), "Cannot navigate from here - " +
                            "please select a valid floor.", Toast.LENGTH_LONG);
                }
                findViewById(R.id.nav_info_layout).setBackgroundColor(Color.parseColor("#F2A440"));

                mMap.setOnCameraChangeListener(null);
                try {
                    fixStartNavIcon(true);
                    drawNavRoute();
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    return;
                } catch (NullPointerException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    return;
                }

                if (mCurrentFloorNum != mEndFloorNum && (mCurrentFloorNum == 1 || mDirectRouteExists)) {
                    ((TextView) findViewById(R.id.navigation_text)).setText("Are you on floor "
                            + Integer.toString(mEndFloorNum) + "?");
                    findViewById(R.id.switch_floor_button).setVisibility(View.VISIBLE);
                } else if (!mDirectRouteExists) {
                    ((TextView) findViewById(R.id.navigation_text)).setText("Are you on floor 1?");
                    findViewById(R.id.switch_floor_button).setVisibility(View.VISIBLE);
                }
                //else if(mCurrentFloorNum == mEndFloorNum){
                else if (mDirectRouteExists && mCurrentFloorNum == mEndFloorNum) {
                    setNavDistanceView(mNavStartLocation, mNavEndPosition);
                }

                mGoButton.setVisibility(View.INVISIBLE);
            }

        });
        mGoButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Build info button - used to display floor picker
     */
    private void buildInfoButton() {
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
     * Create imageview for "start" location - start in this
     * context means navigation point selected during
     */
    private void initStartLocation() {
        mStartLocationImageView = (ImageView) findViewById(R.id.start_location);
        mStartLocationImageView.setVisibility(View.GONE);
    }


    /************************************************************************************
     * Build floor picker and set on click listener - used to select between building
     * floors
     ************************************************************************************/

    private void setCurrentFloorColor() {
        final int color = Color.parseColor("#F2A440");
        for(int i = 0; i < mScrollViewDirectChild.getChildCount(); i++) {
            TextView v = (TextView) mScrollViewDirectChild.getChildAt(i);
            if(v.getText().equals(Integer.toString(mCurrentFloorNum))) {
                v.setBackgroundColor(color);
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
                tv.setBackgroundColor(Color.parseColor("#F2A440"));
                int tmpFloorNum = tv.getText().equals("T") ? mMapTop.get("T")
                        : Integer.parseInt(tv.getText().toString());

                if(tv.getText().equals("T") && !mBlockNavigation) {
                    mBlockNavigation = true;
                }
                else if(mBlockNavigation) {
                    mBlockNavigation = false;
                }

                // don't redraw or show if we are currently on this floor
                if (tmpFloorNum != mCurrentFloorNum) {
                    unselectLastFloor(tv.getText().toString());
                    selectNextFloor(tmpFloorNum);
                }
            }
        });
    }

    /**
     * A new floor has been selected
     * @param lastFloor
     */
    private void unselectLastFloor(final String lastFloor) {

        // remove conference/huddle labels from last floor
        if(mRoomMarkerMap.containsKey(mCurrentFloorNum)) {
            for (Marker m : mRoomMarkerMap.get(mCurrentFloorNum)) {
                m.setVisible(false);
            }
        }

        // change unselected floor from blue to black
        for(int i = 0; i < mScrollViewDirectChild.getChildCount(); i++) {
            TextView child = (TextView) mScrollViewDirectChild.getChildAt(i);
            if(!child.getText().toString().equals(lastFloor)) {
                child.setBackgroundColor(Color.WHITE);
            }
        }
    }

    /**
     * A new floor has been selected - draw it and color in the floor
     * picker number
     * @param newFloorNum
     */
    private void selectNextFloor(final int newFloorNum) {
        if(mEndLocationMarker != null && newFloorNum != mEndFloorNum) {
            mEndLocationMarker.setVisible(false);
        }
        else if(mEndLocationMarker != null) {
            mEndLocationMarker.setVisible(true);
        }

        if(mFixedNavIcon != null && mNavigationOn && newFloorNum != mStartFloorNum) {
            mFixedNavIcon.setVisible(false);
        }
        else if(mFixedNavIcon != null && mNavigationOn) {
            mFixedNavIcon.setVisible(true);
        }
        else if(mFixedNavIcon != null){
            mFixedNavIcon.remove();
        }

        mGeoJsonMap.showLayer(mCurrentFloorNum, false);

        // change current floor
        mCurrentFloorNum = newFloorNum;

        // draw new layer
        mGeoJsonMap.showLayer(mCurrentFloorNum, true);

        // draw conference/huddle labels for selected floor
        if (mRoomMarkerMap.containsKey(mCurrentFloorNum)) {
            for (Marker m : mRoomMarkerMap.get(mCurrentFloorNum)) {
                m.setVisible(true);
            }
        }
        mAsyncColorOccupiedRoomsTask.run();

        if(mPolyLine != null) {
            mPolyLine.remove();
        }

        if((mNavigationOn && mFixedNavIcon != null) && (mCurrentFloorNum == mStartFloorNum
                || mEndFloorNum == mCurrentFloorNum)) {
            drawNavRouteOnFloorChange();
        }
    }

    /**
     * Create a floor picker based on the number of available floors
     */
    private void buildFloorPicker() {
        // Make the floor picker scrollable to handle many floors vs small screen size
        mScrollViewDirectChild = (LinearLayout) findViewById(R.id.scroll_view_direct_child);
        mScrollViewDirectChild.setVisibility(View.INVISIBLE);

        new AsyncGCSBuildingInfoReader(mBuildingName) {
            public void handleBuilding(final BuildingModel model) {
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

    /***********************************************************************************
     * Methods used to initialize navigation / update ui during navigation
     ***********************************************************************************/

    /**
     * start navigation
     */
    private void navigationStarted() {
        // if the slide view is not collapsed, collapse it
        if (!mCollapsed) {
            animateSlideDown();
        }
        else {
            mGoButton.setVisibility(View.VISIBLE);
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
                mNavStartLocation = new LatLng(tmp.latitude, tmp.longitude);
                setNavInformationView();
            }
        });

        mNavigationOn = true;
        findViewById(R.id.nav_info_layout).setVisibility(View.VISIBLE);
    }

    /**
     * Stop navigation
     */
    private void navigationHalted() {
        mStartLocationImageView.setVisibility(View.GONE);
        mNavigationOn = false;
        mMap.setOnCameraChangeListener(null);

        if(mPolyLine != null) mPolyLine.remove();

        findViewById(R.id.nav_info_layout).setVisibility(View.GONE);

        mGoButton.setVisibility(View.INVISIBLE);

        if(mFixedNavIcon != null) {
            mFixedNavIcon.remove();
            mFixedNavIcon = null;
        }

        findViewById(R.id.nav_info_layout).setBackgroundColor(Color.parseColor("#B5B5B5"));
        ((TextView)findViewById(R.id.navigation_text)).setText(null);
    }

    /**
     * Adjust navigation interface
     */
    private void startNavigation() {
        if(mBlockNavigation) {
            Toast.makeText(this, "Current floor is not valid - please select another.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!mNavigationOn && mEndLocationMarker == null) {
            Toast.makeText(this, "Please select a destination.", Toast.LENGTH_LONG).show();
        }
        else if(!mNavigationOn) {
            navigationStarted();
        }
        else {
            navigationHalted();
        }
    }

    /**
     * Display room/building/floor information
     */
    private void setNavInformationView() {
        GeoJsonFeature feature = getContainingFeature(mNavStartLocation);
        if(feature != null) {
            ((TextView) findViewById(R.id.navigation_text)).setText(
                    new StringBuilder()
                            .append("Building: ")
                            .append(mBuildingName)
                            .append(", Floor: ")
                            .append(mCurrentFloorNum)
                            .append(", Room: ")
                            .append(feature.getProperty("room")).toString()
            );
        }
    }

    /**
     * Draw a navigation route
     */
    private void drawNavRoute() {
        if(mPolyLine != null) {
            mPolyLine.remove();
        }
        mStartFloorNum = mCurrentFloorNum;
        MapRoute mRoute = new MapRoute(getApplicationContext(), mMap, mBuildingName, mCurrentFloorNum);
        if(mThreeStageRoute)
            mRoute.thirdStageRoute();
        mPolyLine = mRoute.drawRoute(mNavStartLocation, mStartFloorNum, mNavEndPosition, mEndFloorNum);
        mDirectRouteExists = mRoute.checkDirectRoute();
    }

    /**
     * Draw navigation route to elevator or stairs if we are not on the same floor
     */
    private void drawNavRouteOnFloorChange() {
        if (mNavigationOn) {
            if (mPolyLine != null) {
                mPolyLine.remove();
            }

            // if navigating between floors, draw the next route
            MapRoute mRoute = new MapRoute(getApplicationContext(), mMap, mBuildingName, mCurrentFloorNum);
            if(mThreeStageRoute)
                mRoute.thirdStageRoute();
            if(mNavStartLocation != null && mNavEndPosition != null) {
                mPolyLine = mRoute.drawRoute(mNavStartLocation, mStartFloorNum, mNavEndPosition, mEndFloorNum);
                mDirectRouteExists = mRoute.checkDirectRoute();
            }
        }
    }

    /**
     * Compute distance in feet between two lat lng points
     * @param start
     * @param end
     * @return
     */
    private int getLatLngDistance(LatLng start, LatLng end) {
        Location locationA = new Location("point A");

        locationA.setLatitude(start.latitude);
        locationA.setLongitude(start.longitude);

        Location locationB = new Location("point B");

        locationB.setLatitude(end.latitude);
        locationB.setLongitude(end.longitude);

        return (int)locationA.distanceTo(locationB) ;
    }

    private int metersToFeet(int meters) {
        final double feetConversion = 3.28084;
        return (int) (meters * feetConversion);
    }

    private String getWalkingTimeInMinutes(int meters) {
        final double walkingConvs = 1.4;
        double seconds = meters / walkingConvs;
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        double minutes = seconds / 60;
        return df.format(minutes) + " Minutes";
    }

    /**
     * If "Go" is selected to navigate, create a bitmap of the end location overlay and
     * add it to the map - this is done because our nav pin is an "illusion" - it is just
     * and imageview drawn at the center point of our map.
     * @param toFix
     */
    private void fixStartNavIcon(boolean toFix) {
        if(toFix) {
            findViewById(R.id.start_location).setVisibility(View.INVISIBLE);
            Bitmap image = BitmapUtil.resizeMapIcons(this, "location_start", IMAGE_VIEW_MARKER_WIDTH_DP,
                    IMAGE_VIEW_MARKER_HEIGHT_DP);

            mFixedNavIcon = mMap.addMarker(new MarkerOptions()
                    .position(mNavStartLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(image)));
            mFixedNavIcon.setVisible(true);
        }
        else {
            findViewById(R.id.start_location).setVisibility(View.VISIBLE);
        }
    }

    /**************************************************************************************
     * Adapters
     ***************************************************************************************/

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
                    mAmenitiesAdapter = new AmenityAdapter(getApplicationContext(), 0, occupancy, amenities);
                    mRoomAttributeListView.setAdapter(mAmenitiesAdapter);
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

    /********************************************************************************************
     * Initialze the google map, geojson and draw layers
     *******************************************************************************************/

    /**
     * read file from network and write to local storage
     * @param fileName
     */
    private void pullMapDataGCS(final String fileName) {
        // Acquire the map data
        new AsyncParseGeoJsonGCS(this, mBuildingName) {
            public void handleGeoJson(GeoJsonMap map, ProgressDialog dialog) {
                if(map != null) {
                    constructMap(map, dialog);
                }
            }
        }.execute();
    }

    /**
     * read geojson from internal file system
     * @param fileName
     */
    private void pullMapDataLocalCache(String fileName) {
        // Acquire the map data
        new AsyncParseGeoJsonFromFile(this, fileName) {
            public void handleGeoJson(GeoJsonMap map, ProgressDialog dialog) {
                if(map != null) {
                    constructMap(map, dialog);
                }
            }
        }.execute();
    }

    /**
     * Load map data and draw map
     */
    private void loadMapData() {
        final CachedGeoJsonDataDbHelper helper = new CachedGeoJsonDataDbHelper(getApplicationContext());
        final Integer timestamp = helper.getTimestamp(mBuildingName);

        /**
         * Get a timestamp from remote server - if the geojson has been updated (timestamp
         * is greater than local timestamp - download - else read local cache
         */
        new AsyncGCSGeoJsonTimestamp(mBuildingName) {
            public void handleTimestamp(Integer time) {

                File path = getFilesDir();
                String basePathJson = mBuildingName + ".json";
                File file = new File(path, basePathJson);

                // no data exists in our local storage
                if(timestamp == null) {
                    helper.addCacheInformation(mBuildingName, time);
                    pullMapDataGCS(file.getPath());
                }
                else if(timestamp < time) {
                    helper.updateCache(mBuildingName, time);
                    pullMapDataGCS(file.getPath());
                }
                else {
                    pullMapDataLocalCache(file.getPath());
                }
            }
        }.execute();

    }

    /**
     * initialize map variables, draw bitmap labels etc.
     * @param map
     * @param dialog
     */
    public void constructMap(GeoJsonMap map, ProgressDialog dialog) {
        // set the map
        mGeoJsonMap = map;

        //set the inital floor based on the searched room
        int initialFloor = MapConstants.DEFAULT_FLOOR;

        if(mRoomName != null) {
            initialFloor = getFloorNumber(mRoomName);
        }

        mGeoJsonMap.setCurrentLayer(initialFloor);

        // floors set for navigation
        mCurrentFloorNum = initialFloor;
        mEndFloorNum = initialFloor;
        mStartFloorNum = initialFloor;

        buildGoogleMap(dialog);
        setAndDrawRoomLabels();
        addImages();
    }

    /**
     * Get map context to draw geojson
     * @param dialog
     */
    public void buildGoogleMap(final ProgressDialog dialog) {
        if (mMap == null) {
            try {
                mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mMap = mMapFragment.getMap();
            }
            catch(NullPointerException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                mGeoJsonMap.setMap(mMap);

                /**
                 * clicking a marker should have the same effect as clicking the map -
                 * pass the information to callback handler
                 */
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
                        mapLoadedCallback(dialog);
                    }
                });
            }
        }
    }

    /**
     * Execute on map loaded - configure inital map appearance
     * @param dialog
     */
    private void mapLoadedCallback(ProgressDialog dialog) {
        final GeoJsonMapLayer currentLayer = mGeoJsonMap.getCurrentLayer();
        final int currentLayerNum = currentLayer.getFloorNum();

        // prepare map layers
        for (GeoJsonMapLayer layer : mGeoJsonMap.getLayers()) {
            mGeoJsonMap.drawLayer(layer.getFloorNum(), MapConstants.DEFAULT_FILL_COLOR,
                    MapConstants.DEFAULT_STROKE_COLOR,
                    MapConstants.DEFAULT_STROKE_WIDTH);
            mGeoJsonMap.showLayer(layer.getFloorNum(), false);
        }
        onFirstLoad(currentLayer);
        mGeoJsonMap.showLayer(currentLayerNum, true);

        // pull room occupany statuses - color in polygons
        startOccupiedRoomColorHandler();

        // map has loaded - close animation
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        findViewById(R.id.loading_overlay).setVisibility(View.GONE);
    }

    /**
     *  on map click events
     * @param latLng
     */
    private void mapListenCallback(LatLng latLng) {
        GeoJsonFeature feature = getContainingFeature(latLng);
        handleSelectedRoom(feature);
    }

    /**
     * Initial layer drawn - adjust camera and place marker on selected room
     * @param firstLayer
     */
    public void onFirstLoad(final GeoJsonMapLayer firstLayer) {
        LatLng center = Geometry.getRoomCenter(firstLayer, mRoomName);

        // carousel was clicked or room was not found - render building without
        // selected room
        if (center == null) {
            selectDefaultConfiguration();
        }

        // room was selected - zoom to room, add marker
        else {
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center,
                    MapConstants.DEFAULT_CAMERA_ZOOM_LEVEL);

            GeoJsonFeature feature = getContainingFeature(center);
            if (feature != null) {
                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                Polygon gmsPoly = polygon.getGMSPolygon();
                gmsPoly.setFillColor(MapConstants.DEFAULT_SELECTED_ROOM_COLOR);
            }

            mMap.moveCamera(cameraUpdate);
            mEndLocationMarker = mMap.addMarker(new MarkerOptions().position(center)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.resizeMapIcons(getApplicationContext(),
                            "location_end", IMAGE_VIEW_MARKER_WIDTH_DP, IMAGE_VIEW_MARKER_HEIGHT_DP))));

            mNavEndPosition = mEndLocationMarker.getPosition();
        }
        setCurrentFloorColor();
    }

    /**
     * Carousel item selected from home activity or no room name found in geojson -
     * select default coordinates/floor/zoom level for selected building
     */
    private void selectDefaultConfiguration() {
        animateSlideDown();
        findViewById(R.id.add_to_favorites).setVisibility(View.INVISIBLE);
        LatLng center = MapConstants.DEFAULT_COORD_MAP.get(mBuildingName);
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center,
                MapConstants.DEFAULT_BUILDING_ZOOM_LEVEL);
        mMap.moveCamera(cameraUpdate);
        mRoomNameTextView.setText(mBuildingName);
    }

    /**
     * A room has been selected
     * @param feature
     */
    private void handleSelectedRoom(final GeoJsonFeature feature) {

        // don't allow new end destination selection during navigation
        if(feature != null && !mNavigationOn) {
            findViewById(R.id.add_to_favorites).setVisibility(View.VISIBLE);
            GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();

            // Attach marker to correct map polygon
            if(mEndLocationMarker != null) {
                mEndLocationMarker.remove();
            }

            // place marker on selected location
            LatLng center = polygon.getCentroid();
            mEndLocationMarker = mMap.addMarker(new MarkerOptions().position(center).
                    icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.resizeMapIcons(getApplicationContext(),
                            "location_end", IMAGE_VIEW_MARKER_WIDTH_DP, IMAGE_VIEW_MARKER_HEIGHT_DP))));


            // need to keep track of previous color when new poly is selected...
            int lastFillTmp = polygon.getGMSPolygon().getFillColor();
            polygon.getGMSPolygon().setFillColor(MapConstants.DEFAULT_SELECTED_ROOM_COLOR);
            if(mCurrentSelectedPoly != null) {
                mCurrentSelectedPoly.getGMSPolygon().setFillColor(mLastFillColor);
            }
            mLastFillColor = lastFillTmp;
            mCurrentSelectedPoly = polygon;

            // route has changed - remove the polygon
            if (mNavigationOn && mPolyLine != null) {
                mPolyLine.remove();
            }

            mNavEndPosition = mEndLocationMarker.getPosition();
            mEndFloorNum = mCurrentFloorNum;

            // Update room attributes view
            String selectedRoomName = feature.getProperty(GeoJsonConstants.ROOM_TAG);
            mRoomNameTextView.setText(selectedRoomName);

            // fetch and display room data
            inflateRoomAttributes(selectedRoomName, mBuildingName);
        }
    }

    /**
     * Update ui room color
     * @param model
     * @param gmsPoly
     */
    private void colorRoomsOnUiThread(final RoomModel model, final Polygon gmsPoly) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (model.getOccupancyStatus().equals("N")) {
                    gmsPoly.setFillColor(MapConstants.DEFAULT_UNOCCUPIED_COLOR);
                } else if (model.getOccupancyStatus().equals("Y")) {
                    gmsPoly.setFillColor(MapConstants.DEFAULT_OCCUPIED_COLOR);
                }
            }
        });
    }
    /**
     * Color rooms based on occupancy status
     * @param rooms
     */
    private void colorRooms(final List<RoomModel> rooms) {
        new Thread() {
            public void run() {
                GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
                for(RoomModel model : rooms) {
                    for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                        if (model.getRoomName().equals(feature.getProperty(GeoJsonConstants.ROOM_TAG))) {
                            if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {

                                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                                Polygon gmsPoly = polygon.getGMSPolygon();

                                colorRoomsOnUiThread(model, gmsPoly);
                            }
                        }
                    }
                }
            }
        }.run();
    }

    /**
     * Create timed async task that updates occupancy
     */
    public void startOccupiedRoomColorHandler() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        mAsyncColorOccupiedRoomsTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        new AsyncGCSMultiRoomInfoReader(mBuildingName) {
                            public void handleRooms(List<RoomModel> rooms) {
                                if (rooms != null) {
                                    colorRooms(rooms);
                                }
                            }
                        }.execute();
                    }
                });
            }
        };

        // call every minute - cron job runs every one minute on backend
        timer.schedule(mAsyncColorOccupiedRoomsTask, 0, 60000);
    }

    /**
     * Fetch room names of this building
     */
    private void setAndDrawRoomLabels() {
        new AsyncGCSMultiRoomInfoReader(mBuildingName) {
            public void handleRooms(List<RoomModel> rooms) {
                if (rooms != null) {
                    mRooms = rooms;
                    addRoomNames();
                    for (Marker m : mRoomMarkerMap.get(mCurrentFloorNum)) {
                        m.setVisible(true);
                    }
                }
            }
        }.execute();
    }

    private void addRoomName(GeoJsonMapLayer layer, GeoJsonPolygon poly, BitmapDescriptor text) {
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

    /**
     * Add room name bitmaps that will be drawn on map
     */
    private void addRoomNames() {
        for(RoomModel model : mRooms) {
            String modelName = model.getRoomName();
            for(final GeoJsonMapLayer layer : mGeoJsonMap.getLayers()) {
                for(GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
                    try {
                        String roomName = feature.getProperty(GeoJsonConstants.ROOM_TAG);
                        if (roomName != null) {
                            if (roomName.equals(modelName)) {
                                GeoJsonPolygon poly = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                                BitmapDescriptor text = BitmapUtil.getTextMarker(roomName);

                                addRoomName(layer, poly, text);
                            }
                        }
                    }catch(NullPointerException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Get image for bathrooms, stairs etc.
     * @param c
     * @param roomName
     * @return
     */
    private Bitmap getImage(Context c, String roomName) {
        Bitmap image = null;
        switch (roomName) {
            case MapConstants.WOMENS_BATHROOM:
                image = BitmapUtil.resizeMapIcons(c, "wb_icon", IMAGE_VIEW_ROOM_ICON_WIDTH_DP,
                        IMAGE_VIEW_ROOM_ICON_HEIGHT_DP);
                break;
            case MapConstants.MENS_BATHROOM:
                image = BitmapUtil.resizeMapIcons(c, "mb_icon",IMAGE_VIEW_ROOM_ICON_WIDTH_DP,
                        IMAGE_VIEW_ROOM_ICON_HEIGHT_DP);
                break;
            case MapConstants.STAIRS:
                image = BitmapUtil.resizeMapIcons(c, "stairs_icon",IMAGE_VIEW_ROOM_ICON_WIDTH_DP,
                        IMAGE_VIEW_ROOM_ICON_HEIGHT_DP);
                break;
            case MapConstants.ELEVATOR:
                image = BitmapUtil.resizeMapIcons(c, "elevator_icon", IMAGE_VIEW_ROOM_ICON_WIDTH_DP,
                        IMAGE_VIEW_ROOM_ICON_HEIGHT_DP);

        }
        return image;
    }

    /**
     * Add bitmap image to map
     * @param layer
     * @param poly
     * @param image
     */
    private void addImage(GeoJsonMapLayer layer, GeoJsonPolygon poly, Bitmap image) {
        if (image != null) {
            Marker bmMarker = mMap.addMarker(new MarkerOptions()
                    .position(poly.getCentroid())
                    .icon(BitmapDescriptorFactory.fromBitmap(image)));
            bmMarker.setVisible(false);
            if (mRoomMarkerMap.containsKey(layer.getFloorNum())) {
                mRoomMarkerMap.get(layer.getFloorNum()).add(bmMarker);
            } else {
                List<Marker> markers = new ArrayList<>();
                markers.add(bmMarker);
                mRoomMarkerMap.put(layer.getFloorNum(), markers);
            }
        }
    }

    /**
     * Add all images
     */
    private void addImages() {
        Iterator<GeoJsonMapLayer> iter = mGeoJsonMap.getLayers().iterator();
        while(iter.hasNext()) {
            GeoJsonMapLayer next = iter.next();

            for(GeoJsonFeature feature : next.getGeoJson().getGeoJsonFeatures()) {
                try {
                    GeoJsonPolygon poly = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();

                    String roomName = feature.getProperty(GeoJsonConstants.ROOM_TAG);
                    Bitmap image = getImage(getApplicationContext(), roomName);

                    addImage(next, poly, image);

                }
                catch (NullPointerException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                catch(ClassCastException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }
    }


    /****************************************************************************************
     * Room capacity and amenities slide view animation methods - used to pull
     * view up and down to reveal map/information button
     ****************************************************************************************/

    /**
     * Allow slide view to be swiped up and down
     */
    private void setSlideViewOnTouchListener() {
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
                                slideDown(currentHeight, delta);
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
     * Half of the room activity screen is made up of room stats - allow
     * the user to slide this view done and reveal more of the map
     */
    private void buildSlideView() {

        mAttributeLayout = (LinearLayout) findViewById(R.id.attribute_list_view);

        mRoomAttributeListView = (ListView) findViewById(R.id.roomInfoList);

        mRoomNameTextView = (TextView) findViewById(R.id.roomNameText);
        mRoomNameTextView.setText(mRoomName);
        mRoomNameTextView.setTextColor(Color.WHITE);

        setSlideViewOnTouchListener();
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
    private void dispatchAnimationLock(long timeInMilli, final boolean showButtons) {
        if(!showButtons) {
            mInfoButton.setVisibility(View.INVISIBLE);
            if(mGoButton.isShown()) {
                mGoButton.setVisibility(View.INVISIBLE);
            }
        }
        mAnimationInEffect = true;
        Runnable runnable = new Runnable() {
            public void run() {
                mAnimationInEffect = false;

                if(showButtons) {
                    mInfoButton.setVisibility(View.VISIBLE);
                }

                if(mNavigationOn && mFixedNavIcon == null && showButtons) {
                    mGoButton.setVisibility(View.VISIBLE);
                }

            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, timeInMilli);
    }

    /**
     * Animate slide down to min height
     * @param currentHeight
     * @param delta
     */
    private void slideDown(float currentHeight, float delta) {
        if (currentHeight >= THRESHOLD_SLIDE_DOWN_PRECENTAGE * mViewMaxHeight) {
            ViewGroup.LayoutParams params = findViewById(R.id.attribute_list_view).getLayoutParams();
            params.height -= delta;

            findViewById(R.id.attribute_list_view).setLayoutParams(params);

            mViewCurrentHeight -= delta;
        }
        else {
            animateSlideDown();
        }
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
            dispatchAnimationLock(SLIDE_DOWN_ANIM_DURATION, false);
            view.startAnimation(heightAnim);

            mCollapsed = false;
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
            dispatchAnimationLock(SLIDE_UP_ANIM_DURATION, false);
            view.startAnimation(heightAnim);
        }
    }

    /**
     * Animate sliding down to min height
     */
    private void animateSlideDown() {
        mViewCurrentHeight = mViewMinHeight;

        LinearLayout view = (LinearLayout) findViewById(R.id.attribute_list_view);
        HeightAnimation heightAnim = new HeightAnimation(view, view.getHeight(), mViewMinHeight);
        heightAnim.setDuration(SLIDE_DOWN_ANIM_DURATION);
        dispatchAnimationLock(SLIDE_DOWN_ANIM_DURATION, true);
        view.startAnimation(heightAnim);

        mCollapsed = true;
    }

    /**
     * Custom animation class - used to animate slide view
     */
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

    /****************************************************************************************
     * Utility methods
     ****************************************************************************************/


    /**
     * Get floor number - will in most cases be the first number in the string
     * @param roomName
     * @return
     */
    private int getFloorNumber(String roomName) {
        for (Character ch : roomName.toCharArray()) {
            if (Character.isDigit(ch)) {
                return Integer.parseInt(Character.toString(ch));
            }
        }
        return -1;
    }

    /**
     * Get geojson feature by latlng
     * @param latLng
     * @return
     */
    private GeoJsonFeature getContainingFeature(LatLng latLng) {
        GeoJsonMapLayer layer = mGeoJsonMap.getCurrentLayer();
        for (GeoJsonFeature feature : layer.getGeoJson().getGeoJsonFeatures()) {
            if (feature.getGeoJsonGeometry().getType().equals(GeoJsonConstants.POLYGON)) {
                GeoJsonPolygon polygon = (GeoJsonPolygon) feature.getGeoJsonGeometry().getGeometry();
                if (polygon.contains(latLng)) {
                    return feature;
                }
            }
        }
        return null;
    }

    /***********************************************************************************
     * Activity menu methods
     ***********************************************************************************/


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

    /************************************************************************************
     * Dialogs
     ************************************************************************************


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
}