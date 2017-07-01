package aris.kots.trackmyday;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.goebl.simplify.PointExtractor;
import com.goebl.simplify.Simplify;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import aris.kots.trackmyday.CleverFunctions.ConvertFunctons;
import aris.kots.trackmyday.DataClass.ConstantValues;
import aris.kots.trackmyday.DataClass.DataPoint;
import aris.kots.trackmyday.DataClass.Segment;
import aris.kots.trackmyday.DataClass.Segmentation;
import aris.kots.trackmyday.Database.DBAdapter;

import static aris.kots.trackmyday.CleverFunctions.MathFunctions.CalculationByDistance;

public class TrackService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    public static final int serviceID = 12345;

    private String TAG = "TrackService";
    private PowerManager.WakeLock wakeLock;
    private boolean PAUSED;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long startedServiceTime;
    private String lat, lon;
    private double totalDistance = 0;
    private LatLng previousPoint = null;
    private long previousPointTime = -1;
    private int zeroSpeedCounter = 0;
    private List<DataPoint> completePath;
    private NotificationCompat.Builder notificationBuilder;
    private PendingIntent mActivityRecongPendingIntent;
    private DBAdapter db = new DBAdapter(this);
    private long RECORDING_ID = -1;
    private double averageSpeed = 0;
    private double speedCounter = 0;
    private float currAccuracy = 0;
    ///////////////////////////////
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private List<AccelerometerPack> accValues;
    List<AccelerometerRunning> syncList;
    List<GoogleActivityPack> googleList;

    public TrackService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gps_service");
        wakeLock.acquire();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        startedServiceTime = SystemClock.elapsedRealtime();
        completePath = new ArrayList<DataPoint>();
        googleList = new ArrayList<GoogleActivityPack>();
        //   snakePath =  new CircularFifoQueue<Integer>(2);
        Log.i(TAG, "Service Created");
        db.open();
        RECORDING_ID = db.insertRecording("aris", ConvertFunctons.millisecondsToDateComplete(System.currentTimeMillis()), "my description");
        db.close();

        accValues = new ArrayList<AccelerometerPack>();
        // Sync List for Asynctask operations
        syncList = Collections.synchronizedList(new ArrayList<AccelerometerRunning>());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // fai! we dont have an accelerometer!
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            // Start / Stop / Pause my Service
            if (intent.getAction().equals(TrackActivity.START_FOREGROUND_SERVICE)) {
                Log.i(TAG, "Service Started");
                // Let it continue running until it is stopped.
                if (mGoogleApiClient != null) {
                    mGoogleApiClient.connect();
                }

                totalDistance = 0;
                PAUSED = false;

                Intent notificationIntent = new Intent(this, TrackActivity.class);
                notificationIntent.setAction("main");
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);

                Intent previousIntent = new Intent(this, TrackService.class);
                previousIntent.setAction("prev");
                PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                        previousIntent, 0);

                Intent playIntent = new Intent(this, TrackService.class);
                playIntent.setAction("play");
                PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                        playIntent, 0);


                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher);

                notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.notification_small)
                        .setContentTitle("Track My Day")
                        .setTicker("Track My Day")
                        .setContentText("Speed: ")
                        .setColor(Color.BLACK)
                        .setLargeIcon(icon)
                        .setUsesChronometer(true)
                        .setSubText("Distance: ")
                        .setContentInfo("")
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .addAction(R.mipmap.ic_pause_black_36dp,
                                "Pause", ppreviousIntent)
                        .addAction(R.mipmap.view_path_not, "View",
                                pendingIntent);
                Notification notification = notificationBuilder.build();

                startForeground(serviceID, notification);

            } else if (intent.getAction().equals(TrackActivity.STOP_FOREGROUND_SERVICE)) {
                Log.i(TAG, "Service STOPPING...");
                stopForeground(true);
                stopSelf();
            } else if (intent.getAction().equals(TrackActivity.PAUSE_FOREGROUND_SERVICE)) {
                PAUSED = true;
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//            try {
//                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//            } catch (SecurityException e) {
//                Log.e("SecurityException", e.getMessage());
//            }
            } else if (intent.getAction().equals(TrackActivity.UPDATE_PATH)) {
                ////////////////////////
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(TrackActivity.mBroadcastRecordingId);
                broadcastIntent.putExtra("recording_id", RECORDING_ID);
                sendBroadcast(broadcastIntent);
            }

            if (intent.getAction().equals("prev")) {
                Log.i(TAG, "Clicked Previous");
            } else if (intent.getAction().equals("play")) {
                Log.i(TAG, "Clicked Play");
            } else if (intent.getAction().equals("main")) {
                Log.i(TAG, "Clicked main");
            } else if (intent.getAction().equals("detectActivity")) {
                if (ActivityRecognitionResult.hasResult(intent)) {
                    //Extract the result from the Response
                    ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                    if (result != null) {
                        DetectedActivity detectedActivity = result.getMostProbableActivity();

                        //Get the Confidence and Name of Activity
                        int confidence = detectedActivity.getConfidence();
                        String mostProbableName = getActivityName(detectedActivity.getType());

                        //Fire the intent with activity name & confidence
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(TrackActivity.mBroadcastActivity);
                        broadcastIntent.putExtra("activity", mostProbableName + " " + String.valueOf(confidence) + "%");
                        //i.putExtra("confidence", confidence);

                        Log.d(TAG, "Most Probable Name : " + mostProbableName);
                        Log.d(TAG, "Confidence : " + confidence);
                        googleList.add(new GoogleActivityPack(confidence,System.currentTimeMillis(),mostProbableName));
                        // update Notification
                        notificationBuilder.setContentInfo(mostProbableName + " " + String.valueOf(confidence) + "%");

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(serviceID, notificationBuilder.build());

                        //Send Broadcast to be listen in MainActivity
                        sendBroadcast(broadcastIntent);
                    }
                } else {
                    Log.d(TAG, "Intent had no data returned");
                }

            }
        } catch (Exception ex) {
            Log.i(TAG, ex.toString());
        }
        return START_STICKY;
    }

    //Get the activity name
    private String getActivityName(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bike";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.UNKNOWN:
                return "Uknown";
            case DetectedActivity.RUNNING:
                return "Running";

        }
        return "N/A";
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.i(TAG, String.valueOf(location.getAccuracy()));
        currAccuracy = location.getAccuracy();
        if(currAccuracy < 22) {
            // Timestamp
            long timeStamp = System.currentTimeMillis();
            long elapseTimeMillis = SystemClock.elapsedRealtime() - startedServiceTime;
            String elapseTime = ConvertFunctons.elapseTimeToString(elapseTimeMillis);   //convert to string

            // Coordinates
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());

            // Speed
            String speed = String.format(Locale.getDefault(), "%.1f", (location.getSpeed() * 3600) / 1000);
            if (speed.equals("0.0"))
                zeroSpeedCounter++;
            else {
                zeroSpeedCounter = 0;
                speedCounter++;
                averageSpeed += Double.valueOf(speed);
            }

            // Distance
            double lastTwoPointDistance = 0;
            if (previousPoint != null && previousPointTime != -1 && Double.valueOf(speed) != 0) {
                //   double distance = CalculationByDistance(previousPoint, new LatLng(Double.valueOf(lat), Double.valueOf(lon)));
                //    double timeDistance = (timeStamp - previousPointTime)/1000.0;
                totalDistance += CalculationByDistance(previousPoint, new LatLng(Double.valueOf(lat), Double.valueOf(lon)));
                //   Log.i(TAG,"\ndistance: " + String.valueOf(distance) + "\nseconds: " + String.valueOf(timeDistance));
            }
            previousPointTime = timeStamp;
            previousPoint = new LatLng(Double.valueOf(lat), Double.valueOf(lon));

            // add to the list if there is a speed (so an accurate GPS signal)

            if (zeroSpeedCounter <= ConstantValues.zeroSpeedMaxPoints) {
                db.open();
                db.insertGpsData(RECORDING_ID, timeStamp, elapseTimeMillis, Double.valueOf(lat), Double.valueOf(lon), speed);
                db.close();
                completePath.add(new DataPoint(
                        timeStamp, elapseTimeMillis, new LatLng(Double.valueOf(lat), Double.valueOf(lon)), Float.valueOf(speed)));
            }

            // update Notification
            notificationBuilder.setContentText("Speed: " + speed + " km/h");
            notificationBuilder.setSubText("Distance: " + String.format(Locale.getDefault(), "%.2f km", totalDistance));

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(serviceID, notificationBuilder.build());

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(TrackActivity.mBroadcastStringAction);
            broadcastIntent.putExtra("elapseTime", elapseTime);
            broadcastIntent.putExtra("lat", lat);
            broadcastIntent.putExtra("lon", lon);
            broadcastIntent.putExtra("speed", speed);
            broadcastIntent.putExtra("distance", String.valueOf(totalDistance));
            broadcastIntent.putExtra("timestamp", String.valueOf(timeStamp));
            broadcastIntent.putExtra("accuracy", String.valueOf(currAccuracy));

            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //////
        Intent i = new Intent(this, TrackService.class);
        i.setAction("detectActivity");
        mActivityRecongPendingIntent = PendingIntent
                .getService(this, 0, i, 0);

        Log.d(TAG, "connected to ActivityRecognition");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, mActivityRecongPendingIntent);

        /////////
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second (for sure after)
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                lat = String.valueOf(mLastLocation.getLatitude());
                lon = String.valueOf(mLastLocation.getLongitude());
                Log.e("TrackService", "Connected: " + lat + " " + lon);
            }
        } catch (SecurityException e) {
            Log.e("SecurityException", e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("TrackService", "Connection Failed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        long elapseTimeMillis = SystemClock.elapsedRealtime() - startedServiceTime;
        String avSpeed = "0.0 km/h";
        if (speedCounter != 0)
            avSpeed = String.format(Locale.getDefault(), "%.1f km/h", averageSpeed / speedCounter);
        String distance = String.valueOf(totalDistance);
        db.open();
        db.updateRecordingInfo(distance, avSpeed, elapseTimeMillis, 1, RECORDING_ID);
        synchronized(syncList) {
            for (AccelerometerRunning temp : syncList)
                db.insertAccelerometerData(RECORDING_ID,temp.running,temp.start,temp.end);
        }
        for(GoogleActivityPack pack : googleList)
            db.insertGoogleData(RECORDING_ID,pack.timesamp,pack.activity,pack.percentage);
        db.close();
        if (wakeLock.isHeld())
            wakeLock.release();
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecongPendingIntent);
            mGoogleApiClient.disconnect();
            Log.i("TrackService", "Destroy");
        } catch (Exception ex) {
            Log.i("TrackService", "Destroy Failed");

        }
        //  Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("TrackService", "onTaskRemoved");
        //  Toast.makeText(this, "Service Destroyed from onTAskRemoed", Toast.LENGTH_LONG).show();
        //stop service
//        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timeStamp = System.currentTimeMillis();
        Double norm = new Double(Math.sqrt(x * x + y * y + z * z));

        accValues.add(new AccelerometerPack(timeStamp, norm.intValue()));
        // Log.i("aris",String.valueOf(accValues.size()));
        if (accValues.size() == 500) {
            List<AccelerometerPack> tmp = new ArrayList<AccelerometerPack>(accValues);
            accValues.clear();
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(tmp);
        }
    }
/*
    Calculate whether the users walks or run under 500 measurements of Norm sqrt(x*x+y*y+z*z)
 */
    private class AsyncTaskRunner extends AsyncTask<List<AccelerometerPack>, String, Void> {

        protected Void doInBackground(List<AccelerometerPack>... params) {
            int count = 0;
            long start = 0;
            long end = 0;
            // initialize start-stop timestamps
            if(params[0].size() > 0 ){
                start = params[0].get(0).timestamp;
                end = params[0].get(params[0].size()-1).timestamp;
            }
            // count speed measurements
            for (int i = 0; i < params[0].size(); i++) {
                if (params[0].get(i).norm >= 24)
                    count++;
            }
            if (count > 50) {
                // here append if the last was running or create a new one
                if( !syncList.isEmpty()){
                    if(syncList.get(syncList.size()-1).running == 1)
                        syncList.get(syncList.size()-1).end = end;
                    else
                        syncList.add(new AccelerometerRunning(1,start,end));
                }else
                    syncList.add(new AccelerometerRunning(1,start,end));
            }
            else {
                // here append if the last was walking or create a new one
                if( !syncList.isEmpty()){
                    if(syncList.get(syncList.size()-1).running == 0)
                        syncList.get(syncList.size()-1).end = end;
                    else
                        syncList.add(new AccelerometerRunning(0,start,end));
                }else
                    syncList.add(new AccelerometerRunning(0,start,end));
            }
            return null;
        }

        protected void onProgressUpdate(String... text) {
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class AccelerometerPack {
        long timestamp;
        int norm;

        AccelerometerPack(long timestamp, int norm) {
            this.timestamp = timestamp;
            this.norm = norm;
        }
    }
    class AccelerometerRunning{
        int running;
        long start;
        long end;
        AccelerometerRunning(int running, long start, long end){
            this.running = running;
            this.start = start;
            this.end = end;
        }
    }
    class GoogleActivityPack{
        int percentage;
        long timesamp;
        String activity;
        GoogleActivityPack(int percentage, long timesamp, String activity){
            this.percentage = percentage;
            this.timesamp = timesamp;
            this.activity = activity;
        }
    }
}
