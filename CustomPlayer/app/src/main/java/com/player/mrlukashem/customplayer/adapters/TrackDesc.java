package com.player.mrlukashem.customplayer.adapters;

/**
 * Created by MrLukashem on 31.01.2017.
 */

public class TrackDesc {
    private String mTitle;
    private int mDuration;
    private String mMetaPath;
    private String mMetaArtist;
    private String mMetaAlbumArt;

    public TrackDesc(String title, int dur, String metaPath, String metaArtist, String metaAlbumArt) {
        mTitle = title;
        mDuration = dur;
        mMetaPath = metaPath;
        mMetaArtist = metaArtist;
        mMetaAlbumArt = metaAlbumArt;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getMetaPath() {
        return mMetaPath;
    }

    public String getMetaArtist() {
        return mMetaArtist;
    }

    public String getMetaAlbumArt() {
        return mMetaAlbumArt;
    }
}
