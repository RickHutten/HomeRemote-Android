package nl.rickhutten.homeremote;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GETRequest extends AsyncTask<String, String, String> {

    private OnTaskCompleted listener;

    public GETRequest(OnTaskCompleted listener){
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... uri) {
        String result = "";
        HttpURLConnection urlConnection = null;
        URL url = null;
        try {
            url = new URL(uri[0]);
            Log.v("GETRequest", url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            reader.close();

        } catch (IOException e) {
            if (e.getClass() == FileNotFoundException.class && urlConnection != null) {
                try {
                    // 403 Forbidden, need to register device IP
                    Integer responsecode = urlConnection.getResponseCode();
                    if (responsecode == HttpURLConnection.HTTP_FORBIDDEN) {
                        Log.v("GETRequest", "Registering device IP");
                        // Register this IP adres
                        URL u = new URL("http://rickert.noip.me/register_ip?key=hoerenneukennooitmeerwerken");
                        u.openStream().close();
                    } else {
                        // Other response code than 403
                        Log.w("GETRequest", "Response code: " + responsecode.toString());
                    }
                    // Call GET request again now that we have registered
                    urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }
                    reader.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
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
        listener.onTaskCompleted(result);
    }

}