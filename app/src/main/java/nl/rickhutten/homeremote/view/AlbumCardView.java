package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.activity.AlbumOverviewActivity;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.Utils;
import nl.rickhutten.homeremote.activity.MusicActivity;

public class AlbumCardView extends RelativeLayout {

    private MusicActivity activity;
    private MusicControlView musicControlView;
    private int height;

    public AlbumCardView(Context context) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.album_card_layout, this, false);
        addView(rootView);
    }

    public void set(final MusicActivity activity) {
        this.activity = activity;
        this.musicControlView = activity.musicControlView;
    }

    public void setAlbum(final String album, final String artist) {
        ((TextView) findViewById(R.id.albumText)).setText(album);
        ((TextView) findViewById(R.id.artistText)).setText(artist);

        final ImageView albumImage = (ImageView) findViewById(R.id.albumImage);

        // Download image and load into imageview
        Picasso.with(activity).load(URL.getAlbumImageUrl(activity, artist, album))
                .config(Bitmap.Config.RGB_565)
                .resizeDimen(
                        R.dimen.album_card_view_image_width,
                        R.dimen.album_card_view_image_height)
                .into(albumImage);

        findViewById(R.id.cardView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AlbumOverviewActivity.class);
                intent.putExtra("artist", artist);
                intent.putExtra("album", album);

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        Pair.create((View) musicControlView, "musicControlView"),
                        Pair.create((View) albumImage, "albumImage"));

                activity.startActivity(intent, options.toBundle());
            }
        });
    }

    public void setWidth(int dp) {
        View cardView = findViewById(R.id.cardView);
        View imageView = findViewById(R.id.albumImage);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
        lp.width = Utils.dpToPx(activity, dp);
        lp.height = Utils.dpToPx(activity, (dp * 13 / 9));
        this.height = lp.height;
        cardView.setLayoutParams(lp);

        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        lp2.width = Utils.dpToPx(activity, dp - 16);
        lp2.height = Utils.dpToPx(activity, dp - 16);
        imageView.setLayoutParams(lp2);
    }

    public int getViewHeight() {
        return height;
    }
}
