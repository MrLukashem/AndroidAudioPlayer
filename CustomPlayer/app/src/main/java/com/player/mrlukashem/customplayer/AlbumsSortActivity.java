package com.player.mrlukashem.customplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;

public class AlbumsSortActivity extends AppCompatActivity {

    private static String TITLE = "Artists sorted tracks list";

    private int mThumbnailHeightPX;
    private int mThumbnailWidthPX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_sort);

        setTitle(TITLE);
        initThumbnailDims();
        initializeViews();
    }

    private void initThumbnailDims() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mThumbnailHeightPX = (int)(60 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        mThumbnailWidthPX = mThumbnailHeightPX;
    }

    private Bitmap scaleBitMap(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, mThumbnailWidthPX, mThumbnailHeightPX, false);
        return bitmap;
    }

    private void initializeViews() {
        ListView albumsList = (ListView)findViewById(R.id.sortedListLV);
        TracksListAdapter adapter = new TracksListAdapter(this, R.layout.track_list_item);

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID,  MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM_ID }, null, null,
                        MediaStore.Audio.Media.ARTIST + " ASC");

        if (cursor != null && cursor.getCount() >= 1) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String artist = cursor.getString(3);
                int albumID = cursor.getInt(4);
                Bitmap mb = null;
                String albumArt = "";

                Cursor cursor1 = resolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Audio.Albums.ALBUM_ART },
                        MediaStore.Audio.Albums._ID + "=" + albumID, null, null);
                if (cursor1 != null && cursor1.getCount() >= 1) {
                    cursor1.moveToFirst();
                    if (cursor1.getString(0) != null)
                        albumArt = cursor1.getString(0);

                    if (albumArt != null && (new File(albumArt)).exists())
                        mb = BitmapFactory.decodeFile(albumArt);

                    if (mb != null) {
                        mb = scaleBitMap(mb);
                    }

                    adapter.add(new SimpleTrackDescriptor(mb, title, artist, path, albumArt));
                    cursor1.close();
                    continue;
                }

                adapter.add(new SimpleTrackDescriptor(null, title, artist, path, albumArt));
            }

            cursor.close();
        }

        albumsList.setAdapter(adapter);
        albumsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }
}
