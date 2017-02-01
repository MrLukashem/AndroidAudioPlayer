package com.player.mrlukashem.customplayer.customplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.player.mrlukashem.customplayer.interfaces.IMediaPlayer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by MrLukashem on 28.01.2017.
 */

public class CustomPlayer implements IMediaPlayer {

    private final int NO_MORE_DATA = -1;

    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;
    private AudioTrack mTrack;
    private MediaFormat mFormat;
    private MediaFormat mOutputFormat;

    private Pair<Integer, Long> fillBuffer(ByteBuffer in) {
        int bytes = mExtractor.readSampleData(in, 0);

        Pair<Integer, Long> result = new Pair<>(bytes, mExtractor.getSampleTime());
        mExtractor.advance();

        return result;
    }

    private void processSamples(ByteBuffer samples, int size, long timestamp) {
        mTrack.write(samples, size, AudioTrack.WRITE_BLOCKING, timestamp);
    }

    private void stopPlayback() {
        mDecoder.stop();
        mDecoder.release();
        mTrack.stop();
        mTrack.release();
        mExtractor.release();
    }

    public CustomPlayer() {
    }

    @Override
    public void setDataSource(@NonNull String path) throws IOException {
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(path);
        mFormat = mExtractor.getTrackFormat(0);
    }

    @Override
    public void pause() {
        mDecoder.stop();
        mTrack.pause();
    }

    @Override
    public void play() {
        mTrack.play();
        mExtractor.selectTrack(0);
        mDecoder.start();
    }

    @Override
    public void prepare() throws IOException {
        mDecoder = MediaCodec.createDecoderByType(mFormat.getString(MediaFormat.KEY_MIME));
        mDecoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                ByteBuffer bufferTobeFilled = codec.getInputBuffer(index);
                Pair<Integer, Long> res = fillBuffer(bufferTobeFilled);

                if (res.first == NO_MORE_DATA) {
                    return;
                }

                codec.queueInputBuffer(index, 0, res.first, res.second, 0);
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) ==
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    stopPlayback();
                    return;
                }

                ByteBuffer decodecSamples = codec.getOutputBuffer(index);
                processSamples(decodecSamples, info.size, info.presentationTimeUs);
           //     Log.e("decoding", "presentation time = " + info.presentationTimeUs);
                codec.releaseOutputBuffer(index, info.presentationTimeUs);
            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {

            }
        });

        mDecoder.configure(mExtractor.getTrackFormat(0), null, null, 0);
        mOutputFormat = mDecoder.getOutputFormat();

        int samplerate = mOutputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int s = mExtractor.getTrackFormat(0).getInteger(MediaFormat.KEY_SAMPLE_RATE);
        Log.e("EQWWQEQW", "sample rate = " + s);
        int minBufferSize = AudioTrack.getMinBufferSize(samplerate,
                mOutputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT),
                AudioFormat.ENCODING_PCM_16BIT
        );
        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                samplerate * 2, mOutputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT),
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize,
                AudioTrack.MODE_STREAM);
    }

    @Override
    public int getDuration() {
        return (int)(mExtractor.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION));
    }

    @Override
    public int getCurrentPosition() {
        Log.e("qwewqe", "qwewqe" + mTrack.getPlaybackHeadPosition());
        return (mTrack.getPlaybackHeadPosition() / mTrack.getSampleRate()) * 100000;
    }

    @Override
    public void release() {
        mDecoder.stop();
        mTrack.stop();

        mDecoder.release();
        mExtractor.release();
        mTrack.release();
    }

    @Override
    public void seekTo(int ms) {
        mExtractor.seekTo(ms, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
    }
}
