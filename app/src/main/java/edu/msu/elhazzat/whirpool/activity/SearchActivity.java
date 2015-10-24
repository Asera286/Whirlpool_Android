package edu.msu.elhazzat.whirpool.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.RoomSearchAdapter;
import edu.msu.elhazzat.whirpool.model.RoomModel;
import edu.msu.elhazzat.whirpool.utils.AsyncResourceReader;
import edu.msu.elhazzat.whirpool.utils.TokenHolder;

/**
 *
 */
public class SearchActivity extends Activity {

    public static final String WHIRLPOOL_RESOURCE_URL =  "https://apps-apis.google.com/a/feeds/calendar/resource/2.0/whirlpool.com/";
    private RoomSearchAdapter mAdapter;
    private ListView mList;
    private AsyncResourceReader mResourceReader;
    private List<RoomModel> mRoomModelListValues = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        setContentView(R.layout.activity_search);

        String token = TokenHolder.getInstance().getToken();

        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                RoomModel room = mAdapter.getRoomModel(position);
                Intent roomIntent = new Intent(getApplicationContext(), RoomActivity.class);
                Bundle extras = new Bundle();
                extras.putString("ROOM_ID", room.getRoomName());
                extras.putString("ROOM_EMAIL", room.getEmail());
                roomIntent.putExtras(extras);
                startActivity(roomIntent);
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView sV = (SearchView)findViewById(R.id.searchView);
        sV.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return true;
            }
        };
        sV.setOnQueryTextListener(searchQueryListener);

        mResourceReader = new AsyncResourceReader() {
            @Override
            public void handleRooms(List<RoomModel> rooms) {
                if(rooms != null) {
                    for (RoomModel roomModel : rooms) {
                        mRoomModelListValues.add(roomModel);
                    }
                    mAdapter = new RoomSearchAdapter(getApplicationContext(),
                            android.R.layout.simple_list_item_1, mRoomModelListValues);
                    mList.setAdapter(mAdapter);
                }
            }
        };

        mResourceReader.execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    private void doSearch(String queryStr) {
        mAdapter.sort();
        mAdapter.getFilter().filter(queryStr);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.cancel_button):
                this.finish();
                break;
        }
    }
}