package nl.rickhutten.homeremote.fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import nl.rickhutten.homeremote.URL;
import nl.rickhutten.homeremote.net.GETRequest;
import nl.rickhutten.homeremote.activity.MainActivity;
import nl.rickhutten.homeremote.net.OnTaskCompleted;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.view.ArtistCardView;
import nl.rickhutten.homeremote.view.SlidingTabLayout;

public class ArtistFragment extends Fragment {

    private ViewGroup layout;
    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.layout = (ViewGroup) inflater.inflate(R.layout.fragment_artist, container, false);
        mainActivity = (MainActivity) inflater.getContext();
        GETRequest getArtists = new GETRequest(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                if (!result.equals("")) {
                    setArtists(result);
                }
            }
        });
        getArtists.execute(URL.getUrl(mainActivity, "/artists"));

        final ScrollView scrollView = (ScrollView)layout.findViewById(R.id.scrollView);
        final SlidingTabLayout tabs = (SlidingTabLayout) mainActivity.findViewById(R.id.tabs);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            ActionBar actionBar = mainActivity.getSupportActionBar();
            int oldY = -1;
            int dy;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int posY = (int)event.getY()-actionBar.getHideOffset();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (oldY == -1) {
                            oldY = posY;
                            break;
                        }
                        dy = posY - oldY;
                        oldY = posY;
                        actionBar.setHideOffset(actionBar.getHideOffset()- dy);
                        tabs.setPadding(0, actionBar.getHeight()-actionBar.getHideOffset(), 0, 0);
                        break;
                    case MotionEvent.ACTION_UP:
                        oldY = -1;
                        if (actionBar.getHideOffset() < 0.5 * actionBar.getHeight()) {
                            ValueAnimator animation = ValueAnimator.ofInt(actionBar.getHideOffset(), 0);
                            animation.setDuration(100);
                            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int value = (int)animation.getAnimatedValue();
                                    actionBar.setHideOffset(value);
                                    tabs.setPadding(0, actionBar.getHeight()-value, 0, 0);
                                }
                            });
                            animation.start();
                        } else {
                            ValueAnimator animation = ValueAnimator.ofInt(actionBar.getHideOffset(), actionBar.getHeight());
                            animation.setDuration(100);
                            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int value = (int)animation.getAnimatedValue();
                                    actionBar.setHideOffset(value);
                                    tabs.setPadding(0, actionBar.getHeight()-value, 0, 0);
                                }
                            });
                            animation.start();
                        }
                        tabs.setPadding(0, actionBar.getHeight()-actionBar.getHideOffset(), 0, 0);
                }
                return false;
            }
        });

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
