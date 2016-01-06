package nl.rickhutten.homeremote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AlbumCardView extends RelativeLayout {

    private View rootView;
    private Context context;
    private Activity activity;
    private MusicControlView musicControlView;

    public AlbumCardView(Context context) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.album_card_layout, this, false);
        addView(rootView);

        this.context = context;
    }

    public void set(final Activity activity, MusicControlView musicControlView) {
        this.activity = activity;
        this.musicControlView = musicControlView;
    }

    public void setAlbum(String albArt) {
        String[] albumArtist = albArt.split(":");
        final String album = albumArtist[0];
        final String artist = albumArtist[1];

        ((TextView) rootView.findViewById(R.id.albumText)).setText(album);
        ((TextView) rootView.findViewById(R.id.artistText)).setText(artist);

        final String artistFormat = artist.replace(" ", "_");
        final String albumFormat = album.replace(" ", "_");
        final ImageView albumImage = (ImageView) findViewById(R.id.albumImage);

        // Download image and load into imageview
        Picasso.with(context).load("http://rickert.noip.me/image/" + artistFormat + "/" + albumFormat)
                .config(Bitmap.Config.RGB_565).into(albumImage);

        rootView.findViewById(R.id.cardView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AlbumOverviewActivity.class);
                intent.putExtra("artist", artist);
                intent.putExtra("album", album);

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(activity,
                                Pair.create((View) musicControlView, "musicControlView"),
                                Pair.create((View) albumImage, "albumImage"));

                activity.startActivity(intent, options.toBundle());
            }
        });
    }

    public void setWidth(int dp) {
        View cardView = rootView.findViewById(R.id.cardView);
        View imageView = rootView.findViewById(R.id.albumImage);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
        lp.width = dpToPx(dp);
        lp.height = dpToPx(((Double) (dp * 13 / 9.0)).intValue());
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
