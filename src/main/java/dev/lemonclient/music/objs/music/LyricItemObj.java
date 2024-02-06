package dev.lemonclient.music.objs.music;


public class LyricItemObj {
    public String lyric;
    public String tlyric;

    public LyricItemObj(String lyric, String tlyric) {
        this.lyric = lyric;
        this.tlyric = tlyric;
    }

    public String getString() {
        if (lyric == null || lyric.isEmpty())
            return "";
        String data;
        if (tlyric != null && !tlyric.isEmpty()) {
            data = "%Lyric%(%TLyric%)";
            return data.replace("%Lyric%", lyric)
                .replace("%TLyric%", tlyric);
        }

        data = "%Lyric%";
        return data.replace("%Lyric%", lyric);
    }
}
