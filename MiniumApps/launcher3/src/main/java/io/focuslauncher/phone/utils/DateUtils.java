package io.focuslauncher.phone.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by shahab on 3/24/16.
 */
public class DateUtils {
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_FORMAT = "EEEE, MMMM dd";

    private static final long SECOND = 1000L;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;

    public static long nextIntervalMillis(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        Calendar now = Calendar.getInstance();

        long unit = (long) Math.ceil((now.getTimeInMillis() - cal.getTimeInMillis()) / (double) millis);
        cal.setTimeInMillis(cal.getTimeInMillis() + millis * unit);

        //Tracer.i("nextIntervalMillis: " + SimpleDateFormat.getDateTimeInstance().format(cal.getTimeInMillis()) + " for mins " + (millis / 60 / 1000) + " unit: " + unit);

        return cal.getTimeInMillis();

    }

    public static String log(long millis) {
        return new SimpleDateFormat("hh:mm:ss.SSS a", Locale.US).format(new Date(millis));
    }

    public static String log() {
        return new SimpleDateFormat("hh:mm:ss.SSS a", Locale.US).format(new Date());
    }

    public static String interval(long millis) {
        String ret = "";

        if (millis >= HOUR) {
            ret += (millis / HOUR) + " hrs ";
            millis /= HOUR;
        }
        if (millis >= SECOND) {
            ret += (millis / SECOND) + " sec";
        }

        return ret;
    }
}
