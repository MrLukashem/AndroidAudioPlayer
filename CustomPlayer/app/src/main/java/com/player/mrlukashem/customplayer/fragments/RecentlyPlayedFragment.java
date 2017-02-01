package com.player.mrlukashem.customplayer.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.player.mrlukashem.customplayer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentlyPlayedFragment extends Fragment {


    public RecentlyPlayedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recently_played, container, false);
    }

}
