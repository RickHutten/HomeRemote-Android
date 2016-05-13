package nl.rickhutten.homeremote.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import nl.rickhutten.homeremote.net.GETJSONRequest;
import nl.rickhutten.homeremote.net.OnJSONDownloaded;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.Utils;
import nl.rickhutten.homeremote.view.AlbumCardView;
import nl.rickhutten.homeremote.view.AlbumExpandedCardView;
import nl.rickhutten.homeremote.view.MusicControlView;
import nl.rickhutten.homeremote.view.SongView2;
import nl.rickhutten.homeremote.dialog.VolumeDialog;

public class ArtistOverviewActivity extends AppCompatActivity {

    private String artistName;
    public MusicControlView musicControlView;
    private LinearLayout songContainer;
    private BroadcastReceiver broadcastReceiver;
    private LinearLayout albumContainer;
    private ArrayList<ArrayList<String>> queue;
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
        musicControlView = new MusicControlView(this);
        ((RelativeLayout) findViewById(R.id.activity_artist_overview_container)).addView(musicControlView);

        songContainer = (LinearLayout) findViewById(R.id.songContainer);
        albumContainer = (LinearLayout) findViewById(R.id.albums);

        // Set artist name
        ((TextView) findViewById(R.id.artistName)).setText(artistName);

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

        // Get the artist data
        GETJSONRequest getArtistRequest = new GETJSONRequest(new OnJSONDownloaded() {
            @Override
            public void onJSONCompleted(JSONObject jObject) {
                try {
                    JSONArray albums = jObject.getJSONArray("albums");

                    // Make queue
                    queue = new ArrayList<>();
                    for (int i = 0; i < albums.length(); i++) {
                        JSONObject album = albums.getJSONObject(i);
                        JSONArray songs = album.getJSONArray("songs");
                        for (int j = 0; j < songs.length(); j++) {
                            ArrayList<String> s = new ArrayList<>();
                            s.add(artistName);
                            s.add(album.getString("title"));
                            s.add(songs.getJSONObject(j).getString("title"));
                            queue.add(s);
                        }
                    }

                    // TODO: This one takes a long time
                    for (int i = 0; i < albums.length(); i++) {
                        album = albums.getJSONObject(i);
                        // Add album in horizontal album scrollview
                        addAlbum(album);
                        // Add all songs in song list
                        addSongs(album, queue, offset);

                        offset += album.getJSONArray("songs").length();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getArtistRequest.execute(URL.getArtistUrl(this, artistName));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("ArtistOverviewActivity", "Push Received!");
                musicControlView.setNewSongComming(true);
                musicControlView.update();
            }
        };
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

    private void addSongs(JSONObject album, ArrayList<ArrayList<String>> queue, int songOffset) {

        // Add an AlbumExpandedCardView
        AlbumExpandedCardView cardView = new AlbumExpandedCardView(this);
        try {
            cardView.setAlbum(artistName, album.getString("title"));
            // Add album to songcontainer
            songContainer.addView(cardView);
        } catch (JSONException e){
            e.printStackTrace();
            return;
        }
        try {
            JSONArray songs = album.getJSONArray("songs");
            for (int i = 0; i < songs.length(); i++) {
                // Make song object
                JSONObject song = songs.getJSONObject(i);
                float duration = Float.parseFloat(song.getString("duration"));
                SongView2 songView = new SongView2(this, artistName, queue, songOffset + i, duration);

                // Add song to cardview
                cardView.addSong(songView);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("ArtistOverviewActivity", "onResume MusicControlView ID: " + musicControlView.ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("pushReceived"));
        musicControlView.setActive(true);
        musicControlView.updateHard();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        musicControlView.setActive(false);
        Log.i("ArtistOverviewActivity", "onPause MusicControlView ID: " + musicControlView.ID);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_volume:
                // Show volume dialog
                new VolumeDialog(this, R.style.ThemeDialog).show();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_shutdown:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.ask_shutdown)
                        .setPositiveButton("Yes", Utils.getDialogClickListener(this))
                        .setNegativeButton("No", Utils.getDialogClickListener(this)).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
