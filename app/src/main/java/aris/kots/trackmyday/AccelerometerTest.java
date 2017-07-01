package aris.kots.trackmyday;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.EdgeDetail;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.charts.SeriesLabel;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import weka.core.Instances;

public class AccelerometerTest extends AppCompatActivity {

    private String TAG = "AccelerometerTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// Create background track
        DecoView arcView = (DecoView) findViewById(R.id.dynamicArcView);
        try {
            BufferedReader bufferReader = null;
            bufferReader = new BufferedReader(new InputStreamReader(getAssets().open("vehicle.arff")));

            Instances train;
        }catch(Exception e){

        }
//Create data series track
//        final SeriesItem seriesItem = new SeriesItem.Builder(Color.argb(255, 64, 196, 0))
//                .setRange(0, 100, 0)
//                .setInitialVisibility(false)
//                .setLineWidth(32f)
//              //  .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_OUTER, Color.parseColor("#22000000"), 0.4f))
//                .setSeriesLabel(new SeriesLabel.Builder("Percent %.0f%%").build())
//                .setInterpolator(new OvershootInterpolator())
//                .setShowPointWhenEmpty(false)
//                .setCapRounded(false)
//                .setInset(new PointF(32f, 32f))
//                .setDrawAsPoint(false)
//                .setSpinClockwise(true)
//                .setSpinDuration(4000)
//                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
//                .build();
//        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
//
//        final SeriesItem seriesItem3 = new SeriesItem.Builder(Color.argb(255, 64, 196, 0))
//                .setRange(0, 200, 200)
//                .setSeriesLabel(new SeriesLabel.Builder("Percent %.0f%%")
//                        .setColorBack(Color.argb(218, 0, 0, 0))
//                        .setColorText(Color.argb(255, 255, 255, 255))
//                        .setTypeface(roboto)
//                        .build())
//                .build();
//        int series1Index = arcView.addSeries(seriesItem);
//
//        arcView.addEvent(new DecoEvent.Builder(25).setIndex(series1Index).setDelay(100).build());

//        arcView.addEvent(new DecoEvent.Builder(100).setIndex(series1Index).setDelay(8000).build());
//        arcView.addEvent(new DecoEvent.Builder(10).setIndex(series1Index).setDelay(12000).build());
//        arcView.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
//                .setRange(0, 100, 0)
//                .setInitialVisibility(false)
//                .setLineWidth(32f)
//                .build());

        SeriesItem backgroundLine = new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .setLineWidth(32f)
                .build();

//Create data series track
        SeriesItem seriesItem1 = new SeriesItem.Builder(Color.argb(255, 64, 196, 0))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .setSeriesLabel(new SeriesLabel.Builder("Percent %.0f%%").build())
                .build();
        SeriesItem seriesItem2 = new SeriesItem.Builder(Color.argb(64, 64, 64, 64))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();
        int backgroundLineIndex = arcView.addSeries(backgroundLine);
        int series1Index = arcView.addSeries(seriesItem1);
        int series2Index = arcView.addSeries(seriesItem2);

        arcView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(1000)
                .build());
//
        arcView.addEvent(new DecoEvent.Builder(100).setIndex(backgroundLineIndex).setDelay(2000).build());
        arcView.addEvent(new DecoEvent.Builder(30).setIndex(series1Index).setDelay(3000).build());
        arcView.addEvent(new DecoEvent.Builder(100).setIndex(series2Index).setDelay(4000).build());

    }


    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
    }

}
