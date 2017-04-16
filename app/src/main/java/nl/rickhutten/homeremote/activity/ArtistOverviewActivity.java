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
import nl.rickhutten.homeremote.view.AlbumCardView;
import nl.rickhutten.homeremote.view.AlbumExpandedCardView;
import nl.rickhutten.homeremote.view.MusicControlView;
import nl.rickhutten.homeremote.view.SongView;

public class ArtistOverviewActivity extends MusicActivity {

    private String artistName;
    private LinearLayout songContainer;
    private LinearLayout albumContainer;
    private JSONObject album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_overview);
        this.artistName = getIntent().getStringExtra("artist");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Add music control view
        ((RelativeLayout) findViewById(R.id.activity_artist_overview_container)).addView(musicControlView);

        songContainer = (LinearLayout) findViewById(R.id.songContainer);
        albumContainer = (LinearLayout) findViewById(R.id.albums);

        // Set artist name
        ((TextView) findViewById(R.id.artistName)).setText(artistName);

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

        // Fetch image of artist
        Picasso.with(this).load(URL.getArtistImageUrl(this, artistName)).centerCrop().resize(500, 500)
                .config(Bitmap.Config.RGB_565).into(topView);

        getArtistData();
    }

    private void getArtistData() {
        // Get the artist data
        new GETJSONRequest(new OnJSONDownloaded() {
            @Override
            public void onJSONCompleted(JSONObject jObject) {
                try {
                    JSONArray albums = jObject.getJSONArray("albums");

                    // TODO: Find out what's taking a long time
                    for (int i = 0; i < albums.length(); i++) {
                        album = albums.getJSONObject(i);
                        // Add album in horizontal album scrollview
                        addAlbum(album);
                        // Add all songs in song list
                        addSongs(album);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute(URL.getArtistUrl(this, artistName));
    }

    public MusicControlView getMusicControlView() {
        return musicControlView;
    }

    private void addAlbum(JSONObject album) throws JSONException {
        AlbumCardView albumCardView = new AlbumCardView(this);
        albumCardView.set(this);
        albumCardView.setWidth(100);
        albumCardView.setAlbum(album.getString("title"), artistName);
        albumContainer.addView(albumCardView);
    }

    private void addSongs(JSONObject album) throws JSONException {
        // Add an AlbumExpandedCardView
        AlbumExpandedCardView cardView = new AlbumExpandedCardView(this);
        cardView.setAlbum(artistName, album.getString("title"));
        // Add album to songcontainer
        songContainer.addView(cardView);

        // Add songs to CardView
        JSONArray songs = album.getJSONArray("songs");
        SongView songView;
        for (int i = 0; i < songs.length(); i++) {
            // Make song object
            JSONObject song = songs.getJSONObject(i);
            songView = new SongView(this, song.getString("artist"), album.getString("title"),
                    song.getString("title"),
                    (float) song.getDouble("duration"));

            // Add song to CardView and activity songList
            cardView.addSong(songView);
            addToSongList(songView);
        }
    }
}
