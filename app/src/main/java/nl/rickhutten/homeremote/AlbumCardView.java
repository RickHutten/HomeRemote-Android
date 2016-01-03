package nl.rickhutten.homeremote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AlbumCardView extends RelativeLayout{

    private View rootView;
    private Context context;
    private Activity activity;

    public AlbumCardView(Context context, final Activity activity) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.album_card_layout, this, false);
        addView(rootView);

        this.context = context;
        this.activity = activity;
        activity.findViewById(R.id.playPause).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {
                    // Resume playing
                    RequestTask resume = new RequestTask(new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            setPlayPause(R.drawable.ic_pause_circle_outline_white_48dp);
                        }
                    });
                    resume.execute("http://rickert.noip.me/resume");
                } else {
                    // Pause playing
                    RequestTask pause = new RequestTask(new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            setPlayPause(R.drawable.ic_play_circle_outline_white_48dp);
                        }
                    });
                    pause.execute("http://rickert.noip.me/pause");
                }
            }
        });
    }

    private boolean isPlaying() {
        try {
            return ((MainActivity) activity).isPlaying();
        } catch (ClassCastException e) {
            return ((ArtistOverviewActivity) activity).isPlaying();
        }
    }

    private void setPlayPause(int resId) {
        
        try {
            ((MainActivity) activity).setPlayPause(resId);
        } catch (ClassCastException e) {
            ((ArtistOverviewActivity) activity).setPlayPause(resId);
        }
    }

    private void setPlayingSong(String s) {
        try {
            ((MainActivity) activity).setPlayingSong(s);
        } catch (ClassCastException e) {
            ((ArtistOverviewActivity) activity).setPlayingSong(s);
        }
    }

    public void setAlbum(String albArt) {
        String[] albumArtist = albArt.split(":");
        final String album = albumArtist[0];
        final String artist = albumArtist[1];
        ((TextView) rootView.findViewById(R.id.albumText)).setText(album);
        ((TextView) rootView.findViewById(R.id.artistText)).setText(artist);
        final String artistFormat = artist.replace(" ", "_");
        final String albumFormat = album.replace(" ", "_");
        ImageView albumImage = (ImageView) findViewById(R.id.albumImage);
        System.out.println("http://rickert.noip.me/image/" + artistFormat + "/" + albumFormat);

        // Download image and load into imageview
        Picasso.with(context).load("http://rickert.noip.me/image/" + artistFormat + "/" + albumFormat).into(albumImage);

        rootView.findViewById(R.id.cardView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(getContext());
                Intent intent = new Intent(activity, AlbumOverviewActivity.class);
                intent.putExtra("artist", artist);
                intent.putExtra("album", album);
                activity.startActivity(intent);
            }
        });
    }

    public void setWidth(int dp) {
        View cardView = rootView.findViewById(R.id.cardView);
        View imageView = rootView.findViewById(R.id.albumImage);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
        lp.width = dpToPx(dp);
        lp.height = dpToPx(((Double)(dp * 13/9.0)).intValue());
        cardView.setLayoutParams(lp);

        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        lp2.width = dpToPx(dp - 16);
        lp2.height = dpToPx(dp - 16);
        imageView.setLayoutParams(lp2);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
