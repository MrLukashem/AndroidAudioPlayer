package com.player.mrlukashem.customplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import com.player.mrlukashem.customplayer.adapters.TripleElementsAdapter;

/**
 * Created by MrLukashem on 30.01.2017.
 */

public class BitMapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final TripleElementsAdapter mAdapter;
    private final int mPos;
    private String path;

    public BitMapWorkerTask(TripleElementsAdapter adapter, int index) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        mAdapter = adapter;
        mPos = index;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        path = params[0];
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        // TODO: We don't have to scaled bitmap.
     //   bitmap = Bitmap.createScaledBitmap(
       //         bitmap, TripleElementsAdapter.mImageSize,
         //       TripleElementsAdapter.mImageSize, false);
        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mAdapter != null && bitmap != null) {
            mAdapter.setImageView(bitmap, mPos);
        }
    }
}
