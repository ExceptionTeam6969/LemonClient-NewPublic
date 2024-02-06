package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.game.GameLeftEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.SortPriority;
import dev.lemonclient.utils.entity.TargetUtils;
import dev.lemonclient.utils.path.TeleportPath;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.render.ColorMode;
import dev.lemonclient.utils.render.PathDrawMode;
import dev.lemonclient.utils.render.Render3DUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.PreTimer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class InfiniteAura extends Module {
    public InfiniteAura() {
        super(Categories.Combat, "Infinite Aura", "The original infinite reach :)");
        //workThread.start();
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCombat = settings.createGroup("Combat");
    private final SettingGroup sgTeleport = settings.createGroup("Teleport");
    private final SettingGroup sgDelay = settings.createGroup("Delay");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgColor = settings.createGroup("Color");

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .onlyAttackable()
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables module on kick.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> maxTargets = sgCombat.add(new IntSetting.Builder()
        .name("max-targets")
        .description("attack max targets")
        .range(1, 100)
        .sliderRange(1, 100)
        .defaultValue(1)
        .build()
    );

    private final Setting<Double> range = sgCombat.add(new DoubleSetting.Builder()
        .name("range")
        .description("teleport Range")
        .range(6, 250)
        .sliderRange(6, 250)
        .defaultValue(150)
        .build()
    );

    private final Setting<Boolean> smartDelay = sgCombat.add(new BoolSetting.Builder()
        .name("smart-delay")
        .description("Uses the vanilla cooldown to attack entities.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SortPriority> priority = sgCombat.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );

    private final Setting<Boolean> nametagged = sgCombat.add(new BoolSetting.Builder()
        .name("nametagged")
        .description("Whether or not to attack mobs with a name tag.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> babies = sgCombat.add(new BoolSetting.Builder()
        .name("babies")
        .description("Whether or not to attack baby variants of the entity.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignorePassive = sgCombat.add(new BoolSetting.Builder()
        .name("ignore-passive")
        .description("Only attacks angry piglins and enderman.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ground = sgTeleport.add(new BoolSetting.Builder()
        .name("ground")
        .description("Teleport the ground status.")
        .defaultValue(true)
        .build()
    );

    private final Setting<UpdateTiming> timing = sgDelay.add(new EnumSetting.Builder<UpdateTiming>()
        .name("update-timing")
        .description("Attacks timing")
        .defaultValue(UpdateTiming.Pre)
        .build()
    );

    private final Setting<Integer> backDelay = sgDelay.add(new IntSetting.Builder()
        .name("back-delay")
        .description("How fast you hit the entity then back in ticks.")
        .defaultValue(0)
        .min(0)
        .sliderMax(10000)
        .build()
    );

    private final Setting<Integer> hitDelay = sgDelay.add(new IntSetting.Builder()
        .name("hit-delay")
        .description("How fast you hit the entity in ticks.")
        .defaultValue(0)
        .min(0)
        .sliderMax(60)
        .visible(() -> !smartDelay.get())
        .build()
    );

    private final Setting<Boolean> randomDelayEnabled = sgDelay.add(new BoolSetting.Builder()
        .name("random-delay-enabled")
        .description("Adds a random delay between hits to attempt to bypass anti-cheats.")
        .defaultValue(false)
        .visible(() -> !smartDelay.get())
        .build()
    );

    private final Setting<Integer> randomDelayMax = sgDelay.add(new IntSetting.Builder()
        .name("random-delay-max")
        .description("The maximum value for random delay.")
        .defaultValue(4)
        .min(0)
        .sliderMax(20)
        .visible(() -> randomDelayEnabled.get() && !smartDelay.get())
        .build()
    );

    private final Setting<Integer> switchDelay = sgDelay.add(new IntSetting.Builder()
        .name("switch-delay")
        .description("How many ticks to wait before hitting an entity after switching hotbar slots.")
        .defaultValue(0)
        .min(0)
        .build()
    );

    private final Setting<Boolean> swingHand = sgRender.add(new BoolSetting.Builder()
        .name("swing-hand")
        .description("Attack swing hand.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> drawPath = sgRender.add(new BoolSetting.Builder()
        .name("path")
        .description("render path")
        .defaultValue(false)
        .build()
    );

    private final Setting<PathDrawMode> pathMode = sgRender.add(new EnumSetting.Builder<PathDrawMode>()
        .name("path-mode")
        .description("Rendering path mode")
        .defaultValue(PathDrawMode.Line)
        .build()
    );

    private final Setting<SettingColor> pathColor = sgRender.add(new ColorSetting.Builder()
        .name("path-color")
        .description("The color of the path render.")
        .defaultValue(new SettingColor(200, 200, 200))
        .build()
    );

    private final Setting<Boolean> drawESP = sgRender.add(new BoolSetting.Builder()
        .name("esp")
        .description("render path")
        .defaultValue(false)
        .build()
    );

    private final ColorMode.ColorSettings colorSettings = ColorMode.ColorSettings.create(sgColor);
    private final CopyOnWriteArrayList<Runnable> tasks = new CopyOnWriteArrayList<>();
    private final List<Entity> targets = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private CopyOnWriteArrayList<Vec3d> path = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Vec3d>[] test = new CopyOnWriteArrayList[100];

    private final PreTimer cps = new PreTimer();
    private final PreTimer timer = new PreTimer();
    private final PreTimer backTimer = new PreTimer();

    private boolean backed;
    private int hitDelayTimer, switchTimer;

    private final Thread workThread = new Thread(() -> {
        while (mc.isRunning()) {
            if (!Utils.canUpdate()) return;

            if (!Modules.get().isActive(InfiniteAura.class)) return;

            if (tasks.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }

            for (Runnable runnable : tasks) {
                try {
                    runnable.run();
                    tasks.remove(runnable);
                } catch (Exception e) {
                    tasks.remove(runnable);
                }
            }
        }
    }, "infinite worker");


    public enum UpdateTiming {
        Pre,
        Post
    }

    @Override
    public void onActivate() {
        targets.clear();
        timer.reset();
        backTimer.reset();
    }


    @Override
    public void onDeactivate() {
        hitDelayTimer = 0;
    }


    private void update() {
        TargetUtils.getList(targets, this::entityCheck, priority.get(), maxTargets.get());
        if (delayCheck()) {
            Runnable task = () -> {
                if (targets.size() > 0) {
                    test = new CopyOnWriteArrayList[100];
                    for (int i = 0; i < (targets.size() > maxTargets.get() ? maxTargets.get() : targets.size()); i++) {
                        Entity T = targets.get(i);
                        Vec3d topFrom = mc.player.getPos();
                        Vec3d to = T.getPos();
                        if (backDelay.get() == 0 || backed) {
                            path = TeleportPath.computePath(topFrom, to);
                            test[i] = path;
                            for (Vec3d pathElm : path) {
                                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pathElm.getX(), pathElm.getY(), pathElm.getZ(), ground.get()));
                            }

                            if (backDelay.get() != 0) {
                                backed = false;
                            }
                        }
                        if (swingHand.get()) {
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }
                        mc.interactionManager.attackEntity(mc.player, T);
                        Collections.reverse(path);
                        if (backDelay.get() == 0 || backTimer.check(backDelay.get())) {
                            for (Vec3d pathElmi : path) {
                                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pathElmi.getX(), pathElmi.getY(), pathElmi.getZ(), ground.get()));
                            }
                            if (backDelay.get() != 0) {
                                backTimer.reset();
                                backed = true;
                            }
                        }
                    }
                    cps.reset();
                }
            };

            task.run();
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!this.targets.isEmpty() && drawESP.get() && this.targets.size() > 0) {
            for (int i = 0; i < (this.targets.size() > maxTargets.get() ? maxTargets.get() : this.targets.size()); ++i) {
                event.renderer.box(targets.get(i).getBoundingBox(), new Color(255, 255, 255, 30), Color.WHITE, ShapeMode.Both, 0);
            }
        }

        if (!this.path.isEmpty() && drawPath.get()) {
            CopyOnWriteArrayList<Vec3d>[] test = this.test.clone();
            for (int i = 0; i < targets.size(); i++) {
                if (test != null && test[i] != null) {
                    renderPath(event, test[i]);
                }
            }
            if (this.cps.check(1000.0f)) {
                this.test = new CopyOnWriteArrayList[100];
                this.path.clear();
            }
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            switchTimer = switchDelay.get();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (autoDisable.get()) toggle();
    }

    @EventHandler
    private void pre(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        if (timing.get().equals(UpdateTiming.Pre)) {
            update();
        }

        colorSettings.tick();
    }

    @EventHandler
    private void post(TickEvent.Post e) {
        if (!Utils.canUpdate()) return;
        if (timing.get().equals(UpdateTiming.Post)) {
            update();
        }
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
        if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
        if (PlayerUtils.distanceTo(entity) > range.get()) return false;
        if (!entities.get().contains(entity.getType())) return false;
        if (!nametagged.get() && entity.hasCustomName()) return false;
        if (ignorePassive.get()) {
            if (entity instanceof EndermanEntity enderman && !enderman.isAngry()) return false;
            if (entity instanceof Tameable tameable
                && tameable.getOwnerUuid() != null
                && tameable.getOwnerUuid().equals(mc.player.getUuid())) return false;
            if (entity instanceof MobEntity mob && !mob.isAttacking() && !(entity instanceof PhantomEntity))
                return false; // Phantoms don't seem to set the attacking property
        }
        if (entity instanceof PlayerEntity player) {
            if (player.isCreative()) return false;
            if (!Friends.get().shouldAttack(player)) return false;
        }
        return !(entity instanceof AnimalEntity) || babies.get() || !((AnimalEntity) entity).isBaby();
    }

    private boolean delayCheck() {
        if (switchTimer > 0) {
            switchTimer--;
            return false;
        }

        if (smartDelay.get()) return mc.player.getAttackCooldownProgress(0.5f) >= 1;

        if (hitDelayTimer > 0) {
            hitDelayTimer--;
            return false;
        } else {
            hitDelayTimer = hitDelay.get();
            if (randomDelayEnabled.get()) hitDelayTimer += Math.round(Math.random() * randomDelayMax.get());
            return true;
        }
    }

    public static void drawPath(Render3DEvent event, Vec3d vec, Box box) {
        double boxWidth = box.maxY - box.minY;
        double centerXWidth = (box.maxX - box.minX) / 2;
        double centerZWidth = (box.maxZ - box.minZ) / 2;
        event.renderer.box(vec.x - centerXWidth, vec.y + boxWidth, vec.z - centerZWidth, vec.x + centerXWidth, vec.y, vec.z + centerZWidth, InfiniteAura.getPathColor(), InfiniteAura.getPathColor(), ShapeMode.Lines, 0);
    }

    public void renderPath(Render3DEvent event, List<Vec3d> path) {
        Vec3d lastPoint = null;

        for (Vec3d pos : path) {
            switch (getPathMode()) {
                case Box -> {
                    if (pos != null)
                        drawPath(event, pos, mc.player.getBoundingBox(mc.player.getPose()));
                }
                case Line -> {
                    if (lastPoint != null && pos != null) {
                        int current = event.renderer.lines.vec3(pos.x, pos.y, pos.z).color(getPathColor()).next();
                        int last = event.renderer.lines.vec3(lastPoint.x, lastPoint.y, lastPoint.z).color(getPathColor()).next();
                        event.renderer.lines.line(last, current);
                    }

                    lastPoint = pos;
                }
                case CircleLine -> {
                    if (lastPoint != null && pos != null) {
                        int current = event.renderer.lines.vec3(pos.x, pos.y, pos.z).color(getPathColor()).next();
                        int last = event.renderer.lines.vec3(lastPoint.x, lastPoint.y, lastPoint.z).color(getPathColor()).next();
                        event.renderer.lines.line(last, current);
                    }
                    lastPoint = pos;

                    Render3DUtils.drawGlowCircle(event.matrices, pos.x, pos.y, pos.z, 0.7f, 0.3f, 0.3f, getColorSettings());
                }
            }
        }
    }

    public static Vec3d center(Vec3d target) {
        double x = MathHelper.floor(target.x) + 0.5;
        double z = MathHelper.floor(target.z) + 0.5;
        return new Vec3d(x, target.y, z);
    }

    public static PathDrawMode getPathMode() {
        InfiniteAura mod = Modules.get().get(InfiniteAura.class);
        return mod != null ? mod.pathMode.get() : PathDrawMode.Line;
    }

    public static Color getPathColor() {
        InfiniteAura mod = Modules.get().get(InfiniteAura.class);
        return mod != null ? mod.pathColor.get() : new Color(200, 200, 200);
    }

    public static ColorMode.ColorSettings getColorSettings() {
        InfiniteAura mod = Modules.get().get(InfiniteAura.class);
        return mod.colorSettings;
    }
}
