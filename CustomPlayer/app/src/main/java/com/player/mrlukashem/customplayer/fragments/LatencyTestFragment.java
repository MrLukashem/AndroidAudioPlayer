package com.player.mrlukashem.customplayer.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.player.mrlukashem.customplayer.R;
import com.player.mrlukashem.customplayer.latencyutils.LatencyMeasurement;

import java.util.Map;

public class LatencyTestFragment extends Fragment {

    private final int FLOAT_POINT_SUPPORT = -100;

    private int mBufferSize;
    private int mSampleRate;
    private int mBitDepth;
    private LatencyMeasurement.PLAYER_TYPE mPlayerType;

    private Map<String, Integer> mParametersMap = new ArrayMap<>();

    private EditText mBufferSizeET;
    private Spinner mSpinnerBitDepth;
    private Spinner mSpinnerSampleRate;
    private Spinner mSpinnerPlayerType;

    public LatencyTestFragment() {
        mParametersMap.put("24Khz", 24000);
        mParametersMap.put("32Khz", 32000);
        mParametersMap.put("44.1Khz", 44100);
        mParametersMap.put("48Khz", 48000);
        mParametersMap.put("Default (Native)", 48000); // TODO: Get Native sample rate

        mParametersMap.put("8 bits", 8);
        mParametersMap.put("16 bits", 16);
        mParametersMap.put("float audio", FLOAT_POINT_SUPPORT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_latency_test, container, false);
        initSpinners(view);
        initButtonListener(view);
        initEditText(view);

        return view;
    }

    private void initEditText(View view) {
        EditText bufferSizeET = (EditText)view.findViewById(R.id.bufferSizeET);
        mBufferSizeET = bufferSizeET;
    }

    private void initButtonListener(View view) {
        Button startButton = (Button)view.findViewById(R.id.startTestBtn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Start Latency measurement test
                String bufferSizeString = mBufferSizeET.getText().toString();
                if (bufferSizeString.isEmpty()) {
                    mBufferSize = 16;
                } else {
                    mBufferSize = Integer.valueOf(bufferSizeString);
                }

                String bitDepthString = ((String)mSpinnerBitDepth.getSelectedItem());
                mBitDepth = mParametersMap.get(bitDepthString);

                String sampleRateString = ((String)mSpinnerSampleRate.getSelectedItem());
                mSampleRate = mParametersMap.get(sampleRateString);

                String playerTypeString = ((String)mSpinnerPlayerType.getSelectedItem());
                if (playerTypeString.equals("Audio Track")) {
                    mPlayerType = LatencyMeasurement.PLAYER_TYPE.AUDIO_TRACK;
                } else if (playerTypeString.equals("Native Player")) {
                    mPlayerType = LatencyMeasurement.PLAYER_TYPE.NATIVE_PLAYER;
                }

                LatencyMeasurement latencyMeasurement = new LatencyMeasurement(new LatencyMeasurement.LatencyResultListener() {
                    @Override
                    public void resultCallback(int latency) {
                        Log.e("resultCallback", "Latency = " + latency);
                    }
                }, 10, getContext());
                if (mBitDepth == FLOAT_POINT_SUPPORT) {
                    latencyMeasurement.setFloatSamples();
                } else {
                    latencyMeasurement.setBitDepth(mBitDepth);
                }
                latencyMeasurement.setBufferSize(mBufferSize);
                latencyMeasurement.setPlayerType(mPlayerType);
                latencyMeasurement.setSampleRate(mSampleRate);

                latencyMeasurement.start();
            }
        });
    }

    private void initSpinners(View view) {
        mSpinnerBitDepth = (Spinner)view.findViewById(R.id.spinnerBitDepth);
        mSpinnerSampleRate = (Spinner)view.findViewById(R.id.spinnerSampleRate);
        mSpinnerPlayerType = (Spinner)view.findViewById(R.id.spinnerPlayerType);

        ArrayAdapter<CharSequence> bitDepthAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.bit_depth_values, android.R.layout.simple_spinner_item);
        bitDepthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerBitDepth.setAdapter(bitDepthAdapter);

        final ArrayAdapter<CharSequence> sampleRateAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sample_rate_values, android.R.layout.simple_spinner_item);
        bitDepthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSampleRate.setAdapter(sampleRateAdapter);

        final ArrayAdapter<CharSequence> playerTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.player_type_values, android.R.layout.simple_spinner_item);
        bitDepthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPlayerType.setAdapter(playerTypeAdapter);
    }
}
