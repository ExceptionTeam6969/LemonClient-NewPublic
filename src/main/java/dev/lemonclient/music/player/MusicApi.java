package dev.lemonclient.music.player;

import com.google.gson.JsonObject;
import dev.lemonclient.music.MusicManager;
import dev.lemonclient.music.objs.HttpResObj;
import dev.lemonclient.music.objs.SearchMusicObj;
import dev.lemonclient.music.objs.api.music.info.InfoObj;
import dev.lemonclient.music.objs.api.music.list.DataObj;
import dev.lemonclient.music.objs.api.music.lyric.WLyricObj;
import dev.lemonclient.music.objs.api.music.search.SearchDataObj;
import dev.lemonclient.music.objs.api.music.search.songs;
import dev.lemonclient.music.objs.api.music.trialinfo.TrialInfoObj;
import dev.lemonclient.music.objs.api.program.info.PrInfoObj;
import dev.lemonclient.music.objs.music.SearchPageObj;
import dev.lemonclient.music.objs.music.SongInfoObj;
import okhttp3.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicApi {
    public boolean isUpdate;

    public MusicApi() {
        MusicManager.INSTANCE.info("正在初始化网络爬虫");
        HttpClientUtil.init();
        HttpResObj res = HttpClientUtil.get("https://music.163.com", "");
        if (res == null || !res.ok) {
            MusicManager.INSTANCE.info("初始化网络爬虫连接失败");
        }
    }

    /**
     * 获取手机验证码
     */
    public void sendCode(String phone) {
        JsonObject params = new JsonObject();
        params.addProperty("ctcode", "86");
        params.addProperty("cellphone", phone);
        HttpResObj res = HttpClientUtil.post("https://music.163.com/api/sms/captcha/sent", params, EncryptType.WEAPI, null);
        MusicManager.INSTANCE.info("已发送验证码" + res.data);
    }

    /**
     * 登录
     *
     * @param code 手机验证码
     */
    public void login(String phone, String code) {
        JsonObject params = new JsonObject();
        params.addProperty("rememberLogin", "true");
        if (MusicManager.INSTANCE.cookie.cookieStore.containsKey("music.163.com")) {
            List<Cookie> cookies = MusicManager.INSTANCE.cookie.cookieStore.get("music.163.com");
            for (Cookie item : cookies) {
                if (item.name().equalsIgnoreCase("os")) {
                    cookies.remove(item);
                    break;
                }
            }
            for (Cookie item : cookies) {
                if (item.name().equalsIgnoreCase("appver")) {
                    cookies.remove(item);
                    break;
                }
            }
            List<Cookie> cookies1 = new CopyOnWriteArrayList<>();
            cookies1.addAll(cookies);
            cookies1.add(new Cookie.Builder().name("os").value("pc").domain("music.163.com").build());
            cookies1.add(new Cookie.Builder().name("appver").value("2.10.6").domain("music.163.com").build());
            MusicManager.INSTANCE.cookie.cookieStore.put("music.163.com", cookies1);
            MusicManager.INSTANCE.saveCookie();
        }
        params.addProperty("countrycode", "86");
        params.addProperty("phone", phone);
        params.addProperty("captcha", code);
        HttpResObj res = HttpClientUtil.post("https://music.163.com/eapi/w/login/cellphone", params, EncryptType.WEAPI, null);
        if (res == null || !res.ok) {
            MusicManager.INSTANCE.error("登录失败");
            return;
        }
        if (res.data.contains("200")) {
            MusicManager.INSTANCE.info("已登录");
        } else {
            MusicManager.INSTANCE.warning("登录失败:账号或密码错误\n" + res.data);
        }
    }

    /**
     * 获取音乐详情
     *
     * @param id     音乐ID
     * @param isList 是否是空闲列表
     * @return 结果
     */
    private SongInfoObj getMusicDetail(String id, boolean isList) {
        JsonObject params = new JsonObject();
        params.addProperty("c", "[{\"id\":" + id + "}]");

        HttpResObj res = HttpClientUtil.post("https://music.163.com/api/v3/song/detail", params, EncryptType.WEAPI, null);
        if (res != null && res.ok) {
            InfoObj temp = MusicManager.INSTANCE.gson.fromJson(res.data, InfoObj.class);
            if (temp.isOk()) {
                params = new JsonObject();
                params.addProperty("ids", "[" + id + "]");
                params.addProperty("br", "320000");
                res = HttpClientUtil.post("https://music.163.com/weapi/song/enhance/player/url", params, EncryptType.WEAPI, null);
                if (res == null || !res.ok) {
                    MusicManager.INSTANCE.warning("版权检索失败");
                    return null;
                }
                TrialInfoObj obj = MusicManager.INSTANCE.gson.fromJson(res.data, TrialInfoObj.class);
                return new SongInfoObj(temp.getAuthor(), temp.getName(),
                    id, temp.getAlia(), temp.getAl(), isList, temp.getLength(),
                    temp.getPicUrl(), obj.isTrial(), obj.getFreeTrialInfo());
            }
        }
        return null;
    }

    /**
     * 获取音乐数据
     *
     * @param id     音乐ID
     * @param isList 是否是空闲列表
     * @return 结果
     */
    public SongInfoObj getMusic(String id, boolean isList) {
        SongInfoObj info = getMusicDetail(id, isList);
        if (info != null)
            return info;
        JsonObject params = new JsonObject();
        params.addProperty("id", id);
        HttpResObj res = HttpClientUtil.post("https://music.163.com/api/dj/program/detail", params, EncryptType.WEAPI, null);
        if (res != null && res.ok) {
            PrInfoObj temp = MusicManager.INSTANCE.gson.fromJson(res.data, PrInfoObj.class);
            if (temp.isOk()) {
                return new SongInfoObj(temp.getAuthor(), temp.getName(),
                    temp.getId(), temp.getAlia(), "电台", isList, temp.getLength(),
                    null, false, null);
            } else {
                MusicManager.INSTANCE.warning("歌曲信息获取为空");
            }
        }
        return info;
    }

    /**
     * 获取播放链接
     *
     * @param id 音乐ID
     * @return 结果
     */
    public String getPlayUrl(String id) {
        JsonObject params = new JsonObject();
        params.addProperty("ids", "[" + id + "]");
        params.addProperty("br", 320000);
        HttpResObj res = HttpClientUtil.post("https://music.163.com/weapi/song/enhance/player/url", params, EncryptType.WEAPI, null);
        if (res != null && res.ok) {
            try {
                TrialInfoObj obj = MusicManager.INSTANCE.gson.fromJson(res.data, TrialInfoObj.class);
                return obj.getUrl();
            } catch (Exception e) {
                MusicManager.INSTANCE.warning("播放连接解析错误");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 添加空闲歌单
     *
     * @param id 歌单id
     */
    public void setList(String id) {
        final Thread thread = new Thread(() -> {
            JsonObject params = new JsonObject();
            params.addProperty("id", id);
            params.addProperty("n", 100000);
            params.addProperty("s", 8);
            HttpResObj res = HttpClientUtil.post("https://music.163.com/api/v6/playlist/detail", params, EncryptType.API, null);
            if (res != null && res.ok)
                try {
                    isUpdate = true;
                    DataObj obj = MusicManager.INSTANCE.gson.fromJson(res.data, DataObj.class);
                    //MusicManager.playList.addAll(obj.getPlaylist());
                } catch (Exception e) {
                    MusicManager.INSTANCE.warning("歌曲列表获取错误");
                    e.printStackTrace();
                }
            isUpdate = false;
        }, "MusicManager_setList");
        thread.start();
    }

    /**
     * 获取歌词
     *
     * @param id 歌曲id
     * @return 结果
     */
    public LyricSave getLyric(String id) {
        LyricSave lyric = new LyricSave();
        JsonObject params = new JsonObject();
        params.addProperty("id", id);
        params.addProperty("cp", false);
        params.addProperty("tv", 0);
        params.addProperty("lv", 0);
        params.addProperty("rv", 0);
        params.addProperty("kv", 0);
        params.addProperty("yv", 0);
        params.addProperty("ytv", 0);
        params.addProperty("rtv", 0);
        HttpResObj res = HttpClientUtil.post("https://interface3.music.163.com/eapi/song/lyric/v1",
            params, EncryptType.EAPI, "/api/song/lyric/v1");
        if (res != null && res.ok) {
            try {
                WLyricObj obj = MusicManager.INSTANCE.gson.fromJson(res.data, WLyricObj.class);
                LyricDo temp = new LyricDo();
                for (int times = 0; times < 3; times++) {
                    if (temp.check(obj)) {
                        MusicManager.INSTANCE.warning("歌词解析错误，正在进行第" + times + "重试");
                    } else {
                        if (temp.isHave) {
                            lyric.setHaveLyric(true);
                            lyric.setLyric(temp.getTemp());
                            if (temp.isHaveK) {
                                lyric.setKlyric(temp.getKLyric());
                            }
                        }
                        return lyric;
                    }
                    Thread.sleep(1000);
                }
                MusicManager.INSTANCE.warning("歌词解析失败");
            } catch (Exception e) {
                MusicManager.INSTANCE.warning("歌词解析错误");
                e.printStackTrace();
            }
        }
        return lyric;
    }

    public SearchPageObj search(String... names) {
        return search(names, true);
    }

    /**
     * 搜歌
     *
     * @param name      关键字
     * @param isDefault 是否是默认方式
     * @return 结果
     */
    public SearchPageObj search(String[] name, boolean isDefault) {
        List<SearchMusicObj> resData = new ArrayList<>();
        int maxpage;

        StringBuilder name1 = new StringBuilder();
        for (int a = isDefault ? 0 : 1; a < name.length; a++) {
            name1.append(name[a]).append(" ");
        }
        String MusicName = name1.toString();
        MusicName = MusicName.substring(0, MusicName.length() - 1);

        JsonObject params = new JsonObject();
        params.addProperty("s", MusicName);
        params.addProperty("type", 1);
        params.addProperty("limit", 30);
        params.addProperty("offset", 0);

        HttpResObj res = HttpClientUtil.post("https://music.163.com/weapi/search/get", params, EncryptType.WEAPI, null);
        if (res != null && res.ok) {
            SearchDataObj obj = MusicManager.INSTANCE.gson.fromJson(res.data, SearchDataObj.class);
            if (obj != null && obj.isOk()) {
                List<songs> res1 = obj.getResult();
                SearchMusicObj item;
                for (songs temp : res1) {
                    item = new SearchMusicObj(String.valueOf(temp.getId()), temp.getName(), temp.getArtists(), temp.getAlbum());
                    resData.add(item);
                }
                maxpage = res1.size() / 10;
                return new SearchPageObj(resData, maxpage);
            } else {
                MusicManager.INSTANCE.warning("歌曲搜索出现错误");
            }
        }
        return null;
    }
}
