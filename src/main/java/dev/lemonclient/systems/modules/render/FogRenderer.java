package dev.lemonclient.systems.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.client.render.FogShape;

public class FogRenderer extends Module {
    public FogRenderer() {
        super(Categories.Render, "Fog Renderer", "Customizable fog.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<FogShape> shape = sgGeneral.add(new EnumSetting.Builder<FogShape>()
        .name("Shape")
        .description("Fog shape.")
        .defaultValue(FogShape.SPHERE)
        .build()
    );
    public final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("Distance")
        .description("How far away should the fog start rendering.")
        .defaultValue(25)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    public final Setting<Integer> fading = sgGeneral.add(new IntSetting.Builder()
        .name("Fading")
        .description("How smoothly should the fog fade.")
        .defaultValue(25)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );
    public final Setting<Double> thickness = sgGeneral.add(new DoubleSetting.Builder()
        .name("Thickness")
        .description(".")
        .defaultValue(10)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("Color of the fog.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    public void modifyFog() {
        RenderSystem.setShaderFogColor(color.get().r, color.get().g, color.get().b, color.get().a / (float) ((100 - thickness.get()) * 2.55f));
        RenderSystem.setShaderFogStart((float) (distance.get() * 1f));
        RenderSystem.setShaderFogEnd((float) (distance.get() + fading.get()));
        RenderSystem.setShaderFogShape(shape.get());
    }
}
