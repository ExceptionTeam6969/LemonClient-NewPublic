package dev.lemonclient.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.lemonclient.LemonClient;
import dev.lemonclient.lemonchat.utils.GsonUtils;
import dev.lemonclient.lemonchat.utils.StringUtils;
import dev.lemonclient.music.objs.CookieObj;
import dev.lemonclient.music.player.LMusicPlayer;
import dev.lemonclient.music.player.MusicApi;
import dev.lemonclient.music.player.MusicControl;
import dev.lemonclient.systems.hud.elements.MusicHud;
import dev.lemonclient.utils.Utils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static dev.lemonclient.LemonClient.mc;

public class MusicManager {
    private static final String prefix = Formatting.GRAY + "[" + Formatting.RED + "LemonClient" + Formatting.GRAY + "]";
    public static final Identifier ALLMUSIC_ID = new Identifier("allmusic", "channel");
    public static final Identifier ZMUSIC_ID = new Identifier("zmusic", "channel");
    public static final File folder = new File(LemonClient.FOLDER, "music");
    public static final File cookieFile = new File(folder, "cookie.json");
    public static MusicManager INSTANCE;
    public LMusicPlayer player;
    public CookieObj cookie;
    public MusicApi apiMusic;
    public final Gson gson = GsonUtils.newBuilder().create();
    public static boolean loaded;

    public final AsyncLoopTask async = new AsyncLoopTask();
    public final TickingTask ticker = new TickingTask(this::tick);
    public MusicControl control;

    private void tick() {
        player.tick();
    }

    public MusicManager() {
        INSTANCE = this;
    }

    public static MusicApi api() {
        return INSTANCE.apiMusic;
    }

    public void init() {
        LemonClient.EVENT_BUS.subscribe(this);
        try {
            player = new LMusicPlayer();
            cookie = new CookieObj();
            apiMusic = new MusicApi();
            if (!folder.exists()) folder.mkdirs();
            loadCookie();

            async.start();
            ticker.start();

            control = new MusicControl();
            control.start();

            loaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadCookie() throws IOException {
        if (!cookieFile.exists()) {
            cookie = new CookieObj();
            saveCookie();
        }

        InputStreamReader reader;
        BufferedReader bf;
        reader = new InputStreamReader(Files.newInputStream(cookieFile.toPath()), StandardCharsets.UTF_8);
        bf = new BufferedReader(reader);
        cookie = new Gson().fromJson(bf, CookieObj.class);
        bf.close();
        reader.close();
        if (cookie == null) {
            cookie = new CookieObj();
            saveCookie();
        }
    }

    public void saveCookie() {
        try {
            String data = new GsonBuilder().setPrettyPrinting().create().toJson(cookie);
            if (!cookieFile.exists()) cookieFile.createNewFile();

            FileOutputStream out = new FileOutputStream(cookieFile);
            OutputStreamWriter write = new OutputStreamWriter(
                out, StandardCharsets.UTF_8);
            write.write(data);
            write.close();
        } catch (Exception e) {
            INSTANCE.warning("Cookie文件保存错误");
            e.printStackTrace();
        }
    }

    public int getVolume() {
        return MusicHud.INSTANCE.volume.get();
    }

    public boolean enablePitch() {
        return MusicHud.INSTANCE.enablePitch.get();
    }

    public float getPitch() {
        return MusicHud.INSTANCE.pitch.get() / 100f;
    }

    public static class Song {
        public String url;
        public String id;

        public Song(String url, String id) {
            this.url = url;
            this.id = id;
        }
    }

    public boolean assertLoaded() {
        if (!loaded) error("Music disabled.");

        return loaded;
    }

    public void info(String message, Object... args) {
        String msg = prefix + " " + Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + "Music" + Formatting.GRAY + "] " + Formatting.GRAY + message;
        sendMessage(msg, args);
    }

    public void error(String message, Object... args) {
        String msg = prefix + " " + Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + "Music" + Formatting.GRAY + "] " + Formatting.RED + message;
        sendMessage(msg, args);
    }

    public void warning(String message, Object... args) {
        String msg = prefix + " " + Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + "Music" + Formatting.GRAY + "] " + Formatting.YELLOW + message;
        sendMessage(msg, args);
    }

    public void sendMessage(String text, Object... args) {
        text = StringUtils.getReplaced(text, args);
        if (Utils.canUpdate()) {
            mc.inGameHud.getChatHud().addMessage(Text.literal(text));
        }
    }
}
