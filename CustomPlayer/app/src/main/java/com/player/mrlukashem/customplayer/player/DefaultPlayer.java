package com.player.mrlukashem.customplayer.player;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import com.player.mrlukashem.customplayer.interfaces.IMediaPlayer;

import java.io.IOException;

/**
 * Created by mrlukashem on 31.01.17.
 */

public class DefaultPlayer extends MediaPlayer implements IMediaPlayer {

    @Override
    public void play() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void setDataSource(@NonNull String path) throws IOException {
        super.setDataSource(path);
    }

    @Override
    public void prepare() throws IOException {
        super.prepare();
    }

    @Override
    public int getDuration() {
        return super.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return super.getCurrentPosition();
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    public void seekTo(int ms) {
        super.seekTo(ms);
    }
}
