package com.player.mrlukashem.customplayer.utils;

import android.os.CountDownTimer;

/**
 * Created by MrLukashem on 29.01.2017.
 */

// TODO: We should use CustomTimer instead own implementation in PlayerActivity.
public abstract class CustomCountDownTimer extends CountDownTimer {

    public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }


}
