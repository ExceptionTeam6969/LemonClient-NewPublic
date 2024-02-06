package dev.lemonclient.systems.modules.render;

import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import net.minecraft.world.LightType;

public class Fullbright extends Module {
    public Fullbright() {
        super(Categories.Render, "Fullbright", "Lights up your world!");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("The mode to use for Fullbright.")
        .defaultValue(Mode.Gamma)
        .onChanged(mode -> {
            if (mc.worldRenderer != null && isActive()) mc.worldRenderer.reload();
        })
        .build()
    );

    public final Setting<LightType> lightType = sgGeneral.add(new EnumSetting.Builder<LightType>()
        .name("light-type")
        .description("Which type of light to use for Luminance mode.")
        .defaultValue(LightType.BLOCK)
        .visible(() -> mode.get() == Mode.Luminance)
        .onChanged(integer -> {
            if (mc.worldRenderer != null && isActive()) mc.worldRenderer.reload();
        })
        .build()
    );

    private final Setting<Integer> minimumLightLevel = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-light-level")
        .description("Minimum light level when using Luminance mode.")
        .visible(() -> mode.get() == Mode.Luminance)
        .defaultValue(8)
        .range(0, 15)
        .sliderMax(15)
        .onChanged(integer -> {
            if (mc.worldRenderer != null && isActive()) mc.worldRenderer.reload();
        })
        .build()
    );

    @Override
    public void onActivate() {
        if (mode.get() == Mode.Luminance) mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        if (mode.get() == Mode.Luminance) mc.worldRenderer.reload();
    }

    public int getLuminance(LightType type) {
        if (!isActive() || mode.get() != Mode.Luminance || type != lightType.get()) return 0;
        return minimumLightLevel.get();
    }

    public boolean getGamma() {
        return isActive() && mode.get() == Mode.Gamma;
    }

    public enum Mode {
        Gamma,
        Luminance
    }
}
