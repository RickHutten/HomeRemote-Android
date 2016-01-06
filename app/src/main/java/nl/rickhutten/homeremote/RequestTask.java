package nl.rickhutten.homeremote;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

class RequestTask extends AsyncTask<String, String, String> {

    private OnTaskCompleted listener;

    public RequestTask (OnTaskCompleted listener){
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... uri) {
        String result = "";
        try {
            URL url = new URL(uri[0]);
            Log.v("RequestTask", url.toString());

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        listener.onTaskCompleted(result);
    }
}