package nl.rickhutten.homeremote.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.Utils;
import nl.rickhutten.homeremote.activity.AlbumOverviewActivity;
import nl.rickhutten.homeremote.activity.ArtistOverviewActivity;
import nl.rickhutten.homeremote.activity.MusicActivity;
import nl.rickhutten.homeremote.net.GETJSONRequest;
import nl.rickhutten.homeremote.net.GETRequest;
import nl.rickhutten.homeremote.net.OnJSONDownloaded;
import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.R;

public class MusicControlView extends RelativeLayout {

    private ViewGroup rootView;
    private MusicActivity context;
    private SharedPreferences sp;
    private boolean active;
    public int ID;
    private boolean newSongComing;
    private MusicProgressView progressView;
    public boolean paused;
    private QueueView queueView;
    private View container;

    public MusicControlView(final MusicActivity context) {
        super(context);
        this.context = context;
        setup();
    }

    private void setup() {
        ID = new Random().nextInt(1000);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (ViewGroup) inflater.inflate(R.layout.music_control_view, this, false);
        queueView = (QueueView) rootView.findViewById(R.id.queueView);
        container = rootView.findViewById(R.id.music_control_view_container);
        addView(rootView);
        this.setClipChildren(false);

        this.setTransitionName("musicControlView");
        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        paused = sp.getBoolean("paused", true);
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
                        sp.edit().putBoolean("paused", false).apply();
                        paused = false;
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
                        sp.edit().putBoolean("paused", false).apply();
                        paused = false;
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

        findViewById(R.id.queueButton).setOnClickListener(new OnClickListener() {
            private int originalHeight = (int)context.getResources().getDimension(R.dimen.play_bar_height);
            private LayoutParams lp = (LayoutParams) container.getLayoutParams();
            private boolean actionBarWasShown = true;
            private ActionBar actionBar = context.getSupportActionBar();

            @Override
            public void onClick(View v) {
                // Toggle visibility
                if (queueView.getVisibility() == VISIBLE) {
                    // QueueView going down
                    ValueAnimator anim = ValueAnimator.ofInt(Utils.getScreenHeight(context), originalHeight);
                    anim.setDuration(300);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            lp.height = (int)animation.getAnimatedValue();
                            container.setLayoutParams(lp);
                            if (actionBarWasShown) {
                                actionBar.setHideOffset(actionBar.getHeight() - container.getTop());
                            }
                        }
                    });
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            queueView.setVisibility(INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    Path path = new Path();
                    path.cubicTo(0.75f, 0.25f, 0.95f, 0.75f, 1f, 1f);  // Bezier curve
                    anim.setInterpolator(new PathInterpolator(path));
                    anim.start();
                } else {
                    // QueueView going up
                    queueView.setVisibility(VISIBLE);
                    actionBarWasShown = actionBar.getHideOffset() < 0.5*actionBar.getHeight();
                    ValueAnimator anim = ValueAnimator.ofInt(originalHeight, Utils.getScreenHeight(context));
                    anim.setDuration(500);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            lp.height = (int)animation.getAnimatedValue();
                            container.setLayoutParams(lp);
                            if (actionBarWasShown) {
                                actionBar.setHideOffset(actionBar.getHeight() - container.getTop());
                            }
                        }
                    });
                    Path path = new Path();
                    path.cubicTo(0.1f, 1f, 0.5f, 1f, 1f, 1f);  // Bezier curve
                    anim.setInterpolator(new PathInterpolator(path));
                    anim.start();
                }

            }
        });
    }

    /**
     * Set the information saved in the shared preferences
     */
    public void update() {
        // Set play pause button
        paused = sp.getBoolean("paused", true);
        if (paused) {
            ((ImageView)findViewById(R.id.playPause)).setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
        } else {
            ((ImageView)findViewById(R.id.playPause)).setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
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

    /**
     * Get information from server and update view
     */
    public void updateHard() {
        // Get status and queue from server
        getStatus();
        updateQueue();
    }

    private void getStatus() {
        new GETJSONRequest(new OnJSONDownloaded() {
            @Override
            public void onJSONCompleted(JSONObject jObject) {
                try {
                    String status = jObject.getString("status");
                    if (status.equalsIgnoreCase("STOPPED")) {
                        progressView = (MusicProgressView) findViewById(R.id.progressBar);
                        progressView.setProgress(0);
                        int volume = jObject.getInt("volume");
                        SharedPreferences.Editor e = sp.edit();
                        e.putString("artist", null);
                        e.putString("album", null);
                        e.putString("song", null);
                        e.putFloat("duration", 0);
                        e.putFloat("progress", 0);
                        e.putInt("volume", volume);
                        e.apply();
                        ((TextView) findViewById(R.id.playingText)).setText(" ");
                        return;
                    }

                    SharedPreferences.Editor e = sp.edit();
                    JSONObject playing = jObject.getJSONObject("playing");
                    int volume = jObject.getInt("volume");

                    if (status.equalsIgnoreCase("PAUSED")) {
                        paused = true;
                        e.putBoolean("paused", true);
                    } else if (status.equalsIgnoreCase("PLAYING")) {
                        paused = false;
                        e.putBoolean("paused", false);
                    } else {
                        Log.w("MusicControlView", "Status is wrong: " + status);
                        return;
                    }
                    e.putString("artist", playing.getString("artist"));
                    e.putString("album", playing.getString("album"));
                    e.putString("song", playing.getString("song"));
                    e.putFloat("duration", (float) playing.getDouble("duration"));
                    e.putFloat("progress", (float) playing.getDouble("elapsed"));
                    e.putInt("volume", volume);
                    e.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                update();
            }
        }).execute(URL.getStatusUrl(context));
    }

    /**
     * Downloads new queue from server and updates the queueView
     */
    private void updateQueue() {
        new GETJSONRequest(new OnJSONDownloaded() {
            @Override
            public void onJSONCompleted(JSONObject jObject) {
                ArrayList<ArrayList<String>> queue = new ArrayList<>();
                ArrayList<String> song;

                // Queue => [ [artist, album, song], ... ]
                try {
                    JSONArray songs = jObject.getJSONArray("queue");
                    for (int i = 0; i < songs.length(); i++) {
                        song = new ArrayList<>();
                        song.add(songs.getJSONObject(i).getString("artist"));
                        song.add(songs.getJSONObject(i).getString("album"));
                        song.add(songs.getJSONObject(i).getString("song"));
                        queue.add(song);
                    }
                    saveQueueInActivity(queue);
                    queueView.updateText(context);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }).execute(URL.getQueueUrl(context));
    }

    private void saveQueueInActivity(ArrayList<ArrayList<String>> queue) {
        context.setQueue(queue);
    }


    public boolean isPlaying() {
        return !sp.getBoolean("paused", true);
    }

    private void pauseMusic() {
        GETRequest r = new GETRequest(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                sp.edit().putBoolean("paused", true).apply();
                paused = true;
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
                paused = false;
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

    public void setNewSongComing(boolean newSongComing) {
        if (newSongComing) {
            sp.edit().putFloat("progress", 0f).commit();
        }
        this.newSongComing = newSongComing;
    }

    public boolean isNewSongComing() {
        return this.newSongComing;
    }
}
