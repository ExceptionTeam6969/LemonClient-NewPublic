package dev.lemonclient.systems.modules.combat;

import baritone.api.BaritoneAPI;
import dev.lemonclient.TimeBomber;
import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.entity.player.SprintEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.lemonchat.utils.StringUtils;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.SortPriority;
import dev.lemonclient.utils.entity.TargetUtils;
import dev.lemonclient.utils.misc.RandomUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.player.TargetMode;
import dev.lemonclient.utils.render.Render3DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.ATimerUtil;
import dev.lemonclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class KillAura extends Module {
    public KillAura() {
        super(Categories.Combat, "Kill Aura", "Attacks specified entities around you.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgTiming = settings.createGroup("Timing");
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgBlock = settings.createGroup("Block");
    private final SettingGroup sgMove = settings.createGroup("Movement");
    private final SettingGroup sgHelper = settings.createGroup("Helper");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // A
    private final Setting<TargetMode> targetMode = enumSetting(sgGeneral, "Mode", TargetMode.Single);

    // ATTACK
    private final Setting<Boolean> onlyHeldWeapon = boolSetting(sgAttack, "only-held-weapon", "Held attack.", true);
    private final Setting<Boolean> attackCooldown = boolSetting(sgAttack, "Cooldown", "1.8+ cooldown", true);
    private final Setting<Double> attackCps = doubleSetting(sgAttack, "cps", 10.0, 1.0, 20.0, () -> !attackCooldown.get());
    private final Setting<Integer> switchDelay = intSetting(sgAttack, "switch-delay", 100, 0, 1000);
    private final Setting<Boolean> noFriend = boolSetting(sgAttack, "no-friend", "ignore friend.", false);
    private final Setting<Boolean> alwaysAttack = boolSetting(sgAttack, "always-attack", "Always attack players", true);
    private final Setting<Double> baseDelay = doubleSetting(sgAttack, "base-delay", 0.5, 0.0, 10.0, attackCooldown::get);
    private final Setting<Boolean> checkFov = boolSetting(sgAttack, "Check Fov", "Only aim entities in customized fov.", false);
    private final Setting<Double> fov = doubleSetting(sgAttack, "Fov", "Will only aim entities in the fov.", 360, 0, 360, checkFov::get);

    // Timing
    private final Setting<Boolean> pauseOnLag = sgTiming.add(new BoolSetting.Builder().name("pause-on-lag").description("Pauses if the server is lagging.").defaultValue(true).build());
    private final Setting<Boolean> pauseOnUse = sgTiming.add(new BoolSetting.Builder().name("pause-on-use").description("Does not attack while using an item.").defaultValue(false).build());
    private final Setting<Boolean> pauseOnCA = sgTiming.add(new BoolSetting.Builder().name("pause-on-CA").description("Does not attack while CA is placing.").defaultValue(true).build());
    private final Setting<Boolean> tpsSync = sgTiming.add(new BoolSetting.Builder().name("TPS Sync").description("Tries to sync attack delay with the server's TPS.").defaultValue(true).build());

    // Targeting
    private final Setting<Integer> maxTargets = intSetting(sgTargeting, "max-target", 10, 1, 100);
    private final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder().name("entities").description("Entities to attack.").onlyAttackable().defaultValue(EntityType.PLAYER).build());
    private final Setting<SortPriority> sortPriority = sgTargeting.add(new EnumSetting.Builder<SortPriority>().name("priority").description("How to filter targets within range.").defaultValue(SortPriority.ClosestAngle).build());
    private final Setting<Boolean> ignoreBabies = sgTargeting.add(new BoolSetting.Builder().name("ignore-babies").description("Whether or not to attack baby variants of the entity.").defaultValue(true).build());
    private final Setting<Boolean> ignoreNamed = sgTargeting.add(new BoolSetting.Builder().name("ignore-named").description("Whether or not to attack mobs with a name.").defaultValue(false).build());
    private final Setting<Boolean> ignorePassive = sgTargeting.add(new BoolSetting.Builder().name("ignore-passive").description("Will only attack sometimes passive mobs if they are targeting you.").defaultValue(true).build());
    private final Setting<Boolean> ignoreTamed = sgTargeting.add(new BoolSetting.Builder().name("ignore-tamed").description("Will avoid attacking mobs you tamed.").defaultValue(false).build());

    // BLOCK
    private final Setting<Boolean> autoBlock = boolSetting(sgBlock, "auto-block", true);
    private final Setting<Boolean> offhandBlock = boolSetting(sgBlock, "offhand-block", false);
    private final Setting<ABMode> abMode = enumSetting(sgBlock, "block-mode", ABMode.Vanilla);

    // MOVEMENT
    private final Setting<Boolean> keepSprint = boolSetting(sgMove, "keep-sprint", true);

    // Helper
    private final Setting<Weapon> weapon = sgHelper.add(new EnumSetting.Builder<Weapon>().name("weapon").description("Only attacks an entity when a specified weapon is in your hand.").defaultValue(Weapon.Both).build());
    private final Setting<Boolean> weaponSwitch = sgHelper.add(new BoolSetting.Builder().name("weapon-switch").description("Switches to your selected weapon when attacking the target.").defaultValue(false).build());
    private final Setting<Boolean> onlyOnClick = sgHelper.add(new BoolSetting.Builder().name("only-on-click").description("Only attacks when holding left click.").defaultValue(false).build());
    private final Setting<Boolean> onlyOnLook = sgHelper.add(new BoolSetting.Builder().name("only-on-look").description("Only attacks when looking at an entity.").defaultValue(false).build());
    private final Setting<Boolean> pauseOnCombat = sgHelper.add(new BoolSetting.Builder().name("pause-baritone").description("Freezes Baritone temporarily until you are finished attacking the entity.").defaultValue(true).build());
    private final Setting<ShieldMode> shieldMode = sgHelper.add(new EnumSetting.Builder<ShieldMode>().name("shield-mode").description("Will try and use an axe to break target shields.").defaultValue(ShieldMode.Break).visible(() -> weaponSwitch.get() && weapon.get() != Weapon.Axe).build());

    // Render
    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder().name("Swing").description("Renders your swing client-side.").defaultValue(true).build());
    private final Setting<SwingHand> swingHand = sgRender.add(new EnumSetting.Builder<SwingHand>().name("Swing Hand").description("Which hand should be swung.").defaultValue(SwingHand.RealHand).visible(swing::get).build());

    private final Setting<Boolean> displayAttackRange = boolSetting(sgRender, "Display Attack Range", false);
    private final Setting<SettingColor> attackRangeLineColor = sgRender.add(new ColorSetting.Builder().name("Line Color").description(COLOR).defaultValue(new SettingColor(255, 255, 255, 150)).visible(displayAttackRange::get).build());

    private final Setting<Boolean> alwaysESP = boolSetting(sgRender, "Always Esp", false);
    private final Setting<Boolean> renderEsp = boolSetting(sgRender, "Esp", true);
    private final Setting<RenderMode> espMode = enumSetting(sgRender, "Esp Mode", RenderMode.Jello, renderEsp::get);

    private final Setting<SettingColor> boxLine = sgRender.add(new ColorSetting.Builder().name("Box Line").description(COLOR).defaultValue(new SettingColor(255, 0, 0, 255)).visible(() -> espMode.get().equals(RenderMode.Box) && renderEsp.get()).build());
    private final Setting<SettingColor> boxSide = sgRender.add(new ColorSetting.Builder().name("Box Side").description(COLOR).defaultValue(new SettingColor(255, 0, 0, 30)).visible(() -> espMode.get().equals(RenderMode.Box) && renderEsp.get()).build());

    private final Setting<SettingColor> jelloLine = sgRender.add(new ColorSetting.Builder().name("Jello Line").description(COLOR).defaultValue(new SettingColor(149, 149, 149, 170)).visible(() -> renderEsp.get() && espMode.get().equals(RenderMode.Jello)).build());


    public final List<Entity> targets = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private boolean wasPathing = false;

    private int index;
    private int switchTicks;
    private final ATimerUtil attackTimer = new ATimerUtil();
    public static boolean isBlocking;

    public static Entity curTarget;

    public enum RotationMode {
        LemonClient
    }

    public enum ABMode {
        Vanilla
    }

    public enum RenderMode {
        None,
        Box,
        Jello
    }

    public enum Weapon {
        Sword,
        Axe,
        Both,
        Any
    }

    public enum ShieldMode {
        Ignore,
        Break,
        None
    }

    @Override
    public void onActivate() {
        targets.clear();
        curTarget = null;
        this.index = 0;
    }

    @Override
    public void onDeactivate() {
        curTarget = null;
        this.targets.clear();
        if (this.autoBlock.get() && this.hasSword() && mc.player.isBlocking()) {
            this.unBlock();
        }
    }

    @EventHandler
    private void onSprint(SprintEvent event) {
        if (keepSprint.get() && curTarget != null && itemInHand()) {
            event.isSprint = true;
        }
    }

    @EventHandler
    public void onUpdate(TickEvent.Pre event) {
        if (!mc.player.isAlive() || PlayerUtils.getGameMode() == GameMode.SPECTATOR) return;
        if (pauseOnUse.get() && (mc.interactionManager.isBreakingBlock() || mc.player.isUsingItem())) return;
        if (onlyOnClick.get() && !mc.options.attackKey.isPressed()) return;
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1f && pauseOnLag.get()) return;
        if (pauseOnCA.get()) {
            if (Modules.get().isActive(AutoCrystalPlus.class) || Modules.get().isActive(AutoCrystal.class))
                return;
        }

        if (!TimeBomber.shouldBomb()) {
            if (onlyOnLook.get()) {
                Entity targeted = mc.targetedEntity;
                if (targeted == null) return;
                if (!entityCheck(targeted)) return;
                targets.clear();
                targets.add(mc.targetedEntity);
            } else TargetUtils.getList(targets, this::entityCheck, sortPriority.get(), maxTargets.get());

            if (targets.isEmpty()) {
                if (wasPathing) {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
                    wasPathing = false;
                }
                return;
            }

            switchTicks--;

            if (this.targets.size() > 1 && this.targetMode.get().equals(TargetMode.Switch)) {
                if (switchTicks <= 0) {
                    ++this.index;
                    switchTicks = switchDelay.get() / 20;
                }
            }

            if (this.targets.size() > 1 && this.targetMode.get().equals(TargetMode.Single)) {
                this.index = 0;
            }

            if (curTarget != null) {
                curTarget = null;
            }

            if (!targets.isEmpty()) {
                if (targetMode.get().equals(TargetMode.Multi)) {
                    curTarget = this.targets.get(0);
                } else {
                    if (index >= targets.size()) {
                        index = 0;
                    }
                    curTarget = targets.get(index);
                }
            }

            if (curTarget != null && !itemInHand()) {
                return;
            }

            if (SettingUtils.shouldRotate(RotationType.Attacking) && !Managers.ROTATION.start(curTarget.getBoundingBox(), priority, RotationType.Attacking, Objects.hash(name + "attacking"))) {
                return;
            }
        }

        if (curTarget != null) {
            if (shouldAttack() && itemInHand()) {
                if (hasSword() && mc.player.isBlocking()) {
                    unBlock();
                }

                preAttack();

                attackTimer.reset();

                if (targetMode.get().equals(TargetMode.Multi)) targets.forEach(this::attack);
                else attack(curTarget);
            }
            if (!mc.player.isBlocking() && this.hasSword() && autoBlock.get()) {
                block();
                mc.interactionManager.interactItem(mc.player, offhandBlock.get() ? Hand.OFF_HAND : Hand.MAIN_HAND);
            }
        }

        if (SettingUtils.shouldRotate(RotationType.Attacking)) {
            Managers.ROTATION.end(Objects.hash(name + "attacking"));
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (displayAttackRange.get()) {
            double smoothX = MathHelper.lerp(event.tickDelta, mc.player.lastRenderX, mc.player.getX());
            double smoothY = MathHelper.lerp(event.tickDelta, mc.player.lastRenderY, mc.player.getY());
            double smoothZ = MathHelper.lerp(event.tickDelta, mc.player.lastRenderZ, mc.player.getZ());
            event.renderer.circle(event.matrices, smoothX, smoothY, smoothZ, SettingUtils.getAttackRange(), attackRangeLineColor.get());
        }

        if (!renderEsp.get()) return;
        if (!itemInHand() && !alwaysESP.get()) return;

        for (int i = 0; i < (targets.size() > maxTargets.get() ? maxTargets.get() : targets.size()); ++i) {
            Entity entity = targets.get(i);
            double smoothX = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX());
            double smoothY = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY());
            double smoothZ = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ());

            switch (espMode.get()) {
                case Box -> {
                    double x = smoothX - entity.getX();
                    double y = smoothY - entity.getY();
                    double z = smoothZ - entity.getZ();
                    Box box = entity.getBoundingBox();
                    event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, this.boxSide.get(), this.boxLine.get(), ShapeMode.Both, 0);
                }
                case Jello -> Render3DUtils.drawJello(event.matrices, entity, jelloLine.get());
            }
        }
    }

    public Entity getTarget() {
        if (!targets.isEmpty()) return targets.get(0);
        return null;
    }

    private void preAttack() {
        if (pauseOnCombat.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing() && !wasPathing) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
            wasPathing = true;
        }

        if (weaponSwitch.get()) {
            Predicate<ItemStack> predicate = switch (weapon.get()) {
                case Axe -> stack -> stack.getItem() instanceof AxeItem;
                case Sword -> stack -> stack.getItem() instanceof SwordItem;
                case Both -> stack -> stack.getItem() instanceof AxeItem || stack.getItem() instanceof SwordItem;
                default -> stack -> true;
            };
            FindItemResult weaponResult = InvUtils.findInHotbar(predicate);
            if (shouldShieldBreak()) {
                FindItemResult axeResult = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof AxeItem);
                if (axeResult.found()) weaponResult = axeResult;
            }
            InvUtils.swap(weaponResult.slot(), false);
        }
    }

    private boolean hasSword() {
        if (mc.player.getMainHandStack() == null) {
            return false;
        } else {
            return mc.player.getMainHandStack().getItem() instanceof SwordItem;
        }
    }

    private boolean shouldAttack() {
        return attackCooldown.get() ? delayCheck() : attackTimer.hasReached(1000.0D / (this.attackCps.get() + RandomUtils.nextDouble(0.0D, 5.0D)));
    }

    private void block() {
        if (autoBlock.get() && !mc.options.useKey.isPressed() && !isBlocking && abMode.get().equals(ABMode.Vanilla)) {
            BlockHitResult hitResult = BlockHitResult.createMissed(mc.player.getPos(), Direction.DOWN, new BlockPos(-1, -1, -1));
            boolean off = this.offhandBlock.get();
            sendPacket(new PlayerInteractBlockC2SPacket(off ? Hand.OFF_HAND : Hand.MAIN_HAND, hitResult, 0));
            isBlocking = true;
        }
    }


    private void unBlock() {
        if (autoBlock.get() && isBlocking && abMode.get().equals(ABMode.Vanilla)) {
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            mc.player.clearActiveItem();
            isBlocking = false;
        }
    }

    private boolean shouldShieldBreak() {
        for (Entity target : targets) {
            if (target instanceof PlayerEntity player) {
                if (player.blockedByShield(mc.world.getDamageSources().playerAttack(mc.player)) && shieldMode.get() == ShieldMode.Break) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
        if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;

        Box box = entity.getBoundingBox();
        if (!PlayerUtils.isWithin(
            MathHelper.clamp(mc.player.getX(), box.minX, box.maxX),
            MathHelper.clamp(mc.player.getY(), box.minY, box.maxY),
            MathHelper.clamp(mc.player.getZ(), box.minZ, box.maxZ),
            SettingUtils.getAttackRange()
        )) return false;

        if (!entities.get().contains(entity.getType())) return false;
        if (ignoreNamed.get() && entity.hasCustomName()) return false;
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, SettingUtils.getAttackWallsRange()))
            return false;
        if (ignoreTamed.get()) {
            if (entity instanceof Tameable tameable
                && tameable.getOwnerUuid() != null
                && tameable.getOwnerUuid().equals(mc.player.getUuid())
            ) return false;
        }
        if (ignorePassive.get()) {
            if (entity instanceof EndermanEntity enderman && !enderman.isAngryAt(mc.player)) return false;
            if (entity instanceof ZombifiedPiglinEntity piglin && !piglin.isAngryAt(mc.player)) return false;
            if (entity instanceof WolfEntity wolf && !wolf.isAttacking()) return false;
            if (entity instanceof LlamaEntity llama && !llama.isAttacking()) return false;
        }
        if (entity instanceof PlayerEntity player) {
            if (player.isCreative() && !alwaysAttack.get()) return false;
            if (!Friends.get().shouldAttack(player) && !noFriend.get()) return false;
            if (shieldMode.get() == ShieldMode.Ignore && player.blockedByShield(mc.world.getDamageSources().playerAttack(mc.player)))
                return false;
            if (checkFov.get() && !PlayerUtils.inFov(entity, fov.get())) return false;
        }
        return !(entity instanceof AnimalEntity animal) || !ignoreBabies.get() || !animal.isBaby();
    }

    private boolean delayCheck() {
        float delay = baseDelay.get().floatValue();
        if (tpsSync.get()) delay /= (TickRate.INSTANCE.getTickRate() / 20);

        return mc.player.getAttackCooldownProgress(delay) >= 1;
    }

    private void attack(Entity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.get()) serverSwing(swingHand.get(), Hand.MAIN_HAND);
    }

    private boolean itemInHand() {
        if (!onlyHeldWeapon.get()) return true;

        if (shouldShieldBreak()) return mc.player.getMainHandStack().getItem() instanceof AxeItem;

        return switch (weapon.get()) {
            case Axe -> mc.player.getMainHandStack().getItem() instanceof AxeItem;
            case Sword -> mc.player.getMainHandStack().getItem() instanceof SwordItem;
            case Both ->
                mc.player.getMainHandStack().getItem() instanceof AxeItem || mc.player.getMainHandStack().getItem() instanceof SwordItem;
            default -> true;
        };
    }

    @Override
    public String getInfoString() {
        return StringUtils.getReplaced("[T:{},R:{},C:{}]", targets.size(), SettingUtils.getAttackRange(), curTarget != null ? curTarget.getDisplayName().getString() : "None");
    }
}
