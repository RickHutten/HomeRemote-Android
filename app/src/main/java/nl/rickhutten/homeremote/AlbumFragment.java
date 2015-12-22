package nl.rickhutten.homeremote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AlbumFragment extends Fragment {

    ViewGroup layout;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.layout = (ViewGroup) inflater.inflate(R.layout.album_fragment, container, false);
        mainActivity = (MainActivity) inflater.getContext();
        RequestTask getArtists = new RequestTask(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                setAlbums(result);
            }
        });
        getArtists.execute("http://rickert.noip.me/albums");
        return layout;
    }

    private void setAlbums(String albums) {
        layout.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        RelativeLayout scrollLayout = (RelativeLayout) layout.findViewById(R.id.scrollLayout);
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(albums.split(";")));

        Collections.sort(arrayList);
        int i = 0;
        for (String album : arrayList) {

            AlbumCardView albumCardView = new AlbumCardView(getContext(), mainActivity);
            albumCardView.setAlbum(album);
            RelativeLayout.LayoutParams lp = null;
            int width = getResources().getDimensionPixelSize(R.dimen.album_width);
            int height = getResources().getDimensionPixelSize(R.dimen.album_height);
            if (i % 2 == 0) {
                // Make layoutparams
                System.out.println("Layout 1 " + album);
                lp = new RelativeLayout.LayoutParams(
                        width, height);
                lp.setMargins(0, (int)Math.floor(i/2)*height, 0, 0);
            } else {
                // Make layoutparams
                System.out.println("Layout 2 " + album);
                lp = new RelativeLayout.LayoutParams(
                        width, height);
                lp.setMargins(width, (int)Math.floor(i/2)*height, 0, 0);
            }
            albumCardView.setLayoutParams(lp);
            scrollLayout.addView(albumCardView, 0);

            i += 1;
        }
    }
}
