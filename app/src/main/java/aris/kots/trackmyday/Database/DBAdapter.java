package aris.kots.trackmyday.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;

public class DBAdapter {

    private static final String TAG = "DBAdapter";

    private static final String DATABASE_NAME = "TrackMyDayDB";
    /****************************************************************/
    private static final String TABLE_RECORDINGS = "recordings";
    private static final String KEY_RECORDING_ID = "recording_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DATE = "the_date";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_AVGSPEED = "speed";
    private static final String KEY_FINISHED = "finished";

    /****************************************************************/
    private static final String TABLE_GPS_DATA = "gps_data";
    private static final String KEY_GPS_DATA_ID = "gps_data_id";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_ELAPSE_TIME = "elapse_time";    //millisec
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_SPEED = "speed";

    /****************************************************************/
    private static final String TABLE_ACCELEROMETER_DATA = "accelerometer_data";
    private static final String KEY_ACC_DATA_ID = "accelerometer_data_id";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_START = "start";
    private static final String KEY_END = "end";

    /****************************************************************/
    private static final String TABLE_GOOGLE_API_DATA = "google_api_data";
    private static final String KEY_GOOGLE_ID = "accelerometer_data_id";
    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_PERCENTAGE = "percentage";
    private static final int DATABASE_VERSION = 9;

    private static final String DATABASE_CREATE1 =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECORDINGS +
                    " (" +
                    KEY_RECORDING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_NAME + " VARCHAR," +
                    KEY_DATE + " VARCHAR," +
                    KEY_DISTANCE + " VARCHAR," +
                    KEY_AVGSPEED + " VARCHAR," +
                    KEY_ELAPSE_TIME + " INTEGER," +
                    KEY_FINISHED + " INTEGER" +
                    ");";

    private static final String DATABASE_CREATE2 =
            "CREATE TABLE IF NOT EXISTS " + TABLE_GPS_DATA +
                    " (" +
                    KEY_GPS_DATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_RECORDING_ID + " INTEGER," +
                    KEY_TIMESTAMP + " INTEGER," +
                    KEY_ELAPSE_TIME + " INTEGER," +
                    KEY_LAT + " REAL," +
                    KEY_LON + " REAL," +
                    KEY_SPEED + " VARCHAR" +
                    ");";

    private static final String DATABASE_CREATE3 =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ACCELEROMETER_DATA +
                    " (" +
                    KEY_ACC_DATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_RECORDING_ID + " INTEGER," +
                    KEY_START + " INTEGER," +
                    KEY_END + " INTEGER," +
                    KEY_RUNNING + " INTEGER" +
                    ");";
    private static final String DATABASE_CREATE4 =
            "CREATE TABLE IF NOT EXISTS " + TABLE_GOOGLE_API_DATA +
                    " (" +
                    KEY_GOOGLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_RECORDING_ID + " INTEGER," +
                    KEY_TIMESTAMP + " INTEGER," +
                    KEY_ACTIVITY + " VARCHAR," +
                    KEY_PERCENTAGE + " INTEGER" +
                    ");";
    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                Log.w(TAG, "DB CREATED!");
                db.execSQL(DATABASE_CREATE1);
                db.execSQL(DATABASE_CREATE2);
                db.execSQL(DATABASE_CREATE3);
                db.execSQL(DATABASE_CREATE4);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS_DATA);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCELEROMETER_DATA);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOOGLE_API_DATA);
            onCreate(db);
        }
    }

    //---opens the database---
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() {
        DBHelper.close();
    }

    /*
     INSERT RECORDING
     */
    public long insertRecording(String name, String date, String distance) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_DISTANCE, distance);
        initialValues.put(KEY_AVGSPEED, "0");
        initialValues.put(KEY_ELAPSE_TIME, 0);
        initialValues.put(KEY_FINISHED, 0);
        return db.insert(TABLE_RECORDINGS, null, initialValues);
    }

    /*
    INSERT GPS DATA
    */
    public long insertGpsData(long recording_id, long timestamp, long elapseTimeMill, double lat, double lon, String speed) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_RECORDING_ID, recording_id);
        initialValues.put(KEY_TIMESTAMP, timestamp);
        initialValues.put(KEY_ELAPSE_TIME, elapseTimeMill);
        initialValues.put(KEY_LAT, lat);
        initialValues.put(KEY_LON, lon);
        initialValues.put(KEY_SPEED, speed);
        return db.insert(TABLE_GPS_DATA, null, initialValues);
    }

    /*
    INSERT ACCELEROMETER DATA
    */
    public long insertAccelerometerData(long recording_id, int running, long start, long end) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_RECORDING_ID, recording_id);
        initialValues.put(KEY_START, start);
        initialValues.put(KEY_END, end);
        initialValues.put(KEY_RUNNING, running);
        return db.insert(TABLE_ACCELEROMETER_DATA, null, initialValues);
    }
    /*
    INSERT GOOGLE ACTIVITY DATA
    */
    public long insertGoogleData(long recording_id, long timestamp, String activity, long percentage) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_RECORDING_ID, recording_id);
        initialValues.put(KEY_TIMESTAMP, timestamp);
        initialValues.put(KEY_ACTIVITY, activity);
        initialValues.put(KEY_PERCENTAGE, percentage);
        return db.insert(TABLE_GOOGLE_API_DATA, null, initialValues);
    }
    /*
     DELETE RECORDING
     */
    public boolean deleteRecording(long recording_id) {
        db.delete(TABLE_RECORDINGS, KEY_RECORDING_ID + "=?", new String[]{String.valueOf(recording_id)});
        db.delete(TABLE_GPS_DATA, KEY_RECORDING_ID + "=?", new String[]{String.valueOf(recording_id)});
        db.delete(TABLE_ACCELEROMETER_DATA, KEY_RECORDING_ID + "=?", new String[]{String.valueOf(recording_id)});
        db.delete(TABLE_GOOGLE_API_DATA, KEY_RECORDING_ID + "=?", new String[]{String.valueOf(recording_id)});
        return true;
    }

    //---retrieves all the records---
    public Cursor getAllRecordings() {
        return db.query(TABLE_RECORDINGS, null, null, null, null, null, KEY_RECORDING_ID + " ASC");
    }
    public Cursor getRecording(long recording_id) {
        return db.query(TABLE_RECORDINGS, null, KEY_RECORDING_ID +"=?", new String[]{String.valueOf(recording_id)}, null, null, KEY_RECORDING_ID + " ASC");
    }
    public Cursor getGpsData(long recording_id) {
        return db.query(TABLE_GPS_DATA, null, KEY_RECORDING_ID +"=?", new String[]{String.valueOf(recording_id)}, null, null, KEY_GPS_DATA_ID + " ASC");
    }

    public Cursor getAccelerometerData(long recording_id) {
        return db.query(TABLE_ACCELEROMETER_DATA, null, KEY_RECORDING_ID +"=?", new String[]{String.valueOf(recording_id)}, null, null, KEY_ACC_DATA_ID + " ASC");
    }
    public Cursor getGoogleData(long recording_id) {
        return db.query(TABLE_GOOGLE_API_DATA, null, KEY_RECORDING_ID +"=?", new String[]{String.valueOf(recording_id)}, null, null, KEY_GOOGLE_ID + " ASC");
    }
    /*
     UPDATE RECORDING INFO
     */
    public boolean updateRecordingInfo(String distance, String speed, long elapse_time, int finished, long recording_id)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_DISTANCE, distance);
        args.put(KEY_AVGSPEED, speed);
        args.put(KEY_ELAPSE_TIME, elapse_time);
        args.put(KEY_FINISHED, finished);
        return db.update(TABLE_RECORDINGS, args, KEY_RECORDING_ID + "=?",  new String[] {String.valueOf(recording_id)}) > 0;
    }
    public long getSzie(){
        return new File(db.getPath()).length();
    }
//    //--- Number of Notes!
//    public int NumberofNotes(int exhibition_id) {
//
//        Cursor c = db.query(TABLE_NOTES, new String[]{KEY_NOTE_ID}, "exhibition_id=?", new String[]{String.valueOf(exhibition_id)}, null, null, null);
//        int x = c.getCount();
//        if (x >= 0)
//            return x;
//        else
//            return 0;
//    }
//
//    public boolean ExistsExhibition(String name) {
//
//        String Query = "Select * from " + TABLE_EXHIBITIONS + " where " + KEY_NAME + "="
//                + name;
//        Cursor cursor = db.rawQuery(Query, null);
//        if (cursor.getCount() <= 0) {
//            return false;
//        }
//        return true;
//    }

}
