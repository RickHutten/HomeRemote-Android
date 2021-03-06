package nl.rickhutten.homeremote.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.net.GETJSONRequest;
import nl.rickhutten.homeremote.net.OnJSONDownloaded;
import nl.rickhutten.homeremote.view.SongView;

public class AlbumOverviewActivity extends MusicActivity {

    private String albumArtist;
    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_overview);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Add view to main activity
        ((RelativeLayout) findViewById(R.id.activity_album_overview_container)).addView(musicControlView);

        this.albumArtist = getIntent().getStringExtra("artist");
        this.albumName = getIntent().getStringExtra("album");
        ((TextView) findViewById(R.id.artistText)).setText(albumArtist);
        ((TextView) findViewById(R.id.albumText)).setText(albumName);

        // Set scrolling behaviour
        final ImageView topView = (ImageView) findViewById(R.id.topview);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                topView.setTranslationY(-scrollY / 2);
            }
        });

        // Download image and load into imageview
        Picasso.with(this).load(URL.getAlbumImageUrl(this, albumArtist, albumName))
                .config(Bitmap.Config.RGB_565).into(topView);

        setAlbum();
    }

    public void setAlbum() {
        // Download the songs
        new GETJSONRequest(new OnJSONDownloaded() {
            @Override
            public void onJSONCompleted(JSONObject jObject) {
                try {
                    addSongs(jObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute(URL.getAlbumUrl(this, albumArtist, albumName));
    }

    private void addSongs(JSONObject album) throws JSONException {
        LinearLayout songContainer = (LinearLayout) findViewById(R.id.song_container);
        JSONArray songs = album.getJSONArray("songs");

        for (int i = 0; i < songs.length(); i++) {
            JSONObject song = (JSONObject) songs.get(i);

            SongView songView = new SongView(this, song.getString("artist"), albumName,
                    song.getString("title"),
                    (float) song.getDouble("duration"));
            songContainer.addView(songView);
            addToSongList(songView);
        }
    }
}
