package com.player.mrlukashem.customplayer.latencyutils;

/**
 * Created by MrLukashem on 01.07.2017.
 */

public class Foo {
    private int sampleRate = 0;

    public int getSampleRate() {
        return sampleRate;
    }

    public int convertSampleRate(int[] samples) {
        for (int sample : samples) {
            sampleRate = sample * sampleRate;
        }

        return sampleRate;
    }
}
