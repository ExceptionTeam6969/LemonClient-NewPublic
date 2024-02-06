package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.enums.HoleType;
import dev.lemonclient.events.entity.player.PlayerMoveEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.mixininterface.IVec3d;
import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.world.Timer;
import dev.lemonclient.utils.player.CombatInfo;
import dev.lemonclient.utils.player.Rotations;
import dev.lemonclient.utils.render.BezierCurve;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.hole.Hole;
import dev.lemonclient.utils.world.hole.HoleUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class HoleSnap extends Module {
    public HoleSnap() {
        super(Categories.Movement, "Hole Snap", "Move to the hole nearby.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgHole = settings.createGroup("Hole");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    public final Setting<Boolean> step = sgGeneral.add(new BoolSetting.Builder()
        .name("Use Step")
        .description("Open the Step module while this module is active.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> dStep = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle Step")
        .description("Disable the Step module when this module is deactivate.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> jump = sgGeneral.add(new BoolSetting.Builder()
        .name("Jump")
        .description("Jumps to the hole.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> jumpCoolDown = sgGeneral.add(new IntSetting.Builder()
        .name("Jump Cooldown")
        .description("Ticks between jumps.")
        .defaultValue(5)
        .min(0)
        .sliderMax(100)
        .visible(jump::get)
        .build()
    );
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Range")
        .description("Horizontal range for finding holes.")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> downRange = sgGeneral.add(new IntSetting.Builder()
        .name("Down Range")
        .description("Vertical range for finding holes.")
        .defaultValue(3)
        .range(0, 5)
        .sliderMax(5)
        .build()
    );
    private final Setting<Integer> coll = sgGeneral.add(new IntSetting.Builder()
        .name("Collisions to disable")
        .description("0 = doesn't disable.")
        .defaultValue(15)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Integer> rDisable = sgGeneral.add(new IntSetting.Builder()
        .name("Rubberbands to disable")
        .description("0 = doesn't disable.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Movement Speed.")
        .defaultValue(0.2873)
        .min(0)
        .sliderMax(1)
        .build()
    );
    private final Setting<Boolean> boost = sgSpeed.add(new BoolSetting.Builder()
        .name("Speed Boost")
        .description("Jumps to the hole (very useful).")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> boostedSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Boosted Speed")
        .description("Movement Speed.")
        .defaultValue(0.5)
        .min(0)
        .sliderMax(1)
        .visible(boost::get)
        .build()
    );
    private final Setting<Integer> boostTicks = sgSpeed.add(new IntSetting.Builder()
        .name("Boost Ticks")
        .description("How many boosted speed packets should be sent before returning to normal speed.")
        .defaultValue(3)
        .min(1)
        .sliderMax(10)
        .visible(boost::get)
        .build()
    );
    private final Setting<Double> timer = sgSpeed.add(new DoubleSetting.Builder()
        .name("Timer")
        .description("Sends packets faster.")
        .defaultValue(1.5)
        .min(0)
        .sliderMax(100)
        .build()
    );

    //--------------------Hole--------------------//
    private final Setting<Boolean> singleTarget = sgHole.add(new BoolSetting.Builder()
        .name("Single Target")
        .description("Only chooses target hole once.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> depth = sgHole.add(new IntSetting.Builder()
        .name("Hole Depth")
        .description("How deep a hole has to be.")
        .defaultValue(3)
        .range(1, 5)
        .sliderRange(1, 5)
        .build()
    );
    private final Setting<Boolean> singleHoles = sgHole.add(new BoolSetting.Builder()
        .name("Single Holes")
        .description("Targets single block holes.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> doubleHoles = sgHole.add(new BoolSetting.Builder()
        .name("Double Holes")
        .description("Targets double holes.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> quadHoles = sgHole.add(new BoolSetting.Builder()
        .name("Quad Holes")
        .description("Targets quad holes.")
        .defaultValue(true)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description(COLOR)
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> segments = sgRender.add(new IntSetting.Builder()
        .name("Segments")
        .description(".")
        .defaultValue(20)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(render::get)
        .build()
    );
    private final Setting<Double> lineWidth = sgRender.add(new DoubleSetting.Builder()
        .name("Line Width")
        .description("The width of the rendered line.")
        .defaultValue(3)
        .min(1)
        .sliderRange(1, 4)
        .visible(render::get)
        .build()
    );
    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(render::get)
        .build()
    );


    private Hole singleHole;
    private int collisions;
    private int rubberbands;
    private int ticks;
    private int boostLeft = 0;
    private int alpha;
    private Hole targetHole;
    private Step stepModule;

    @Override
    public void onActivate() {
        singleHole = findHole();
        rubberbands = 0;
        ticks = 0;
        boostLeft = boost.get() ? boostTicks.get() : 0;
        stepModule = Modules.get().get(Step.class);
    }

    @Override
    public void onDeactivate() {
        if (dStep.get() && step.get() && stepModule.isActive()) {
            stepModule.toggle();
        }

        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && rDisable.get() > 0) {
            rubberbands++;
            if (rubberbands >= rDisable.get() && rDisable.get() > 0) {
                toggle();
                sendDisableMsg("rubberbanding");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        if (mc.player != null && mc.world != null) {
            Hole hole = singleTarget.get() ? singleHole : findHole();
            targetHole = hole;

            if (hole != null && !singleBlocked()) {
                alpha = 150;
                Modules.get().get(Timer.class).setOverride(timer.get());
                double yaw = Math.cos(Math.toRadians(getAngle(hole.middle) + 90.0f));
                double pit = Math.sin(Math.toRadians(getAngle(hole.middle) + 90.0f));

                if (mc.player.getX() == hole.middle.x && mc.player.getZ() == hole.middle.z) {
                    if (mc.player.getY() == hole.middle.y) {
                        this.toggle();
                        sendDisableMsg("in hole");
                        ((IVec3d) event.movement).setXZ(0, 0);
                    } else if (CombatInfo.inside(mc.player, mc.player.getBoundingBox().offset(0, -0.05, 0))) {
                        this.toggle();
                        sendDisableMsg("hole unreachable");
                    } else {
                        ((IVec3d) event.movement).setXZ(0, 0);
                    }
                } else {
                    if (step.get() && !stepModule.isActive()) {
                        stepModule.toggle();
                    }

                    double x = getSpeed() * yaw;
                    double dX = hole.middle.x - mc.player.getX();
                    double z = getSpeed() * pit;
                    double dZ = hole.middle.z - mc.player.getZ();

                    if (CombatInfo.inside(mc.player, mc.player.getBoundingBox().offset(x, 0, z))) {
                        collisions++;
                        if (collisions >= coll.get() && coll.get() > 0) {
                            this.toggle();
                            sendDisableMsg("collided");
                        }
                    } else {
                        collisions = 0;
                    }
                    if (ticks > 0) {
                        ticks--;
                    } else if (CombatInfo.inside(mc.player, mc.player.getBoundingBox().offset(0, -0.05, 0)) && jump.get()) {
                        ticks = jumpCoolDown.get();
                        ((IVec3d) event.movement).setY(0.42);
                    }
                    boostLeft--;
                    ((IVec3d) event.movement).setXZ(Math.abs(x) < Math.abs(dX) ? x : dX, Math.abs(z) < Math.abs(dZ) ? z : dZ);
                }
            } else {
                this.toggle();
                sendDisableMsg("no hole found");
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent.Unlimited event) {
        if (!render.get()) return;

        Renderer3D renderer = new Renderer3D();
        renderer.begin();

        if (alpha > 0) alpha -= 2;
        else alpha = 0;

        if (targetHole == null) return;
        assert mc.player != null;

        Vec3d from = mc.player.getPos();
        Vec3d to = targetHole.middle;
        Vec3d linePath = to.add(0, 1.5, 0);

        int segments = this.segments.get();
        for (int i = 0; i <= segments; i++) {
            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            Vec3d bezierPoint0 = BezierCurve.calculatePoint(from, linePath, to, t0);
            Vec3d bezierPoint1 = BezierCurve.calculatePoint(from, linePath, to, t1);

            renderer.lines.line(
                renderer.lines.vec3(bezierPoint0.x, bezierPoint0.y, bezierPoint0.z).color(color.get().a(alpha)).next(),
                renderer.lines.vec3(bezierPoint1.x, bezierPoint1.y, bezierPoint1.z).color(color.get().a(alpha)).next()
            );
        }

        renderer.render(event.matrices, lineWidth.get().floatValue());
    }

    private boolean singleBlocked() {
        if (!singleTarget.get()) {
            return false;
        }

        for (BlockPos pos : singleHole.positions) {
            if (BlockUtils.collidable(pos)) {
                return true;
            }
        }
        return false;
    }

    private Hole findHole() {
        Hole closest = null;

        for (int x = -range.get(); x <= range.get(); x++) {
            for (int y = -downRange.get(); y < 1; y++) {
                for (int z = -range.get(); z < range.get(); z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);

                    Hole hole = HoleUtils.getHole(pos, singleHoles.get(), doubleHoles.get(), quadHoles.get(), depth.get(), true);

                    if (hole.type == HoleType.NotHole) {
                        continue;
                    }

                    if (y == 0 && inHole(hole)) {
                        return hole;
                    }
                    if (closest == null || hole.middle.distanceTo(mc.player.getPos()) < closest.middle.distanceTo(mc.player.getPos())) {
                        closest = hole;
                    }
                }
            }
        }

        return closest;
    }

    private boolean inHole(Hole hole) {
        for (BlockPos pos : hole.positions) {
            if (mc.player.getBlockPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private float getAngle(Vec3d pos) {
        return (float) Rotations.getYaw(pos);
    }

    private double getSpeed() {
        return boostLeft > 0 ? boostedSpeed.get() : speed.get();
    }
}
