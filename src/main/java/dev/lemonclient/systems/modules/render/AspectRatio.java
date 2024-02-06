package dev.lemonclient.systems.modules.render;

import dev.lemonclient.clion.animations.Easing;
import dev.lemonclient.clion.animations.plus.EasingSingle;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class AspectRatio extends Module {
    public AspectRatio() {
        super(Categories.Render, "Aspect Ratio", "Pull your minecraft screen.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> smooth = boolSetting(sgGeneral, "Smooth", true);
    private final Setting<Double> ratio = doubleSetting(sgGeneral, "Ratio", 1.78f, 0.1, 8.0);

    private final EasingSingle easingSingle = new EasingSingle(1.78f, 1500f, Easing.OUT_SINE);

    @Override
    public void onDeactivate() {
        easingSingle.forceUpdatePos(1.78f);
    }

    public float getSettingRatio() {
        if (smooth.get()) {
            easingSingle.updatePos(ratio.get().floatValue());
            return easingSingle.getUpdate();
        }
        return ratio.get().floatValue();
    }
}
