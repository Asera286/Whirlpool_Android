package edu.msu.elhazzat.whirpool.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import edu.msu.elhazzat.whirpool.R;
import edu.msu.elhazzat.whirpool.adapter.RoomSearchAdapter;

public class SearchActivity extends Activity {
    private RoomSearchAdapter mAdapter;
    private ListView mList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        setContentView(R.layout.activity_search);

        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                String room = (String) mList.getItemAtPosition(position);
                Intent roomIntent = new Intent(getApplicationContext(), RoomActivity.class);
                roomIntent.putExtra("ROOM_D", room);
                startActivity(roomIntent);
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView sV = (SearchView)findViewById(R.id.searchView);
        sV.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

      //  new AsyncRoomParseFromResource(this, new HandleAsyncRoomReader()).execute();

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
            case (R.id.filter_button):
                LinearLayout lin = (LinearLayout)findViewById(R.id.filters);
                lin.setVisibility(lin.isShown() ? LinearLayout.GONE : LinearLayout.VISIBLE);
                break;
        }
    }
}