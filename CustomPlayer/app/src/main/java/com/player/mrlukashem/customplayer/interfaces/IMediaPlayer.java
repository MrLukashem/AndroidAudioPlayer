package com.player.mrlukashem.customplayer.interfaces;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Created by mrlukashem on 31.01.17.
 */

public interface IMediaPlayer {
    void setDataSource(@NonNull String path) throws IOException;

    void pause();

    void play();

    void prepare() throws IOException;

    int getDuration();

    int getCurrentPosition();

    void release();

    void seekTo(int ms);
}
