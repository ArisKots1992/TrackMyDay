package aris.kots.trackmyday.DataClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Aris on 6/20/2016.
 */
public class Segment {
    private int mode;       // isZero, isWalking, isNotWalking
    private List<DataPoint> path;

    public Segment(int mode){
        this.path = new ArrayList<DataPoint>();
        this.mode = mode;
    }

    public int segmentSize(){
        return path.size();
    }
    public int returnMode(){
        return mode;
    }
    public void addDataPoint(DataPoint dataPoint){
        path.add(dataPoint);
    }
    public void appendPath(List<DataPoint> list){
        path.addAll(list);
    }
    public void sortPath(){
        Collections.sort(path);
    }
    public List<DataPoint> getPath(){
        return path;
    }
}
