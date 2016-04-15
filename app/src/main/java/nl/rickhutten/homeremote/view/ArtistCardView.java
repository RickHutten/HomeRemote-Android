package nl.rickhutten.homeremote.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import nl.rickhutten.homeremote.activity.ArtistOverviewActivity;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.Utils;

public class ArtistCardView extends RelativeLayout {

    private View rootView;
    private Context context;
    private LinearLayout card;
    private TextView firstLetter;
    private MusicControlView musicControlView;
    private Activity activity;

    public ArtistCardView(Context context) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.artist_card_layout, this, false);
        addView(rootView);

        firstLetter = (TextView) rootView.findViewById(R.id.firstLetter);
        card = (LinearLayout) rootView.findViewById(R.id.card);

        this.context = context;
    }

    public void set(final Activity activity, MusicControlView musicControlView) {
        this.activity = activity;
        this.musicControlView = musicControlView;
    }

    public Character getFirstLetter() {
        return firstLetter.getText().charAt(0);
    }

    public void setFirstLetter(Character letter) {
        firstLetter.setText(letter.toString());
    }

    public void addArtist(final String artist) {
        final TextView artistTextView = new TextView(context);
        artistTextView.setText(artist);
        final int id = generateViewId();
        artistTextView.setId(id);
        int dp = Utils.dpToPx(context, 6);
        artistTextView.setPadding(dp, dp, dp, dp);
        artistTextView.setWidth(card.getWidth());
        artistTextView.setBackgroundResource(R.drawable.ripple);
        artistTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        artistTextView.setSingleLine(true);

        artistTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("ArtistCardView", "Clicked on " + artist);
                Intent intent = new Intent(context, ArtistOverviewActivity.class);
                intent.putExtra("artist", artist);

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(activity, musicControlView, "musicControlView");

                context.startActivity(intent, options.toBundle());
            }
        });

        card.addView(artistTextView);
    }
}
