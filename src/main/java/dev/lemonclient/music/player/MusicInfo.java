package dev.lemonclient.music.player;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.LemonClient;
import dev.lemonclient.music.MusicManager;
import dev.lemonclient.music.decoder.Bitstream;
import dev.lemonclient.music.decoder.Header;
import dev.lemonclient.music.objs.music.SongInfoObj;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.network.Http;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import static dev.lemonclient.LemonClient.mc;

public class MusicInfo {
    private final String name;
    private final String author;

    public long maxTime, nowTime;

    public double musicProgress;
    private final LyricSave lyric;

    private final SongInfoObj musicObj;

    private final Identifier music_image;
    private boolean imageLoaded;

    private final String playUrl;

    private final boolean async;

    public boolean playing;
    public boolean paused;

    public boolean stopped;

    private static final MusicManager manager = MusicManager.INSTANCE;

    public MusicInfo(SongInfoObj obj) {
        this.musicObj = obj;
        this.name = obj.getName();
        this.author = obj.getAuthor();
        this.musicProgress = 0.0;
        this.lyric = new LyricSave();
        this.music_image = new Identifier(LemonClient.MOD_ID, "textures/heart.png");
        this.async = true;
        this.imageLoaded = false;
        this.playUrl = obj.getUrl();

        submit(() -> {
            this.maxTime = getTime(playUrl);
            this.nowTime = 0L;
        });
    }

    public void onLoad(LMusicPlayer player) {
        player.setMusic(this.playUrl);
        manager.info("Now playing: " + this.name + " | " + this.author);
        playing = true;
        stopped = false;
        paused = false;
    }

    public void loadImage() {
        RenderSystem.recordRenderCall(() -> {
            if (Utils.canUpdate()) {
                try {
                    Http.Request request = Http.get(pictureUrl());
                    InputStream stream = request.sendInputStream();
                    if (stream != null) {
                        NativeImage image = NativeImage.read(stream);
                        NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                        mc.getTextureManager().registerTexture(this.music_image, texture);
                    }
                } catch (IOException e) {
                    MusicManager.INSTANCE.warning("获取'{}'歌曲图片时失败 [{}]", this.name, e);
                } finally {
                    imageLoaded = true;
                }
            }
        });
    }

    private ByteBuffer read(InputStream stream) throws IOException {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = TextureUtil.readResource(stream);
            byteBuffer.rewind();
        } finally {
            MemoryUtil.memFree(byteBuffer);
            IOUtils.closeQuietly(stream);
        }
        return byteBuffer;
    }

    public String pictureUrl() {
        return musicObj.getPicUrl();
    }

    public String name() {
        return this.name;
    }

    public String author() {
        return this.author;
    }

    public Identifier picture() {
        if (!imageLoaded) {
            loadImage();
        }
        return this.music_image;
    }

    public LyricSave lyric() {
        return this.lyric;
    }

    public String nowTimeStr() {
        return convertMillis(Math.min(nowTime, maxTime));
    }

    public String maxTimeStr() {
        return convertMillis(maxTime);
    }


    public String timeInfo() {
        return nowTimeStr() + "/" + maxTimeStr();
    }

    public String statusInfo() {
        return playing ? paused ? "Paused" : "Playing" : "Stopped";
    }

    public void update(LMusicPlayer player, Runnable stopAction) {
        if (playing) {
            nowTime = player.getTime();
            if (nowTime != 0 && maxTime != 0) musicProgress = Math.min(((double) nowTime) / ((double) maxTime), 1.0);

            if (nowTime > maxTime) {
                onStop();
                stopAction.run();
            }
        }
    }

    private void submit(Runnable task) {
        if (async) MusicManager.INSTANCE.async.submit(() -> {
            if (Utils.canUpdate()) task.run();
        });
        else task.run();
    }

    public String convertMillis(long time) {
        long s = time / 1000;
        long m = s / 60;
        if ((m * 60) > s) {
            m = m - 1;
        }
        long M = s - m * 60;
        String ma = (M + "").length() == "1".length() ? "0" + M : M + "";
        return m + ":" + ma;
    }

    private int getTime(String url) {
        try {
            URL _url = new URL(url);
            URLConnection con = _url.openConnection();
            int b = con.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            Bitstream bt = new Bitstream(bis);
            Header h = bt.readFrame();
            int le = 6000000;
            if (h == null) {
                MusicManager.INSTANCE.warning("'{}' 未知音乐类型", this.name);
            } else {
                le = (int) h.total_ms(b);
            }
            return le;
        } catch (Exception e) {
            MusicManager.INSTANCE.warning("歌曲'{}'信息解析错误 []", this.name, e);
        }
        return -1;
    }

    public void onStop() {
        playing = false;
        stopped = true;
        paused = false;
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.music_image);
    }
}
