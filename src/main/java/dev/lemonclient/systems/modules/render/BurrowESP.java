package dev.lemonclient.systems.modules.render;

import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.entity.EntityInfo;
import dev.lemonclient.utils.entity.SortPriority;
import dev.lemonclient.utils.entity.TargetUtils;
import dev.lemonclient.utils.render.NametagUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

public class BurrowESP extends Module {
    public BurrowESP() {
        super(Categories.Render, "Burrow Esp", "Displays if the closest target to you is burrowed / webbed.");
    }

    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgText = settings.createGroup("Text");

    //--------------------Render--------------------//
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The side color of the rendering.")
        .defaultValue(new SettingColor(230, 0, 255, 5))
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("The line color of the rendering.")
        .defaultValue(new SettingColor(250, 0, 255, 255))
        .visible(() -> shapeMode.get() != ShapeMode.Sides)
        .build()
    );

    private final Setting<Boolean> renderWebbed = sgRender.add(new BoolSetting.Builder()
        .name("Render Webbed")
        .description("Will render if the target is webbed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> webSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Web Side Color")
        .description("The side color of the rendering for webs.")
        .defaultValue(new SettingColor(240, 250, 65, 35))
        .visible(() -> shapeMode.get() != ShapeMode.Lines && renderWebbed.get())
        .build()
    );

    private final Setting<SettingColor> webLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Web Line Color")
        .description("The line color of the rendering for webs.")
        .defaultValue(new SettingColor(0, 0, 0, 0))
        .visible(() -> shapeMode.get() != ShapeMode.Sides && renderWebbed.get())
        .build()
    );

    //--------------------Text--------------------//
    private final Setting<Boolean> text = sgText.add(new BoolSetting.Builder()
        .name("Text")
        .description("Renders mine progress text in the block overlay.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> textScale = sgText.add(new DoubleSetting.Builder()
        .name("Text Scale")
        .description("How big the progress text should be.")
        .defaultValue(1.25)
        .min(0.1)
        .sliderMax(4)
        .visible(text::get)
        .build()
    );
    private final Setting<SettingColor> textColor = sgText.add(new ColorSetting.Builder()
        .name("Text Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(text::get)
        .build()
    );
    private final Setting<Boolean> shadow = sgText.add(new BoolSetting.Builder()
        .name("Shadow")
        .description("Do text shadow render.")
        .defaultValue(true)
        .visible(text::get)
        .build()
    );

    public BlockPos target;
    public boolean isTargetWebbed;
    public boolean isTargetBurrowed;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        PlayerEntity targetEntity = TargetUtils.getPlayerTarget(mc.interactionManager.getReachDistance() + 2, SortPriority.LowestDistance);

        if (TargetUtils.isBadTarget(targetEntity, mc.interactionManager.getReachDistance() + 2)) {
            target = null;
        } else if (renderWebbed.get() && EntityInfo.isWebbed(targetEntity)) {
            target = targetEntity.getBlockPos();
        } else if (EntityInfo.isBurrowed(targetEntity, EntityInfo.BlastResistantType.Any)) {
            target = targetEntity.getBlockPos();
        } else {
            target = null;
        }

        isTargetWebbed = (target != null && EntityInfo.isWebbed(targetEntity));
        isTargetBurrowed = (target != null && EntityInfo.isBurrowed(targetEntity));
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (target == null) return;

        if (isTargetWebbed) {
            event.renderer.box(target, webSideColor.get(), webLineColor.get(), shapeMode.get(), 0);
        } else if (isTargetBurrowed) {
            event.renderer.box(target, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!text.get() || target == null) return;

        String text = null;
        if (isTargetWebbed) text = "Webbed";
        else if (isTargetBurrowed) text = "Burrowed";
        if (text == null) return;

        Vector3d vec3 = new Vector3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        if (NametagUtils.to2D(vec3, 1)) {
            TextRenderer textRenderer = TextRenderer.get();

            NametagUtils.begin(vec3);
            textRenderer.begin(textScale.get());

            textRenderer.render(
                text,
                -textRenderer.getWidth(text) / 2.0,
                0.0,
                textColor.get(),
                shadow.get()
            );

            textRenderer.end();
            NametagUtils.end();
        }
    }
}
