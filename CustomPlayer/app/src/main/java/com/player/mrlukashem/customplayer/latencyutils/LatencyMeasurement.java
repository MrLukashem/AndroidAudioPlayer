package com.player.mrlukashem.customplayer.latencyutils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.util.Log;

import com.player.mrlukashem.customplayer.nativeplayer.NativePlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MrLukashem on 19.04.2017.
 */

public class LatencyMeasurement {
    static {
        System.loadLibrary("native-lib");
    }

    public enum PLAYER_TYPE {
        AUDIO_TRACK,
        NATIVE_PLAYER,
        MEDIA_PLAYER,
    }

    private static final int DEFAULT_SAMPLE_RATE_48KHZ = 48000;

    protected native void startLatencyTest(int bitDepth, int sampleRate);

    protected native void release();

    private PLAYER_TYPE mType = PLAYER_TYPE.AUDIO_TRACK; // Default player
    private int mBitDepth = 16; // Default bit depth
    private int mSampleRate = DEFAULT_SAMPLE_RATE_48KHZ; // Default sample rate; 44 kHz

    private int mNumOfItrs = 1;
    private List<Integer> results = new ArrayList<>();

    private LatencyResultListener mClientListener;

    public LatencyMeasurement(@NonNull LatencyResultListener listener, int numOfItrs) {
        mNumOfItrs = numOfItrs;
        mClientListener = listener;
    }

    public void setBitDepth(int bitDepth) {
        mBitDepth = bitDepth;
    }

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
    }

    public void setPlayerType(PLAYER_TYPE type) {
        mType = type;
    }

    public void start() {
        startLatencyTest(mBitDepth, mSampleRate);
    }

    public void reset() { // Reset test to defaults parameters
        mBitDepth = 16;
        mSampleRate = DEFAULT_SAMPLE_RATE_48KHZ;
    }

    public void setTestResultListener(@NonNull LatencyResultListener listener) {
        mClientListener = listener;
    }

    public void nativeResultCallback(int latency) {
        mNumOfItrs--;
        results.add(latency);

        for (int i = 0; i < results.size(); i++)
            Log.e("ARRAYY", String.valueOf(results.get(i)));

        if (mNumOfItrs == 0) {
            int sum = 0;
            for (int i = 0; i < results.size(); i++) {
                sum += results.get(i);
            }
            latency = sum / results.size();
            mClientListener.resultCallback(latency);
        } else {
            //release();
            Log.e("eqweqw", "WQJEKHWQJKHEJQWHKEJQWHKEJHQWKJEHQWKJ");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    startLatencyTest(mBitDepth, mSampleRate);
                }
            });
            t.start();
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

    protected void playUsingAudioTrack(short[] samples, int sampleRate) {
//        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
  //              AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
    //            500, AudioTrack.MODE_STATIC);

        //track.write(samples, 0, 500);
        //track.play();
        // track.stop();

        NativePlayer nativePlayer = new NativePlayer();
        nativePlayer.setSourceBuffer(samples, samples.length, 16, 1, sampleRate, 400);
        try {
            nativePlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        nativePlayer.play();
    }

    private void playSinWaveRequest() {

        int sampleRate = 48000;
        short[] samples = generateNoise16bitdepth(10 * sampleRate, sampleRate);
        playUsingAudioTrack(samples, sampleRate);
    }

    public interface LatencyResultListener {
        void resultCallback(int latency);
    }
}
