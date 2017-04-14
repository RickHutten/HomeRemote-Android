package nl.rickhutten.homeremote.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.net.GETJSONRequest;
import nl.rickhutten.homeremote.net.OnJSONDownloaded;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.Utils;
import nl.rickhutten.homeremote.view.MusicControlView;
import nl.rickhutten.homeremote.dialog.VolumeDialog;
import nl.rickhutten.homeremote.view.SongView;

public class AlbumOverviewActivity extends MusicActivity {

    private String albumArtist;
    private String albumName;
    public SharedPreferences sp;
    public MusicControlView musicControlView;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_overview);

        sp = getSharedPreferences("prefs", MODE_PRIVATE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Add view to main activity
        musicControlView = new MusicControlView(this);
        ((RelativeLayout) findViewById(R.id.activity_album_overview_container)).addView(musicControlView);

        this.albumArtist = getIntent().getStringExtra("artist");
        this.albumName = getIntent().getStringExtra("album");
        ((TextView)findViewById(R.id.artistText)).setText(albumArtist);
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

        // Download image and load into imageview
        Picasso.with(this).load(URL.getAlbumImageUrl(this, albumArtist, albumName))
                .config(Bitmap.Config.RGB_565).into(topView);

        setAlbum();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Log.i("AlbumOverviewActivity", "Push Received!");
                musicControlView.setNewSongComing(true);
                musicControlView.update();
            }
        };
    }

    public void setAlbum() {
        // Download the songs
        GETJSONRequest getSongs = new GETJSONRequest(new OnJSONDownloaded() {
            @Override
            public void onJSONCompleted(JSONObject jObject) {
                try {
                    addSongs(jObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getSongs.execute(URL.getAlbumUrl(this, albumArtist, albumName));
    }

    private void addSongs(JSONObject album) throws JSONException{
        // Create playlist
        ArrayList<ArrayList<String>> playlist = new ArrayList<>();
        LinearLayout songContainer = (LinearLayout) findViewById(R.id.song_container);

        JSONArray songs = album.getJSONArray("songs");

        for (int i = 0; i < songs.length(); i++) {
            JSONObject song = (JSONObject)songs.get(i);

            ArrayList<String> songList = new ArrayList<>();
            songList.add(song.getString("artist"));
            songList.add(albumName);
            songList.add(song.getString("title"));
            playlist.add(songList);
        }

        for (int i = 0; i < songs.length(); i++) {
            JSONObject song = (JSONObject) songs.get(i);
            // Add songs to linearlayout
            SongView songView = new SongView(this, playlist, i, (float) song.getDouble("duration"));
            songContainer.addView(songView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i("AlbumOverviewActivity", "onResume MusicControlView ID: " + musicControlView.ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("pushReceived"));
        musicControlView.setActive(true);
        musicControlView.updateHard();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
//        Log.i("AlbumOverviewActivity", "onPause MusicControlView ID: " + musicControlView.ID);
        musicControlView.setActive(false);
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
