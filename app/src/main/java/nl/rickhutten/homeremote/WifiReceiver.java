package nl.rickhutten.homeremote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;


public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null) {
            // Get Wifi SSID
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            Toast.makeText(context, ssid, Toast.LENGTH_SHORT).show();

            if (ssid.contains("Wie dit leest is gek")) {
                final GETRequest play = new GETRequest(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                    }
                });

                GETRequest register = new GETRequest(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String result) {
                        play.execute("http://rickert.noip.me/play");
                    }
                });
//                register.execute("http://rickert.noip.me/register_ip?key=hoerenneukennooitmeerwerken");
            } else {
                // I am not home
                System.out.println(ssid);
            }
        }
    }
}