package com.player.mrlukashem.customplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.player.mrlukashem.customplayer.R;
import com.player.mrlukashem.customplayer.utils.Utils;

/**
 * Created by MrLukashem on 31.01.2017.
 */

public class TrackListAdapter extends ArrayAdapter<TrackDesc> {

    private static final String TAG = "TrackListAdapter";

    private LayoutInflater mInflater;

    public TrackListAdapter(Context context, int resource) {
        super(context, resource);

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.track_desc_item, null, true);
        }

        try {
            TextView titleTV = (TextView)rowView.findViewById(R.id.titleTV);
            TextView durTV = (TextView)rowView.findViewById(R.id.durTV);
            titleTV.setText(getItem(position).getTitle());
            durTV.setText(Utils.secondsToTimeString(getItem(position).getDuration() / 1000));
        } catch (NullPointerException npe) {
            Log.e(TAG, npe.getMessage());
            // TODO: We should handle.
        }

        return rowView;
    }
}
