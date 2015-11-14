package edu.msu.elhazzat.whirpool.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.SearchAdapter;
import edu.msu.elhazzat.whirpool.model.BuildingModel;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.rest.AsyncGCSAllRooms;
import edu.msu.elhazzat.whirpool.utils.WIMAppConstants;

/**
 * Created by christianwhite on 11/11/15.
 */
public class SearchActivity extends AppCompatActivity {

    private static final String LOG_TAG = SearchActivity.class.getSimpleName();
    private static final String TOOL_BAR_COLOR = "#F9DC71";
    private static final String ROOM_BUNDLE_EXTRA_KEY = "ROOM";

    private List<BuildingModel> mBuildingModels = new ArrayList<>();
    private List<BuildingModel> mBuildingModelsFiltered = new ArrayList<>();

    private ExpandableListView mListView;
    private SearchAdapter mSearchAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        buildSearchView();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor(TOOL_BAR_COLOR)));
        }
    }

    /**
     * Create searchable list view
     */
    private void buildSearchView() {
        mListView = (ExpandableListView) findViewById(R.id.exp_list_view);
        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (mListView.isGroupExpanded(groupPosition)) {
                    mListView.collapseGroup(groupPosition);
                } else {
                    mListView.expandGroup(groupPosition, true);
                }
                return true;
            }
        });

        // Navigate to a map if a room has been selected
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                RoomModel model = mSearchAdapter.getChild(groupPosition, childPosition);
                Intent roomIntent = new Intent(getApplicationContext(), RoomActivity.class);
                roomIntent.putExtra(ROOM_BUNDLE_EXTRA_KEY, model);
                startActivity(roomIntent);
                return false;
            }
        });

        // Fetch room data and populate search view
        new AsyncGCSAllRooms() {
            public void handleBuildings(List<BuildingModel> items) {
                for(BuildingModel model : items) {
                    if(WIMAppConstants.WHIRLPOOL_ABBRV_MAP.containsValue(model.getBuildingName())) {
                        mBuildingModelsFiltered.add(model);
                        mBuildingModels.add(model);
                    }
                }
                mSearchAdapter = new SearchAdapter(getApplicationContext(), mBuildingModelsFiltered);
                mListView.setAdapter(mSearchAdapter);
            }
        }.execute();
    }

    private void doSearch(String queryStr) {
        filterData(queryStr);
    }

    /**
     * Maintain two lists - the full list of data and the filtered one
     * @param query
     */
    public void filterData(String query) {

        query = query.toLowerCase();
        mBuildingModelsFiltered.clear();

        // Empty query - use the full list of values
        if(query.isEmpty()){
            mBuildingModelsFiltered.addAll(mBuildingModels);
            mSearchAdapter.notifyDataSetChanged();
            for ( int i = 0; i < mBuildingModelsFiltered.size(); i++ ) {
                mListView.collapseGroup(i);
            }
        }

        // Reset filtered list with values containing query
        else {

            for(BuildingModel building: mBuildingModels) {

                List<RoomModel> roomList = building.getRooms();
                List<RoomModel> newList = new ArrayList<RoomModel>();
                if(roomList != null) {
                    for (RoomModel room : roomList) {
                        if (room.getRoomName().toLowerCase().contains(query)) {
                            newList.add(room);
                        }
                    }
                    if (newList.size() > 0) {
                        BuildingModel nBuildingModel = new BuildingModel(building.getBuildingName(),
                                newList);
                        mBuildingModelsFiltered.add(nBuildingModel);
                    }
                }
            }
            mSearchAdapter.notifyDataSetChanged();
            for ( int i = 0; i < mBuildingModelsFiltered.size(); i++ ) {
                mListView.expandGroup(i);
            }
        }
    }

    /**
     * Create search menu
     *
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                doSearch(query);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


}