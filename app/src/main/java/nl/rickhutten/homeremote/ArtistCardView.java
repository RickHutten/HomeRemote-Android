package nl.rickhutten.homeremote;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ArtistCardView extends CardView{

    private View rootView;
    private Context context;
    private MainActivity mainActivity;
    private String artist;

    public ArtistCardView(Context context, final MainActivity mainActivity) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.artist_card_layout, this, false);
        addView(rootView);

        this.context = context;
        this.mainActivity = mainActivity;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
        final String artistFormat = artist.replace(" ", "_");
        ((TextView) rootView.findViewById(R.id.artistText)).setText(artist);
        rootView.findViewById(R.id.shuffleButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestTask playArtist = new RequestTask(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        mainActivity.setPlayPause(R.drawable.ic_pause_circle_outline_white_48dp);
                    }
                });
                playArtist.execute("http://rickert.noip.me/play/" + artistFormat);

                RequestTask getPlaying = new RequestTask(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        mainActivity.setPlayingSong(result);
                    }
                });
                getPlaying.execute("http://rickert.noip.me/playing");
            }
        });

        rootView.findViewById(R.id.artistContainer).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked on " + artist);
                Intent intent = new Intent(mainActivity, ArtistOverviewActivity.class);
                intent.putExtra("artist", artist);
                mainActivity.startActivity(intent);
            }
        });
    }
}
