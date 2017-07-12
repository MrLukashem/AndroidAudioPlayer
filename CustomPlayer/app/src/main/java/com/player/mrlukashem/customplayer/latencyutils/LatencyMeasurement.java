package com.player.mrlukashem.customplayer.latencyutils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.SystemClock;
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
    private final String TAG = "LatencyMeasurement";

    static {
        System.loadLibrary("native-lib");
    }

    public enum PLAYER_TYPE {
        AUDIO_TRACK,
        NATIVE_PLAYER,
        MEDIA_PLAYER,
    }

    private static double n = 0.0;

    private static final int DEFAULT_SAMPLE_RATE_48KHZ = 48000;

    protected native void startLatencyTest(int bitDepth, int sampleRate);

    protected native void release();

    private Context mContext;

    private PLAYER_TYPE mType = PLAYER_TYPE.AUDIO_TRACK; // Default player
    private int mBitDepth = 16; // Default bit depth
    private int mSampleRate = DEFAULT_SAMPLE_RATE_48KHZ; // Default sample rate; 44 kHz
    private int mBufferSize = 500;
    private int mNumOfChannels = 1;

    private boolean mFloatSamples = false;

    private int mNumOfItrs = 1;
    private List<Integer> results = new ArrayList<>();

    private LatencyResultListener mClientListener;

    private AudioTrack mTrack;
    private NativePlayer mNativePlayer;

    public LatencyMeasurement(@NonNull LatencyResultListener listener, int numOfItrs, Context context) {
        mNumOfItrs = numOfItrs;
        mClientListener = listener;
        mContext = context;
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

    public void setBufferSize(int size) {
        mBufferSize = size;
    }

    public void setFloatSamples() {
        mFloatSamples = true;
    }

    public void unsetFloatSamples() {
        mFloatSamples = false;
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

        if (mTrack != null) {
            mTrack.release();
        }
        if (mNativePlayer != null) {
            mNativePlayer.release();
        }

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

    private short[] generateNoise16bitdepth(int bufferSize, int sampleRate, int numOfChannels) {
        if ((bufferSize % numOfChannels) != 0) {
            Log.e(TAG, "");
            return null;
        }

        int bufferSizePerChannel = bufferSize / numOfChannels;
        short[] samples = new short[bufferSize];
        int maxAmplitude = 32760;
        int waveFrequency = 1600;
        double normalize = 1.0 / sampleRate;
        double time = normalize;

        for (int sampleNumber = 0; sampleNumber < bufferSizePerChannel; sampleNumber++) {
            time = (sampleNumber + 1.0) * normalize;
            for (int channelNumber = 0; channelNumber < numOfChannels; channelNumber++) {
                int sampleIndex = sampleNumber * numOfChannels + channelNumber;
                samples[sampleIndex] = (short)(maxAmplitude
                        * Math.sin(2 * Math.PI * waveFrequency * time));
            }
        }

        return samples;
    }

    private byte[] generateNoise8bitdepth(int bufferSize, int sampleRate, int numOfChannels) {
        if ((bufferSize % numOfChannels) != 0) {
            Log.e(TAG, "");
            return null;
        }

        int bufferSizePerChannel = bufferSize / numOfChannels;
        byte[] samples = new byte[bufferSize];
        int maxAmplitude = (int)(127.0);
        int waveFrequency = 1600;
        double normalize = 1.0 / sampleRate;
        double time = normalize;

        for (int sampleNumber = 0; sampleNumber < bufferSizePerChannel; sampleNumber++) {
            time = (sampleNumber + 1.0) * normalize;
            for (int channelNumber = 0; channelNumber < numOfChannels; channelNumber++) {
                int sampleIndex = sampleNumber * numOfChannels + channelNumber;
                samples[sampleIndex] = (byte) (maxAmplitude
                        * Math.sin(2 * Math.PI * waveFrequency * time));
            }
        }

        return samples;
    }

    private float[] generateNoiseFloat(int bufferSize, int sampleRate, int numOfChannels) {
        if ((bufferSize % numOfChannels) != 0) {
            Log.e(TAG, "");
            return null;
        }

        int bufferSizePerChannel = bufferSize / numOfChannels;
        float[] samples = new float[bufferSize];
        int maxAmplitude = (int)(127.0);
        int waveFrequency = 1600;
        double normalize = 1.0 / sampleRate;
        double time = normalize;

        for (int sampleNumber = 0; sampleNumber < bufferSizePerChannel; sampleNumber++) {
            time = (sampleNumber + 1.0) * normalize;
            for (int channelNumber = 0; channelNumber < numOfChannels; channelNumber++) {
                int sampleIndex = sampleNumber * numOfChannels + channelNumber;
                samples[sampleIndex] = (float)Math.sin(2 * Math.PI * waveFrequency * time);
            }
        }

        return samples;
    }

    private void playUsingAudioTrack(float[] samples, int sampleRate, int bufferSize) {
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STATIC);

        track.write(samples, 0, bufferSize, AudioTrack.WRITE_BLOCKING);
        track.play();
        track.stop();
    }

    private void playUsingNativePlayer(float[] floatSamples, int sampleRate, int bufferSize) {
        NativePlayer nativePlayer = new NativePlayer();
        nativePlayer.setSourceBuffer(floatSamples, floatSamples.length, 8, 1, sampleRate, bufferSize);
        try {
            nativePlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        nativePlayer.play();
    }

    private void playUsingAudioTrack(short[] samples, int sampleRate, int bufferSize) {
      //     SystemClock.sleep(2500);

        Log.e("playUsingAudioTrack", "START!!!!!");
        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STATIC);

        mTrack.play();
        mTrack.write(samples, 0, bufferSize);
        //SystemClock.sleep(1000);
       // mTrack.stop();
    }

    private void playUsingNativePlayer(short[] samples, int sampleRate, int bufferSize) {
        mNativePlayer = new NativePlayer();
        mNativePlayer.setSourceBuffer(samples, samples.length, 16, 1, sampleRate, bufferSize);
        try {
            mNativePlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mNativePlayer.play();

    }

    private void playUsingAudioTrack(byte[] samples, int sampleRate, int bufferSize) {
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT,
                bufferSize, AudioTrack.MODE_STATIC);

        track.write(samples, 0, bufferSize);
        track.play();
        track.stop();
    }

    protected void playUsingNativePlayer(byte[] samples, int sampleRate, int bufferSize) {
        NativePlayer nativePlayer = new NativePlayer();
        nativePlayer.setSourceBuffer(samples, samples.length, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.CHANNEL_OUT_MONO, sampleRate, bufferSize);
        try {
            nativePlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        nativePlayer.play();
    }

    private void playSinWaveRequest() {
        if (mType == PLAYER_TYPE.AUDIO_TRACK) {
            if (mBitDepth == 16) {
                short[] samples16bit = generateNoise16bitdepth(mBufferSize,
                        mSampleRate, mNumOfChannels);
                playUsingAudioTrack(samples16bit, mSampleRate, mBufferSize);
            } else if (mBitDepth == 8) {
                byte[] samples8bit = generateNoise8bitdepth(mBufferSize,
                        mSampleRate, mNumOfChannels);
                playUsingAudioTrack(samples8bit, mSampleRate, mBufferSize);
            } else if (mFloatSamples) {
                float[] floatSamples = generateNoiseFloat(mBufferSize, mSampleRate,
                        mNumOfChannels);
                playUsingAudioTrack(floatSamples, mSampleRate, mBufferSize);
            }
        } else if (mType == PLAYER_TYPE.NATIVE_PLAYER) {
            if (mBitDepth == 16) {
                short[] samples16bit = generateNoise16bitdepth(mBufferSize,
                        mSampleRate, mNumOfChannels);
                playUsingNativePlayer(samples16bit, mSampleRate, mBufferSize);
            } else if (mBitDepth == 8) {
                byte[] samples8bit = generateNoise8bitdepth(mBufferSize,
                        mSampleRate, mNumOfChannels);
                playUsingNativePlayer(samples8bit, mSampleRate, mBufferSize);
            } else if(mFloatSamples) {
                float[] floatSamples = generateNoiseFloat(mBufferSize, mSampleRate,
                        mNumOfChannels);
                playUsingNativePlayer(floatSamples, mSampleRate, mBufferSize);
            }
        }
    }

    public interface LatencyResultListener {
        void resultCallback(int latency);
    }
}

// TODO: Float framers support!