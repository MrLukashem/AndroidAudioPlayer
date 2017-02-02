package com.player.mrlukashem.customplayer.player;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.player.mrlukashem.customplayer.MainActivity;
import com.player.mrlukashem.customplayer.R;
import com.player.mrlukashem.customplayer.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerActivity extends AppCompatActivity {

    private static final String CANNOT_PLAY_CONTENT = "The audio content is not supported!";
    private static final String TAG = "PlayerActivity";

    private SeekBar mSeekBar;
    private CountDownTimer mTrackTimer;
    private AtomicReference<Integer> mDurationToEnd = new AtomicReference<>(-1);
    private AtomicReference<Integer> mDuration = new AtomicReference<>(-1);
    private AtomicReference<Integer> mCurrentTimeInSeconds = new AtomicReference<>(-1);

    private TextView mTimerFromStartTV;
    private TextView mTimerFromEndTV;
    private TextView mTitleTV;
    private TextView mArtistTV;
    private ImageView mAlbumArtIV;
    private Button mPlayPauseButton;
    private int mAlbumArtWidthPX;
    private int mAlbumArtHeightPX;

    private int mClickColor;
    private Drawable mDefaultButtonDrawable;

    private boolean mIsPlaying = false;
    private boolean mDisablePlayer = false;

    private MainPlayer mMainPlayer;
    private String mDataSourcePath = "";
    private boolean mUseCustomPlayer = false;

    private void switchToPlayButton(Button button) {
        button.setText(getResources().getString(R.string.play));
        button.setBackground(mDefaultButtonDrawable);
    }

    private void switchToPauseButton(Button button) {
        button.setText(getResources().getString(R.string.pause));
        button.setBackgroundColor(mClickColor);
    }

    private void handlePlayPauseState(Button button) {
        if (mDisablePlayer) {
            initRealPlayer(mUseCustomPlayer);
        }

        if (mIsPlaying) {
            switchToPauseButton(button);

            if (!mMainPlayer.play()) {
                mIsPlaying = false;
            }

            startTimer();
        } else {
            switchToPlayButton(button);

            mMainPlayer.pause();
            initializeTimer();
        }
    }

    private void showCannotPlayContentToast() {
        Toast.makeText(this, CANNOT_PLAY_CONTENT, Toast.LENGTH_LONG).show();
    }

    private boolean handleFastSeekEvent(View view, MotionEvent motionEvent, boolean rightSeek) {
        if (mDisablePlayer) {
            return false;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.setBackground(mDefaultButtonDrawable);
            return true;
        } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.setBackgroundColor(mClickColor);
            if (rightSeek)
                mMainPlayer.fastSeekRight();
            else
                mMainPlayer.fastSeekLeft();

            initializeTimer();
            startTimer();
            return true;
        }

        return false;
    }

    private void initializeViews() {
        mClickColor = ContextCompat.getColor(this, R.color.colorPrimaryDark2);
        mDefaultButtonDrawable = findViewById(R.id.playPauseButton).getBackground();

        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mTimerFromStartTV = (TextView)findViewById(R.id.timerStartTV);
        mTimerFromEndTV = (TextView)findViewById(R.id.timerEndTV);
        mTitleTV = (TextView)findViewById(R.id.titleTV);
        mArtistTV = (TextView)findViewById(R.id.artistTV);
        mAlbumArtIV = (ImageView)findViewById(R.id.trackThumbnailIV);
        mPlayPauseButton = (Button)findViewById(R.id.playPauseButton);

        findViewById(R.id.fastSeekLeftButton).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return handleFastSeekEvent(view, motionEvent, false);
            }
        });

        findViewById(R.id.fastSeekRightButton).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return handleFastSeekEvent(view, motionEvent, true);
            }
        });
    }

    private void initializeSeekBar() {
        mSeekBar.setMax(Utils.msToSeconds(mMainPlayer.getDuration()));
    }

    private void startTimer() {
        initializeTimer();
        mTrackTimer.start();
    }

    private boolean initRealPlayer(boolean useCustomPlayer) {
        mMainPlayer = new MainPlayer(useCustomPlayer);
        return mMainPlayer.setDataSource(mDataSourcePath) && mMainPlayer.prepare();
    }

    private void releaseResources() {
        if (mDisablePlayer) {
            return;
        }

        mMainPlayer.release();
        if (mTrackTimer != null) {
            mTrackTimer.cancel();
        }

        mDisablePlayer = true;
        mIsPlaying = false;
    }

    private void resetPlayer() {
        releaseResources();

        int curPos = 0;
        int dur = mDuration.get();

        updateProgressBar(curPos, dur);
        switchToPlayButton(mPlayPauseButton);
    }

    private void updateProgressBar(int curPos, int dur) {
        mSeekBar.setProgress(curPos);
        Log.e(TAG, "updateProgressBar = " + Utils.msToSeconds(dur));
        mTimerFromStartTV.setText(Utils.secondsToTimeString(curPos));
        mTimerFromEndTV.setText(
                Utils.secondsToTimeString(Utils.msToSeconds(dur) - curPos));
    }

    // TODO: Create custom timer.
    private void initializeTimer() {
        if (!mIsPlaying) {
            return;
        }

        if (mTrackTimer != null) {
            mTrackTimer.cancel();
        }

        mDuration.set(mMainPlayer.getDuration());
        mDurationToEnd.set(mDuration.get() - mMainPlayer.getCurrentPos());
        Log.e(TAG, "mDuration  = " + mDuration + " mDurationToEnd = " + mDurationToEnd);
        // TODO: We should handle when duration == -1.
        mTrackTimer = new CountDownTimer(mDurationToEnd.get(), 1000) {
            @Override
            public void onTick(long tickTime) {
                if (mIsPlaying) {
                    mCurrentTimeInSeconds.set(Utils.msToSeconds(mDuration.get() - (int)tickTime));
                    Log.e(TAG, "currentTimeInSeconds = " + mCurrentTimeInSeconds.get());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateProgressBar(mCurrentTimeInSeconds.get(), mDuration.get());
                        }
                    });
                }
            }

            @Override
            public void onFinish() {
                resetPlayer();
                // TODO: Do we need handle finish?
            }
        };
    }

    private void initializeAlbumArtSize() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mAlbumArtHeightPX = (int)(300 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        mAlbumArtWidthPX = (int)(300 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initializeViews();
        initializeAlbumArtSize();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String path = extras.getString(MainPlayer.PATH, "");
            mDataSourcePath = path;
            mUseCustomPlayer = extras.getBoolean(MainPlayer.USE_CUSTOM_PLAYER, false);

            if (!path.isEmpty()) {
                boolean isReady = initRealPlayer(mUseCustomPlayer);
                if (!isReady) {
                    showCannotPlayContentToast();
                    mDisablePlayer = true;
                    return;
                }

                initializeSeekBar();
                initializeTimer();

                // TODO: Split player initialization logic and views between onStart and OnCreate.
                String title = extras.getString(MainPlayer.TITLE, "No TITLE Found");
                mTitleTV.setText(title);
                String artist = extras.getString(MainPlayer.ARTIST, "No Artist Found");
                mArtistTV.setText(artist);
                String albumArt = extras.getString(MainPlayer.ALBUM_ART, "");

                if (!albumArt.isEmpty()) {
                    Bitmap albumArtBitMap = BitmapFactory.decodeFile(albumArt);

                    if (albumArtBitMap != null) {
                        albumArtBitMap = Bitmap.createScaledBitmap(
                                albumArtBitMap, mAlbumArtWidthPX, mAlbumArtHeightPX, false);
                        mAlbumArtIV.setImageBitmap(albumArtBitMap);
                    }
                }

                mAlbumArtIV.getLayoutParams().height = mAlbumArtHeightPX;
                mAlbumArtIV.getLayoutParams().width = mAlbumArtWidthPX;
                mAlbumArtIV.setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseResources();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void PlayOnButtonClick(View view) {
        if (mDisablePlayer) {
            // Check more more time real player initialization if it fails show again error toast.
            boolean isReady = initRealPlayer(mUseCustomPlayer);
            if (!isReady) {
                showCannotPlayContentToast();
                return;
            }

            mDisablePlayer = false;
        }

        mIsPlaying = !mIsPlaying;
        handlePlayPauseState((Button)view);
    }

    private void addCurrentTrackToFavouritesList() {
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> tracksTemp = settings.getStringSet(MainActivity.FAV_TRACKS, new HashSet<String>());
        Set<String> tracks = new HashSet<>(tracksTemp);
        tracks.add(mDataSourcePath);

        settings.edit().putStringSet(MainActivity.FAV_TRACKS, tracks).apply();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void addToFavourite(View view) {
        if (mDataSourcePath.isEmpty()) {
            return;
        }

        addCurrentTrackToFavouritesList();
        showToast("Track " + mTitleTV.getText() + "has been added to favourites list");
    }
}
