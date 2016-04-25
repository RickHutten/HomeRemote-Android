package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.activity.AlbumOverviewActivity;
import nl.rickhutten.homeremote.activity.ArtistOverviewActivity;
import nl.rickhutten.homeremote.net.GETRequest;
import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.R;

public class MusicControlView extends RelativeLayout {

    private ViewGroup rootView;
    private Context context;
    private SharedPreferences sp;
    private boolean active;
    public int ID;
    private boolean newSongComming;
    private MusicProgressView progressView;

    public MusicControlView(Context context) {
        super(context);
        this.context = context;
        setup();
    }

    private void setup() {
        ID = new Random().nextInt(1000);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (ViewGroup) inflater.inflate(R.layout.music_control_view, this, false);
        addView(rootView);
        this.setClipChildren(false);

        this.setTransitionName("musicControlView");
        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        setClickListeners();
    }

    private void setClickListeners() {
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
                next.execute(URL.getNextUrl(context));
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
                next.execute(URL.getPreviousUrl(context));
            }
        });

        rootView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        final View optionsButton = findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(context, optionsButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.menu_music_control_view, popup.getMenu());
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent;
                        String artist = sp.getString("artist", null);
                        String album = sp.getString("album", null);
                        if (artist == null || album == null) {
                            return true;
                        }
                        switch (item.getItemId()) {
                            case R.id.to_artist:
                                intent = new Intent(context, ArtistOverviewActivity.class);
                                intent.putExtra("artist", artist);
                                context.startActivity(intent);
                                break;
                            case R.id.to_album:
                                intent = new Intent(context, AlbumOverviewActivity.class);
                                intent.putExtra("artist", artist);
                                intent.putExtra("album", album);
                                context.startActivity(intent);
                                break;
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
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
        r.execute(URL.getPauseUrl(context));
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
        r.execute(URL.getResumeUrl(context));
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
