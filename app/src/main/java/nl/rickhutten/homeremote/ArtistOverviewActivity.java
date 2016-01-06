package nl.rickhutten.homeremote;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class ArtistOverviewActivity extends AppCompatActivity {

    private String artistName;
    private ArrayList<String> albums;
    private ArrayList<String> songs = new ArrayList<>();
    private SharedPreferences sp;
    MusicControlView musicControlView;
    private ArtistOverviewActivity artistOverviewActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_overview);
        this.artistName = getIntent().getStringExtra("artist");

        sp = getSharedPreferences("prefs", MODE_PRIVATE);

        // Add view to main activity
        musicControlView = new MusicControlView(this);
        ((RelativeLayout) findViewById(R.id.activity_artist_overview_container)).addView(musicControlView);

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
                    AlbumCardView albumCardView = new AlbumCardView(getApplicationContext());
                    albumCardView.set(artistOverviewActivity, musicControlView);
                    albumCardView.setWidth(100);
                    albumCardView.setAlbum(album + ":" + artistName);
                    albumContainer.addView(albumCardView);
                }

                //TODO: Set songs in list

            }
        });
        getArtists.execute("http://rickert.noip.me/get/" + artistName.replace(" ", "_"));
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
