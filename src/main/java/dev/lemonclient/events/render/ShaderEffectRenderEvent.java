package dev.lemonclient.events.render;

public class ShaderEffectRenderEvent {
    private static final ShaderEffectRenderEvent INSTANCE = new ShaderEffectRenderEvent();

    public float tickDelta;

    public static ShaderEffectRenderEvent get(float tickDelta) {
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
