package nl.rickhutten.homeremote;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class RequestTask extends AsyncTask<String, String, String> {

    private OnTaskCompleted listener;

    public RequestTask (OnTaskCompleted listener){
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... uri) {
        String strFileContents = "";
        try {
            URL url = new URL(uri[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            System.out.println("Response code: " + urlConnection.getResponseCode());
            BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] contents = new byte[1024];

            int bytesRead;

            while( (bytesRead = in.read(contents)) != -1){
                strFileContents = new String(contents, 0, bytesRead);
            }
            System.out.println(strFileContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strFileContents;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        listener.onTaskCompleted(result);
    }
}