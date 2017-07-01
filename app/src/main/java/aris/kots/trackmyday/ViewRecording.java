package aris.kots.trackmyday;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goebl.simplify.PointExtractor;
import com.goebl.simplify.Simplify;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.EdgeDetail;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.charts.SeriesLabel;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import aris.kots.trackmyday.CleverFunctions.ConvertFunctons;
import aris.kots.trackmyday.DataClass.ConstantValues;
import aris.kots.trackmyday.DataClass.DataPoint;
import aris.kots.trackmyday.DataClass.Segment;
import aris.kots.trackmyday.DataClass.Segmentation;
import aris.kots.trackmyday.Database.DBAdapter;
import aris.kots.trackmyday.Recyclerview.ListViewItem;

import static aris.kots.trackmyday.CleverFunctions.MathFunctions.CalculationByDistance;

public class ViewRecording extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "ViewRecording";
    private static int WALKz = 1;
    private static int RUNz = 2;
    private static int VEHICLEz = 3;
    private long RECORDING_ID = -1;
    private TextView distance;
    private TextView speed;
    private TextView elapseTime;
    private TextView distanceLabel;
    private TextView speedLabel;
    private TextView elapseTimeLabel;
    private TextView labelActivities;
    private Button walk;
    private Button run;
    private Button vehicle;
    private View walk_line;
    private View run_line;
    private View car_line;
    private GoogleMap mMap = null;
    private List<Polyline> polylines;
    private DBAdapter db = new DBAdapter(this);
    private List<Segment> completeSegmentList;
    private List<AccelerometerRunning> accelerometerList = new ArrayList<AccelerometerRunning>();
    private List<GoogleData> googleDataList = new ArrayList<GoogleData>();
    private String overallDistance = "";
    private String overallSpeed = "";
    private String overallTime = "";
    private boolean walkingButtonEnabled = false;
    private boolean runningButtonEnabled = false;
    private boolean vehicleButtonEnabled = false;
    private float avgSpeedRunning = 0;
    private float avgSpeedWalkng = 0;
    private float avgSpeedCar = 0;
    private double wDistance = 0;
    private double rDistance = 0;
    private double cDistance = 0;
    private long wTime = 0;
    private long rTime = 0;
    private long cTime = 0;
    private DecoView arcView;
    private boolean viewChart = false;
    private boolean firstTime = true;
    private RelativeLayout chartLayout;
    private RelativeLayout cardLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recording);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        applyFontForToolbarTitle(ViewRecording.this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent().hasExtra("recording_id"))
            RECORDING_ID = getIntent().getExtras().getLong("recording_id");

        completeSegmentList = new ArrayList<Segment>();
        polylines = new ArrayList<Polyline>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        cardLayout = (RelativeLayout) findViewById(R.id.cardLayout);
        chartLayout = (RelativeLayout) findViewById(R.id.chartLayout);
        labelActivities = (TextView) findViewById(R.id.label_activities);
        distance = (TextView) findViewById(R.id.card_recording_distance);
        speed = (TextView) findViewById(R.id.card_recording_average_speed);
        elapseTime = (TextView) findViewById(R.id.card_recording_elapse_time);
        distanceLabel = (TextView) findViewById(R.id.card_recording_distance_label);
        speedLabel = (TextView) findViewById(R.id.card_recording_average_speed_label);
        elapseTimeLabel = (TextView) findViewById(R.id.card_recording_elapse_time_label);
        walk = (Button) findViewById(R.id.walking);
        run = (Button) findViewById(R.id.running);
        vehicle = (Button) findViewById(R.id.onVehicle);
        walk_line = (View) findViewById(R.id.walk_line);
        run_line = (View) findViewById(R.id.run_line);
        car_line = (View) findViewById(R.id.car_line);

        Typeface light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        Typeface reg = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");

        labelActivities.setTypeface(bold);
        distance.setTypeface(bold);
        speed.setTypeface(bold);
        elapseTime.setTypeface(bold);

        distanceLabel.setTypeface(light);
        elapseTimeLabel.setTypeface(light);
        speedLabel.setTypeface(light);

        initializeInfo(toolbar);
       // String all = "";
        db.open();
        Cursor c = db.getGoogleData(RECORDING_ID);
        if (c.moveToFirst()) {
            do {
                googleDataList.add(new GoogleData(c.getString(3),String.valueOf(c.getLong(4)),c.getLong(2)));
              //  all += c.getString(3) +" " + String.valueOf(c.getLong(4)) + "," + ConvertFunctons.millisecondsToDate(c.getLong(2)) + "\n";
            } while (c.moveToNext());
        }
        c.close();
        db.close();
      //  Toast.makeText(ViewRecording.this, all, Toast.LENGTH_LONG).show();

        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (walkingButtonEnabled) {
                    walkingButtonEnabled = false;
                    walk_line.setVisibility(View.INVISIBLE);
                    highlightWalking(false);
                } else {
                    walkingButtonEnabled = true;
                    walk_line.setVisibility(View.VISIBLE);
                    highlightWalking(true);
                    // disable all the other buttons
                    vehicleButtonEnabled = false;
                    runningButtonEnabled = false;
                    car_line.setVisibility(View.INVISIBLE);
                    run_line.setVisibility(View.INVISIBLE);
                    highlightVehicle(false);
                    highlightRunning(false);
                }
                updateDisplay();
            }
        });
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (runningButtonEnabled) {
                    runningButtonEnabled = false;
                    run_line.setVisibility(View.INVISIBLE);
                    highlightRunning(false);
                } else {
                    runningButtonEnabled = true;
                    run_line.setVisibility(View.VISIBLE);
                    highlightRunning(true);
                    // disable all the other buttons
                    vehicleButtonEnabled = false;
                    walkingButtonEnabled = false;
                    car_line.setVisibility(View.INVISIBLE);
                    walk_line.setVisibility(View.INVISIBLE);
                    highlightVehicle(false);
                    highlightWalking(false);
                }
                updateDisplay();
            }
        });
        vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vehicleButtonEnabled) {
                    vehicleButtonEnabled = false;
                    car_line.setVisibility(View.INVISIBLE);
                    highlightVehicle(false);
                } else {
                    vehicleButtonEnabled = true;
                    car_line.setVisibility(View.VISIBLE);
                    highlightVehicle(true);
                    // disable all the other buttons
                    runningButtonEnabled = false;
                    walkingButtonEnabled = false;
                    run_line.setVisibility(View.INVISIBLE);
                    walk_line.setVisibility(View.INVISIBLE);
                    highlightRunning(false);
                    highlightWalking(false);
                }
                updateDisplay();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_recording, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        invalidateOptionsMenu();
        if (id == R.id.viewChart && walk.isEnabled()) {
            if (firstTime) {
                createChart();
                firstTime = false;
            }
            if (!viewChart) {
                viewChart = true;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Slide slideLeft = new Slide();
                    slideLeft.setSlideEdge(Gravity.BOTTOM);
                    slideLeft.setDuration(1000);
                    TransitionManager.beginDelayedTransition(cardLayout, slideLeft);
                    cardLayout.setVisibility(View.INVISIBLE);

                    Fade slideRight = new Fade();
                    slideRight.setDuration(1000);
                    TransitionManager.beginDelayedTransition(chartLayout, slideRight);
                    chartLayout.setVisibility(View.VISIBLE);
                } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Fade slideLeft = new Fade();
                    slideLeft.setDuration(1000);
                    TransitionManager.beginDelayedTransition(cardLayout, slideLeft);
                    cardLayout.setVisibility(View.INVISIBLE);
                    Fade slideRight = new Fade();
                    slideRight.setDuration(1000);
                    TransitionManager.beginDelayedTransition(chartLayout, slideRight);
                    chartLayout.setVisibility(View.VISIBLE);
                } else {
                    cardLayout.setVisibility(View.INVISIBLE);
                    chartLayout.setVisibility(View.VISIBLE);
                }
            } else {
                viewChart = false;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Slide slideLeft = new Slide();
                    slideLeft.setSlideEdge(Gravity.BOTTOM);
                    slideLeft.setDuration(1000);
                    TransitionManager.beginDelayedTransition(cardLayout, slideLeft);
                    cardLayout.setVisibility(View.VISIBLE);

                    Fade slideRight = new Fade();
                    slideRight.setDuration(1000);
                    TransitionManager.beginDelayedTransition(chartLayout, slideRight);
                    chartLayout.setVisibility(View.INVISIBLE);
                } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Fade slideLeft = new Fade();
                    slideLeft.setDuration(1000);
                    TransitionManager.beginDelayedTransition(cardLayout, slideLeft);
                    cardLayout.setVisibility(View.VISIBLE);
                    Fade slideRight = new Fade();
                    slideRight.setDuration(1000);
                    TransitionManager.beginDelayedTransition(chartLayout, slideRight);
                    chartLayout.setVisibility(View.INVISIBLE);
                }else{
                    cardLayout.setVisibility(View.VISIBLE);
                    chartLayout.setVisibility(View.INVISIBLE);
                }

            }


            return true;
        } else if (id == android.R.id.home) {
            this.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateDisplay() {
        if (!runningButtonEnabled && !walkingButtonEnabled && !vehicleButtonEnabled) {
            this.distance.setText(overallDistance);
            this.speed.setText(overallSpeed);
            this.elapseTime.setText(overallTime);
            walk_line.setVisibility(View.INVISIBLE);
            run_line.setVisibility(View.INVISIBLE);
            car_line.setVisibility(View.INVISIBLE);

            labelActivities.setText("General Overview");
            return;
        }
        if (runningButtonEnabled) {
            String speed = String.format(Locale.getDefault(), "%.1f km/h", avgSpeedRunning);
            this.speed.setText(String.valueOf(speed));
            this.elapseTime.setText(ConvertFunctons.elapseTimeToString(rTime));
            this.distance.setText(String.format(Locale.getDefault(), "%.2f km", rDistance));
            labelActivities.setText("Running");
        } else if (walkingButtonEnabled) {
            String speed = String.format(Locale.getDefault(), "%.1f km/h", avgSpeedWalkng);
            this.speed.setText(String.valueOf(speed));
            this.elapseTime.setText(ConvertFunctons.elapseTimeToString(wTime));
            this.distance.setText(String.format(Locale.getDefault(), "%.2f km", wDistance));
            labelActivities.setText("Walking");
        } else if (vehicleButtonEnabled) {
            String speed = String.format(Locale.getDefault(), "%.1f km/h", avgSpeedCar);
            this.speed.setText(String.valueOf(speed));
            this.elapseTime.setText(ConvertFunctons.elapseTimeToString(cTime));
            this.distance.setText(String.format(Locale.getDefault(), "%.2f km", cDistance));
            labelActivities.setText("On Vehicle");
        }
    }

    public void initializeInfo(Toolbar toolbar) {
        db.open();
        Cursor c = db.getRecording(RECORDING_ID);
        if (c.moveToFirst()) {
            do {
                toolbar.setTitle(c.getString(2));
                setSupportActionBar(toolbar);
                applyFontForToolbarTitle(ViewRecording.this);
                Double distance = Double.valueOf(c.getString(3));
                overallDistance = String.format(Locale.getDefault(), "%.2f km", distance);
                overallSpeed = c.getString(4);
                overallTime = ConvertFunctons.elapseTimeToString(c.getLong(5));

            } while (c.moveToNext());
        }
        c.close();
        db.close();
        updateDisplay();
    }

    public void highlightWalking(boolean highlight) {
        if (highlight) {
            for (Polyline polyline : polylines)
                if (polyline.getZIndex() == WALKz)
                    polyline.setColor(Color.BLACK);
        } else {
            for (Polyline polyline : polylines)
                if (polyline.getZIndex() == WALKz)
                    polyline.setColor(Color.TRANSPARENT);
        }
    }

    public void highlightRunning(boolean highlight) {
        if (highlight) {
            for (Polyline polyline : polylines)
                if (polyline.getZIndex() == RUNz)
                    polyline.setColor(Color.BLACK);
        } else {
            for (Polyline polyline : polylines)
                if (polyline.getZIndex() == RUNz)
                    polyline.setColor(Color.TRANSPARENT);
        }
    }

    public void highlightVehicle(boolean highlight) {
        if (highlight) {
            for (Polyline polyline : polylines)
                if (polyline.getZIndex() == VEHICLEz)
                    polyline.setColor(Color.BLACK);
        } else {
            for (Polyline polyline : polylines)
                if (polyline.getZIndex() == VEHICLEz)
                    polyline.setColor(Color.TRANSPARENT);
        }
    }

    public void applyFontForToolbarTitle(Activity context) {
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
        } catch (Exception ex) {
            Log.e(TAG, "Failed to change title Font");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(RECORDING_ID);

    }

    /* Chart Bar
     */
    private void createChart() {

        double overall = wDistance + rDistance + cDistance;
        int w = (int) (wDistance * 100 / overall);
        int r = (int) (rDistance * 100 / overall);
        int c = (int) (cDistance * 100 / overall);

        arcView = (DecoView) findViewById(R.id.dynamicArcView);
        SeriesItem walkingItem = new SeriesItem.Builder(Color.WHITE)
                .setRange(0, 100, 0)
                .setLineWidth(90f)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, Color.parseColor("#22000000"), 0.3f))
                .setSeriesLabel(new SeriesLabel.Builder("Walking").build())
                .build();

        SeriesItem runningItem = new SeriesItem.Builder(Color.RED)
                .setRange(0, 100, 0)
                .setLineWidth(90f)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, Color.parseColor("#22000000"), 0.3f))
                .setSeriesLabel(new SeriesLabel.Builder("Running").build())
                .build();
        SeriesItem onVehicleItem = new SeriesItem.Builder(Color.argb(255, 64, 196, 0))
                .setRange(0, 100, 0)
                .setLineWidth(90f)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, Color.parseColor("#22000000"), 0.3f))
                .setSeriesLabel(new SeriesLabel.Builder("On Vehicle").build())
                .build();


        int walkingIndex = arcView.addSeries(walkingItem);
        arcView.addEvent(new DecoEvent.Builder(w + r + c).setIndex(walkingIndex).build());
        if (r != 0) {
            int runningIndex = arcView.addSeries(runningItem);
            arcView.addEvent(new DecoEvent.Builder(r + c).setIndex(runningIndex).build());
        }
        if (c != 0) {
            int onVehicleIndex = arcView.addSeries(onVehicleItem);
            arcView.addEvent(new DecoEvent.Builder(c).setIndex(onVehicleIndex).build());
        }
    }

    /*
Update The Whole Path
 */
    private class AsyncTaskRunner extends AsyncTask<Long, String, List<Segment>> {

        protected List<Segment> doInBackground(Long... params) {

            // Create the complete path with DataPoint objects
            List<DataPoint> completePath = new ArrayList<DataPoint>();
            db.open();
            Cursor c = db.getGpsData(params[0]);
            if (c.moveToFirst()) {
                do {
                   // Log.i(TAG,c.getString(6));
                    completePath.add(new DataPoint(
                            c.getLong(2), c.getLong(3), new LatLng(c.getDouble(4), c.getDouble(5)), Float.valueOf(c.getString(6))));
                } while (c.moveToNext());
            }
            db.close();

            // Get the accelerometer Data
            accelerometerList.clear();
            db.open();
            c = db.getAccelerometerData(RECORDING_ID);
            if (c.moveToFirst()) {
                do {
                    accelerometerList.add(new AccelerometerRunning(c.getInt(4), c.getLong(2), c.getLong(3)));
                } while (c.moveToNext());
            }
            c.close();
            db.close();

            // Apply Segmentation
            //List<LatLng> finalPath = simplifyPath(completePath);
            Segmentation segmentation = new Segmentation(completePath);

            List<Segment> mySegmentList = segmentation.getSegmentList();
            int wCount = 0;
            int rCount = 0;
            int cCount = 0;
            DataPoint prevPoint = null;
            DataPoint prevPointNotWalking = null;
            for (Segment segment : mySegmentList) {
                if (segment.returnMode() == ConstantValues.isWalking) {
                    for (DataPoint myPoint : segment.getPath()) {
                        // distance and time calculation only if prev and curr points are with the same mode
                        if (prevPoint != null && isThePointRunning(myPoint.getTime()) && isThePointRunning(prevPoint.getTime()) &&
                                myPoint.getSpeed() != 0) {
                            rDistance += CalculationByDistance(prevPoint.getPoint(), myPoint.getPoint());
                            rTime += myPoint.getTime() - prevPoint.getTime();
                        }
                        if (prevPoint != null && !isThePointRunning(myPoint.getTime()) && !isThePointRunning(prevPoint.getTime()) &&
                                myPoint.getSpeed() != 0) {
                            wDistance += CalculationByDistance(prevPoint.getPoint(), myPoint.getPoint());
                            wTime += myPoint.getTime() - prevPoint.getTime();
                        }
                        // Average Speed
                        if (myPoint.getSpeed() > 0) {
                            if (isThePointRunning(myPoint.getTime())) {
                                rCount++;
                                avgSpeedRunning += myPoint.getSpeed();
                            } else {
                                wCount++;
                                avgSpeedWalkng += myPoint.getSpeed();
                            }
                        }
                        prevPoint = myPoint;
                    }
                } else if (segment.returnMode() == ConstantValues.isNotWalking) {
                    for (DataPoint myPoint : segment.getPath()) {
                        // Distance,Time
                        if (prevPointNotWalking != null && myPoint.getSpeed() != 0) {
                            cDistance += CalculationByDistance(prevPointNotWalking.getPoint(), myPoint.getPoint());
                            cTime += myPoint.getTime() - prevPointNotWalking.getTime();
                            //  Log.i(TAG,ConvertFunctons.millisecondsToDate(myPoint.getTime()));
                        }
                        // Average Speed
                        if (myPoint.getSpeed() > 0) {
                            cCount++;
                            avgSpeedCar += myPoint.getSpeed();
                        }
                        prevPointNotWalking = myPoint;
                    }
                }

                // null in order not to connect multiple non-walking distances
                prevPointNotWalking = null;
                prevPoint = null;
            }
            if (wCount != 0)
                avgSpeedWalkng = avgSpeedWalkng / wCount;
            if (rCount != 0)
                avgSpeedRunning = avgSpeedRunning / rCount;
            if (cCount != 0)
                avgSpeedCar = avgSpeedCar / cCount;

            return mySegmentList;
        }

        protected void onPostExecute(List<Segment> segmentList) {

            completeSegmentList = segmentList;
            // create perfect view camera point in the middle
            for (Polyline pLine : polylines)
                pLine.remove();
            polylines.clear();
            LatLng middlePoint = returnMiddlePoint(segmentList);
            if (middlePoint != null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(middlePoint, 14));

            // Here express visually the segmentation results
            // Yellow --> on Vehicle
            // Blue --> walking
            // Red --> Running
            // just initialize the start-end point
            if (!segmentList.isEmpty()) {
                if (!segmentList.get(0).getPath().isEmpty()) {
                    DataPoint start = segmentList.get(0).getPath().get(0);
                  //  Log.i(TAG, String.valueOf(start.getPoint()));
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(start.getPoint())
                            .title("Start")
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.mipmap.transparent))); // transparent image
                    marker.showInfoWindow();
                    mMap.addCircle(new CircleOptions().
                            fillColor(Color.CYAN).
                            strokeColor(Color.BLACK).
                            strokeWidth(2).
                            center(start.getPoint()).
                            radius(7).
                            zIndex(11));
                }

                if (!segmentList.get(segmentList.size() - 1).getPath().isEmpty()) {
                    DataPoint end = segmentList.get(segmentList.size() - 1).getPath().get(segmentList.get(segmentList.size() - 1).getPath().size() - 1);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(end.getPoint())
                            .title("Finish")
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.mipmap.transparent))); // transparent image
                    marker.showInfoWindow();
                    mMap.addCircle(new CircleOptions().
                            fillColor(Color.CYAN).
                            strokeColor(Color.BLACK).
                            strokeWidth(2).
                            center(end.getPoint()).
                            radius(7).
                            zIndex(11));
                }

            }
            // we also use the Z-Index to distinguish the lines
            LatLng forSpace = null;
            LatLng forSpace2 = null;
            for (Segment segment : segmentList) {

                if (segment.returnMode() == ConstantValues.isNotWalking) {
                    // get the lat long points from the segment
                    List<LatLng> onVehicle = new ArrayList<LatLng>();
                    if(forSpace!=null) {
                        onVehicle.add(forSpace);
                        mMap.addCircle(new CircleOptions().fillColor(Color.BLACK).center(forSpace).radius(1).zIndex(10));
                    }
                    for (DataPoint myPoint : segment.getPath()) {
                        //////
//                        if(isThePointGoogleVehicle(myPoint.getTime())){
//                            mMap.addCircle(new CircleOptions().fillColor(Color.RED).center(myPoint.getPoint()).radius(5).zIndex(11));
//                        }
                        /////
                        onVehicle.add(myPoint.getPoint());
                        forSpace2 = myPoint.getPoint();
                    }
                    polylines.add(mMap.addPolyline(new PolylineOptions().width(10).color(Color.TRANSPARENT).addAll(onVehicle).zIndex(VEHICLEz)));
                    polylines.add(mMap.addPolyline(new PolylineOptions().width(5).color(Color.argb(255, 64, 196, 0)).addAll(onVehicle).zIndex(5)));
                    forSpace = null;
                } else if (segment.returnMode() == ConstantValues.isWalking) {
                    List<LatLng> onFoot = new ArrayList<LatLng>();
                    if(forSpace2 != null){
                        onFoot.add(forSpace2);
                        mMap.addCircle(new CircleOptions().fillColor(Color.BLACK).center(forSpace2).radius(1).zIndex(10));
                    }
                    forSpace2 = null;
                    boolean wasRunning = false;
                    for (DataPoint myPoint : segment.getPath()) {
                        //////
//                        if(isThePointGoogleVehicle(myPoint.getTime())){
//                            mMap.addCircle(new CircleOptions().fillColor(Color.RED).center(myPoint.getPoint()).radius(5).zIndex(11));
//                        }
                        /////
                        forSpace = myPoint.getPoint();
                        if (isThePointRunning(myPoint.getTime())) {
                            if (wasRunning) {
                                onFoot.add(myPoint.getPoint());
                            } else {
                                LatLng lastP = null;
                                if (!onFoot.isEmpty()) {
                                    lastP = onFoot.get(onFoot.size() - 1);    // to connect between running-walking
                                    mMap.addCircle(new CircleOptions().fillColor(Color.BLACK).center(lastP).radius(1).zIndex(10));
                                    polylines.add(mMap.addPolyline(new PolylineOptions().width(10).color(Color.TRANSPARENT).addAll(onFoot).zIndex(WALKz)));
                                    polylines.add(mMap.addPolyline(new PolylineOptions().width(5).color(Color.BLUE).addAll(onFoot).zIndex(5)));
                                }
                                onFoot.clear();
                                if (lastP != null)
                                    onFoot.add(lastP);
                                onFoot.add(myPoint.getPoint());
                            }
                            wasRunning = true;
                        } else {
                            if (!wasRunning) {
                                onFoot.add(myPoint.getPoint());
                            } else {
                                LatLng lastP = null;
                                if (!onFoot.isEmpty()) {
                                    lastP = onFoot.get(onFoot.size() - 1);
                                    mMap.addCircle(new CircleOptions().fillColor(Color.BLACK).center(lastP).radius(1).zIndex(10));
                                    polylines.add(mMap.addPolyline(new PolylineOptions().width(10).color(Color.TRANSPARENT).addAll(onFoot).zIndex(RUNz)));
                                    polylines.add(mMap.addPolyline(new PolylineOptions().width(5).color(Color.rgb(255, 99, 71)).addAll(onFoot).zIndex(5)));
                                }
                                onFoot.clear();
                                if (lastP != null)
                                    onFoot.add(lastP);
                                onFoot.add(myPoint.getPoint());
                            }
                            wasRunning = false;
                        }
                    }
                    if (!onFoot.isEmpty()) {
                        if (wasRunning) {
                            polylines.add(mMap.addPolyline(new PolylineOptions().width(10).color(Color.TRANSPARENT).addAll(onFoot).zIndex(RUNz)));
                            polylines.add(mMap.addPolyline(new PolylineOptions().width(5).color(Color.RED).addAll(onFoot).zIndex(5)));
                        } else {
                            polylines.add(mMap.addPolyline(new PolylineOptions().width(10).color(Color.TRANSPARENT).addAll(onFoot).zIndex(WALKz)));
                            polylines.add(mMap.addPolyline(new PolylineOptions().width(5).color(Color.BLUE).addAll(onFoot).zIndex(5)));
                        }
                    }
                }

            }
            walk.setEnabled(true);
            run.setEnabled(true);
            vehicle.setEnabled(true);

        }

        protected void onProgressUpdate(String... text) {

            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

    }

    private LatLng returnMiddlePoint(List<Segment> segmentList) {

        if (segmentList.isEmpty())
            return null;
        int middlePoint = 0;
        for (Segment segment : segmentList) {
            middlePoint += segment.getPath().size();
        }
        middlePoint = middlePoint / 2;
        int count = 0;
        for (Segment segment : segmentList) {
            for (DataPoint myPoint : segment.getPath()) {
                if (count == middlePoint)
                    return myPoint.getPoint();
                count++;
            }

        }
        return null;
    }

    private boolean isThePointRunning(long timeStamp) {

        for (AccelerometerRunning accPoint : accelerometerList) {
            if (timeStamp >= accPoint.start && timeStamp <= accPoint.end) {
                if (accPoint.running == 1)
                    return true;
                else
                    return false;
            }
        }
        return false;
    }
    private boolean isThePointGoogleVehicle(long timeStamp) {
        GoogleData prev = null;
        for (GoogleData accPoint : googleDataList) {
            if (timeStamp < accPoint.timestamp && prev != null) {
               // Log.i(TAG,prev.activity);
                if (prev.activity.equals("In Vehicle"))
                    return true;
                else
                    return false;
            }
            prev = accPoint;
        }
        return false;
    }


    public List<LatLng> simplifyPath(List<DataPoint> completePath) {
        LatLng[] latlonPath = new LatLng[completePath.size()];
        for (int i = 0; i < completePath.size(); i++) {
            latlonPath[i] = completePath.get(i).getPoint();
        }
        PointExtractor<LatLng> latLngPointExtractor = new PointExtractor<LatLng>() {
            @Override
            public double getX(LatLng point) {
                return point.latitude * 1000000;
            }

            @Override
            public double getY(LatLng point) {
                return point.longitude * 1000000;
            }
        };

        Simplify<LatLng> simplify = new Simplify<LatLng>(new LatLng[0], latLngPointExtractor);
        if (latlonPath.length == 0)
            return null;
        if(ConstantValues.humanSpeedLimit >=45){
        }
        LatLng[] simplified = simplify.simplify(latlonPath, 50f, true); // or simplify(coords, 20f, false);

        List<LatLng> finalPath = Arrays.asList(simplified);

        return finalPath;
    }

    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    class AccelerometerRunning {
        int running;
        long start;
        long end;

        AccelerometerRunning(int running, long start, long end) {
            this.running = running;
            this.start = start;
            this.end = end;
        }
    }
    class GoogleData{
        String activity;
        String accuracy;
        long timestamp;
        GoogleData(String activity, String accuracy, long timestamp){
            this.accuracy = accuracy;
            this.activity = activity;
            this.timestamp = timestamp;
        }
    }
}

