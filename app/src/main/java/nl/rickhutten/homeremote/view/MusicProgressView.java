package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class MusicProgressView extends AppCompatSeekBar {

    private MusicProgressView view = this;
    private MusicControlView musicControlView;
    public int ID;
    private float seconds;
    private float duration;

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

    /**
     * Creates thread that sets the ProgressView to the current progress of the playing song
     * @param sp the SharedPreferences that has the values 'duration' and 'progress'
     */
    public void startCountdown(final SharedPreferences sp) {
        duration = sp.getFloat("duration", 180);
        seconds = sp.getFloat("progress", 0f);

        final Thread thread = new Thread() {
            @Override
            public void run() {
                if (!musicControlView.paused) {
                    seconds += 0.5;
                }
                musicControlView.setNewSongComing(false);
//                Log.i("MusicProgressView ID: " + ID, "Start counting; duration, offset: " + duration + ", " + seconds);
                try {
                    while (seconds <= duration) {
                        if (!musicControlView.isViewActive()) {
//                            Log.i("MusicProgressView ID: " + ID, "Not active, stop counting");
                            break;
                        }
                        if (musicControlView.isNewSongComing()) {
                            musicControlView.setNewSongComing(false);
//                            Log.i("MusicProgressView ID: " + ID, "New song comming, stop counting");
                            break;
                        }
                        float progress = (seconds / duration) * 10000;
                        view.setProgress((int) progress);
                        sleep(200);
                        if (!musicControlView.paused) {
                            // Only increment time if the music is not paused
                            seconds += 0.2;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        // Start thread after 500 millis
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                thread.start();
            }
        }, 500);
    }

    public void setMusicControlView(MusicControlView v) {
        musicControlView = v;
        this.ID = v.ID;
    }

    public float getSeconds() {
        return this.seconds;
    }
}
