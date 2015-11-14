package edu.msu.elhazzat.whirpool.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonConstants;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonFeature;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMap;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonMapLayer;
import edu.msu.elhazzat.whirpool.geojson.GeoJsonPolygon;
import edu.msu.elhazzat.whirpool.geojson.Geometry;
import edu.msu.elhazzat.whirpool.model.BuildingModel;
import edu.msu.elhazzat.whirpool.model.EventModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSBuildingInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSRoomInfoReader;
import edu.msu.elhazzat.whirpool.rest.AsyncParseGeoJsonGCS;
import edu.msu.elhazzat.whirpool.utils.RoomNameRegexMapper;
import edu.msu.elhazzat.whirpool.utils.WIMAppConstants;

public class RoomActivity extends AppCompatActivity {

    private static final String EVENT = "EVENT";
    private static final String ROOM = "ROOM";
    private static final long SLIDE_DOWN_ANIM_DURATION = 250;
    private static final long SLIDE_UP_ANIM_DURATION = 400;
    private static final float THRESHOLD_SLIDE_DOWN_PRECENTAGE = .70f;
    private static final float THRESHOLD_SLIDE_UP_PERCENTAGE = .30f;
    private static final float ATTRIBUTE_MAX_VIEW_PERCENTAGE = .95f;

    private ArrayAdapter<String> mAmenitiesAdapter;
    private ListView mRoomAttributeListView;
    TextView mRoomNameTextView;

    private GeoJsonMap mGeoJsonMap;
    private Marker mCurrentMarker;

    private EventModel mEvent = null;
    private RoomModel mRoomModel = null;

    private String mBuildingName;
    private int mNumFloors;
    private String mRoomName;

    private String mBuildingAbbrName;
    private String mGeoJsonRoomName;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mEvent = (EventModel) b.getParcelable(EVENT);
            if(mEvent == null) {
                mRoomModel = (RoomModel) b.getParcelable(ROOM);
            }
        }

        if (mEvent != null && mEvent.getLocation() != null) {
            String location = mEvent.getLocation();
            mBuildingName = RoomNameRegexMapper.getBuildingNameFromResource(location);
            mBuildingAbbrName = WIMAppConstants.WHIRLPOOL_ABBRV_MAP.get(mBuildingName);
            mRoomName = RoomNameRegexMapper.getRoomNameFromResource(location);
            mGeoJsonRoomName = RoomNameRegexMapper.getGeoJsonRoomNameFromMap(mBuildingName, mRoomName);
        }

        else if(mRoomModel != null) {
            mBuildingAbbrName = mRoomModel.getBuildingName();
            mGeoJsonRoomName = mRoomModel.getRoomName();
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8286F8")));
        }

        mRoomNameTextView = (TextView) findViewById(R.id.roomNameText);
        mRoomNameTextView.setText(mGeoJsonRoomName);
        mRoomNameTextView.setTextColor(Color.BLUE);

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
                            if(mCollapsed && delta < 0) {
                                slideUp(currentHeight, delta);
                            }
                            else if(delta > 0) {
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

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float heightdp = displayMetrics.heightPixels / displayMetrics.density;

        mAttributeLayout = (LinearLayout) findViewById(R.id.attribute_list_view);
        mAttributeLayout.getLayoutParams().height = (int) (heightdp * ATTRIBUTE_MAX_VIEW_PERCENTAGE);

        mRoomAttributeListView = (ListView) findViewById(R.id.roomInfoList);

        mScrollViewDirectChild = (LinearLayout) findViewById(R.id.scroll_view_direct_child);

        mScrollViewDirectChild.setVisibility(View.INVISIBLE);

        buildScrollView();
        getLayoutDimensions();
        setUpView();
    }

    private void buildScrollView() {
        new AsyncGCSBuildingInfoReader(mBuildingAbbrName) {

            public void handleBuilding(BuildingModel model) {
                if(model != null) {
                    mNumFloors = model.getFloors();
                    for (int i = 1; i < mNumFloors + 1; i++) {
                        TextView tv = new TextView(getApplicationContext());
                        tv.setWidth(50);
                        tv.setTextSize(25f);
                        String text = Integer.toString(i);
                        tv.setText(text);
                        tv.setTextColor(Color.BLACK);
                        tv.setGravity(Gravity.CENTER);
                        tv.setBackgroundResource(R.drawable.floor_picker_border);

                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TextView tv = (TextView) v;
                                tv.setTextColor(Color.BLUE);
                                mGeoJsonMap.drawLayer(Integer.parseInt(tv.getText().toString()), WIMAppConstants.MAP_DEFAULT_FILL_COLOR,
                                        WIMAppConstants.MAP_DEFAULT_STROKE_COLOR,
                                        WIMAppConstants.MAP_DEFAULT_STROKE_WIDTH);
                                for(int i = 0; i < mScrollViewDirectChild.getChildCount(); i++) {
                                    TextView child = (TextView) mScrollViewDirectChild.getChildAt(i);
                                    if(!child.getText().toString().equals(tv.getText().toString())) {
                                        child.setTextColor(Color.BLACK);
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

    private void getLayoutDimensions() {
        final ViewTreeObserver vto = mAttributeLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mViewMaxHeight = mAttributeLayout.getHeight();
                mViewMinHeight = findViewById(R.id.roomNameText).getHeight();
                mViewCurrentHeight = mViewMaxHeight;
                mAttributeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);            }
        });
    }

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

    private void slideUp(float currentHeight, float delta) {
        if (currentHeight <= THRESHOLD_SLIDE_UP_PERCENTAGE * mViewMaxHeight) {
            ViewGroup.LayoutParams params = findViewById(R.id.attribute_list_view).getLayoutParams();
            params.height -= delta;
            findViewById(R.id.attribute_list_view).setLayoutParams(params);
            mViewCurrentHeight -= delta;
        }
        else {
            mViewCurrentHeight = mViewMaxHeight;
            LinearLayout view = (LinearLayout) findViewById(R.id.attribute_list_view);
            HeightAnimation heightAnim = new HeightAnimation(view, view.getHeight(), mViewMaxHeight);
            heightAnim.setDuration(SLIDE_DOWN_ANIM_DURATION);
            dispatchAnimationLock(SLIDE_DOWN_ANIM_DURATION);
            view.startAnimation(heightAnim);
            mCollapsed = false;
            mScrollViewDirectChild.setVisibility(View.INVISIBLE);
        }
    }

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

    private void inflateRoomAttributes(String geoJsonRoomName, String buildingAbbr) {
        AsyncGCSRoomInfoReader reader = new AsyncGCSRoomInfoReader(buildingAbbr, geoJsonRoomName) {
            @Override
            public void handleRoom(RoomModel room) {
                if (room != null) {
                    String[] amenities = room.getAmenities();
                    mAmenitiesAdapter = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.room_simple_text, amenities);
                    mRoomAttributeListView.setAdapter(mAmenitiesAdapter);
                } else {
                    mRoomAttributeListView.setAdapter(null);
                }
            }
        };
        reader.execute();
    }

    private void setUpView() {
        new AsyncParseGeoJsonGCS(mBuildingAbbrName) {
            public void handleGeoJson(GeoJsonMap map) {
                mGeoJsonMap = map;
                int initialFloor = getInitialFloor(mGeoJsonRoomName);
                mGeoJsonMap.setCurrentLayer(initialFloor);
                setUpMap();
                inflateRoomAttributes(mGeoJsonRoomName, mBuildingAbbrName);
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

                LatLng center = Geometry.getRoomCenter(layer, mGeoJsonRoomName);

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
            }
        });
    }


    private void mapListen() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
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

                            String selectedRoomName = feature.getProperty(GeoJsonConstants.ROOM_TAG);
                            mRoomNameTextView.setText(selectedRoomName);
                            inflateRoomAttributes(selectedRoomName, mBuildingAbbrName);
                        }
                        else {
                            Polygon gmsPoly = polygon.getGMSPolygon();
                            int color1 = WIMAppConstants.MAP_DEFAULT_FILL_COLOR;
                            gmsPoly.setFillColor(color1);
                        }
                    }
                }
            }
        });
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
            case R.id.action_search:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
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
}