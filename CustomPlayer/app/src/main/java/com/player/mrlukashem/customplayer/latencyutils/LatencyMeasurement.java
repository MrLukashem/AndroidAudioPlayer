package com.player.mrlukashem.customplayer.latencyutils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by MrLukashem on 19.04.2017.
 */

public class LatencyMeasurement {
    static {
        System.loadLibrary("native-lib");
    }

    protected native void startLatencyTest(int bitDepth, int sampleRate);

    public enum PLAYER_TYPE {
        AUDIO_TRACK,
        NATIVE_PLAYER,
        MEDIA_PLAYER,
    }

    private PLAYER_TYPE mType = PLAYER_TYPE.AUDIO_TRACK;
    private int mBitDepth = 16;
    private int mSampleRate = 44000;
    private short[] mSamples16bit;

    public void setBitDepth(int bitDepth) {
        mBitDepth = bitDepth;
    }

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
    }

    public void setPlayerType(PLAYER_TYPE type) {
        mType = type;
    }

    public void startTest() {
        startLatencyTest(mBitDepth, mSampleRate);
    }

    public void reset() {

    }

    public void prepare() {

    }

    private void startPlaybackRequest() {
        if (mType.equals(PLAYER_TYPE.AUDIO_TRACK)) {
            AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    mSamples16bit.length, AudioTrack.MODE_STATIC);

            track.write(mSamples16bit, 0, mSamples16bit.length);
            track.play();
        } else if (mType.equals(PLAYER_TYPE.NATIVE_PLAYER)) {

        } else {

        }
    }

    protected short[] generateNoise16bitdepth(int bufferSize, int sampleRate) {
        short[] samples = new short[bufferSize];
        int maxAmplitude = 32760;
        int waveFrequency = 1600;
        double normalize = 1.0 / sampleRate;
        double time = normalize;

        for (int sampleNumber = 0; sampleNumber < bufferSize; sampleNumber++) {
            time = (sampleNumber + 1.0) * normalize;
            samples[sampleNumber] = (short)(maxAmplitude * Math.sin(2 * Math.PI * waveFrequency * time));
        }

        return samples;
    }
}
