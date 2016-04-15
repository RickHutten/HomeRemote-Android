package nl.rickhutten.homeremote;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETJSONRequest extends AsyncTask<String, String, String> {

    private OnJSONDownloaded listener;

    public GETJSONRequest(OnJSONDownloaded listener){
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... uri) {
        String result = "";
        HttpURLConnection urlConnection = null;
        URL url;
        try {
            url = new URL(uri[0]);
            Log.v("GETJSONRequest", url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-type", "application/json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        JSONObject jObject = null;
        try {
            jObject  = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listener.onJSONCompleted(jObject);
    }

}