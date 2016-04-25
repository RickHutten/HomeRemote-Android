package nl.rickhutten.homeremote.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.activity.AlbumOverviewActivity;
import nl.rickhutten.homeremote.activity.ArtistOverviewActivity;

public class AlbumExpandedCardView extends RelativeLayout {

    private View rootView;
    private Context context;
    private LinearLayout card;

    public AlbumExpandedCardView(Context context) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.album_expanded_card_layout, this, false);
        addView(rootView);
        card = (LinearLayout) rootView.findViewById(R.id.card);

        this.context = context;
    }

    public void setAlbum(final String artist, final String album) {
        ((TextView)findViewById(R.id.albumTitle)).setText(album);

        // Download image and load into imageview
        Picasso.with(context).load(URL.getAlbumImageUrl(context, artist, album))
                .config(Bitmap.Config.RGB_565)
                .resizeDimen(
                        R.dimen.album_card_view_image_width,
                        R.dimen.album_card_view_image_height)
                .into((ImageView) findViewById(R.id.albumImage));

        final ImageView albumImage = (ImageView)rootView.findViewById(R.id.albumImage);
        albumImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AlbumOverviewActivity.class);
                intent.putExtra("artist", artist);
                intent.putExtra("album", album);

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) context,
                                Pair.create((View) ((ArtistOverviewActivity) context).getMusicControlView(), "musicControlView"),
                                Pair.create((View) albumImage, "albumImage"));

                context.startActivity(intent, options.toBundle());
            }
        });
    }

    public void addSong(SongView2 song) {
        card.addView(song);
    }
}
