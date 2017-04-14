package nl.rickhutten.homeremote.activity;

import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;


/**
 * Super class of all music activities
 */

abstract public class MusicActivity extends AppCompatActivity {

    private ArrayList<ArrayList<String>> queue = null;

    public void setQueue(ArrayList<ArrayList<String>> queue) {
        this.queue = queue;
    }

    public ArrayList<ArrayList<String>> getQueue() {
        return this.queue;
    }

}
