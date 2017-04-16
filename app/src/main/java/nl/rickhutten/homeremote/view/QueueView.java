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

    public QueueView(Context context) {
        this(context, null);
    }

    public QueueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.view_queue, this, false);
        addView(rootView);
    }

    public void updateQueue(MusicActivity activity) {
        ArrayList<ArrayList<String>> queue = activity.getQueue();
        LinearLayout listView = (LinearLayout) findViewById(R.id.queueLinearLayout);
        listView.removeAllViews();
        SongView songView;
        for (int i = 0; i < queue.size(); i++) {
            songView = new SongView(activity, queue.get(i).get(0), queue.get(i).get(1),
                    queue.get(i).get(2), Float.parseFloat(queue.get(i).get(3)));
            listView.addView(songView);
        }
    }
}
