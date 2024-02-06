package dev.lemonclient.systems.modules.render;

import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.render.NametagUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class BreakESP extends Module {
    public BreakESP() {
        super(Categories.Render, "Break ESP", "Renders a box at blocks being mined by other players.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Only renders inside this range.")
        .defaultValue(10.0)
        .min(0.0)
        .sliderRange(0.0, 50.0)
        .build()
    );
    private final Setting<Double> maxTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Time")
        .description("Removes rendered box after this time.")
        .defaultValue(10.0)
        .min(0.0)
        .sliderRange(0.0, 50.0)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> renderName = sgRender.add(new BoolSetting.Builder()
        .name("Name")
        .description("Render the target's name in the box.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> nameColor = sgRender.add(new ColorSetting.Builder()
        .name("Name Text Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(renderName::get)
        .build()
    );
    private final Setting<Boolean> renderProgess = sgRender.add(new BoolSetting.Builder()
        .name("Progess")
        .description("Rendering mining progress in the box.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> breakingColor = sgRender.add(new ColorSetting.Builder()
        .name("Progress Breaking Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 0, 0))
        .visible(renderProgess::get)
        .build()
    );
    private final Setting<SettingColor> brokeColor = sgRender.add(new ColorSetting.Builder()
        .name("Progress Broke Color")
        .description(COLOR)
        .defaultValue(new SettingColor(0, 255, 0))
        .visible(renderProgess::get)
        .build()
    );
    private final Setting<Double> scale = sgRender.add(new DoubleSetting.Builder()
        .name("Scale")
        .defaultValue(1)
        .sliderRange(0.1, 2.0)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outline.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<SettingColor> friendLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Friend Line Color")
        .description("Color of the outline.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> friendSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Friend Side Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private Render render = null;
    private final List<Render> renders = new ArrayList<>();

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (render != null && contains()) {
            render = null;
        }

        renders.removeIf(r -> System.currentTimeMillis() > r.time + Math.round(maxTime.get() * 1000.0) || render != null && r.player == render.player);

        if (render != null) {
            renders.add(render);
            render = null;
        }

        renders.forEach(r -> {
            if (!PlayerUtils.isWithin(r.pos, range.get())) return;

            double delta = Math.min((System.currentTimeMillis() - r.time) / (maxTime.get() * 1000.0), 1.0);

            if (Friends.get().isFriend(r.player)) {
                event.renderer.box(getBox(r.pos, getProgress(Math.min(delta * 4.0, 1.0))), getColor(friendSideColor.get(), 1.0 - delta), getColor(friendLineColor.get(), 1.0 - delta), shapeMode.get(), 0);
            } else {
                event.renderer.box(getBox(r.pos, getProgress(Math.min(delta * 4.0, 1.0))), getColor(sideColor.get(), 1.0 - delta), getColor(lineColor.get(), 1.0 - delta), shapeMode.get(), 0);
            }
        });
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        renders.forEach(info -> {
            if (info.player != null) {
                if (!PlayerUtils.isWithin(info.pos, range.get())) return;

                Vector3d vec3 = new Vector3d(info.pos.getX() + 0.5, info.pos.getY() + 0.7, info.pos.getZ() + 0.5);
                if (NametagUtils.to2D(vec3, 1)) {
                    TextRenderer textRenderer = TextRenderer.get();

                    NametagUtils.begin(vec3);
                    textRenderer.begin(scale.get());

                    String name = info.player.getGameProfile().getName();
                    String text = !BlockUtils.solid(info.pos) ? "Broke" : "Breaking";

                    if (renderProgess.get())
                        textRenderer.render(text, -(textRenderer.getWidth(text) / 2.0), -(textRenderer.getHeight() / 2.0) + 2.0 + textRenderer.getHeight(), !BlockUtils.solid(info.pos) ? brokeColor.get() : breakingColor.get(), false);
                    if (renderName.get())
                        textRenderer.render(name, -(textRenderer.getWidth(name) / 2.0), -textRenderer.getHeight(), nameColor.get(), false);

                    textRenderer.end();
                    NametagUtils.end();
                }
            }
        });
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockBreakingProgressS2CPacket packet) {
            this.render = new Render(packet.getPos(), mc.world.getEntityById(packet.getEntityId()) == null ? null : (PlayerEntity) mc.world.getEntityById(packet.getEntityId()), System.currentTimeMillis());
        }
    }

    private boolean contains() {
        for (Render r : renders) {
            if (r.player != render.player || !r.pos.equals(render.pos)) continue;
            return true;
        }
        return false;
    }

    private Color getColor(Color color, double delta) {
        return new Color(color.r, color.g, color.b, (int) Math.floor((double) color.a * delta));
    }

    private double getProgress(double delta) {
        return 1.0 - Math.pow(1.0 - delta, 5.0);
    }

    private Box getBox(BlockPos pos, double progress) {
        return new Box((double) pos.getX() + 0.5 - progress / 2.0, (double) pos.getY() + 0.5 - progress / 2.0, (double) pos.getZ() + 0.5 - progress / 2.0, (double) pos.getX() + 0.5 + progress / 2.0, (double) pos.getY() + 0.5 + progress / 2.0, (double) pos.getZ() + 0.5 + progress / 2.0);
    }

    private record Render(BlockPos pos, PlayerEntity player, long time) {
    }
}
