package aris.kots.trackmyday.DataClass;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Aris on 6/19/2016.
 */
public class DataPoint implements  Comparable<DataPoint> {

    private long time;              // current point time
    private long elapseTime;        // elapse time from the service started time
    private LatLng point;           // current point coordinates
    private float speed;            // current speed in the point

    public DataPoint(long time, long elapseTime, LatLng point, float speed){
        this.time = time;
        this.elapseTime = elapseTime;
        this.point = point;
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public long getElapseTime() {
        return elapseTime;
    }

    public LatLng getPoint() {
        return point;
    }

    public float getSpeed() {
        return speed;
    }



    public int compareTo(DataPoint another) {
        if(this.time < another.time)
            return -1;
        else if (this.time > another.time)
            return 1;
        else
            return 0;
    }
}
