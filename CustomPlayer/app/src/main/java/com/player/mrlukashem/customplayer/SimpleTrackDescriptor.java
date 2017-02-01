package com.player.mrlukashem.customplayer;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

/**
 * Created by MrLukashem on 29.01.2017.
 */

public class SimpleTrackDescriptor {

    private Bitmap mThumbnail;
    private String mAlbumArt;
    private String mTitle;
    private String mArtist;
    private String mPath;

    public SimpleTrackDescriptor(
            @Nullable Bitmap thumbnail, @NonNull String title, @NonNull String artist,
            @NonNull String path, @NonNull String albumArt) {
        mThumbnail = thumbnail;
        mTitle = title;
        mArtist = artist;
        mPath = path;
        mAlbumArt = albumArt;
    }

    public @Nullable Bitmap getThumbnail() {
        return mThumbnail;
    }

    public @NonNull String getTitle() {
        return mTitle;
    }

    public @NonNull String getArtist() {
        return mArtist;
    }

    public @NonNull String getPath() {
        return mPath;
    }

    public @NonNull String getAlbumArt() {
        return mAlbumArt;
    }
}
