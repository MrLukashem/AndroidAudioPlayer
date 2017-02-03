package com.player.mrlukashem.customplayer.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.player.mrlukashem.customplayer.R;
import com.player.mrlukashem.customplayer.SimpleTrackDescriptor;
import com.player.mrlukashem.customplayer.adapters.TripleElementsAdapter;
import com.player.mrlukashem.customplayer.adapters.TripleListItem;
import com.player.mrlukashem.customplayer.interfaces.IShowAlbum;
import com.player.mrlukashem.customplayer.utils.BitMapWorkerTask;

import java.io.File;

/**
 * Created by MrLukashem on 30.01.2017.
 */

public class SortedListFragment extends Fragment {

    public static String SORT_MODE = "SORT_MODE";
    public static int SORT_BY_ALBUM = 1;
    public static int SORT_BY_ARTIST = 2;

    private int mSortMode;
    private IShowAlbum mTrigger;

    private void prepareOnClickListeners(ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TripleListItem item = (TripleListItem)adapterView.getItemAtPosition(i);
                if (mSortMode == SORT_BY_ALBUM) {
                    String album = item.getFirst();
                    mTrigger.showAlbum(album);
                } else if (mSortMode == SORT_BY_ARTIST) {
                    String artist = item.getFirst();
                    mTrigger.showArtist(artist);
                }
            }
        });
    }

    private void triggerSetBitMap(final TripleElementsAdapter adapter, final int index, final String path) {
        new BitMapWorkerTask(adapter, index).execute(path);
    }

    private void prepareListItems(TripleElementsAdapter adapter) {
        Cursor cursor;
        ContentResolver resolver = getContext().getContentResolver();

        if (mSortMode == SORT_BY_ALBUM) {
            cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {
                            MediaStore.Audio.Albums.ALBUM_ART,
                            MediaStore.Audio.Albums.ALBUM,
                            MediaStore.Audio.Albums.ARTIST,
                    },
                    null, null, MediaStore.Audio.Albums.ALBUM + " ASC");

            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    String albumArtPath = cursor.getString(0);
                    String album = cursor.getString(1);
                    String artist = cursor.getString(2);

                    adapter.add(new TripleListItem(null, album, artist));
                    if (albumArtPath != null && (new File(albumArtPath).exists())) {
                        triggerSetBitMap(
                                adapter, adapter.getCount() - 1, albumArtPath);
                    }
                }

                cursor.close();
            }
        } else {
            cursor = resolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    new String[] {
                            MediaStore.Audio.Artists.ARTIST,
                            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                    },
                    null, null, MediaStore.Audio.Artists.ARTIST + " ASC");

            if (cursor != null && cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    String artist = cursor.getString(0);
                    String numbertracks = cursor.getString(1);

                    adapter.add(new TripleListItem(null, artist, numbertracks + " tracks"));
                }

                cursor.close();
            }
        }
    }

    private void setActivityName() {
        if (mSortMode == SORT_BY_ALBUM) {
            getActivity().setTitle("Albums");
        } else {
            getActivity().setTitle("Artists");
        }
    }

    public SortedListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.activity_albums_sort, container, false);
        ListView list = (ListView)view.findViewById(R.id.sortedListLV);
        TripleElementsAdapter adapter =
                new TripleElementsAdapter(getContext(), R.layout.track_list_item);

        Bundle args = getArguments();
        if (args != null)
            mSortMode = args.getInt(SORT_MODE, SORT_BY_ALBUM);
        else
            mSortMode = SORT_BY_ALBUM;

        setActivityName();

        list.setAdapter(adapter);
        prepareListItems(adapter);
        prepareOnClickListeners(list);
        mTrigger = (IShowAlbum)getActivity();

        return view;
    }
}
