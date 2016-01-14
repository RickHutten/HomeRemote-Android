package nl.rickhutten.homeremote;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class POSTRequest extends AsyncTask<String, String, String> {

    ArrayList<ArrayList<String>> data;
    OnTaskCompleted listener;

    public POSTRequest(ArrayList<ArrayList<String>> data, OnTaskCompleted listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... uri) {
        String data_string = "";
        String msg = "";
        // data is of shape: [ [artist, album, song], ... ]
        for (ArrayList<String> song : data) {
            data_string += song.get(0) + ":" + song.get(1) + ":" + song.get(2);
            if (song != data.get(data.size() - 1)) {
                data_string += ";";
            }
        }
        Log.v("POSTRequeset", "POST: " + data_string);

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(uri[0]);

            // Check for 403 forbidden
            urlConnection = (HttpURLConnection) url.openConnection();
            int responsecode = urlConnection.getResponseCode();
            if (responsecode == HttpURLConnection.HTTP_FORBIDDEN) {
                Log.w("POSTRequest", "Registering device IP");
                // Register this IP adres
                URL u = new URL("http://rickert.noip.me/register_ip?key=hoerenneukennooitmeerwerken");
                u.openStream().close();
            }
            // Reconnect
            urlConnection.disconnect();
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

            // Get outputstream
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

            // Write data and close outputstream
            out.write(data_string.getBytes());
            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                msg += line;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            msg = "Fail";
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return msg;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        listener.onTaskCompleted(result);
    }
}
