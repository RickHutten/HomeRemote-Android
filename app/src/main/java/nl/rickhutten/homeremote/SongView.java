package nl.rickhutten.homeremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SongView extends RelativeLayout {

    private View rootView;
    private Context context;
    private String artist;
    private String album;

    public SongView(Context context, String artist, String album) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.song_layout, this, false);
        addView(rootView);
        this.context = context;
        this.artist = artist;
        this.album = album;
    }

    public void set(final MusicControlView musicControlView, final ArrayList<ArrayList<String>> queue,
                    int position, int length) {
        // Queue => [ [artist, album, song], ... ]
        ArrayList<String> songList = queue.get(position);
        final String title = songList.get(2);
        ((TextView)findViewById(R.id.songName)).setText(title);
        int minutes = length / 60;
        int seconds = length % 60;
        if (seconds < 10) {
            ((TextView) findViewById(R.id.length)).setText(minutes + ":0" + seconds);
        } else{
            ((TextView) findViewById(R.id.length)).setText(minutes + ":" + seconds);
        }

        findViewById(R.id.container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GETRequest r = new GETRequest(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        SharedPreferences sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor e = sp.edit();
                        e.putInt("playpause", R.drawable.ic_pause_circle_outline_white_48dp);
                        e.putString("artist", artist);
                        e.putString("album", album);
                        e.putString("song", title);
                        e.commit();

                        musicControlView.update();
                    }
                });
                r.execute("http://rickert.noip.me/play/" + artist.replace(" ", "_") + "/" +
                        album.replace(" ", "_") + "/" + title.replace(" ", "_"));

                POSTRequest p = new POSTRequest(queue, new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        Log.i("SongView", "POSTRequest result: " + result);
                    }
                });
                p.execute("http://rickert.noip.me/set/queue");
            }
        });
    }
}
