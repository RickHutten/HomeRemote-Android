package nl.rickhutten.homeremote.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.URL;


public class AlbumListItemView extends RelativeLayout {

    ViewGroup rootView;
    Context context;

    public AlbumListItemView(Context context) {
        super(context);
        // Inflate layout from XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (ViewGroup) inflater.inflate(R.layout.album_list_item, this, false);
        addView(rootView);
        this.context = context;
    }

    public void set(String artist, String album) {
        ImageView imageView = (ImageView) rootView.findViewById(R.id.image);
        TextView textView = (TextView) rootView.findViewById(R.id.albumText);
        textView.setText(album);

        // Download image and load into imageview
        Picasso.with(context).load(URL.getAlbumImageUrl(context, artist, album))
                .config(Bitmap.Config.RGB_565).into(imageView);
    }

}
