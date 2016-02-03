package nl.rickhutten.homeremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MusicControlView extends RelativeLayout {
    ViewGroup rootView;
    SharedPreferences sp;

    public MusicControlView(Context context) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (ViewGroup) inflater.inflate(R.layout.music_control_view, this, false);
        addView(rootView);

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
                        sp.edit().putInt("playpause", R.drawable.ic_pause_circle_outline_white_48dp).apply();
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
                        sp.edit().putInt("playpause", R.drawable.ic_pause_circle_outline_white_48dp).apply();
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

    }

    public void setPlayPause(int id) {
        sp.edit().putInt("playpause", id).apply();
        ((ImageView)findViewById(R.id.playPause)).setImageResource(id);
    }

    public boolean isPlaying() {
        return sp.getInt("playpause", 0) == R.drawable.ic_pause_circle_outline_white_48dp;
    }

    private void pauseMusic() {
        GETRequest r = new GETRequest(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                setPlayPause(R.drawable.ic_play_circle_outline_white_48dp);
            }
        });
        r.execute("http://rickert.noip.me/pause");
    }

    private void resumeMusic() {
        GETRequest r = new GETRequest(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                setPlayPause(R.drawable.ic_pause_circle_outline_white_48dp);
            }
        });
        r.execute("http://rickert.noip.me/resume");
    }

}
