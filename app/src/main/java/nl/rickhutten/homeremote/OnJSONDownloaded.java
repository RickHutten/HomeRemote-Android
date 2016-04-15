package nl.rickhutten.homeremote;

import org.json.JSONObject;

public interface OnJSONDownloaded {
    void onJSONCompleted(JSONObject jObject);
}
