package nl.rickhutten.homeremote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;


public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String SILENT_SSID = "Stadsdeel_Oost_Sporthal_IJburg";

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null) {
            // Get Wifi SSID
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            Toast.makeText(context, ssid, Toast.LENGTH_SHORT).show();

            SharedPreferences sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
            String lastSsid = sp.getString("last_ssid", "");

            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (ssid.contains(SILENT_SSID) && !lastSsid.contains(SILENT_SSID)) {
                // User enters wifi zone
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            } else if (lastSsid.contains(SILENT_SSID) && !ssid.contains(SILENT_SSID)){
                // User leaves wifi zone
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
            sp.edit().putString("last_ssid", ssid).apply();
        }
    }
}