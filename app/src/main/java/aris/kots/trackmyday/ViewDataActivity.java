package aris.kots.trackmyday;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import aris.kots.trackmyday.Database.DBAdapter;
import aris.kots.trackmyday.Recyclerview.ListViewItem;
import aris.kots.trackmyday.Recyclerview.RecyclerViewAdapter;

public class ViewDataActivity extends AppCompatActivity {

    private String TAG = "ViewDataActivity";
    RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    ArrayList<ListViewItem> items;
    DBAdapter db = new DBAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        applyFontForToolbarTitle(ViewDataActivity.this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclelist);
        items = new ArrayList<>();

        insertItems(items);


        adapter = new RecyclerViewAdapter(ViewDataActivity.this, items, getAssets());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewDataActivity.this));
        Log.i(TAG,String.valueOf(db.getSzie()/1024.0/1024.0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void insertItems(ArrayList<ListViewItem> items) {
        items.clear();
        db.open();
        final Cursor c = db.getAllRecordings();
        if (c.moveToFirst()) {
            do {
                if(c.getInt(6) == 1) {
                    items.add(new ListViewItem() {
                        {
                            recording_id = c.getLong(0);
                            name = c.getString(1);
                            date = c.getString(2);
                            distance = c.getString(3);
                            speed = c.getString(4);
                            elapse_time = c.getLong(5);
                        }
                    });
                }
            } while (c.moveToNext());
        }
        c.close();
        db.close();
    }
    public void applyFontForToolbarTitle(Activity context){
        try {
            Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View view = toolbar.getChildAt(i);
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    Typeface titleFont = Typeface.
                            createFromAsset(context.getAssets(), "fonts/Roboto-Black.ttf");
                    if (tv.getText().equals(context.getTitle())) {
                        tv.setTypeface(titleFont);
                        break;
                    }
                }
            }
        }catch(Exception ex){
            Log.e(TAG,"Failed to change title Font");
        }
    }
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }
}
