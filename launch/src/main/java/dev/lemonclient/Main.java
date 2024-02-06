/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package dev.lemonclient;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        int option = JOptionPane.showOptionDialog(
                null,
                "要安装 Lemon Client，您需要将其放入 mods 文件夹中并运行 Fabric 以获取最新的 Minecraft 版本",
                "Lemon Client",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[] { "进入交流群", "打开 Mods 文件夹" },
                null
        );

        switch (option) {
            case 0: getOS().open("http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=0E-64rYBrEdThCsbmmZfuXZHowVpNoU5&authKey=jMwSBqXbJQsL1RPzqo2gwk5aZJkqDPnRuHFOxA3ECdiYz%2FiC%2BWG5%2FAoddk7B76Cb&noverify=0&group_code=893811510"); break;
            case 1: {
                String path;

                switch (getOS()) {
                    case WINDOWS: path = System.getenv("AppData") + "/.minecraft/mods"; break;
                    case OSX:     path = System.getProperty("user.home") + "/Library/Application Support/minecraft/mods"; break;
                    default:      path = System.getProperty("user.home") + "/.minecraft"; break;
                }

                File mods = new File(path);
                if (!mods.exists()) mods.mkdirs();

                getOS().open(mods);
                break;
            }
        }
    }

    private static OperatingSystem getOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("linux") || os.contains("unix"))  return OperatingSystem.LINUX;
        if (os.contains("mac")) return OperatingSystem.OSX;
        if (os.contains("win")) return OperatingSystem.WINDOWS;

        return OperatingSystem.UNKNOWN;
    }

    private enum OperatingSystem {
        LINUX,
        WINDOWS {
            @Override
            protected String[] getURLOpenCommand(URL url) {
                return new String[] { "rundll32", "url.dll,FileProtocolHandler", url.toString() };
            }
        },
        OSX {
            @Override
            protected String[] getURLOpenCommand(URL url) {
                return new String[] { "open", url.toString() };
            }
        },
        UNKNOWN;

        public void open(URL url) {
            try {
                Runtime.getRuntime().exec(getURLOpenCommand(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void open(String url) {
            try {
                open(new URL(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        public void open(File file) {
            try {
                open(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        protected String[] getURLOpenCommand(URL url) {
            String string = url.toString();

            if ("file".equals(url.getProtocol())) {
                string = string.replace("file:", "file://");
            }

            return new String[] { "xdg-open", string };
        }
    }
}
