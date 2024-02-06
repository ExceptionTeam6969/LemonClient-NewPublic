package dev.lemonclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.commands.Command;
import dev.lemonclient.lemonchat.utils.GsonUtils;
import net.minecraft.command.CommandSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static dev.lemonclient.utils.esu.Infos.*;

public class OpenBoxCommand extends Command {
    public OpenBoxCommand() {
        super("open-box", "lol", "openbox", "cr");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("lol").then(argument("number", StringArgumentType.string()).executes(context -> {
            Runnable run = () -> {
                String qq;
                StringBuilder stringBuilder;
                String inputLine;
                URL url;
                BufferedReader in;
                LOLInfo info;

                qq = StringArgumentType.getString(context, "number");
                stringBuilder = new StringBuilder();
                try {
                    url = new URL("https://zy.xywlapi.cc/qqlol?qq=" + qq);
                    in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                } catch (Exception exception) {
                    error(exception.getMessage());
                }

                info(stringBuilder.toString());
                info = GsonUtils.jsonToBean(stringBuilder.toString(), LOLInfo.class);
                if (info != null) {
                    info("返回状态: " + info.getStatus());
                    info("返回消息: " + info.getMessage());
                    info("QQ: " + qq);
                    info("手机号: " + info.getName());
                    info("地区: " + info.getDaqu());
                }
            };
            new Thread(run).start();
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("lolfc").then(argument("name", StringArgumentType.string()).executes(context -> {
            Runnable run = () -> {
                String qName;
                StringBuilder stringBuilder;
                String inputLine;
                URL url;
                BufferedReader in;
                LOLInfo info;

                qName = StringArgumentType.getString(context, "name");
                stringBuilder = new StringBuilder();
                try {
                    url = new URL("https://zy.xywlapi.cc/lolname?name=" + qName);
                    in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                } catch (Exception exception) {
                    error(exception.getMessage());
                }

                info(stringBuilder.toString());
                info = GsonUtils.jsonToBean(stringBuilder.toString(), LOLInfo.class);
                if (info != null) {
                    info("返回状态: " + info.getStatus());
                    info("返回消息: " + info.getMessage());
                    info("QQ: " + info.getQq());
                    info("手机号: " + info.getName());
                    info("大区: " + info.getDaqu());
                }
            };
            new Thread(run).start();
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("qq").then(argument("qq-number", StringArgumentType.string()).executes(context -> {
            Runnable run = () -> {
                String sfzNum;
                StringBuilder stringBuilder;
                String inputLine;
                URL url;
                BufferedReader in;
                QQInfo info;

                sfzNum = StringArgumentType.getString(context, "qq-number");
                stringBuilder = new StringBuilder();
                try {
                    url = new URL("https://zy.xywlapi.cc/qqapi?qq=" + sfzNum);
                    in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                } catch (Exception exception) {
                    error(exception.getMessage());
                }

                info(stringBuilder.toString());
                info = GsonUtils.jsonToBean(stringBuilder.toString(), QQInfo.class);
                if (info != null) {
                    info("返回状态: " + info.getStatus());
                    info("返回消息: " + info.getMessage());
                    info("QQ: " + info.getQq());
                    info("手机号: " + info.getPhone());
                    info("地区: " + info.getPhonediqu());
                }
            };
            new Thread(run).start();
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("qqfc").then(argument("qq-phone", StringArgumentType.string()).executes(context -> {
            Runnable run = () -> {
                String sfzNum;
                StringBuilder stringBuilder;
                String inputLine;
                URL url;
                BufferedReader in;
                QQInfo info;

                sfzNum = StringArgumentType.getString(context, "qq-phone");
                stringBuilder = new StringBuilder();
                try {
                    url = new URL("https://zy.xywlapi.cc/qqphone?phone=" + sfzNum);
                    in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                } catch (Exception exception) {
                    error(exception.getMessage());
                }

                info(stringBuilder.toString());
                info = GsonUtils.jsonToBean(stringBuilder.toString(), QQInfo.class);
                if (info != null) {
                    info("返回状态: " + info.getStatus());
                    info("返回消息: " + info.getMessage());
                    info("QQ: " + info.getQq());
                    info("手机号: " + info.getPhone());
                    info("地区: " + info.getPhonediqu());
                }
            };
            new Thread(run).start();
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("qqlm").then(argument("qq-lm", StringArgumentType.string()).executes(context -> {
            Runnable run = () -> {
                String sfzNum;
                StringBuilder stringBuilder;
                String inputLine;
                URL url;
                BufferedReader in;
                QQlmInfo info;

                sfzNum = StringArgumentType.getString(context, "qq-lm");
                stringBuilder = new StringBuilder();
                try {
                    url = new URL("https://zy.xywlapi.cc/qqlm?qq=" + sfzNum);
                    in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                } catch (Exception exception) {
                    error(exception.getMessage());
                }

                info(stringBuilder.toString());
                info = GsonUtils.jsonToBean(stringBuilder.toString(), QQlmInfo.class);
                if (info != null) {
                    info("返回状态: " + info.getStatus());
                    info("返回消息: " + info.getMessage());
                    info("QQ: " + info.getQq());
                    info("QQ老密: " + info.getQqlm());
                }
            };
            new Thread(run).start();
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("wb").then(argument("wb-id", StringArgumentType.string()).executes(context -> {
            Runnable run = () -> {
                String sfzNum;
                StringBuilder stringBuilder;
                String inputLine;
                URL url;
                BufferedReader in;
                WBInfo info;

                sfzNum = StringArgumentType.getString(context, "wb-id");
                stringBuilder = new StringBuilder();
                try {
                    url = new URL("https://zy.xywlapi.cc/wbapi?id=" + sfzNum);
                    assert url != null;
                    in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                } catch (Exception exception) {
                    error(exception.getMessage());
                }

                info(stringBuilder.toString());
                info = GsonUtils.jsonToBean(stringBuilder.toString(), WBInfo.class);
                if (info != null) {
                    info("返回状态: " + info.getStatus());
                    info("返回消息: " + info.getMessage());
                    info("ID: " + info.getID());
                    info("手机号: " + info.getPhone());
                    info("地区: " + info.getPhonediqu());
                }
            };
            new Thread(run).start();
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("wbfc").then(argument("wb-phone", StringArgumentType.string()).executes(context -> {
            Runnable run = () -> {
                String sfzNum;
                StringBuilder stringBuilder;
                String inputLine;
                URL url;
                BufferedReader in;
                WBInfo info;

                sfzNum = StringArgumentType.getString(context, "wb-phone");
                stringBuilder = new StringBuilder();
                try {
                    url = new URL("https://zy.xywlapi.cc/wbphone?phone=" + sfzNum);
                    in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                } catch (Exception exception) {
                    error(exception.getMessage());
                }

                info(stringBuilder.toString());
                info = GsonUtils.jsonToBean(stringBuilder.toString(), WBInfo.class);
                if (info != null) {
                    info("返回状态: " + info.getStatus());
                    info("返回消息: " + info.getMessage());
                    info("ID: " + info.getID());
                    info("手机号: " + info.getPhone());
                    info("地区: " + info.getPhonediqu());
                }
            };
            new Thread(run).start();
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("sfz").then(argument("sfz-hm", StringArgumentType.string()).executes(context -> {
            Runnable run = () -> {
                String sfzNum;
                BufferedReader in;
                SfzInfo info;
                try {
                    sfzNum = StringArgumentType.getString(context, "sfz-hm");
                    URL url = new URL("http://api.k780.com/?app=idcard.get&idcard=" + sfzNum + "&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json");
                    StringBuilder stringBuilder = new StringBuilder();
                    assert url != null;
                    in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }

                    info(stringBuilder.toString());
                    info(stringBuilder.toString());
                    info = GsonUtils.jsonToBean(stringBuilder.toString(), SfzInfo.class);
                    if (info != null) {
                        info("success: " + info.getSuccess());
                        info("status: " + info.getResult().getStatus());
                        info("idcard: " + info.getResult().getIdcard());
                        info("par: " + info.getResult().getPar());
                        info("born: " + info.getResult().getBorn());
                        info("sex: " + info.getResult().getSex());
                        info("att: " + info.getResult().getAtt());
                        info("postno: " + info.getResult().getPostno());
                        info("areano: " + info.getResult().getAreano());
                        info("style_simcall: " + info.getResult().getStyle_simcall());
                        info("style_citynm: " + info.getResult().getStyle_citynm());
                        info("msg: " + info.getResult().getMsg());
                    }
                } catch (IOException e) {
                    error(e.getMessage());
                }
            };
            new Thread(run).start();
            return SINGLE_SUCCESS;
        })));
    }
}
