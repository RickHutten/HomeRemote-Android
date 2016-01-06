package nl.rickhutten.homeremote;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    MusicControlView musicControlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_overview);

        sp = getSharedPreferences("prefs", MODE_PRIVATE);

        // Add view to main activity
        musicControlView = new MusicControlView(this);
        ((RelativeLayout) findViewById(R.id.activity_album_overview_container)).addView(musicControlView);

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
                + "/" + albumName.replace(" ", "_")).config(Bitmap.Config.RGB_565).into(topView);

        setAlbum();
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
            Log.v("AlbumOverViewActivity", song);
            String[] albumArtist = song.split(":");
            String title = albumArtist[0];
            int length = Integer.parseInt(albumArtist[1]);
            SongView songView = new SongView(this, artistName, albumName);
            songView.set(musicControlView, title, length);
            songContainer.addView(songView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        musicControlView.update();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }
}
