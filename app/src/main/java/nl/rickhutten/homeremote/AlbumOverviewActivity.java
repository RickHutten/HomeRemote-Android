package nl.rickhutten.homeremote;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

public class AlbumOverviewActivity extends AppCompatActivity {

    private ArrayList<String> songs;
    private String artistName;
    private String albumName;
    public SharedPreferences sp;
    private int ResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_overview);

        sp = getSharedPreferences("prefs", MODE_PRIVATE);
        this.artistName = getIntent().getStringExtra("artist");
        this.albumName = getIntent().getStringExtra("album");
        ((TextView)findViewById(R.id.artistText)).setText(artistName);
        ((TextView)findViewById(R.id.albumText)).setText(albumName);

        final ImageView topView = (ImageView) findViewById(R.id.topview);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                topView.setTranslationY(-scrollY / 2);
            }
        });

        Picasso.with(this).load("http://rickert.noip.me/image/" + artistName.replace(" ", "_")
                + "/" + albumName.replace(" ", "_")).into(topView);

        setAlbum();

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

        RequestTask getPlaying = new RequestTask(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                setPlayingSong(result);
            }
        });
        getPlaying.execute("http://rickert.noip.me/playing");
    }

    public void setAlbum() {
        // Download the songs
        RequestTask getSongs = new RequestTask(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                songs = new ArrayList<>(Arrays.asList(result.split(";")));
                addSongs();
            }
        });
        getSongs.execute("http://rickert.noip.me/get/" + artistName.replace(" ", "_") + "/" +
                albumName.replace(" ", "_"));
    }

    private void addSongs() {
        // Add songs to linearlayout
        LinearLayout songContainer = (LinearLayout) findViewById(R.id.songs);

        for (String song : songs) {
            System.out.println(song);
            String[] albumArtist = song.split(":");
            String title = albumArtist[0];
            int length = Integer.parseInt(albumArtist[1]);
            SongView songView = new SongView(this, artistName, albumName);
            songView.set(title, length);
            songContainer.addView(songView);
        }
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

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
