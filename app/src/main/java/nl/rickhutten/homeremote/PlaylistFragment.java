package nl.rickhutten.homeremote;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaylistFragment extends Fragment {

    ViewGroup layout;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.layout = (ViewGroup) inflater.inflate(R.layout.album_fragment, container, false);
        mainActivity = (MainActivity) inflater.getContext();

        // Get all playlists

        return layout;
    }
}
