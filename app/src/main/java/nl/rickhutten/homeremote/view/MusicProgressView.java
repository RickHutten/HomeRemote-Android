package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;


public class MusicProgressView extends SeekBar {

    private MusicProgressView view = this;
    private MusicControlView musicControlView;
    public int ID;
    private float seconds;
    private float offset;
    private int duration;

    public MusicProgressView(Context context) {
        super(context);
    }

    public MusicProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void startCountdown(final SharedPreferences sp) {
        duration = sp.getInt("duration", 180);
        offset = sp.getFloat("progress", 0f);
        Log.i("MusicProgressView ID: " + ID, "Start counting; duration, offset: " + duration + ", " + offset);

        Thread thread = new Thread() {
            @Override
            public void run() {
                seconds = offset;
                try {
                    while (seconds <= duration) {
                        if (!musicControlView.isViewActive()) {
                            Log.i("MusicProgressView ID: " + ID, "Not active, stop counting");
                            break;
                        }
                        if (musicControlView.isNewSongComming()) {
                            musicControlView.setNewSongComming(false);
                            Log.i("MusicProgressView ID: " + ID, "New song comming, stop counting");
                            break;
                        }
                        float progress = (seconds / duration) * 10000;
                        view.setProgress((int) progress);
                        sleep(200);
                        if (!sp.getBoolean("paused", true)) {
                            // Only increment time if the music is not paused
                            seconds += 0.2;
                        }
                    }
                    if (seconds > duration) {
                        Log.i("MusicProgressView ID: " + ID, "Counted all the way; s, d:" + seconds + " " + duration);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    public void setMusicControlView(MusicControlView v) {
        musicControlView = v;
        this.ID = v.ID;
    }

    public float getSeconds() {
        return this.seconds;
    }
}
