package aris.kots.trackmyday.CleverFunctions;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aris on 6/19/2016.
 */
public class ConvertFunctons {

    static public String millisecondsToDate(long milliseconds) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date myDate = new Date(milliseconds);

        return dateFormat.format(myDate);
    }

    static public String millisecondsToDateComplete(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy, HH:mm a");
        Date myDate = new Date(milliseconds);
        return dateFormat.format(myDate);
    }

    static public String elapseTimeToString(long elapseTimeMillis) {


        // claculate elapse time (human format)
        long hours = TimeUnit.MILLISECONDS.toHours(elapseTimeMillis);
        elapseTimeMillis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapseTimeMillis);
        elapseTimeMillis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapseTimeMillis);

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

    }
}
