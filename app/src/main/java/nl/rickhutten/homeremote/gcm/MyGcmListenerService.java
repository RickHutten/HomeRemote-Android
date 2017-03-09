package nl.rickhutten.homeremote.gcm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;


public class MyGcmListenerService extends GcmListenerService {
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String artist = data.getString("artist");
        String album = data.getString("album");
        String song = data.getString("song");
        float duration = Float.parseFloat(data.getString("duration"));

//        Log.v("GcmListenerService", "Artist: " + artist + " Album: " + album + " Song: " + song);

        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("artist", artist);
        editor.putString("album", album);
        editor.putString("song", song);
        editor.putFloat("duration", duration);
        editor.apply();

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent pushReceived = new Intent("pushReceived");
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushReceived);
    }
}