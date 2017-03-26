package com.player.mrlukashem.customplayer.nativeplayer;

import android.support.annotation.NonNull;

import com.player.mrlukashem.customplayer.interfaces.IMediaPlayer;

import java.io.IOException;

/**
 * Created by MrLukashem on 25.03.2017.
 */

public class NativePlayer extends NativePlayerJNIBridge implements IMediaPlayer {

    @Override
    public void setDataSource(@NonNull String path) throws IOException {
        setDataSourceJNI(path);
    }

    @Override
    public void pause() {
        pauseJNI();
    }

    @Override
    public void play() {

        playJNI();
    }

    @Override
    public void prepare() throws IOException {
        prepareJNI();
    }

    @Override
    public int getDuration() {
        getDurationJNI();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        getCurrentPositionJNI();
        return 0;
    }

    @Override
    public void release() {
        releaseJNI();
    }

    @Override
    public void seekTo(int ms) {
        if (ms < 0) {
            ms = 0;
        }

        seekToJNI(ms);
    }
}
