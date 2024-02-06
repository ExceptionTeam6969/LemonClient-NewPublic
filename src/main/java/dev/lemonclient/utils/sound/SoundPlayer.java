package dev.lemonclient.utils.sound;

import dev.lemonclient.LemonClient;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class SoundPlayer {
    private final File file;

    public SoundPlayer(File sound) {
        this.file = sound;
    }

    public void play(float volume) {
        new Thread(() -> playSound(volume)).start();
    }

    private void playSound(float volume) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(this.file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);

            clip.start();
        } catch (Exception e) {
            LemonClient.LOG.error("Error with playing sound.");
            e.printStackTrace();
        }
    }
}
