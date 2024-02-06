package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.misc.Pool;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayDeque;
import java.util.Queue;

public class Breadcrumbs extends Module {
    public Breadcrumbs() {
        super(Categories.Render, "Breadcrumbs", "Displays a trail behind where you have walked.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("The color of the Breadcrumbs trail.")
        .defaultValue(new SettingColor(225, 25, 25))
        .build()
    );
    private final Setting<Double> lineWidth = sgGeneral.add(new DoubleSetting.Builder()
        .name("Line Width")
        .description(".")
        .defaultValue(1)
        .min(0)
        .sliderMax(4)
        .build()
    );
    private final Setting<Integer> maxSections = sgGeneral.add(new IntSetting.Builder()
        .name("Max Sections")
        .description("The maximum number of sections.")
        .defaultValue(1000)
        .min(1)
        .sliderRange(1, 5000)
        .build()
    );
    private final Setting<Double> sectionLength = sgGeneral.add(new DoubleSetting.Builder()
        .name("Section Length")
        .description("The section length in blocks.")
        .defaultValue(0.5)
        .min(0)
        .sliderMax(1)
        .build()
    );

    private final Pool<Section> sectionPool = new Pool<>(Section::new);
    private final Queue<Section> sections = new ArrayDeque<>();

    private Section section;

    private DimensionType lastDimension;

    @Override
    public void onActivate() {
        section = sectionPool.get();
        section.set1();

        lastDimension = mc.world.getDimension();
    }

    @Override
    public void onDeactivate() {
        for (Section section : sections) sectionPool.free(section);
        sections.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (lastDimension != mc.world.getDimension()) {
            for (Section sec : sections) sectionPool.free(sec);
            sections.clear();
        }

        if (isFarEnough(section.x1, section.y1, section.z1)) {
            section.set2();

            if (sections.size() >= maxSections.get()) {
                Section section = sections.poll();
                if (section != null) sectionPool.free(section);
            }

            sections.add(section);
            section = sectionPool.get();
            section.set1();
        }

        lastDimension = mc.world.getDimension();
    }

    @EventHandler
    private void onRender(Render3DEvent.Unlimited event) {
        Renderer3D renderer = new Renderer3D();
        renderer.begin();

        int iLast = -1;

        for (Section section : sections) {
            if (iLast == -1) {
                iLast = renderer.lines.vec3(section.x1, section.y1, section.z1).color(color.get()).next();
            }

            int i = renderer.lines.vec3(section.x2, section.y2, section.z2).color(color.get()).next();
            renderer.lines.line(iLast, i);
            iLast = i;
        }

        renderer.render(event.matrices, lineWidth.get().floatValue());
    }

    private boolean isFarEnough(double x, double y, double z) {
        return Math.abs(mc.player.getX() - x) >= sectionLength.get() || Math.abs(mc.player.getY() - y) >= sectionLength.get() || Math.abs(mc.player.getZ() - z) >= sectionLength.get();
    }

    private class Section {
        public float x1, y1, z1;
        public float x2, y2, z2;

        public void set1() {
            x1 = (float) mc.player.getX();
            y1 = (float) mc.player.getY();
            z1 = (float) mc.player.getZ();
        }

        public void set2() {
            x2 = (float) mc.player.getX();
            y2 = (float) mc.player.getY();
            z2 = (float) mc.player.getZ();
        }

        public void render(Render3DEvent event) {
            event.renderer.line(x1, y1, z1, x2, y2, z2, color.get());
        }
    }
}
