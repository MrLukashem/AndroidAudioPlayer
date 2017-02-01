package com.player.mrlukashem.customplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by MrLukashem on 29.01.2017.
 */

public class TracksListAdapter extends ArrayAdapter<SimpleTrackDescriptor> {

    private Activity mContext;
    private int mAlbumArtHeightPX;
    private int mAlbumArtWidthPX;

    private void initializeAlbumArtSize() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mAlbumArtHeightPX = (int)(80 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        mAlbumArtWidthPX = (int)(80 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public TracksListAdapter(Activity context, int resource) {
        super(context, resource);

        mContext = context;
        initializeAlbumArtSize();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater layoutInflater = mContext.getLayoutInflater();
            rowView = layoutInflater.inflate(R.layout.track_list_item, null, true);
        }

        TextView titleTV = (TextView)rowView.findViewById(R.id.TitleTV);
        TextView artistTV = (TextView)rowView.findViewById(R.id.ArtistTV);
        ImageView thumbnailTV = (ImageView)rowView.findViewById(R.id.thumbNailIV);

        titleTV.setText(getItem(position).getTitle());
        artistTV.setText(getItem(position).getArtist());

        if (getItem(position).getThumbnail() != null) {
            thumbnailTV.setImageBitmap(getItem(position).getThumbnail());
        } else {
            thumbnailTV.getLayoutParams().height = mAlbumArtHeightPX;
            thumbnailTV.getLayoutParams().width = mAlbumArtWidthPX;
            thumbnailTV.setBackgroundColor(Color.WHITE);
        }

        return rowView;
    }
}
