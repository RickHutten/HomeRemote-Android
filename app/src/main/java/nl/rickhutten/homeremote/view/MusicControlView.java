package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import nl.rickhutten.homeremote.GETRequest;
import nl.rickhutten.homeremote.OnTaskCompleted;
import nl.rickhutten.homeremote.R;

public class MusicControlView extends RelativeLayout {
    private ViewGroup rootView;
    private SharedPreferences sp;
    private boolean active;
    public int ID;
    private boolean newSongComming;
    private MusicProgressView progressView;

    public MusicControlView(Context context) {
        super(context);
        ID = new Random().nextInt(1000);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (ViewGroup) inflater.inflate(R.layout.music_control_view, this, false);
        addView(rootView);
        this.setClipChildren(false);

        this.setTransitionName("musicControlView");
        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        findViewById(R.id.playPause).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying()) {
                    pauseMusic();
                } else {
                    resumeMusic();
                }
            }
        });

        findViewById(R.id.next).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GETRequest next = new GETRequest(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        // Put values in sharedpreferences
                        sp.edit().putInt("playpause", R.drawable.ic_pause_circle_outline_black_48dp).apply();
                        // Dont update musicControlView, its updated from push notification
                    }
                });
                next.execute("http://rickert.noip.me/next");
            }
        });

        findViewById(R.id.prev).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GETRequest next = new GETRequest(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        // Put values in sharedpreferences
                        sp.edit().putInt("playpause", R.drawable.ic_pause_circle_outline_black_48dp).apply();
                        // Dont update musicControlView, its updated from push notification
                    }
                });
                next.execute("http://rickert.noip.me/previous");
            }
        });

        // Don't let the container let touches get 'through'
        findViewById(R.id.music_control_view_container).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Do nothing, return true means consume the event
                return true;
            }
        });
    }

    public void update() {
        // Set play pause button
        int ResId = sp.getInt("playpause", 0);
        if (ResId != 0) {
            ((ImageView) findViewById(R.id.playPause)).setImageResource(ResId);
        }

        // Set text
        String artist = sp.getString("artist", null);
        String song = sp.getString("song", null);

        if (artist != null && song != null) {
            ((TextView) findViewById(R.id.playingText)).setText(
                    artist + " - " + song);
        }

        progressView = (MusicProgressView) findViewById(R.id.progressBar);
        progressView.setMusicControlView(this);
        progressView.startCountdown(sp);
    }

    public boolean isPlaying() {
        return !sp.getBoolean("paused", true);
    }

    private void pauseMusic() {
        GETRequest r = new GETRequest(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                sp.edit().putBoolean("paused", true).apply();
                ((ImageView)findViewById(R.id.playPause))
                        .setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
            }
        });
        r.execute("http://rickert.noip.me/pause");
    }

    private void resumeMusic() {
        GETRequest r = new GETRequest(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                sp.edit().putBoolean("paused", false).apply();
                ((ImageView)findViewById(R.id.playPause))
                        .setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
            }
        });
        r.execute("http://rickert.noip.me/resume");
    }

    public boolean isViewActive() {
        return this.active;
    }

    public void setActive(boolean b) {
        this.active = b;
        if (!b) {
            // If not active, save the progress in shared preferences
            sp.edit().putFloat("progress", progressView.getSeconds()).commit();
        }
    }

    public void setNewSongComming(boolean newSongComming) {
        if (newSongComming) {
            sp.edit().putFloat("progress", 0f).commit();
        }
        this.newSongComming = newSongComming;

    }

    public boolean isNewSongComming() {
        return this.newSongComming;
    }
}
