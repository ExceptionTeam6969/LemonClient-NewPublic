package dev.lemonclient.events.render;

import dev.lemonclient.events.Cancellable;
import net.minecraft.client.render.LightmapTextureManager;

public class WeatherRenderEvent extends Cancellable {
    private static final WeatherRenderEvent INSTANCE = new WeatherRenderEvent();

    public LightmapTextureManager lightmapTextureManager;
    public float tickDelta;
    public double cameraX, cameraY, cameraZ;

    public static WeatherRenderEvent get(LightmapTextureManager lightmapTextureManager, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        INSTANCE.setCancelled(false);
        INSTANCE.lightmapTextureManager = lightmapTextureManager;
        INSTANCE.tickDelta = tickDelta;
        INSTANCE.cameraX = cameraX;
        INSTANCE.cameraY = cameraY;
        INSTANCE.cameraZ = cameraZ;
        return INSTANCE;
    }
}
