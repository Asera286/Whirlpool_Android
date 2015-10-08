package edu.msu.elhazzat.whirpool;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Stephanie on 10/1/2015.
 */
public class RoomActivity extends Activity {

    private String roomName;
    private String[] roomsDummyInfo = {"Projector", "Fridge", "Blah", "More Blah", "Other Stuff", "More Stuff", "Just Stuff"};
    private ListView roomListView;
    private ArrayAdapter arrayAdapter;
    TextView roomTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);

        Bundle b = getIntent().getExtras();
        if(b!=null) roomName = b.getString("room");

        roomTextView = (TextView) findViewById(R.id.roomNameText);
        roomTextView.setText(roomName);

        roomListView = (ListView) findViewById(R.id.roomInfoList);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, roomsDummyInfo);
        roomListView.setAdapter(arrayAdapter);
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