package com.player.mrlukashem.customplayer.nativeplayer;

import android.support.annotation.NonNull;

/**
 * Created by MrLukashem on 25.03.2017.
 */

class NativePlayerJNIBridge {
    static {
        System.loadLibrary("native-lib");
    }

    protected native void setDataSourceJNI(@NonNull String path);

    protected native void pauseJNI();

    protected native void playJNI();

    protected native void prepareJNI();

    protected native int getDurationJNI();

    protected native int getCurrentPositionJNI();

    protected native void releaseJNI();

    protected native void seekToJNI(int ms);
}
