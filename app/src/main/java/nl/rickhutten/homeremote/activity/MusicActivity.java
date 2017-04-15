package nl.rickhutten.homeremote.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import nl.rickhutten.homeremote.view.MusicControlView;


/**
 * Super class of all music activities
 */

abstract public class MusicActivity extends AppCompatActivity {

    public MusicControlView musicControlView;
    public BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        musicControlView = new MusicControlView(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                musicControlView.setNewSongComing(true);
                musicControlView.updateHard();
            }
        };
    }

    private ArrayList<ArrayList<String>> queue = null;  // [[artist, album, song], ...]

    public void setQueue(ArrayList<ArrayList<String>> queue) {
        this.queue = queue;
    }

    public ArrayList<ArrayList<String>> getQueue() {
        return this.queue;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("pushReceived"));
        musicControlView.setActive(true);
        musicControlView.updateHard();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        musicControlView.setActive(false);
        super.onPause();
    }
}
