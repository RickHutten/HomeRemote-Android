package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.net.POSTRequest;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.URL;

public class SongView extends RelativeLayout {

    private View rootView;
    private Context context;
    private String artist;
    private String album;
    private String title;
    private ArrayList<ArrayList<String>> queue;
    private float duration;
    private SharedPreferences sp;

    @Deprecated
    public SongView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.view_song, this, false);
        addView(rootView);
        ((TextView)findViewById(R.id.songName)).setText("[NO TITLE]");
    }

    public SongView(Context context, ArrayList<ArrayList<String>> queue,
                    int positionInQueue, float duration) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.view_song, this, false);
        addView(rootView);
        this.context = context;
        this.artist = queue.get(positionInQueue).get(0);
        this.album = queue.get(positionInQueue).get(1);
        this.title = queue.get(positionInQueue).get(2);
        this.queue = queue;
        this.duration = duration;

        createView();
        setClickListener();
    }

    /**
     * Sets the duration of the song in the TextView
     */
    private void createView() {
        // Queue => [ [artist, album, song], ... ]
        ((TextView) findViewById(R.id.songName)).setText(title);
        int minutes = ((int) duration) / 60;
        int seconds = ((int) duration) % 60;
        if (seconds < 10) {
            ((TextView) findViewById(R.id.length)).setText(minutes + ":0" + seconds);
        } else {
            ((TextView) findViewById(R.id.length)).setText(minutes + ":" + seconds);
        }
    }

    /**
     * Set the click listener for the song view
     */
    private void setClickListener() {
        findViewById(R.id.container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    json.put("artist", artist);
                    json.put("album", album);
                    json.put("song", title);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Play the song
                POSTRequest playSong = new POSTRequest(json.toString(), new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        setPlayingIcon(true);
                        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                        sp.edit().putBoolean("paused", false).apply();
                        // Dont update musicControlView, its updated from push notification
                    }
                });
                playSong.execute(URL.getPlaySongUrl(context));


                // Send the queue to the server
                //TODO: Send the queue in JSON format
                String data_string = "";
                for (ArrayList<String> song : queue) {
                    data_string += song.get(0) + ":" + song.get(1) + ":" + song.get(2);
                    if (song != queue.get(queue.size() - 1)) {
                        data_string += ";";
                    }
                }
                POSTRequest setQueue = new POSTRequest(data_string);
                setQueue.execute(URL.getSetQueueUrl(context));
            }
        });
    }

    /**
     * Set the visibility of the play icon at the right of the view
     * @param playing if the song is being played
     */
    public void setPlayingIcon(boolean playing) {
        if (playing) {
            rootView.findViewById(R.id.playIcon).setAlpha(1f);
        } else {
            rootView.findViewById(R.id.playIcon).setAlpha(0.05f);
        }
    }
}
