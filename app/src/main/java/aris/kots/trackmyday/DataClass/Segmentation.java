package aris.kots.trackmyday.DataClass;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Aris on 6/20/2016.
 */
public class Segmentation {

    private List<Segment> segmentList;
    private String TAG = "Segmentation";

    public Segmentation(List<DataPoint> completePath) {
        segmentList = new ArrayList<Segment>();

        segmentationFirstStage(completePath);   // walking,nonWalking,Zero segments
        segmentationSecondStage();              // Merge small ALL the small segments
        segmentationThirdStage();       // merge the same segments ( fix spaces between polylines -> same for running/walking )
        sortPathInEverySegment();
    }

    /*
        First Stage of Segmentation:
        Divide into segments of walking, Non-Walking and Zero mode depending on the speed
     */
    private void segmentationFirstStage(List<DataPoint> completePath) {

        int mode = -1;
        Segment segment = null;
        for (int i = 0; i < completePath.size(); i++) {

            if (completePath.get(i).getSpeed() == 0) {
                // create new segment if the mode is new
                if (mode != ConstantValues.isZero) {
                    mode = ConstantValues.isZero;
                    segment = new Segment(mode);
                    segmentList.add(segment);
                    Log.i(TAG, "Segment isZero created.");
                }
            } else if (completePath.get(i).getSpeed() < ConstantValues.speedUpperBound) {
                if (mode != ConstantValues.isWalking) {
                    mode = ConstantValues.isWalking;
                    segment = new Segment(mode);
                    segmentList.add(segment);
                    Log.i(TAG, "Segment isWalking created.");
                }
            } else if (completePath.get(i).getSpeed() >= ConstantValues.speedUpperBound) {
                if (mode != ConstantValues.isNotWalking) {
                    mode = ConstantValues.isNotWalking;
                    segment = new Segment(mode);
                    segmentList.add(segment);
                    Log.i(TAG, "Segment isNotWalking created.");
                }
            }
            // now add the dataPoint to our segment
            segment.addDataPoint(completePath.get(i));
            Log.i(TAG, "Segment added.");
        }
        Log.i(TAG, "Number of Segments: " + String.valueOf(segmentListSize()));
    }

    /*
    Second Stage of segmentation:
    Merge ALL the small segments < minimumSegmentSize
     */
    private void segmentationSecondStage() {

        // the second condition is to avoid an infinity loop if our numberOfSegments at the end is "1" and
        // this is smallSegment, so we have to end the loop if we only have 1 segment.
        while (numberOfSmallSizeSegments() != 0 && segmentListSize() > 1) {

            // Inner loop that will break evey time its job is done (delete 1 segment)
            for (int i = 0; i < segmentList.size(); i++) {

                // if the segment is "SMALL"
                if (segmentList.size() >= 2 && segmentList.get(i).segmentSize() < ConstantValues.minimumSegmentSize) {
                    // Head
                    if (i == 0) {
                        // ++ add if isZero = other the other wins ** +
                        if (segmentList.get(i).segmentSize() <= segmentList.get(i + 1).segmentSize()) {
                            segmentList.get(i + 1).appendPath(segmentList.get(i).getPath());
                            segmentList.remove(i);
                            break;
                        }
                        // Tail
                    } else if (i == segmentList.size() - 1) {
                        if (segmentList.get(i).segmentSize() <= segmentList.get(i - 1).segmentSize()) {
                            segmentList.get(i - 1).appendPath(segmentList.get(i).getPath());
                            segmentList.remove(i);
                            break;
                        }
                        // Body
                    } else {
                        // here we will add to the larger of the two (before, after)
                        if (segmentList.get(i + 1).segmentSize() > segmentList.get(i - 1).segmentSize()) {
                            if (segmentList.get(i).segmentSize() < segmentList.get(i + 1).segmentSize()) {
                                segmentList.get(i + 1).appendPath(segmentList.get(i).getPath());
                                segmentList.remove(i);
                                break;
                            }
                        } else {
                            if (segmentList.get(i).segmentSize() <= segmentList.get(i - 1).segmentSize()) {
                                segmentList.get(i - 1).appendPath(segmentList.get(i).getPath());
                                segmentList.remove(i);
                                break;
                            }
                        }

                    }
                }
            }
        }
    }
/*
Merge all the same segments
 */
    private void segmentationThirdStage() {

        Segment prevSegment = null;
        Iterator<Segment> i = segmentList.iterator();

        while (i.hasNext()) {
            Segment segment = i.next();
            if (prevSegment != null && prevSegment.returnMode() == segment.returnMode()) {
                prevSegment.appendPath(segment.getPath());
                i.remove();
                continue;
            }
            prevSegment = segment;
        }

    }

    // Sort every segment by timestamp
    private void sortPathInEverySegment() {
        for(Segment segment : segmentList)
            segment.sortPath();
    }

    private int numberOfSmallSizeSegments() {
        int counter = 0;
        for (int i = 0; i < segmentList.size(); i++)
            if (segmentList.get(i).segmentSize() < ConstantValues.minimumSegmentSize)
                counter++;

        return counter;
    }

    public List<Segment> getSegmentList() {
        return segmentList;
    }

    public int segmentListSize() {
        return segmentList.size();
    }
}
