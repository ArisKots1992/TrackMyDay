package aris.kots.trackmyday;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    boolean bike = true;
    boolean walk = true;
    boolean car = true;
    boolean bus = true;
    boolean auto = true;
    private Menu menu;
//    TextView bikeText;
//    TextView walkText;
//    TextView carText;
//    TextView runText;
//    TextView trainText;
//    TextView tramText;
//    TextView busText;
    TextView scoreLabel;
    TextView score;
    TextView activities_label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        applyFontForToolbarTitle(MainActivity.this);

        activities_label = (TextView) findViewById(R.id.activities_label);


        scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        score = (TextView) findViewById(R.id.score);

        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");
        Typeface troika = Typeface.createFromAsset(getAssets(), "fonts/troika.otf");

        activities_label.setTypeface(roboto);
        scoreLabel.setTypeface(roboto);
        score.setTypeface(troika);

        // Button functionality
        mehdi.sakout.fancybuttons.FancyButton start_tracking = (mehdi.sakout.fancybuttons.FancyButton) findViewById(R.id.start_tracking);
        if (start_tracking != null) {
            start_tracking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, TrackActivity.class);
                   // Bundle extras = new Bundle();
                    //extras.putBooleanArray("detectable_activities", new boolean[]{bike, walk, car, bus});
                    //intent.putExtras(extras);
                    startActivityForResult(intent, 0);
                    overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                }
            });
        }
        mehdi.sakout.fancybuttons.FancyButton viewData = (mehdi.sakout.fancybuttons.FancyButton) findViewById(R.id.view_data);
        if (viewData != null) {
            viewData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ViewDataActivity.class);
                    startActivityForResult(intent, 0);
                    overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                }
            });
        }
        mehdi.sakout.fancybuttons.FancyButton test = (mehdi.sakout.fancybuttons.FancyButton) findViewById(R.id.view_statistics);
        if (test != null) {
            test.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ViewStatistics.class);
                    startActivityForResult(intent, 0);
                    overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//        invalidateOptionsMenu();
//        if (id == R.id.auto) {
//            if (auto) {
//                item.getIcon().setAlpha(65);
//                menu.findItem(R.id.bike).getIcon().setAlpha(65);
//                menu.findItem(R.id.walk).getIcon().setAlpha(65);
//                menu.findItem(R.id.car).getIcon().setAlpha(65);
//                menu.findItem(R.id.bus).getIcon().setAlpha(65);
//                auto = false;
//                bike = false;
//                car = false;
//                bus = false;
//                walk = false;
//
//            } else {
//                item.getIcon().setAlpha(255);
//                menu.findItem(R.id.bike).getIcon().setAlpha(255);
//                menu.findItem(R.id.walk).getIcon().setAlpha(255);
//                menu.findItem(R.id.car).getIcon().setAlpha(255);
//                menu.findItem(R.id.bus).getIcon().setAlpha(255);
//                auto = true;
//                bike = true;
//                car = true;
//                bus = true;
//                walk = true;
//
//            }
//            return true;
//        } else if (id == R.id.bike) {
//            if (bike) {
//                item.getIcon().setAlpha(65);
//                bike = false;
//            } else {
//                item.getIcon().setAlpha(255);
//                bike = true;
//            }
//            checkAuto();
//            return true;
//        } else if (id == R.id.walk) {
//            if (walk) {
//                item.getIcon().setAlpha(65);
//                walk = false;
//            } else {
//                item.getIcon().setAlpha(255);
//                walk = true;
//            }
//            checkAuto();
//            return true;
//        } else if (id == R.id.car) {
//            if (car) {
//                item.getIcon().setAlpha(65);
//                car = false;
//            } else {
//                item.getIcon().setAlpha(255);
//                car = true;
//            }
//            checkAuto();
//            return true;
//        } else if (id == R.id.bus) {
//            if (bus) {
//                item.getIcon().setAlpha(65);
//                bus = false;
//            } else {
//                item.getIcon().setAlpha(255);
//                bus = true;
//            }
//            checkAuto();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
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
    void checkAuto() {
        if (bus && car && bike && walk) {
            auto = true;
            menu.findItem(R.id.auto).getIcon().setAlpha(255);
        } else {
            auto = false;
            menu.findItem(R.id.auto).getIcon().setAlpha(65);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
