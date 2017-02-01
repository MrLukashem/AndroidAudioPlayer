package com.player.mrlukashem.customplayer.adapters;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by MrLukashem on 30.01.2017.
 */

public class TripleListItem {

    private Bitmap mBitmap;
    private String mFirst;
    private String mSecond;

    public TripleListItem(@Nullable Bitmap bitmap, @NonNull String first, @NonNull String second) {
        mBitmap = bitmap;
        mFirst = first;
        mSecond = second;
    }

    public @Nullable Bitmap getBitmap() {
        return mBitmap;
    }

    public String getFirst() {
        return mFirst;
    }

    public String getSecond() {
        return mSecond;
    }
}
