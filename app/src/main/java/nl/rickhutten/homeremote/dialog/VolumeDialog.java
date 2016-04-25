package nl.rickhutten.homeremote.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.net.GETRequest;
import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.R;

public class VolumeDialog extends Dialog {

    private Context context;


    public VolumeDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_volume);

        final SharedPreferences sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final int saved_progress = sp.getInt("volume", 50);

        final ImageView imageView = (ImageView) findViewById(R.id.volumeImage);
        setVolumeImage(imageView, saved_progress);

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
                setVolumeImage(imageView, progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!scrollAllowed) {
                    seekBar.setProgress(progress);
                    return;
                }

                GETRequest changeVolume = new GETRequest(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        scrollAllowed = true;
                    }
                });
                scrollAllowed = false;
                changeVolume.execute(URL.getVolumeUrl(context, progress));

                sp.edit().putInt("volume", progress).apply();
            }
        });
    }

    private void setVolumeImage(ImageView imageView, int saved_progress) {
        if (saved_progress == 0) {
            imageView.setImageResource(R.drawable.ic_volume_off_black_48dp);
        } else if (saved_progress > 0 && saved_progress <= 33 ) {
            imageView.setImageResource(R.drawable.ic_volume_mute_black_48dp);
        } else if (saved_progress > 33 && saved_progress <= 67) {
            imageView.setImageResource(R.drawable.ic_volume_down_black_48dp);
        } else if (saved_progress > 67) {
            imageView.setImageResource(R.drawable.ic_volume_up_black_48dp);
        }
    }
}
