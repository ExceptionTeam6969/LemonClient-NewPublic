package dev.lemonclient.systems.modules.render;

import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.render.WireframeEntityRenderer;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class CrystalChams extends Module {
    public CrystalChams() {
        super(Categories.Render, "Crystal Chams", "Renders end crystal.");
    }

    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgColor = settings.createGroup("Color");

    //--------------------Render--------------------//
    public final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<Double> renderDistance = sgRender.add(new DoubleSetting.Builder()
        .name("Render Distance")
        .description(".")
        .defaultValue(12)
        .min(0)
        .sliderMax(256)
        .build()
    );
    private final Setting<Double> fadeDistance = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Distance")
        .description("The distance from an entity where the color begins to fade.")
        .defaultValue(3)
        .min(0)
        .sliderMax(12)
        .build()
    );
    public final Setting<Double> fillOpacity = sgRender.add(new DoubleSetting.Builder()
        .name("Fill Opacity")
        .description("The opacity of the shape fill.")
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .defaultValue(0.3)
        .range(0, 1)
        .sliderMax(1)
        .build()
    );

    //--------------------Color--------------------//
    public final Setting<Boolean> distance = sgColor.add(new BoolSetting.Builder()
        .name("Distance Colors")
        .description("Changes the color of tracers depending on distance.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SettingColor> color = sgColor.add(new ColorSetting.Builder()
        .name("Color")
        .description("The color.")
        .defaultValue(new SettingColor(175, 175, 175, 255))
        .visible(() -> !distance.get())
        .build()
    );

    private final Color lineColor = new Color();
    private final Color sideColor = new Color();
    private final Color baseColor = new Color();

    private int count;

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        count = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (shouldSkip(entity)) continue;

            Color color = getColor(entity);
            if (color != null) {
                lineColor.set(color);
                sideColor.set(color).a((int) (sideColor.a * fillOpacity.get()));
            }

            WireframeEntityRenderer.render(event, entity, 1, sideColor, lineColor, shapeMode.get());
            count++;
        }
    }

    public boolean shouldSkip(Entity entity) {
        if (!entity.getType().equals(EntityType.END_CRYSTAL)) return true;
        if (entity == mc.cameraEntity && mc.options.getPerspective().isFirstPerson()) return true;
        if (PlayerUtils.distanceTo(entity) > renderDistance.get()) return true;
        return !EntityUtils.isInRenderDistance(entity);
    }

    public Color getColor(Entity entity) {
        if (!entity.getType().equals(EntityType.END_CRYSTAL)) return null;

        double alpha = getFadeAlpha(entity);
        if (alpha == 0) return null;

        Color color = getEntityTypeColor(entity);
        return baseColor.set(color.r, color.g, color.b, (int) (color.a * alpha));
    }

    private double getFadeAlpha(Entity entity) {
        double dist = PlayerUtils.squaredDistanceToCamera(entity.getX() + entity.getWidth() / 2, entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ() + entity.getWidth() / 2);
        double fadeDist = Math.pow(fadeDistance.get(), 2);
        double alpha = 1;
        if (dist <= fadeDist * fadeDist) alpha = (float) (Math.sqrt(dist) / fadeDist);
        if (alpha <= 0.075) alpha = 0;
        return alpha;
    }

    public Color getEntityTypeColor(Entity entity) {
        if (distance.get()) {
            return EntityUtils.getColorFromDistance(entity);
        } else {
            return color.get();
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }
}
