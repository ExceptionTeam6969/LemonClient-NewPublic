package dev.lemonclient.music;

import net.minecraft.client.MinecraftClient;

public class TickingTask extends Thread {
    public final Runnable tickTask;

    public TickingTask(Runnable tickTask) {
        this.tickTask = tickTask;
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        while (MinecraftClient.getInstance().isRunning()) {
            try {
                tickTask.run();
                Thread.sleep(50L);
            } catch (Exception ignored) {
            }
        }
    }
}
