package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.fakeplayer.FakePlayerEntity;
import dev.lemonclient.utils.misc.Keybind;
import dev.lemonclient.utils.misc.Pool;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Vector3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Blink extends Module {
    public Blink() {
        super(Categories.Movement, "Blink", "Allows you to essentially teleport while suspending motion updates.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Keybind> cancelBlink = sgGeneral.add(new KeybindSetting.Builder()
        .name("Cancel Blink")
        .description("Cancels sending packets and sends you back to your original position.")
        .defaultValue(Keybind.none())
        .action(() -> {
            cancelled = true;
            if (isActive()) toggle();
        })
        .build()
    );

    private final Setting<Boolean> renderOriginal = sgRender.add(new BoolSetting.Builder()
        .name("Render Original")
        .description("Renders your player model at the original position.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> breadcrumbs = sgRender.add(new BoolSetting.Builder()
        .name("Breadcrumbs")
        .description("Displays a trail behind where you have walked.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("The color of the Breadcrumbs trail.")
        .defaultValue(new SettingColor(225, 25, 25))
        .visible(breadcrumbs::get)
        .build()
    );
    private final Setting<Double> lineWidth = sgGeneral.add(new DoubleSetting.Builder()
        .name("Line Width")
        .description(".")
        .defaultValue(1)
        .min(0)
        .sliderMax(4)
        .visible(breadcrumbs::get)
        .build()
    );
    private final Setting<Integer> maxSections = sgGeneral.add(new IntSetting.Builder()
        .name("Max Sections")
        .description("The maximum number of sections.")
        .defaultValue(1000)
        .min(1)
        .sliderRange(1, 5000)
        .visible(breadcrumbs::get)
        .build()
    );
    private final Setting<Double> sectionLength = sgGeneral.add(new DoubleSetting.Builder()
        .name("Section Length")
        .description("The section length in blocks.")
        .defaultValue(0.5)
        .min(0)
        .sliderMax(1)
        .visible(breadcrumbs::get)
        .build()
    );

    private final List<PlayerMoveC2SPacket> packets = new ArrayList<>();
    private FakePlayerEntity model;
    private final Vector3d start = new Vector3d();

    private boolean cancelled = false;
    private int timer = 0;

    private final Pool<Section> sectionPool = new Pool<>(Section::new);
    private final Queue<Section> sections = new ArrayDeque<>();

    private Section section;

    private DimensionType lastDimension;

    @Override
    public void onActivate() {
        if (breadcrumbs.get()) {
            section = sectionPool.get();
            section.set1();

            lastDimension = mc.world.getDimension();
        }

        if (renderOriginal.get()) {
            model = new FakePlayerEntity(mc.player, mc.player.getGameProfile().getName(), 20, true);
            model.doNotPush = true;
            model.hideWhenInsideCamera = true;
            model.spawn();
        }

        Utils.set(start, mc.player.getPos());
    }

    @Override
    public void onDeactivate() {
        if (breadcrumbs.get()) {
            for (Section section : sections) sectionPool.free(section);
            sections.clear();
        }

        dumpPackets(!cancelled);
        if (cancelled) mc.player.setPos(start.x, start.y, start.z);
        cancelled = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        timer++;

        if (breadcrumbs.get()) {
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
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof PlayerMoveC2SPacket packet)) return;
        event.cancel();

        PlayerMoveC2SPacket prev = packets.size() == 0 ? null : packets.get(packets.size() - 1);

        if (prev != null &&
            packet.isOnGround() == prev.isOnGround() &&
            packet.getYaw(-1) == prev.getYaw(-1) &&
            packet.getPitch(-1) == prev.getPitch(-1) &&
            packet.getX(-1) == prev.getX(-1) &&
            packet.getY(-1) == prev.getY(-1) &&
            packet.getZ(-1) == prev.getZ(-1)
        ) return;

        synchronized (packets) {
            packets.add(packet);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent.Unlimited event) {
        if (!breadcrumbs.get()) return;

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

    @Override
    public String getInfoString() {
        return String.format("%.1f", timer / 20f);
    }

    private void dumpPackets(boolean send) {
        synchronized (packets) {
            if (send) packets.forEach(mc.player.networkHandler::sendPacket);
            packets.clear();
        }

        if (model != null) {
            model.despawn();
            model = null;
        }

        timer = 0;
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
