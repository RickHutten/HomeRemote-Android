package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import nl.rickhutten.homeremote.net.GETRequest;
import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.net.POSTRequest;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.URL;

public class SongView2 extends RelativeLayout {

    private View rootView;
    private Context context;
    private String artist;
    private String album;
    private String title;
    private ArrayList<ArrayList<String>> queue;
    private int duration;
    private SharedPreferences sp;
    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (!key.equals("song")) return;
            System.out.println(title + sharedPreferences.getString("song", ""));
            if (key.equals("song") && !sharedPreferences.getString("song", "").equals(title)) {
                rootView.findViewById(R.id.playIcon).setVisibility(GONE);
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            }
        }
    };

    @Deprecated
    public SongView2(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.song_layout, this, false);
        addView(rootView);
        ((TextView)findViewById(R.id.songName)).setText("[NO TITLE]");
    }

    public SongView2(Context context, String artist, ArrayList<ArrayList<String>> queue,
                     int positionInQueue, int duration) {

        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.song_layout, this, false);
        addView(rootView);
        this.context = context;
        this.artist = artist;
        this.album = queue.get(positionInQueue).get(1);
        this.title = queue.get(positionInQueue).get(2);
        this.queue = queue;
        this.duration = duration;

        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String artist_saved = sp.getString("artist", null);
        String album_saved = sp.getString("album", null);
        String song_saved = sp.getString("song", null);

        if (!artist.equals(artist_saved) || !album.equals(album_saved) || !title.equals(song_saved)) {
            rootView.findViewById(R.id.playIcon).setVisibility(GONE);
        }

        createView();
    }

    public void createView() {
        // Queue => [ [artist, album, song], ... ]
        ((TextView)findViewById(R.id.songName)).setText(title);
        int minutes = duration / 60;
        int seconds = duration % 60;
        if (seconds < 10) {
            ((TextView) findViewById(R.id.length)).setText(minutes + ":0" + seconds);
        } else{
            ((TextView) findViewById(R.id.length)).setText(minutes + ":" + seconds);
        }

        String data_string = "";
        for (ArrayList<String> song : queue) {
            data_string += song.get(0) + ":" + song.get(1) + ":" + song.get(2);
            if (song != queue.get(queue.size() - 1)) {
                data_string += ";";
            }
        }
        final String data = data_string;

        findViewById(R.id.container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GETRequest r = new GETRequest(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        rootView.findViewById(R.id.playIcon).setVisibility(VISIBLE);
                        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor e = sp.edit();
                        e.putInt("playpause", R.drawable.ic_pause_circle_outline_black_48dp);
                        e.commit();
                        sp.registerOnSharedPreferenceChangeListener(listener);
                        // Dont update musicControlView, its updated from push notification
                    }
                });
                r.execute(URL.getPlaySongUrl(context, artist, album, title));

                POSTRequest p = new POSTRequest(data, new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        Log.i("SongView", "POSTRequest result: " + result);
                    }
                });
                p.execute(URL.getQueueUrl(context));
            }
        });
    }
}
