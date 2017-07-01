package aris.kots.trackmyday;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hookedonplay.decoviewlib.DecoView;

import java.util.ArrayList;
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

public class ViewStatistics extends AppCompatActivity {
    private String TAG = "ViewStatistics";
    private long RECORDING_ID = -1;
    private TextView distance;
    private TextView speed;
    private TextView elapseTime;
    private TextView distanceLabel;
    private TextView speedLabel;
    private TextView elapseTimeLabel;
    private TextView labelActivities;
    private TextView distancer;
    private TextView speedr;
    private TextView elapseTimer;
    private TextView distanceLabelr;
    private TextView speedLabelr;
    private TextView elapseTimeLabelr;
    private TextView labelActivitiesr;
    private TextView distancev;
    private TextView speedv;
    private TextView elapseTimev;
    private TextView distanceLabelv;
    private TextView speedLabelv;
    private TextView elapseTimeLabelv;
    private TextView labelActivitiesv;
    private List<AccelerometerRunning> accelerometerList = new ArrayList<AccelerometerRunning>();
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
    private ProgressBar progressBar;
    private ProgressBar progressBar2;
    private ProgressBar progressBar3;

    DBAdapter db = new DBAdapter(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_statistics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        applyFontForToolbarTitle(ViewStatistics.this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        labelActivities = (TextView) findViewById(R.id.label_activities);
        distance = (TextView) findViewById(R.id.card_recording_distance);
        speed = (TextView) findViewById(R.id.card_recording_average_speed);
        elapseTime = (TextView) findViewById(R.id.card_recording_elapse_time);
        distanceLabel = (TextView) findViewById(R.id.card_recording_distance_label);
        speedLabel = (TextView) findViewById(R.id.card_recording_average_speed_label);
        elapseTimeLabel = (TextView) findViewById(R.id.card_recording_elapse_time_label);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        labelActivitiesr = (TextView) findViewById(R.id.label_activitiesr);
        distancer = (TextView) findViewById(R.id.card_recording_distancer);
        speedr = (TextView) findViewById(R.id.card_recording_average_speedr);
        elapseTimer = (TextView) findViewById(R.id.card_recording_elapse_timer);
        distanceLabelr = (TextView) findViewById(R.id.card_recording_distance_labelr);
        speedLabelr = (TextView) findViewById(R.id.card_recording_average_speed_labelr);
        elapseTimeLabelr = (TextView) findViewById(R.id.card_recording_elapse_time_labelr);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);

        labelActivitiesv = (TextView) findViewById(R.id.label_activitiesv);
        distancev = (TextView) findViewById(R.id.card_recording_distancev);
        speedv = (TextView) findViewById(R.id.card_recording_average_speedv);
        elapseTimev = (TextView) findViewById(R.id.card_recording_elapse_timev);
        distanceLabelv = (TextView) findViewById(R.id.card_recording_distance_labelv);
        speedLabelv = (TextView) findViewById(R.id.card_recording_average_speed_labelv);
        elapseTimeLabelv = (TextView) findViewById(R.id.card_recording_elapse_time_labelv);
        progressBar3 = (ProgressBar) findViewById(R.id.progressBar3);

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
        progressBar.setIndeterminate(true);

        labelActivitiesr.setTypeface(bold);
        distancer.setTypeface(bold);
        speedr.setTypeface(bold);
        elapseTimer.setTypeface(bold);
        distanceLabelr.setTypeface(light);
        elapseTimeLabelr.setTypeface(light);
        speedLabelr.setTypeface(light);
        progressBar2.setIndeterminate(true);

        labelActivitiesv.setTypeface(bold);
        distancev.setTypeface(bold);
        speedv.setTypeface(bold);
        elapseTimev.setTypeface(bold);
        distanceLabelv.setTypeface(light);
        elapseTimeLabelv.setTypeface(light);
        speedLabelv.setTypeface(light);
        progressBar3.setIndeterminate(true);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(RECORDING_ID);

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
    private class AsyncTaskRunner extends AsyncTask<Long, String, List<Segment>> {

        protected List<Segment> doInBackground(Long... params) {



            int wCount = 0;
            int rCount = 0;
            int cCount = 0;

            db.open();
            final Cursor c = db.getAllRecordings();
            if (c.moveToFirst()) {
                do {
                    RECORDING_ID = c.getLong(0);
                    // GET GPS DATA
                    List<DataPoint> completePath = new ArrayList<DataPoint>();
                    Cursor c1 = db.getGpsData(RECORDING_ID);
                    if (c1.moveToFirst()) {
                        do {
                            // Log.i(TAG,c.getString(6));
                            completePath.add(new DataPoint(
                                    c1.getLong(2), c1.getLong(3), new LatLng(c1.getDouble(4), c1.getDouble(5)), Float.valueOf(c1.getString(6))));
                        } while (c1.moveToNext());
                    }
                    // GET ACCELEROEMTER DATA
                    accelerometerList.clear();
                    c1 = db.getAccelerometerData(RECORDING_ID);
                    if (c1.moveToFirst()) {
                        do {
                            accelerometerList.add(new AccelerometerRunning(c1.getInt(4), c1.getLong(2), c1.getLong(3)));
                        } while (c1.moveToNext());
                    }
                    c1.close();
                    //////////////
                    Segmentation segmentation = new Segmentation(completePath);
                    List<Segment> mySegmentList = segmentation.getSegmentList();
                    DataPoint prevPoint = null;
                    DataPoint prevPointNotWalking = null;
                    Log.i( TAG,String.valueOf(mySegmentList.size()));

                    for (Segment segment : mySegmentList) {
                        if (segment.returnMode() == ConstantValues.isWalking) {
                            for (DataPoint myPoint : segment.getPath()) {
                                // distance and time calculation only if prev and curr points are with the same mode
                                if (prevPoint != null && isThePointRunning(myPoint.getTime()) && isThePointRunning(prevPoint.getTime()) &&
                                        myPoint.getSpeed() != 0) {
                                    //Log.i( TAG,String.valueOf(CalculationByDistance(prevPoint.getPoint(), myPoint.getPoint())));
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

                } while (c.moveToNext());
            }
            c.close();
            db.close();

            if (wCount != 0)
                avgSpeedWalkng = avgSpeedWalkng / wCount;
            if (rCount != 0)
                avgSpeedRunning = avgSpeedRunning / rCount;
            if (cCount != 0)
                avgSpeedCar = avgSpeedCar / cCount;

            return null;
        }

        protected void onPostExecute(List<Segment> segmentList) {
            progressBar.setIndeterminate(false);
            progressBar.setProgress(100);
            progressBar2.setIndeterminate(false);
            progressBar2.setProgress(100);
            progressBar3.setIndeterminate(false);
            progressBar3.setProgress(100);
            progressBar.setVisibility(View.INVISIBLE);
            progressBar2.setVisibility(View.INVISIBLE);
            progressBar3.setVisibility(View.INVISIBLE);

            distancer.setText(String.format(Locale.getDefault(), "%.2f km", rDistance));
            elapseTimer.setText(ConvertFunctons.elapseTimeToString(rTime));
            speedr.setText( String.format(Locale.getDefault(), "%.1f km/h", avgSpeedRunning));

            distancev.setText(String.format(Locale.getDefault(), "%.2f km", cDistance));
            elapseTimev.setText(ConvertFunctons.elapseTimeToString(cTime));
            speedv.setText( String.format(Locale.getDefault(), "%.1f km/h", avgSpeedCar));

            distance.setText(String.format(Locale.getDefault(), "%.2f km", wDistance));
            elapseTime.setText(ConvertFunctons.elapseTimeToString(wTime));
            speed.setText( String.format(Locale.getDefault(), "%.1f km/h", avgSpeedWalkng));
        }

        protected void onProgressUpdate(String... text) {

            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

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
}
