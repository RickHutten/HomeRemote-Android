package nl.rickhutten.homeremote.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import nl.rickhutten.homeremote.Utils;
import nl.rickhutten.homeremote.fragment.PlaylistFragment;
import nl.rickhutten.homeremote.R;
import nl.rickhutten.homeremote.gcm.RegistrationIntentService;
import nl.rickhutten.homeremote.view.SlidingTabLayout;
import nl.rickhutten.homeremote.fragment.AlbumFragment;
import nl.rickhutten.homeremote.fragment.ArtistFragment;
import nl.rickhutten.homeremote.view.MusicControlView;
import nl.rickhutten.homeremote.dialog.VolumeDialog;

public class MainActivity extends AppCompatActivity {

    public MusicControlView musicControlView;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add view to main activity
        musicControlView = new MusicControlView(this);
        ((RelativeLayout) findViewById(R.id.activity_main_container)).addView(musicControlView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        SlidingTabLayout mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        int[] colors = {0, 0, 0};
        colors[0] = getResources().getColor(R.color.white);
        colors[1] = getResources().getColor(R.color.white);
        colors[2] = getResources().getColor(R.color.white);
        mTabs.setSelectedIndicatorColors(colors);
        mTabs.setBackgroundResource(R.color.primary);
        mTabs.setViewPager(mPager);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Log.i("MainActivity", "Push Received!");
                musicControlView.setNewSongComming(true);
                musicControlView.update();
            }
        };

        if (checkPlayServices()) {
//            Log.i("MainActivity", "Google Play Services available");
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i("MainActivity", "onResume MusicControlView ID: " + musicControlView.ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("pushReceived"));
        musicControlView.setActive(true);
        musicControlView.updateHard();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
//        Log.i("MainActivity", "onPause MusicControlView ID: " + musicControlView.ID);
        musicControlView.setActive(false);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_volume:
                // Show volume dialog
                new VolumeDialog(this, R.style.ThemeDialog).show();
                return true;
            case R.id.action_shutdown:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.ask_shutdown)
                        .setPositiveButton("Yes", Utils.getDialogClickListener(this))
                        .setNegativeButton("No", Utils.getDialogClickListener(this)).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000)
                        .show();
            } else {
                Log.i("MainActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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
