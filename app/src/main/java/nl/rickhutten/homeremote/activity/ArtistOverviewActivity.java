package nl.rickhutten.homeremote.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import nl.rickhutten.homeremote.GETRequest;
import nl.rickhutten.homeremote.OnTaskCompleted;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.view.SongView;
import nl.rickhutten.homeremote.view.AlbumCardView;
import nl.rickhutten.homeremote.view.AlbumListItemView;
import nl.rickhutten.homeremote.view.MusicControlView;

public class ArtistOverviewActivity extends AppCompatActivity {

    private String artistName;
    private ArrayList<String> albums;
    private ArrayList<ArrayList<String>> queue = new ArrayList<>();
    private SharedPreferences sp;
    private ArrayList<Integer> lengthList = new ArrayList<>();
    public MusicControlView musicControlView;
    private LinearLayout songContainer;
    private ArtistOverviewActivity artistOverviewActivity = this;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_overview);
        this.artistName = getIntent().getStringExtra("artist");

        sp = getSharedPreferences("prefs", MODE_PRIVATE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Add music control view
        musicControlView = new MusicControlView(this);
        ((RelativeLayout) findViewById(R.id.activity_artist_overview_container)).addView(musicControlView);

        songContainer = (LinearLayout) findViewById(R.id.songContainer);
        final LinearLayout albumContainer = (LinearLayout) findViewById(R.id.albums);

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
        new FetchArtistImage().setTopView(this, topView).execute(artistName.replace(" ", "%20"));

        // Get albums
        GETRequest getArtists = new GETRequest(new OnTaskCompleted() {
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
            }
        });
        getArtists.execute("http://rickert.noip.me/get/" + artistName.replace(" ", "_"));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("ArtistOverviewActivity", "Push Received!");
                musicControlView.setNewSongComming(true);
                musicControlView.update();
            }
        };
    }

    private void setAlbums() {
        for (final String album : albums) {

            GETRequest getArtists = new GETRequest(new OnTaskCompleted() {
                @Override
                public void onTaskCompleted(String result) {
                    // Add songs to queue
                    ArrayList<String> songsplit = new ArrayList<>(Arrays.asList(result.split(";")));
                    for (String song : songsplit) {
                        ArrayList<String> songArray = new ArrayList<>();
                        String[] split = song.split(":");
                        songArray.add(artistName);
                        songArray.add(album);
                        songArray.add(split[0]);
                        queue.add(songArray);
                        lengthList.add(Integer.parseInt(split[1]));
                    }
                    if (album.equalsIgnoreCase(albums.get(albums.size() - 1))) {
                        setSongs();
                    }
                }
            });
            getArtists.execute("http://rickert.noip.me/get/" + artistName.replace(" ", "_")
                    + "/" + album.replace(" ", "_"));
        }
    }

    private void setSongs() {
        // Queue => [ [artist,album,song], ... ]

        String album = "";
        for (int i = 0; i < queue.size(); i++) {
            ArrayList<String> song = queue.get(i);

            if (!song.get(1).equalsIgnoreCase(album)) {
                // New album
                album = song.get(1);
                AlbumListItemView albumListItemView = new AlbumListItemView(this);
                albumListItemView.set(artistName, album);
                songContainer.addView(albumListItemView);
            }

            SongView songView = new SongView(this, artistName, album);
            songView.set(queue, i, lengthList.get(i));
            songContainer.addView(songView);
        }
        View v = new View(this);
        v.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.play_bar_height));
        songContainer.addView(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("ArtistOverviewActivity", "onResume MusicControlView ID: " + musicControlView.ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("pushReceived"));
        musicControlView.setActive(true);
        musicControlView.update();
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
                Intent intent = new Intent(this, VolumeControlActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class FetchArtistImage extends AsyncTask<String, String, String> {

        private ImageView topView;
        private Context context;

        @Override
        protected String doInBackground(String... uri) {
            String result = "";
            HttpURLConnection urlConnection = null;
            URL url;
            try {
                url = new URL("https://api.spotify.com/v1/search?type=artist&q=" + uri[0]);
                Log.v("GETRequest", url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-type", "application/json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
                reader.close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject jObject;
            try {
                jObject  = new JSONObject(result);
                JSONObject jArtists = jObject.getJSONObject("artists");
                JSONArray jItems = jArtists.getJSONArray("items");
                JSONObject jFirstItem = jItems.getJSONObject(0);
                JSONArray jArray = jFirstItem.getJSONArray("images");
                int best_height = 0;
                String best_url = "";
                for (int i = 0; i < jArray.length(); i++) {
                    try {
                        JSONObject oneObject = jArray.getJSONObject(i);
                        // Pulling items from the array
                        String url = oneObject.getString("url");
                        int height = Integer.parseInt(oneObject.getString("height"));
                        int width = Integer.parseInt(oneObject.getString("width"));

                        if (height > best_height) {
                            // If no image is above 300x300 px, the best is used
                            best_height = height;
                            best_url = url;
                        }

                        if ((height < 300 || width < 300) && i < jArray.length() - 1) {
                            // Image is small and there is another one available
                            continue;
                        }

                        // Now we have the url, download image
                        Picasso.with(context).load(best_url).centerCrop().resize(500, 500)
                                .config(Bitmap.Config.RGB_565).into(topView);
                        Log.i("FetchArtistImage", "Artist image downloaded for " + artistName);
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        public FetchArtistImage setTopView(Context context, ImageView topView) {
            this.context = context;
            this.topView = topView;
            return this;
        }
    }

}
