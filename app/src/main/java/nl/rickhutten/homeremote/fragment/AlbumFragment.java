package nl.rickhutten.homeremote.fragment;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.activity.MusicActivity;
import nl.rickhutten.homeremote.net.GETRequest;
import nl.rickhutten.homeremote.activity.MainActivity;
import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.Utils;
import nl.rickhutten.homeremote.view.AlbumCardView;
import nl.rickhutten.homeremote.view.SlidingTabLayout;

public class AlbumFragment extends Fragment {

    private ViewGroup layout;
    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.layout = (ViewGroup) inflater.inflate(R.layout.fragment_album, container, false);
        mainActivity = (MainActivity) inflater.getContext();
        GETRequest getArtists = new GETRequest(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                if (!result.equals("")) {
                    setAlbums(result);
                }
            }
        });
        getArtists.execute(URL.getUrl(mainActivity, "/albums"));

        final ScrollView scrollView = (ScrollView) layout.findViewById(R.id.scrollView);
        final SlidingTabLayout tabs = (SlidingTabLayout) mainActivity.findViewById(R.id.tabs);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            ActionBar actionBar = mainActivity.getSupportActionBar();
            int oldY = -1;
            int dy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int posY = (int) event.getY() - actionBar.getHideOffset();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (oldY == -1) {
                            oldY = posY;
                            break;
                        }
                        dy = posY - oldY;
                        oldY = posY;
                        actionBar.setHideOffset(actionBar.getHideOffset() - dy);
                        tabs.setPadding(0, actionBar.getHeight() - actionBar.getHideOffset(), 0, 0);
                        break;
                    case MotionEvent.ACTION_UP:
                        oldY = -1;
                        if (actionBar.getHideOffset() < 0.5 * actionBar.getHeight()) {
                            ValueAnimator animation = ValueAnimator.ofInt(actionBar.getHideOffset(), 0);
                            animation.setDuration(100);
                            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int value = (int) animation.getAnimatedValue();
                                    actionBar.setHideOffset(value);
                                    tabs.setPadding(0, actionBar.getHeight() - value, 0, 0);
                                }
                            });
                            animation.start();
                        } else {
                            ValueAnimator animation = ValueAnimator.ofInt(actionBar.getHideOffset(), actionBar.getHeight());
                            animation.setDuration(100);
                            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int value = (int) animation.getAnimatedValue();
                                    actionBar.setHideOffset(value);
                                    tabs.setPadding(0, actionBar.getHeight() - value, 0, 0);
                                }
                            });
                            animation.start();
                        }
                        tabs.setPadding(0, actionBar.getHeight() - actionBar.getHideOffset(), 0, 0);
                }
                return false;
            }
        });
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
        // Get the height of the first view
        int itemHeight = ((AlbumCardView) albumAdapter.getView(0, null, null)).getViewHeight();

        gridview.setLayoutParams(new LinearLayout.LayoutParams(Utils.getScreenWidth(getContext()), itemHeight * (int) (Math.ceil(arrayList.size() / 3))));
    }

    private class AlbumAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> albumList;

        // Gets the context so it can be used later
        AlbumAdapter(Context context) {
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
                albumCardView.set((MusicActivity) context);
            } else {
                albumCardView = (AlbumCardView) convertView;
            }
            albumCardView.setAlbum(albumList.get(position).split(":")[0], albumList.get(position).split(":")[1]);
            albumCardView.setWidth(Utils.pxToDp(getContext(), Utils.getScreenWidth(getContext()) / 3));

            return albumCardView;
        }
    }
}
