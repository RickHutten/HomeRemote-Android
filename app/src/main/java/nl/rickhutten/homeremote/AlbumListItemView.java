package nl.rickhutten.homeremote;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


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

        final String artistFormat = artist.replace(" ", "_");
        final String albumFormat = album.replace(" ", "_");

        // Download image and load into imageview
        Picasso.with(context).load("http://rickert.noip.me/image/" + artistFormat + "/" + albumFormat)
                .config(Bitmap.Config.RGB_565).into(imageView);
    }

}
