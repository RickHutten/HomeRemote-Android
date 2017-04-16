package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.activity.MusicActivity;

public class QueueView extends RelativeLayout {

    private View rootView;
    private Context context;
    private ArrayList<ArrayList<String>> queue;

    public QueueView(Context context) {
        this(context, null);
    }

    public QueueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.view_queue, this, false);
        addView(rootView);

        this.context = context;
    }

    public void updateQueue(MusicActivity activity) {
        queue = activity.getQueue();
        LinearLayout listView = (LinearLayout) findViewById(R.id.queueLinearLayout);
        listView.removeAllViews();
        SongView songView;
        for (int i = 0; i < queue.size(); i++) {
            songView = new SongView(context, queue, i, Float.parseFloat(queue.get(i).get(3)));
            listView.addView(songView);
        }
    }
}
