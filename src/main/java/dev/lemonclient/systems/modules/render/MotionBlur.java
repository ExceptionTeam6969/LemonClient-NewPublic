package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.render.ShaderEffectRenderEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class MotionBlur extends Module {
    public MotionBlur() {
        super(Categories.Render, "Motion Blur", "Make your game blurry when you move the camera.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> blurAmount = sgGeneral.add(new DoubleSetting.Builder()
        .name("Blur Amount")
        .description(".")
        .defaultValue(20)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    @EventHandler
    private void onRender(ShaderEffectRenderEvent event) {
        Managers.SHADER.renderMotionBlur(Math.min(blurAmount.get().floatValue(), 99) / 100F);
    }
}
