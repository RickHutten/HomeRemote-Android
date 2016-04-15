package nl.rickhutten.homeremote.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;

import nl.rickhutten.homeremote.GETRequest;
import nl.rickhutten.homeremote.OnTaskCompleted;
import nl.rickhutten.homeremote.R;

public class VolumeControlActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_control);

        final SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        final int saved_progress = sp.getInt("volume", 50);

        SeekBar seekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        seekBar.setProgress(saved_progress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public boolean scrollAllowed = true; // Whether the user may scroll
            public Integer progress = saved_progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (scrollAllowed) {
                    this.progress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!scrollAllowed) {seekBar.setProgress(progress); return;}

                GETRequest changeVolume = new GETRequest(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        scrollAllowed = true;
                    }
                });
                scrollAllowed = false;
                changeVolume.execute("http://rickert.noip.me/set/volume/" + progress.toString());

                sp.edit().putInt("volume", progress).apply();
            }
        });
    }
}
