package com.player.mrlukashem.customplayer.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by MrLukashem on 29.01.2017.
 */

public class Utils {
    public static int msToSeconds(int ms) {
        return ms / 1000;
    }

    public static String secondsToTimeString(int currentTimeInSeconds) {
        String timeString;
        int minutes = currentTimeInSeconds / 60;
        int seconds = currentTimeInSeconds - (minutes * 60);

        if (seconds >= 10) {
            timeString = String.valueOf(minutes) + ":" + String.valueOf(seconds);
        } else {
            timeString = String.valueOf(minutes) + ":0" + String.valueOf(seconds);
        }
        return timeString;
    }

    public static int getPXFromDP(int dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int)(dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
