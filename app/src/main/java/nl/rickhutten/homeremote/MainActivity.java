package nl.rickhutten.homeremote;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private SharedPreferences sp;
    MusicControlView musicControlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("prefs", MODE_PRIVATE);

        // Add view to main activity
        musicControlView = new MusicControlView(this);
        ((RelativeLayout) findViewById(R.id.activity_main_container)).addView(musicControlView);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        int[] colors = {0, 0, 0};
        colors[0] = getResources().getColor(R.color.white);
        colors[1] = getResources().getColor(R.color.white);
        colors[2] = getResources().getColor(R.color.white);
        mTabs.setSelectedIndicatorColors(colors);
        mTabs.setBackgroundResource(R.color.primary);
        mTabs.setViewPager(mPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        musicControlView.update();
    }

    class MyPagerAdapter extends FragmentPagerAdapter {

        String[] tabTitles;
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            tabTitles = getResources().getStringArray(R.array.tabTitles);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ArtistFragment();
                case 1:
                    return new AlbumFragment();
                case 2:
                    return new PlaylistFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
