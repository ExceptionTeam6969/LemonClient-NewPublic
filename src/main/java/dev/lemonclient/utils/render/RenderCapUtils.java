package dev.lemonclient.utils.render;

import org.lwjgl.opengl.GL32C;

import java.util.HashMap;
import java.util.Map;

public class RenderCapUtils {
    private static final Map<Integer, String> caps = new HashMap<>();

    public static void enable(int cap) {
        enable("default", cap);
    }

    public static void reset() {
        reset("default");
    }

    public static void disable(int cap) {
        disable("default", cap);
    }

    public static void enable(String locate, int cap) {
        if (caps.containsKey(cap) && caps.get(cap).equalsIgnoreCase(locate)) {
            caps.remove(cap);
        }
        caps.put(cap, locate);
        GL32C.glEnable(cap);
    }

    public static void reset(String locate) {
        caps.forEach((cap, key) -> {
            if (key.equalsIgnoreCase(locate)) {
                GL32C.glDisable(cap);
            }
        });
    }

    public static void disable(String locate, int cap) {
        if (caps.containsKey(cap) && caps.get(cap).equalsIgnoreCase(locate)) {
            caps.remove(cap);
        }

        GL32C.glDisable(cap);
    }
}
