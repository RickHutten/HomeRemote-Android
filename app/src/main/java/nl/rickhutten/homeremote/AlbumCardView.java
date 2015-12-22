package nl.rickhutten.homeremote;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AlbumCardView extends CardView{

    private View rootView;
    private Context context;
    private MainActivity mainActivity;

    public AlbumCardView(Context context, final MainActivity mainActivity) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.album_card_layout, this, false);
        addView(rootView);

        this.context = context;
        this.mainActivity = mainActivity;
        mainActivity.findViewById(R.id.playPause).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mainActivity.isPlaying()) {
                    // Resume playing
                    RequestTask resume = new RequestTask(new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            mainActivity.setPlayPause(R.drawable.ic_pause_circle_outline_white_48dp);
                        }
                    });
                    resume.execute("http://rickert.noip.me/resume");
                } else {
                    // Pause playing
                    RequestTask pause = new RequestTask(new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            mainActivity.setPlayPause(R.drawable.ic_play_circle_outline_white_48dp);
                        }
                    });
                    pause.execute("http://rickert.noip.me/pause");
                }
            }
        });
    }

    public void setAlbum(String albArt) {
        String[] albumArtist = albArt.split(":");
        String album = albumArtist[0];
        String artist = albumArtist[1];
        ((TextView) rootView.findViewById(R.id.albumText)).setText(album);
        ((TextView) rootView.findViewById(R.id.artistText)).setText(artist);
        final String artistFormat = artist.replace(" ", "_");
        final String albumFormat = album.replace(" ", "_");
        ImageView albumImage = (ImageView) findViewById(R.id.albumImage);
        System.out.println("http://rickert.noip.me/image/" + artistFormat + "/" + albumFormat);
        Picasso.with(context).load("http://rickert.noip.me/image/" + artistFormat + "/" + albumFormat).into(albumImage);

        rootView.findViewById(R.id.container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Button clicked");
                RequestTask playAlbum = new RequestTask(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        mainActivity.setPlayPause(R.drawable.ic_pause_circle_outline_white_48dp);
                    }
                });
                playAlbum.execute("http://rickert.noip.me/play/" + artistFormat + "/" + albumFormat);

                RequestTask getPlaying = new RequestTask(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        mainActivity.setPlayingSong(result);
                    }
                });
                getPlaying.execute("http://rickert.noip.me/playing");
            }
        });
    }
}
