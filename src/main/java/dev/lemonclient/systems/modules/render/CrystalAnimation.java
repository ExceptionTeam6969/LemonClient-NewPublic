package dev.lemonclient.systems.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.ColorMode;
import dev.lemonclient.utils.render.Render3DUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.TimerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalAnimation extends Module {
    public CrystalAnimation() {
        super(Categories.Render, "Crystal Animation", "Renders a circle below the placed crystal.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgColor = settings.createGroup("Color");

    //--------------------General--------------------//
    private final Setting<Boolean> range = sgGeneral.add(new BoolSetting.Builder()
        .name("Check Range")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> rangeValue = sgGeneral.add(new IntSetting.Builder()
        .name("Render Range")
        .defaultValue(12)
        .sliderRange(0, 256)
        .visible(range::get)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Double> lineWidth = sgRender.add(new DoubleSetting.Builder()
        .name("Line Width")
        .defaultValue(2)
        .sliderRange(1, 4)
        .build()
    );
    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Color")
        .defaultValue(new SettingColor(255, 255, 255, 150))
        .build()
    );
    private final Setting<Integer> animationTime = sgRender.add(new IntSetting.Builder()
        .name("Animation Time")
        .defaultValue(500)
        .sliderRange(0, 1500)
        .build()
    );
    private final Setting<Double> fadeSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Speed")
        .defaultValue(500.0)
        .sliderRange(0.0, 1500.0)
        .build()
    );
    private final Setting<Mode> mode = sgRender.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .defaultValue(Mode.Normal)
        .build()
    );
    private final Setting<Integer> pointsNew = sgRender.add(new IntSetting.Builder()
        .name("points")
        .defaultValue(10)
        .sliderRange(1, 100)
        .visible(() -> mode.get().equals(Mode.New))
        .build()
    );
    private final Setting<Integer> pointsColorful = sgRender.add(new IntSetting.Builder()
        .name("Points")
        .defaultValue(3)
        .sliderRange(1, 10)
        .visible(() -> mode.get().equals(Mode.ColorfulNew))
        .build()
    );
    private final Setting<Integer> interval = sgRender.add(new IntSetting.Builder()
        .name("Interval")
        .defaultValue(2)
        .sliderRange(1, 100)
        .visible(() -> !mode.get().equals(Mode.Normal))
        .build()
    );

    //--------------------Color--------------------//
    private final ColorMode.ColorSettings colorSettings = ColorMode.ColorSettings.create(sgColor);

    private final ConcurrentHashMap<EndCrystalEntity, RenderInfo> cryList = new ConcurrentHashMap<>();
    private final TimerUtils timer = new TimerUtils();

    public enum Mode {
        Normal,
        New,
        Colorful,
        ColorfulNew
    }

    @EventHandler
    private void onWorldRender(Render3DEvent.Unlimited event) {
        Renderer3D renderer = new Renderer3D();
        renderer.begin();

        for (Entity e : new Iterable<Entity>() {
            @Override
            public Iterator<Entity> iterator() {
                return mc.world.getEntities().iterator();
            }
        }) {
            if (!(e instanceof EndCrystalEntity)) continue;
            if (range.get() && mc.player.distanceTo(e) > rangeValue.get()) continue;
            if (!cryList.containsKey(e)) {
                cryList.put((EndCrystalEntity) e, new RenderInfo((EndCrystalEntity) e, System.currentTimeMillis()));
            }
        }

        switch (mode.get()) {
            case Normal ->
                cryList.forEach((e, renderInfo) -> draw(renderer, renderInfo.entity, renderInfo.time, renderInfo.time, event.tickDelta));
            case New -> {
                var time = 0;
                for (int i = 0; i < pointsNew.get(); i++) {
                    if (timer.passedMs(500)) {
                        int finalTime = time;
                        cryList.forEach((e, renderInfo) ->
                            draw(renderer, renderInfo.entity, renderInfo.time - finalTime, renderInfo.time - finalTime, event.tickDelta)
                        );
                    }
                    time += interval.get();
                }
            }
            case Colorful -> cryList.forEach((e, renderInfo) -> {
                var rad = System.currentTimeMillis() - renderInfo.time;
                var height = System.currentTimeMillis() - renderInfo.time;
                if (rad <= animationTime.get()) {
                    drawCircleColorful(event.matrices, renderInfo.entity, rad / fadeSpeed.get(), height / 1000F, event.tickDelta);
                }
            });
            case ColorfulNew -> {
                var time = 0;
                for (int i = 0; i < pointsColorful.get(); i++) {
                    if (timer.passedMs(500)) {
                        int finalTime = time;
                        cryList.forEach((e, renderInfo) -> {
                            var rad = System.currentTimeMillis() - renderInfo.time - finalTime;
                            var height = System.currentTimeMillis() - renderInfo.time - finalTime;
                            if (rad <= animationTime.get()) {
                                drawCircleColorful(event.matrices, renderInfo.entity, rad / fadeSpeed.get(), height / 1000F, event.tickDelta);
                            }
                        });
                    }
                    time += interval.get();
                }
            }
        }

        cryList.forEach((e, renderInfo) -> {
            if (((System.currentTimeMillis() - renderInfo.time) > animationTime.get()) && !e.isAlive()) {
                cryList.remove(e);
            }
            if (((System.currentTimeMillis() - renderInfo.time) > animationTime.get()) && mc.player.distanceTo(e) > rangeValue.get()) {
                cryList.remove(e);
            }
        });

        renderer.render(event.matrices, lineWidth.get().floatValue());
    }

    private void draw(Renderer3D renderer, EndCrystalEntity entity, long radTime, long heightTime, float tickDelta) {
        var rad = System.currentTimeMillis() - radTime;
        var height = System.currentTimeMillis() - heightTime;
        if (rad <= animationTime.get()) {
            drawCircle(renderer, entity, rad / fadeSpeed.get(), height / 1000F, color.get(), tickDelta);
        }
    }

    private void drawCircle(Renderer3D renderer, EndCrystalEntity entity, double rad, float height, Color color, float tickDelta) {
        var x = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * tickDelta;
        var y = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * tickDelta;
        var z = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * tickDelta;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(lineWidth.get().floatValue());
        for (int i = 5; i <= 360; i++) {
            double MPI = Math.PI;
            double x0 = x - Math.sin((double) i * MPI / (double) 180.0F) * rad;
            double z0 = z + Math.cos((double) i * MPI / (double) 180.0F) * rad;
            double x00 = x - Math.sin((double) (i - 5) * MPI / (double) 180.0F) * rad;
            double z00 = z + Math.cos((double) (i - 5) * MPI / (double) 180.0F) * rad;

            renderer.line(x0, y + height, z0, x00, y + height, z00, color);
        }

        RenderSystem.disableBlend();
    }

    private void drawCircleColorful(MatrixStack matrices, EndCrystalEntity entity, double rad, float height, float tickDelta) {
        var x = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * tickDelta;
        var y = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * tickDelta;
        var z = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * tickDelta;

        Render3DUtils.drawGlowCircle(matrices, x, y + height, z, (float) rad, this.colorSettings);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        colorSettings.tick();
    }

    @Override
    public void onDeactivate() {
        cryList.clear();
    }

    record RenderInfo(EndCrystalEntity entity, long time) {
    }
}
