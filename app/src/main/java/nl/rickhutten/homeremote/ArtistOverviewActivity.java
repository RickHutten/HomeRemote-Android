package nl.rickhutten.homeremote;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class ArtistOverviewActivity extends AppCompatActivity {

    private int ResId;
    private String artistName;
    private ArrayList<String> albums;
    private ArrayList<String> songs = new ArrayList<>();
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_overview);
        this.artistName = getIntent().getStringExtra("artist");

        sp = getSharedPreferences("prefs", MODE_PRIVATE);

        final LinearLayout albumContainer = (LinearLayout) findViewById(R.id.albums);
        final ArtistOverviewActivity activity = this;
        // Set artist name
                ((TextView) findViewById(R.id.artistName)).setText(artistName);

        // Get albums
        RequestTask getArtists = new RequestTask(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                // Restult: "album1;album2;album3;..."
                albums = new ArrayList<>(Arrays.asList(result.split(";")));
                setAlbums();

                // Set albums in list
                for (String album : albums) {
                    AlbumCardView albumCardView = new AlbumCardView(getApplicationContext(), activity);
                    albumCardView.setWidth(100);
                    albumCardView.setAlbum(album + ":" + artistName);
                    albumContainer.addView(albumCardView);
                }

                // Set songs in list

            }
        });
        getArtists.execute("http://rickert.noip.me/get/" + artistName.replace(" ", "_"));

        findViewById(R.id.playPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {
                    // Resume playing
                    RequestTask resume = new RequestTask(new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            setPlayPause(R.drawable.ic_pause_circle_outline_white_48dp);
                        }
                    });
                    resume.execute("http://rickert.noip.me/resume");
                } else {
                    // Pause playing
                    RequestTask pause = new RequestTask(new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            setPlayPause(R.drawable.ic_play_circle_outline_white_48dp);
                        }
                    });
                    pause.execute("http://rickert.noip.me/pause");
                }
            }
        });
    }

    private void setAlbums() {
        for (String album : albums) {

            RequestTask getArtists = new RequestTask(new OnTaskCompleted() {
                @Override
                public void onTaskCompleted(String result) {
                    // Result add songs to list
                    songs.addAll(new ArrayList<>(Arrays.asList(result.split(";"))));
                }
            });
            getArtists.execute("http://rickert.noip.me/get/" + artistName.replace(" ", "_")
                    + "/" + album.replace(" ", "_"));
        }

        setSongs();
    }

    private void setSongs() {

    }

    public void setPlayingSong(String text) {
        int savedResId = sp.getInt("playpause", 0);
        if (ResId != savedResId && savedResId != 0) {
            setPlayPause(savedResId);
        }
        ((TextView)findViewById(R.id.playingText)).setText(text);
    }

    public void setPlayPause(int id) {
        ResId = id;
        sp.edit().putInt("playpause", ResId).apply();
        ((ImageView)findViewById(R.id.playPause)).setImageResource(id);
    }

    public boolean isPlaying() {
        return sp.getInt("playpause", 0) != R.drawable.ic_play_circle_outline_white_48dp;
    }

    @Override
    protected void onResume() {
        super.onResume();
        RequestTask getPlaying = new RequestTask(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                setPlayingSong(result);
            }
        });
        getPlaying.execute("http://rickert.noip.me/playing");
    }
}
