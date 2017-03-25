package com.player.mrlukashem.customplayer.nativeplayer;

import android.support.annotation.NonNull;

import com.player.mrlukashem.customplayer.interfaces.IMediaPlayer;

import java.io.IOException;

/**
 * Created by MrLukashem on 25.03.2017.
 */

public class NativePlayer implements IMediaPlayer {
    @Override
    public void setDataSource(@NonNull String path) throws IOException {

    }

    @Override
    public void pause() {

    }

    @Override
    public void play() {

    }

    @Override
    public void prepare() throws IOException {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void release() {

    }

    @Override
    public void seekTo(int ms) {

    }
}
