package dev.lemonclient.music.objs;

public class HttpResObj {
    public final String data;
    public final boolean ok;

    public HttpResObj(String data, boolean ok) {
        this.data = data;
        this.ok = ok;
    }
}
