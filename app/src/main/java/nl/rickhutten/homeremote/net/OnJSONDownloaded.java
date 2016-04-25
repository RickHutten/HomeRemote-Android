package nl.rickhutten.homeremote.net;

import org.json.JSONObject;

public interface OnJSONDownloaded {
    void onJSONCompleted(JSONObject jObject);
}
