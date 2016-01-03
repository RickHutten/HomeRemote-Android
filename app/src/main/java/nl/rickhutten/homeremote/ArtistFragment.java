package nl.rickhutten.homeremote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ArtistFragment extends Fragment {

    ViewGroup layout;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.layout = (ViewGroup) inflater.inflate(R.layout.artist_fragment, container, false);
        mainActivity = (MainActivity) inflater.getContext();
        RequestTask getArtists = new RequestTask(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                if (!result.equals("")) {
                    setArtists(result);
                }
            }
        });
        getArtists.execute("http://rickert.noip.me/artists");
        return layout;
    }

    private void setArtists(String artists) {
        layout.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        LinearLayout scrollLayout = (LinearLayout) layout.findViewById(R.id.scrollLayout);
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(artists.split(";")));


        Collections.sort(arrayList);
        Collections.reverse(arrayList);
        for (String artist : arrayList) {
            ArtistCardView artistCardView = new ArtistCardView(getContext(), mainActivity);
            artistCardView.setArtist(artist);
            scrollLayout.addView(artistCardView, 0);
        }
    }
}
