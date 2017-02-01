package com.player.mrlukashem.customplayer.player;

import android.support.annotation.NonNull;
import android.util.Log;

import com.player.mrlukashem.customplayer.customplayer.CustomPlayer;
import com.player.mrlukashem.customplayer.interfaces.IMediaPlayer;

import java.io.IOException;

/**
 * Created by MrLukashem on 28.01.2017.
 */

public class MainPlayer {
    public static final String PATH = "PATH";
    public static final String TITLE = "TITLE";
    public static final String ARTIST = "ARTIST";
    public static final String ALBUM_ART = "ALBUM_ART";
    public static final String USE_CUSTOM_PLAYER = "USE_CUSTOM_PLAYER";

    private static final String TAG = "MainPlayer";
    private static final int TenSecondsInMs = 10000;

    private IMediaPlayer mRealPlayer;

    public MainPlayer(boolean useCustomPlayer) {
        if (useCustomPlayer) {
            // to be filled.
            mRealPlayer = new CustomPlayer();
        } else {
            mRealPlayer = new DefaultPlayer();
        }
    }

    public boolean setDataSource(@NonNull String path) {
        try {
            mRealPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean pause() {
        try {
            mRealPlayer.pause();
        } catch (IllegalStateException ise) {
            Log.e(TAG, ise.getMessage());
            return false;
        }

        return true;
    }

    public boolean play() {
        try {
            mRealPlayer.play();
        } catch (IllegalStateException ise) {
            Log.e(TAG, ise.getMessage());
            return false;
        }

        return true;
    }

    public boolean fastSeekRight() {
        try {
            int currentPos = mRealPlayer.getCurrentPosition();
            int duration = mRealPlayer.getDuration();
            int positionAfterSeek = currentPos + TenSecondsInMs;
            if (positionAfterSeek > duration) {
                positionAfterSeek = 0;
            }
            Log.e(TAG, "currentpos = " + currentPos);
            mRealPlayer.seekTo(positionAfterSeek);
        } catch (IllegalStateException ise) {
            Log.e(TAG, ise.getMessage());
            return false;
        }

        return true;
    }

    public boolean fastSeekLeft() {
        try {
            int positionAfterSeek = mRealPlayer.getCurrentPosition() - TenSecondsInMs;
            if (positionAfterSeek < 0) {
                positionAfterSeek = 0;
            }

            mRealPlayer.seekTo(positionAfterSeek);
        } catch (IllegalStateException ise) {
            Log.e(TAG, ise.getMessage());
            return false;
        }

        return true;
    }

    public boolean prepare() {
        try {
            mRealPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int getDuration() {
        return mRealPlayer.getDuration();
    }

    public int getCurrentPos() {
        return mRealPlayer.getCurrentPosition();
    }

    public void release() {
        mRealPlayer.release();
    }
}
