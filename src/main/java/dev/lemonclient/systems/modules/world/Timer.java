package dev.lemonclient.systems.modules.world;

import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class Timer extends Module {
    public Timer() {
        super(Categories.World, "Timer", "Changes the speed of everything in your game.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> multiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("Multiplier")
        .description("The timer multiplier amount.")
        .defaultValue(1)
        .min(0.1)
        .sliderMin(0.1)
        .build()
    );

    public static final double OFF = 1;
    private double override = 1;

    public double getMultiplier() {
        return override != OFF ? override : (isActive() ? multiplier.get() : OFF);
    }

    public void setOverride(double override) {
        this.override = override;
    }
}
