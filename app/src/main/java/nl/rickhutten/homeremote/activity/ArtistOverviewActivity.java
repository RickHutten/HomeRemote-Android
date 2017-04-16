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

import java.util.ArrayList;

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
    private ArrayList<ArrayList<String>> queue2;
    private int offset = 0;
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
        GETJSONRequest getArtistRequest = new GETJSONRequest(new OnJSONDownloaded() {
            @Override
            public void onJSONCompleted(JSONObject jObject) {
                try {
                    JSONArray albums = jObject.getJSONArray("albums");

                    // Make queue
                    queue2 = new ArrayList<>();
                    for (int i = 0; i < albums.length(); i++) {
                        JSONObject album = albums.getJSONObject(i);
                        JSONArray songs = album.getJSONArray("songs");
                        for (int j = 0; j < songs.length(); j++) {
                            ArrayList<String> s = new ArrayList<>();
                            s.add(artistName);
                            s.add(album.getString("title"));
                            s.add(songs.getJSONObject(j).getString("title"));
                            queue2.add(s);
                        }
                    }

                    // TODO: This one takes a long time
                    for (int i = 0; i < albums.length(); i++) {
                        album = albums.getJSONObject(i);
                        // Add album in horizontal album scrollview
                        addAlbum(album);
                        // Add all songs in song list
                        addSongs(album, queue2, offset);

                        offset += album.getJSONArray("songs").length();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getArtistRequest.execute(URL.getArtistUrl(this, artistName));
    }

    public MusicControlView getMusicControlView() {
        return musicControlView;
    }

    private void addAlbum(JSONObject album) {
        AlbumCardView albumCardView = new AlbumCardView(this);
        albumCardView.set(this, musicControlView);
        albumCardView.setWidth(100);
        try {
            albumCardView.setAlbum(album.getString("title"), artistName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        albumContainer.addView(albumCardView);
    }

    private void addSongs(JSONObject album, ArrayList<ArrayList<String>> queue2, int songOffset) {

        // Add an AlbumExpandedCardView
        AlbumExpandedCardView cardView = new AlbumExpandedCardView(this);
        try {
            cardView.setAlbum(artistName, album.getString("title"));
            // Add album to songcontainer
            songContainer.addView(cardView);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        try {
            JSONArray songs = album.getJSONArray("songs");
            SongView songView;
            for (int i = 0; i < songs.length(); i++) {
                // Make song object
                JSONObject song = songs.getJSONObject(i);
                songView = new SongView(this, song.getString("artist"),  album.getString("title"),
                        song.getString("title"),
                        (float) song.getDouble("duration"));

                // Add song to cardview and activity songList
                cardView.addSong(songView);
                addToSongList(songView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
