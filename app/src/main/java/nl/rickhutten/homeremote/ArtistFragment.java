package nl.rickhutten.homeremote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
public class ArtistFragment extends Fragment {

    private ViewGroup layout;
    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.layout = (ViewGroup) inflater.inflate(R.layout.artist_fragment, container, false);
        mainActivity = (MainActivity) inflater.getContext();
        GETRequest getArtists = new GETRequest(new OnTaskCompleted() {
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

        ArtistCardView artistCardView = new ArtistCardView(getContext());
        artistCardView.set(mainActivity, mainActivity.musicControlView);
        artistCardView.setFirstLetter(arrayList.get(0).charAt(0));

        for (String artist : arrayList) {
            if (artistCardView.getFirstLetter().compareTo(artist.charAt(0)) != 0) {
                // New card because the card a artist with a new first letter has come
                scrollLayout.addView(artistCardView, scrollLayout.getChildCount() - 1);

                // Make new card
                artistCardView = new ArtistCardView(getContext());
                artistCardView.set(mainActivity, mainActivity.musicControlView);
                artistCardView.setFirstLetter(artist.charAt(0));
            }
            artistCardView.addArtist(artist);
        }
        scrollLayout.addView(artistCardView, scrollLayout.getChildCount() - 1);

    }
}
