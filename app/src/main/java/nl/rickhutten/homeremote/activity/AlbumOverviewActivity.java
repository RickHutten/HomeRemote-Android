package nl.rickhutten.homeremote.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
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

import java.util.ArrayList;
import java.util.Arrays;

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.net.GETRequest;
import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.Utils;
import nl.rickhutten.homeremote.view.SongView;
import nl.rickhutten.homeremote.view.MusicControlView;
import nl.rickhutten.homeremote.dialog.VolumeDialog;

public class AlbumOverviewActivity extends AppCompatActivity {

    private ArrayList<String> songs;
    private String artistName;
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

        // Download image and load into imageview
        Picasso.with(this).load(URL.getAlbumImageUrl(this, artistName, albumName))
                .config(Bitmap.Config.RGB_565).into(topView);

        setAlbum();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("AlbumOverviewActivity", "Push Received!");
                musicControlView.setNewSongComming(true);
                musicControlView.update();
            }
        };
    }

    public void setAlbum() {
        // Download the songs
        GETRequest getSongs = new GETRequest(new OnTaskCompleted() {
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
        // Create playlist
        ArrayList<ArrayList<String>> playlist = new ArrayList<>();
        for (String song : songs) {
            ArrayList<String> songList = new ArrayList<>();
            songList.add(artistName);
            songList.add(albumName);
            songList.add(song.split(":")[0]);

            playlist.add(songList);
        }

        // Add songs to linearlayout
        LinearLayout songContainer = (LinearLayout) findViewById(R.id.songs);

        for (int i = 0; i < songs.size(); i++) {
            String song = songs.get(i);
            System.out.println(song);
            String[] albumArtist = song.split(":");
            float length = Float.parseFloat(albumArtist[1]);

            SongView songView = new SongView(this, artistName, albumName);
            songView.set(playlist, i, length);
            songContainer.addView(songView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("AlbumOverviewActivity", "onResume MusicControlView ID: " + musicControlView.ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("pushReceived"));
        musicControlView.setActive(true);
        musicControlView.updateHard();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        Log.i("AlbumOverviewActivity", "onPause MusicControlView ID: " + musicControlView.ID);
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
