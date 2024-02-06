package dev.lemonclient.music;

import net.minecraft.client.MinecraftClient;

import java.util.HashSet;

public class AsyncLoopTask extends Thread {
    public final HashSet<Runnable> runnableSet = new HashSet<>();

    public void submit(Runnable task) {
        runnableSet.add(task);
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        while (MinecraftClient.getInstance().isRunning()) {
            try {
                synchronized (runnableSet) {
                    if (runnableSet.isEmpty()) {
                        Thread.sleep(1000L);
                        continue;
                    }
                    Runnable runnable = runnableSet.stream().findAny().get();
                    runnableSet.remove(runnable);
                    runnable.run();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
