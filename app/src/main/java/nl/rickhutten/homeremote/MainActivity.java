package nl.rickhutten.homeremote;

import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private int ResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiReceiver wifiReceiver = new WifiReceiver();
        wifiReceiver.setMainActivityHandler(this);
        IntentFilter intentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
        registerReceiver(wifiReceiver, intentFilter);

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

        findViewById(R.id.playPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {
                    // Resume playing
                    RequestTask resume = new RequestTask(new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            setPlayPause(R.drawable.ic_pause_circle_outline_white_48dp);
                        }
                    });
                    resume.execute("http://rickert.noip.me/resume");
                } else {
                    // Pause playing
                    RequestTask pause = new RequestTask(new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(String result) {
                            setPlayPause(R.drawable.ic_play_circle_outline_white_48dp);
                        }
                    });
                    pause.execute("http://rickert.noip.me/pause");
                }
            }
        });

        RequestTask getPlaying = new RequestTask(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String result) {
                setPlayingSong(result);
            }
        });
        getPlaying.execute("http://rickert.noip.me/playing");
    }

    public void setPlayingSong(String text) {
        ((TextView)findViewById(R.id.playingText)).setText(text);
    }

    public void setPlayPause(int id) {
        ResId = id;
        ((ImageView)findViewById(R.id.playPause)).setImageResource(id);
    }

    public boolean isPlaying() {
        return ResId != R.drawable.ic_play_circle_outline_white_48dp;
    }

    class MyPagerAdapter extends FragmentPagerAdapter {

        String[] tabTitles;
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            tabTitles = getResources().getStringArray(R.array.tabTitles);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ArtistFragment();
            } else {
                return new AlbumFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
