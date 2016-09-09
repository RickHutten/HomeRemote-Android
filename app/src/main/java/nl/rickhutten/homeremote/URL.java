package nl.rickhutten.homeremote;

import android.content.Context;

public class URL {

    static public String urlSafe(String string) {
        return string.replace(" ", "_").replace("/", "%2F");
    }

    // Don't know if i'm going to use it
    static public String outputSafe(String string) {
        return string.replace("_", " ");
    }

    static public String getUrl(Context context, String route) {
        if (!route.startsWith("/")) {
            route = "/" + route;
        }
        return context.getString(R.string.url_base) + route;
    }

    static public String getArtistUrl(Context context, String artist) {
        return getUrl(context, "/get2/" + urlSafe(artist));
    }

    static public String getAlbumUrl(Context context, String artist, String album) {
        return getUrl(context, "/get2/" + urlSafe(artist) + "/" + urlSafe(album));
    }

    static public String getArtistImageUrl(Context context, String artist) {
        return getUrl(context, "/image/" + urlSafe(artist));
    }

    static public String getAlbumImageUrl(Context context, String artist, String album) {
        return getUrl(context, "/image/" + urlSafe(artist) + "/" + urlSafe(album));
    }

    static public String getPlaySongUrl(Context context) {
        return getUrl(context, "/play");
    }

    static public String getRegisterUrl(Context context) {
        return getUrl(context, "/register_ip?key=hoerenneukennooitmeerwerken");
    }

    static public String getVolumeUrl(Context context, int volume) {
        return getUrl(context, "/set/volume/" + volume);
    }

    static public String getSetQueueUrl(Context context) {
        return getUrl(context, "/set/queue");
    }

    static public String getPauseUrl(Context context) {
        return getUrl(context, "/pause");
    }

    static public String getResumeUrl(Context context) {
        return getUrl(context, "/resume");
    }

    static public String getNextUrl(Context context) {
        return getUrl(context, "/next");
    }

    static public String getPreviousUrl(Context context) {
        return getUrl(context, "/previous");
    }

    static public String getShutdownUrl(Context context) {
        return getUrl(context, "/shutdown");
    }

    static public String getStatusUrl(Context context) {
        return getUrl(context, "status");
    }
}
