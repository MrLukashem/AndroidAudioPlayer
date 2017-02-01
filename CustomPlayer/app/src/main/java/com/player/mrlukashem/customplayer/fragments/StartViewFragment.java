package com.player.mrlukashem.customplayer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.player.mrlukashem.customplayer.R;

/**
 * Created by MrLukashem on 30.01.2017.
 */

public class StartViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plus_one, container, false);
    }
}
