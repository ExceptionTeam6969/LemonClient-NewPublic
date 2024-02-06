package dev.lemonclient.commands.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Command;
import dev.lemonclient.music.MusicManager;
import dev.lemonclient.music.objs.SearchMusicObj;
import dev.lemonclient.music.objs.music.SearchPageObj;
import dev.lemonclient.music.player.MusicControl;
import dev.lemonclient.utils.network.Http;
import net.minecraft.command.CommandSource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class MusicCommand extends Command {
    public static SearchPageObj currentSearch;
    public static boolean canChoose;
    public static String phone;
    public static String code;
    public static boolean debug;

    public MusicCommand() {
        super("music", "*** now playing ***", "music");
        LemonClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("search").then(argument("name", StringArgumentType.greedyString()).executes(context -> {
            String name = StringArgumentType.getString(context, "name");
            MusicManager.INSTANCE.async.submit(() -> {
                currentSearch = MusicManager.api().search(name);
                if (currentSearch == null) {
                    info("未找到名为" + name + "的歌曲");
                    return;
                }
                if (currentSearch.resData.isEmpty()) {
                    info("未找到名为" + name + "的歌曲");
                } else {
                    info("----------------Search----------------(" + currentSearch.size() + ")");
                    for (int i = 0; i < currentSearch.size(); i++) {
                        SearchMusicObj music = currentSearch.getRes(i);

                        String debugString = "";
                        if (debug) {
                            debugString = " | " + MusicManager.api().getPlayUrl(music.id);
                        }
                        info(i + ": " + music.name + " |by: " + music.author + debugString);
                    }
                    info("----------------Search------------------------");
                    canChoose = true;
                }
            });
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("download").then(argument("id", IntegerArgumentType.integer(0, 29)).executes(context -> {
            if (canChoose) {
                int num = IntegerArgumentType.getInteger(context, "id");

                MusicManager.INSTANCE.async.submit(() -> {
                    if (num > currentSearch.size()) {
                        info(num + "超出索引范围");
                    } else {
                        if (currentSearch.getRes(num) != null) {
                            SearchMusicObj music = currentSearch.getRes(num);

                            if (MusicManager.api().getPlayUrl(music.id) == null) {
                                info("该歌曲" + music.name + "没有解析到链接，无法进行下载");
                            } else {
                                String url = MusicManager.api().getPlayUrl(music.id);

                                new Thread(() -> {
                                    File folder = new File(MusicManager.folder, "downloads");
                                    if (!folder.exists()) folder.mkdirs();

                                    File f = new File(folder, music.name + "-" + music.author + ".mp3");
                                    downloadFile(url, f);
                                    info("已下载完成");
                                }).start();


                                if (debug) {
                                    info("Debug: Url-> " + url);
                                }
                            }
                        }
                    }
                });
            } else {
                info("你还未搜索歌曲，使用music get进行搜索");
                return SINGLE_SUCCESS;
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("load").then(argument("id", IntegerArgumentType.integer(0, 29)).executes(context -> {
            if (canChoose) {
                int num = IntegerArgumentType.getInteger(context, "id");

                MusicManager.INSTANCE.async.submit(() -> {
                    if (num > currentSearch.resData.size()) {
                        info(num + "超出索引范围");
                    } else {
                        if (currentSearch.getRes(num) != null) {
                            SearchMusicObj music = currentSearch.getRes(num);

                            if (MusicManager.api().getPlayUrl(music.id) == null) {
                                info("该歌曲" + music.name + "没有解析到链接，无法进行播放");
                            } else {
                                MusicControl.INSTANCE.playMusic(music.id);
                            }
                        }
                    }
                });
            } else {
                info("你还未搜索歌曲，使用music get进行搜索");
                return SINGLE_SUCCESS;
            }
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("login").then(argument("phone", StringArgumentType.string()).executes(context -> {
            phone = StringArgumentType.getString(context, "phone");

            String sb = "*".repeat(Math.max(0, phone.length() - 2));
            info("设置手机号为" + phone.substring(0, 2) + sb);

            MusicManager.api().sendCode(phone);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("captcha").then(argument("code", StringArgumentType.string()).executes(context -> {
            code = StringArgumentType.getString(context, "code");

            info("设置验证码为" + code);

            if (phone != null) {
                MusicManager.api().login(phone, code);
            } else info("未设置手机号.");
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("debug").then(argument("enable", BoolArgumentType.bool()).executes(context -> {
            debug = BoolArgumentType.getBool(context, "enable");

            info("设置Debug为" + debug);
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("stop").executes(context -> {
            MusicControl.INSTANCE.stopPlay();
            info("已停止播放");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("pause").then(argument("pa", BoolArgumentType.bool()).executes(context -> {
            return SINGLE_SUCCESS;
        })));
    }

    public void downloadFile(String str, File out) {
        try {
            URL url = new URL(str);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            InputStream inputStream = conn.getInputStream();
            byte[] getData = readInputStream(inputStream);
            FileOutputStream fos = new FileOutputStream(out);
            fos.write(getData);
            fos.close();
            inputStream.close();
        } catch (Exception e) {
            InputStream inputStream;
            try {
                inputStream = Http.get(str).sendInputStream();
            } catch (Exception e1) {
                inputStream = Http.post(str).sendInputStream();
            }
            try {
                if (inputStream != null) {
                    byte[] getData = readInputStream(inputStream);
                    FileOutputStream fos = new FileOutputStream(out);
                    fos.write(getData);
                    fos.close();
                    inputStream.close();
                }
            } catch (IOException ex) {
                try {
                    Files.copy(inputStream, out.toPath());
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        }
    }


    private byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4 * 1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}
