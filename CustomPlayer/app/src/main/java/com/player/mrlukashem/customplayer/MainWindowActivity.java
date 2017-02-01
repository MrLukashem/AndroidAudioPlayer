package com.player.mrlukashem.customplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.player.mrlukashem.customplayer.player.MainPlayer;
import com.player.mrlukashem.customplayer.player.PlayerActivity;

import java.util.HashSet;
import java.util.Set;

public class MainWindowActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 12;
    private static final String LAST_PLAYED_MEMORY = "LAST_PLAYED_MEMORY";
    private static final String LAST_PLAYED = "LAST_PLAYED";

    private void startPlayerActivity(String path, String artist, String title, String albumArt) {

        Intent intent;
        intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(MainPlayer.PATH, path);
        intent.putExtra(MainPlayer.TITLE, title);
        intent.putExtra(MainPlayer.ARTIST, artist);
        intent.putExtra(MainPlayer.ALBUM_ART, albumArt);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        SharedPreferences lastPlayedMemory = getSharedPreferences(LAST_PLAYED_MEMORY, MODE_PRIVATE);
        Set<String> lastTracks = lastPlayedMemory.getStringSet(LAST_PLAYED, new HashSet<String>());
        if (lastTracks.isEmpty()) {

        } else {
            // TODO: We should handle this case.
        }

        final TracksListAdapter adapter = new TracksListAdapter(this, R.layout.track_list_item);
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID},
                null, null, null);

        String path = "";
        String title = "No Title";
        String artist = "Artist name";
        int counter = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                path = cursor.getString(1);
                title = cursor.getString(2);
                artist = cursor.getString(3);
                int album_id = cursor.getInt(4);

                Cursor c1 = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Albums.ALBUM_ART},  MediaStore.Audio.Albums._ID + "=" + album_id, null, null);
                //adapter.add(new SimpleTrackDescriptor(thumb, title, artist));
                if (c1 != null && c1.getCount() >= 1) {
                    if (!c1.moveToFirst())
                        continue;

                    String artUri = c1.getString(0);

                    if (artUri == null) {
                        continue;
                    }

                    Bitmap mb = BitmapFactory.decodeFile(artUri);
                    if (mb == null) {
                        continue;
                    }
                    DisplayMetrics metrics = getResources().getDisplayMetrics();

                    mb = Bitmap.createScaledBitmap(mb, (int)(60 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)), (int)(60 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)), false);
                    adapter.add(new SimpleTrackDescriptor(mb, title, artist, path, artUri));
                    c1.close();
                }
                Log.e("MAINACTIVITY", "id = " + id + " path = " + path);
                counter++;
                if (counter == 30) {
                    break;
                }
            }
        }

        cursor.close();

        ListView listView = (ListView)findViewById(R.id.recentlyPlayedLV);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SimpleTrackDescriptor descriptor = (SimpleTrackDescriptor)adapterView.getItemAtPosition(i);
                startPlayerActivity(descriptor.getPath(), descriptor.getArtist(), descriptor.getTitle(), descriptor.getAlbumArt());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.

            } else {
                // User refused to grant permission.
            }
        }
    }

    public void click(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
        }
    }
}
