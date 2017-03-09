package nl.rickhutten.homeremote.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class POSTRequest extends AsyncTask<String, String, String> {

    private String data;
    private OnTaskCompleted listener;

    public POSTRequest(String data, OnTaskCompleted listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... uri) {
        String msg = "";


        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(uri[0]);
            Log.v("POSTRequeset", url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

            // Get outputstream
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

            // Write data and close outputstream
            out.write(data.getBytes());
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
