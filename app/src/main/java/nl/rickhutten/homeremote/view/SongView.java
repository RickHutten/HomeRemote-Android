package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.net.GETRequest;
import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.net.POSTRequest;
import nl.rickhutten.homeremote.R;

public class SongView extends RelativeLayout {

    private static final String TAG = "SongView2";
    private View rootView;
    private Context context;
    private String artist;
    private String album;
    private String title;
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

    public SongView(Context context, String artist, String album) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.song_layout, this, false);
        addView(rootView);
        this.context = context;
        this.artist = artist;
        this.album = album;
    }

    public void set(final ArrayList<ArrayList<String>> queue,
                    int position, float length) {
        // Queue => [ [artist, album, song], ... ]
        ArrayList<String> songList = queue.get(position);
        title = songList.get(2);

        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final String artist_saved = sp.getString("artist", null);
        final String album_saved = sp.getString("album", null);
        final String song_saved = sp.getString("song", null);

        if (!artist.equals(artist_saved) || !album.equals(album_saved) || !title.equals(song_saved)) {
            rootView.findViewById(R.id.playIcon).setVisibility(GONE);
        }

        ((TextView)findViewById(R.id.songName)).setText(title);
        int minutes = ((int)length) / 60;
        int seconds = ((int)length) % 60;
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
                JSONObject json = new JSONObject();
                try {
                    json.put("artist", artist);
                    json.put("album", album);
                    json.put("song", title);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                POSTRequest r = new POSTRequest(json.toString(), new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        rootView.findViewById(R.id.playIcon).setVisibility(VISIBLE);
                        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                        sp.edit().putBoolean("paused", false).commit();
                        // Dont update musicControlView, its updated from push notification
                        sp.registerOnSharedPreferenceChangeListener(listener);
                    }
                });
                r.execute(URL.getPlaySongUrl(context));

//                GETRequest r = new GETRequest(new OnTaskCompleted() {
//                    @Override
//                    public void onTaskCompleted(String result) {
//                        rootView.findViewById(R.id.playIcon).setVisibility(VISIBLE);
//                        sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
//                        sp.edit().putBoolean("paused", false).commit();
//                        // Dont update musicControlView, its updated from push notification
//                        sp.registerOnSharedPreferenceChangeListener(listener);
//                    }
//                });
//                r.execute(URL.getPlaySongUrl(context));

                POSTRequest p = new POSTRequest(data, new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        Log.i("SongView", "POSTRequest result: " + result);
                    }
                });
                p.execute(URL.getSetQueueUrl(context));
            }
        });
    }


}
