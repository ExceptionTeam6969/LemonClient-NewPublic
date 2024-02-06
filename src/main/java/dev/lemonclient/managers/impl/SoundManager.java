package dev.lemonclient.managers.impl;

import dev.lemonclient.LemonClient;
import dev.lemonclient.utils.sound.SoundPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SoundManager {
    public SoundPlayer clientStartupSound;
    public SoundPlayer enableSound;
    public SoundPlayer disableSound;

    public SoundManager() {
        File clientStartupSoundFile = new File(LemonClient.SOUNDS_FOLDER, "startup.wav");
        File enableSoundFile = new File(LemonClient.SOUNDS_FOLDER, "enable.wav");
        File disableSoundFile = new File(LemonClient.SOUNDS_FOLDER, "disable.wav");

        File clientStartupSoundFolder = new File(clientStartupSoundFile.getParent());
        File enableSoundFolder = new File(enableSoundFile.getParent());
        File disableSoundFolder = new File(disableSoundFile.getParent());

        if (!clientStartupSoundFile.exists()) {
            unpackFile(clientStartupSoundFile, "startup.wav");
        }
        if (!enableSoundFile.exists()) {
            unpackFile(enableSoundFile, "enable.wav");
        }
        if (!disableSoundFile.exists()) {
            unpackFile(disableSoundFile, "disable.wav");
        }
        if (!clientStartupSoundFolder.exists()) {
            clientStartupSoundFolder.mkdir();
        }
        if (!enableSoundFolder.exists()) {
            enableSoundFolder.mkdir();
        }
        if (!disableSoundFolder.exists()) {
            disableSoundFolder.mkdir();
        }
        clientStartupSound = new SoundPlayer(clientStartupSoundFile);
        enableSound = new SoundPlayer(enableSoundFile);
        disableSound = new SoundPlayer(disableSoundFile);
    }

    public void unpackFile(File file, String name) {
        try {
            InputStream is = SoundManager.class.getResourceAsStream("/assets/lemon-client/sounds/" + name);
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            int index;
            byte[] bytes = new byte[12800];
            while ((index = is.read(bytes)) != -1) {
                os.write(bytes, 0, index);
            }
            os.flush();
            os.close();
            is.close();
        } catch (Exception e) {
            LemonClient.LOG.error("Error unpackFile: " + name);
            e.printStackTrace();
        }
    }
}
