package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import nl.rickhutten.homeremote.R;

public class QueueView extends RelativeLayout {

    private View rootView;
    private Context context;

    public QueueView(Context context) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.artist_card_layout, this, false);
        addView(rootView);

        this.context = context;
    }



}
