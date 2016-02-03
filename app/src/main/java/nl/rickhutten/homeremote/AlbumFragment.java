package nl.rickhutten.homeremote;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AlbumFragment extends Fragment {

    private ViewGroup layout;
    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.layout = (ViewGroup) inflater.inflate(R.layout.album_fragment, container, false);
        mainActivity = (MainActivity) inflater.getContext();
        GETRequest getArtists = new GETRequest(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                if (!result.equals("")) {
                    setAlbums(result);
                }
            }
        });
        getArtists.execute("http://rickert.noip.me/albums");
        return layout;
    }

    private void setAlbums(String albums) {
        layout.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(albums.split(";")));
        Collections.sort(arrayList);
        GridView gridview = (GridView) layout.findViewById(R.id.gridview);
        AlbumAdapter albumAdapter = new AlbumAdapter(mainActivity);
        albumAdapter.set(arrayList);
        gridview.setAdapter(albumAdapter);
        gridview.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(360), 173 * dpToPx(arrayList.size()/3 + 1)));
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public class AlbumAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> albumList;

        // Gets the context so it can be used later
        public AlbumAdapter(Context context) {
            this.context = context;
        }

        public void set(ArrayList<String> albumList) {
            this.albumList = albumList;
        }

        // Total number of things contained within the adapter
        public int getCount() {
            return albumList.size();
        }

        // Require for structure, not really used in my code.
        public Object getItem(int position) {
            return null;
        }

        // Require for structure, not really used in my code. Can
        // be used to get the id of an item in the adapter for
        // manual control.
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumCardView albumCardView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                albumCardView = new AlbumCardView(context);
                albumCardView.set((MainActivity)context, ((MainActivity)context).musicControlView);
            }
            else {
                albumCardView = (AlbumCardView) convertView;
            }
            albumCardView.setAlbum(albumList.get(position));
            albumCardView.setWidth(120);

            return albumCardView;
        }
    }
}
