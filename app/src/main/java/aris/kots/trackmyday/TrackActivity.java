package aris.kots.trackmyday;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.goebl.simplify.PointExtractor;
import com.goebl.simplify.Simplify;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import aris.kots.trackmyday.DataClass.DataPoint;
import aris.kots.trackmyday.Database.DBAdapter;
import mehdi.sakout.fancybuttons.FancyButton;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "TrackActivity";
    public static final String mBroadcastStringAction = "aris.kots.string";
    public static final String mBroadcastRecordingId = "aris.kots.recording_id";
    public static final String mBroadcastActivity = "aris.kots.activity";

    public static final String START_FOREGROUND_SERVICE = "START";
    public static final String STOP_FOREGROUND_SERVICE = "STOP";
    public static final String PAUSE_FOREGROUND_SERVICE = "PAUSE";
    public static final String UPDATE_PATH = "UPDATE_PATH";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  //  private ProgressBar progressBar;
    private IntentFilter mIntentFilter;
    private boolean activities[];
    GoogleMap mMap;
    TextView timerText, speedText, distanceText, activityText;
    TextView switchText, timerTextLabel, speedTextLabel, distanceTextLabel, activityTextLabel;
    TextView accuracyText;
    Switch mySwitch;
    FancyButton finishButton, pauseButton;
    ImageView activityIcon;
    // Map display
    boolean autoFocus = true;
    int counterBroadcasts = 0;
    Marker currentLocationMarker = null;
    Polyline polyline = null;
    Polyline polyline2 = null;
    DBAdapter db = new DBAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get detectable activities
//        if (getIntent().hasExtra("detectable_activities"))
//            activities = getIntent().getExtras().getBooleanArray("detectable_activities");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        switchText = (TextView) findViewById(R.id.switchText);
        mySwitch = (Switch) findViewById(R.id.switch1);
        finishButton = (FancyButton) findViewById(R.id.finishButton);
        pauseButton = (FancyButton) findViewById(R.id.pauseButton);
        timerText = (TextView) findViewById(R.id.timer);
        timerTextLabel = (TextView) findViewById(R.id.elapseTime);
        speedText = (TextView) findViewById(R.id.speed);
        speedTextLabel = (TextView) findViewById(R.id.current_speed);
        distanceText = (TextView) findViewById(R.id.distance);
        distanceTextLabel = (TextView) findViewById(R.id.distanceText);
        activityText = (TextView) findViewById(R.id.activity);
        activityTextLabel = (TextView) findViewById(R.id.activityLabel);
        accuracyText = (TextView) findViewById(R.id.accuracy);
        //   activityIcon = (ImageView) findViewById(R.id.activityImage);

        // Fonts
        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        switchText.setTypeface(robotoLight);
        timerTextLabel.setTypeface(robotoLight);
        speedTextLabel.setTypeface(robotoLight);
        distanceTextLabel.setTypeface(robotoLight);
        activityTextLabel.setTypeface(robotoLight);
        accuracyText.setTypeface(robotoLight);
        timerText.setTypeface(roboto);
        speedText.setTypeface(roboto);
        distanceText.setTypeface(roboto);
        activityText.setTypeface(roboto);
        applyFontForToolbarTitle(TrackActivity.this);

        // Initialize Broadcast for Service
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastStringAction);
        mIntentFilter.addAction(mBroadcastRecordingId);
        mIntentFilter.addAction(mBroadcastActivity);

        // Register receiver
        registerReceiver(mReceiver, mIntentFilter);

        // Ask to enable GPS
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showGPSDisabledAlertToUser();

        // Start the service
        startService();

        // Button Listeners
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked == true) {
                    autoFocus = true;
                } else {
                    autoFocus = false;
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TrackActivity.this);
                alertDialogBuilder.setMessage("Are you sure you want to end your tracking?")
                        // .setCancelable(false)
                        .setPositiveButton("Finish",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        stopService();
                                        finish();
                                        overridePendingTransition(android.R.anim.fade_in,
                                                android.R.anim.fade_out);
                                    }
                                });
                alertDialogBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pauseService();
               // mMap.addMarker(new MarkerOptions().position(currentLocationMarker.getPosition()).title(":D").snippet(":)").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bike_custom)));
            }
        });
        Log.i(TAG, "onCreate");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        invalidateOptionsMenu();
        if (id == R.id.viewPath) {
            // Update or View Path
            //Log.i(TAG, "update Path");
            Intent startIntent = new Intent(TrackActivity.this, TrackService.class);
            startIntent.setAction(UPDATE_PATH);
            startService(startIntent);
            return true;
        }else if(id == android.R.id.home){
            this.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
//        List<LatLng> val = new ArrayList<LatLng>();
//        val.add(new LatLng(33.919387, -118.189983));
//        val.add(new LatLng(33.965442, -118.280787));
//        val.add(new LatLng(34.013123, -118.281130));
//        val.add(new LatLng(34.110544, -118.328682));
//        val.add(new LatLng(34.219349, -118.237845));
//        val.add(new LatLng(34.013123, -118.281130));

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastStringAction)) {
                counterBroadcasts++;
                String elapseTime = intent.getStringExtra("elapseTime");
                String timestamp = intent.getStringExtra("timestamp");
                String lat = intent.getStringExtra("lat");
                String lon = intent.getStringExtra("lon");
                String speed = intent.getStringExtra("speed");
                String accuracy = intent.getStringExtra("accuracy");
                accuracyText.setText(accuracy);
                double distance = Double.valueOf(intent.getStringExtra("distance"));
                if (distance >= 1.0)
                    distanceText.setText(String.format(Locale.getDefault(),"%.2f km", distance));
                else
                    distanceText.setText(String.format(Locale.getDefault(),"%.0f meters", distance * 1000));

                timerText.setText(elapseTime);
                speedText.setText(speed + " km/h");
                // Create Current Point
                LatLng currentPoint = new LatLng(Double.valueOf(lat), Double.valueOf(lon));

                if (counterBroadcasts == 1 || counterBroadcasts > 5 && autoFocus)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPoint, 18), 1300, null);

                // if there is no marker created
                if (currentLocationMarker == null)
                    currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentPoint).title(String.format(Locale.getDefault(),"%.2f km", distance)).snippet(speed).icon(BitmapDescriptorFactory.fromResource(R.mipmap.test2)));
                else {
                    currentLocationMarker.setPosition(currentPoint);
                    currentLocationMarker.setTitle(String.format(Locale.getDefault(),"%.2f km", distance));
                    currentLocationMarker.setSnippet(speed);
                    currentLocationMarker.showInfoWindow();
                }
            } else if (intent.getAction().equals(mBroadcastRecordingId)) {
                String pathString[] = intent.getStringArrayExtra("complete_path");
                long recording_id = intent.getLongExtra("recording_id",-1);
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(recording_id);
            } else if (intent.getAction().equals(TrackActivity.mBroadcastActivity)){
                /////
//                if(intent.getStringExtra("activity").equals("Walking 100%") && currentLocationMarker != null)
//                    mMap.addMarker(new MarkerOptions().position(currentLocationMarker.getPosition()).title("Walking").snippet("100%").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bike_custom)));
//                if(intent.getStringExtra("activity").equals("On Foot 100%") && currentLocationMarker != null)
//                    mMap.addMarker(new MarkerOptions().position(currentLocationMarker.getPosition()).title("On Foot").snippet("100%").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bike_custom)));
//                if(intent.getStringExtra("activity").equals("Running 100%") && currentLocationMarker != null)
//                    mMap.addMarker(new MarkerOptions().position(currentLocationMarker.getPosition()).title("Running").snippet("100%").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bike_custom)));

                activityText.setText(intent.getStringExtra("activity"));
            }
        }
    };
    /*
    Update The Whole Path
     */
    private class AsyncTaskRunner extends AsyncTask<Long, String, List<LatLng>> {

        protected List<LatLng> doInBackground(Long... params) {
            // re-initialize our path for the display

            List<DataPoint> completePath = new ArrayList<DataPoint>();

            db.open();
            final Cursor c = db.getGpsData(params[0]);
            if (c.moveToFirst()) {
                do {
                    completePath.add(new DataPoint(
                            c.getLong(2), c.getLong(3), new LatLng(c.getDouble(4), c.getDouble(5)), Float.valueOf(c.getString(6))));
                } while (c.moveToNext());
            }
            db.close();


            /////////////////////////////////
          //  Log.i(TAG, "Original Path = " + String.valueOf(completePath.size()));

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
            // optimize the path
            Simplify<LatLng> simplify = new Simplify<LatLng>(new LatLng[0], latLngPointExtractor);
            if (latlonPath.length == 0)
                return null;
            LatLng[] simplified = simplify.simplify(latlonPath, 50f, true); // or simplify(coords, 20f, false);

            List<LatLng> finalPath = Arrays.asList(simplified);
            return finalPath;
        }

        protected void onPostExecute(List<LatLng> result) {
            // execution of result of Long time consuming operation

            if (result != null) {
           //     Log.i(TAG, "Path after simplification = " + String.valueOf(result.size()));

                if (polyline != null)
                    polyline.remove();
                if (polyline2 != null)
                    polyline2.remove();
                PolylineOptions polylineOptions = new PolylineOptions().width(9).color(Color.BLACK).addAll(result);
                polyline = mMap.addPolyline(polylineOptions);
                PolylineOptions polylineOptions2 = new PolylineOptions().width(4).color(Color.BLUE).addAll(result);
                polyline2 = mMap.addPolyline(polylineOptions2);
            }
        }

        protected void onProgressUpdate(String... text) {

            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

    }

    /*
    Start the Service
     */
    public void startService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if (!isServiceRunning(TrackService.class)) {

                Intent startIntent = new Intent(TrackActivity.this, TrackService.class);
                startIntent.setAction(START_FOREGROUND_SERVICE);
                startService(startIntent);
                // Enable progress Bar
             //   progressBar.setIndeterminate(true);
                Log.i(TAG, "Service Just Started!");
            } else {
              //  progressBar.setIndeterminate(true);
                Log.i(TAG, "Service Already Started");
            }

        }
    }

    /*
    Stop the Service
     */
    public void stopService() {
        if (isServiceRunning(TrackService.class)) {
            // stopService(new Intent(getBaseContext(), TrackService.class));
            Intent startIntent = new Intent(TrackActivity.this, TrackService.class);
            startIntent.setAction(STOP_FOREGROUND_SERVICE);
            startService(startIntent);

           // progressBar.setIndeterminate(false);
            Log.i(TAG, "Service Stopped!");
        }
    }

    /*
    Pause the Service
     */
    public void pauseService() {
        if (isServiceRunning(TrackService.class)) {
            // stopService(new Intent(getBaseContext(), TrackService.class));
            Intent startIntent = new Intent(TrackActivity.this, TrackService.class);
            startIntent.setAction(PAUSE_FOREGROUND_SERVICE);
            startService(startIntent);

           // progressBar.setIndeterminate(false);
            Log.i(TAG, "Service PAUSED!!");
        }
    }

    /*
    Custom method to determine whether a service is running
    */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        // Loop through the running services
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                // If the service is running then return true
                return true;
            }
        }
        return false;
    }

    /*
    Enable GPS
    */
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent gpsOptionsIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /*
    Handling Permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permision", "Permision Granted!");
                    startService();
                } else {
                    Log.e("Permision", "Permision Denied");
                }
                return;
            }

        }

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
    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(mReceiver, mIntentFilter);
        Log.e(TAG, "Resume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Log.e(TAG, "Destroy");
    }
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }
}
