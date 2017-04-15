package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.activity.MusicActivity;

public class QueueView extends RelativeLayout {

    private View rootView;
    private Context context;

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

    public void updateText(MusicActivity activity) {
        TextView textView = (TextView) findViewById(R.id.testText);
        textView.setText(activity.getQueue().toString());
    }
}
