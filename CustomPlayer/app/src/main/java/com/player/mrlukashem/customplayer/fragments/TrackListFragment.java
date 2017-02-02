package com.player.mrlukashem.customplayer.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.player.mrlukashem.customplayer.R;
import com.player.mrlukashem.customplayer.SettingsActivity;
import com.player.mrlukashem.customplayer.adapters.TrackDesc;
import com.player.mrlukashem.customplayer.adapters.TrackListAdapter;
import com.player.mrlukashem.customplayer.player.MainPlayer;
import com.player.mrlukashem.customplayer.player.PlayerActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by MrLukashem on 30.01.2017.
 */

public class TrackListFragment extends Fragment {

    public static final String ALBUM = "ALBUM";
    public static final String ARTIST = "ARTIST";
    public static final String DATA_SET = "DATA_SET";

    private static final int ALBUM_MODE = 1;
    private static final int ARTIST_MODE = 2;
    private static final int DATA_SET_MODE = 3;
    private int mMode = ALBUM_MODE;

    private List<String> mDataList = new ArrayList<>();

    private void setOnClickListener(ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TrackDesc desc = (TrackDesc)adapterView.getItemAtPosition(i);
                Intent intent = new Intent(getContext(), PlayerActivity.class);
                intent.putExtra(MainPlayer.PATH, desc.getMetaPath());
                intent.putExtra(MainPlayer.TITLE, desc.getTitle());
                intent.putExtra(MainPlayer.ARTIST, desc.getMetaArtist());
                intent.putExtra(MainPlayer.ALBUM_ART, desc.getMetaAlbumArt());

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean mode = sharedPreferences.getBoolean("CustomPlayerSwitch", false);
                intent.putExtra(MainPlayer.USE_CUSTOM_PLAYER, mode);

                startActivity(intent);
            }
        });
    }

    private void prepareListViewUsingTracks(TrackListAdapter adapter) {
        Cursor cursor;
        ContentResolver resolver = getContext().getContentResolver();

        for (String data : mDataList) {
            cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM_ID,
                    },
                    MediaStore.Audio.Media.DATA + "=" + "'" + data + "'",
                    null, null);

            if (cursor != null && cursor.getCount() >= 1) {
                cursor.moveToFirst();

                String title = cursor.getString(0);
                int dur = cursor.getInt(1);
                String artist = cursor.getString(2);
                int albumID = cursor.getInt(3);

                String albumArt = "";
                Cursor cursor1 = resolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Audio.Albums.ALBUM_ART},
                        MediaStore.Audio.Albums._ID + "=" + albumID, null, null);
                if (cursor1 != null && cursor1.getCount() >= 1) {
                    cursor1.moveToFirst();
                    String temp = cursor1.getString(0);
                    if (temp != null) {
                        albumArt = temp;
                    }
                }

                adapter.add(new TrackDesc(title, dur, data, artist, albumArt));
            }

            cursor.close();
        }
    }

    private void prepareListView(String key, TrackListAdapter adapter) {
        if (mMode == DATA_SET_MODE) {
            prepareListViewUsingTracks(adapter);
            return;
        }

        if (key.isEmpty()) {
            return;
        }

        String selectColumn = "";
        if (mMode == ALBUM_MODE) {
            selectColumn = MediaStore.Audio.Media.ALBUM;
        } else {
            selectColumn = MediaStore.Audio.Media.ARTIST;
        }

        Cursor cursor;
        ContentResolver resolver = getContext().getContentResolver();

        cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM_ID,
                },
                selectColumn + "=" + "'" + key + "'",
                null, null);

        if (cursor != null && cursor.getCount() >= 1) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0);
                String path = cursor.getString(1);
                int dur = cursor.getInt(2);
                String artist = cursor.getString(3);
                int albumID = cursor.getInt(4);

                String albumArt = "";
                Cursor cursor1 = resolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Audio.Albums.ALBUM_ART},
                        MediaStore.Audio.Albums._ID + "=" + albumID, null, null);
                if (cursor1 != null && cursor1.getCount() >= 1) {
                    cursor1.moveToFirst();
                    String temp = cursor1.getString(0);
                    if (temp != null) {
                        albumArt = temp;
                    }
                }

                adapter.add(new TrackDesc(title, dur, path, artist, albumArt));
            }

            cursor.close();
        }
    }

    private String extractArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String arg = args.getString(ARTIST, "");
            if (!arg.isEmpty()) {
                mMode = ARTIST_MODE;
                return arg;
            }

            arg = args.getString(ALBUM, "");
            if (!arg.isEmpty()) {
                mMode = ALBUM_MODE;
                return arg;
            }

            boolean isDataSetMode = args.getBoolean(DATA_SET, true);
            if (isDataSetMode) {
                mMode = DATA_SET_MODE;

                SharedPreferences settings =
                        PreferenceManager.getDefaultSharedPreferences(getContext());
                Set<String> tracks = settings.getStringSet("fav_tracks", new HashSet<String>());
                for (String track : tracks) {
                    mDataList.add(track);
                }
            }
        }

        return "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.track_list_fragment, container, false);
        ListView list = (ListView)view.findViewById(R.id.trackListLV);
        TrackListAdapter adapter = new TrackListAdapter(getContext(), R.layout.track_desc_item);

        prepareListView(extractArguments(), adapter);
        setOnClickListener(list);
        list.setAdapter(adapter);

        return view;
    }
}
