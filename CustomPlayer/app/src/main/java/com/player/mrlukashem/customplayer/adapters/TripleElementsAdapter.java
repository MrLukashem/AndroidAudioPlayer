package com.player.mrlukashem.customplayer.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.player.mrlukashem.customplayer.R;
import com.player.mrlukashem.customplayer.utils.BitMapWorkerTask;
import com.player.mrlukashem.customplayer.utils.Utils;

/**
 * Created by MrLukashem on 30.01.2017.
 */

public class TripleElementsAdapter extends ArrayAdapter<TripleListItem> {

    static final int mImageSize = 80;

    private static final String TAG = "TripleElementsAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private final Object mSetImageViewLock = new Object();
    private SparseArray<Bitmap> mMap = new SparseArray<>();

    private static class ViewHolder {
        ImageView p_img;
        TextView p_first;
        TextView p_second;
    }

    public TripleElementsAdapter(Context context, int resource) {
        super(context, resource);

        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public @NonNull View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;

        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.track_list_item, null, true);

            TextView firstTV = (TextView)rowView.findViewById(R.id.TitleTV);
            TextView secondTV = (TextView)rowView.findViewById(R.id.ArtistTV);
            ImageView thumbnailTV = (ImageView)rowView.findViewById(R.id.thumbNailIV);

            holder = new ViewHolder();
            holder.p_first = firstTV;
            holder.p_second = secondTV;
            holder.p_img = thumbnailTV;

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder)rowView.getTag();
            // Remove current bitmap because probably it is reused by ListView.
            // So we want to avoid wrong image errors.
            holder.p_img.setImageBitmap(null);
        }

        try {
            holder.p_first.setText(getItem(position).getFirst());
            holder.p_second.setText(getItem(position).getSecond());

            if (mMap.get(position) != null) {
                // Get img view from resource.
                ImageView thumbnailTV = (ImageView)rowView.findViewById(R.id.thumbNailIV);
                // Set bitmap from Map that was put from non ui thread.
                thumbnailTV.setImageBitmap(mMap.get(position));
            }

            holder.p_img.getLayoutParams().height = Utils.getPXFromDP(mImageSize, mContext);
            holder.p_img.getLayoutParams().width = Utils.getPXFromDP(mImageSize, mContext);
            holder.p_img.setBackgroundColor(Color.WHITE);
        } catch (NullPointerException npe) {
            // TODO: We should handle.
            Log.e(TAG, npe.getMessage());
        }

        return rowView;
    }

    public void setImageView(Bitmap bitmap, int position) {
        synchronized (mSetImageViewLock) {
            if (mMap.get(position) == null) {
                mMap.put(position, bitmap);
                notifyDataSetChanged();
            }
        }
    }
}
