package dev.lemonclient.music.player;

import dev.lemonclient.music.MusicManager;
import dev.lemonclient.music.TickingTask;
import dev.lemonclient.music.objs.music.SongInfoObj;

public class MusicControl {
    // INSTANCE
    public static MusicControl INSTANCE;
    public final MusicManager manager = MusicManager.INSTANCE;
    public final MusicApi api = manager.apiMusic;
    public final TickingTask ticker = new TickingTask(this::task);
    /**
     * 当前歌曲信息
     */
    public MusicInfo nowPlayMusic;

    public MusicControl() {
        INSTANCE = this;
    }


    public void playMusic(String id) {
        if (!assertMusicManager()) {
            return;
        }

        manager.async.submit(() -> {
            SongInfoObj obj = api.getMusic(id, false);
            if (obj == null) {
                manager.info("不能找到id为{}的歌曲", id);
                return;
            }

            if (nowPlayMusic != null && !nowPlayMusic.stopped) {
                nowPlayMusic.onStop();
            }

            if (!manager.player.isClose()) {
                manager.player.closePlayer();
            }

            this.nowPlayMusic = new MusicInfo(obj);
            this.nowPlayMusic.loadImage();
            this.nowPlayMusic.onLoad(this.manager.player);
        });
    }

    public void stopPlay() {
        if (!assertMusicManager()) {
            return;
        }

        if (!nowPlayMusic.stopped) {
            nowPlayMusic.onStop();
        }
        nowPlayMusic = null;
        manager.player.closePlayer();
        manager.info("Stopping.");
    }

    /**
     * 开始歌曲逻辑
     */
    public void start() {
        ticker.start();
    }

    private void task() {
        try {
            if (nowPlayMusic != null) nowPlayMusic.update(this.manager.player, this::stopPlay);
        } catch (Exception e) {
            MusicManager.INSTANCE.warning("歌曲处理出现问题");
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean assertMusicManager() {
        return MusicManager.INSTANCE != null;
    }
}

