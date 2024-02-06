package dev.lemonclient.asm;

import dev.lemonclient.events.render.GetFovEvent;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.render.postprocess.PostProcessShaders;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings("rawtypes")
public class AsmHelper {
    public AsmHelper() {
    }

    @AsmInvoke
    public boolean isModuleActive(Class<? extends Module> klass) {
        return Modules.get().isActive(klass);
    }

    @AsmInvoke
    public static void endRenderPost() {
        PostProcessShaders.endRender();
    }

    @AsmInvoke
    public static GetFovEvent get(double fov) {
        return GetFovEvent.get(fov);
    }

    @AsmInvoke
    public static Field findField(Class klass, Class type) {
        return Arrays.stream(klass.getDeclaredFields()).filter((f) -> f.getType().equals(type)).findFirst().orElse(null);
    }

    @AsmInvoke
    public static Method findMethod(Class klass, Class... types) {
        return Arrays.stream(klass.getDeclaredMethods()).filter((f) -> Arrays.equals(f.getParameterTypes(), types)).findFirst().orElse(null);
    }

    @AsmInvoke
    public static String getClassPath(Class klass) {
        return klass.getName().replaceAll("\\.", "/");
    }
}
