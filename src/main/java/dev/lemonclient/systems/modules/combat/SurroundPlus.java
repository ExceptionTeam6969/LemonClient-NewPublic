package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.TimeBomber;
import dev.lemonclient.enums.*;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.BlockUpdateEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.*;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.TimerList;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SurroundPlus extends Module {
    public SurroundPlus() {
        super(Categories.Combat, "Surround+", "Places blocks around your legs to protect from explosions.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgToggle = settings.createGroup("Toggle");
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgBlocks = settings.createGroup("Blocks");
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder()
        .name("Center")
        .description("Moves to block center before surrounding.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> smartCenter = sgGeneral.add(new BoolSetting.Builder()
        .name("Smart Center")
        .description("Only moves until whole hitbox is inside target block.")
        .defaultValue(true)
        .visible(center::get)
        .build()
    );
    private final Setting<Boolean> phaseCenter = sgGeneral.add(new BoolSetting.Builder()
        .name("Phase Friendly")
        .description("Doesn't center if clipped inside a block.")
        .defaultValue(true)
        .visible(center::get)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Eat")
        .description("Pause while eating.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> onlyConfirmed = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Confirmed")
        .description("Only places on blocks the server has confirmed to exist.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> packet = sgGeneral.add(new BoolSetting.Builder()
        .name("Packet")
        .description("Place blocks by sending packets.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable but delays crystals on some servers.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Boolean> extend = sgGeneral.add(new BoolSetting.Builder()
        .name("Extend")
        .description(".")
        .defaultValue(true)
        .build()
    );

    //--------------------General--------------------//
    private final Setting<Boolean> toggleMove = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Move")
        .description(".")
        .defaultValue(false)
        .build()
    );
    private final Setting<VerticalToggleMode> toggleVertical = sgToggle.add(new EnumSetting.Builder<VerticalToggleMode>()
        .name("Toggle Vertical")
        .description(".")
        .defaultValue(VerticalToggleMode.Up)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<PlaceDelayMode> placeDelayMode = sgSpeed.add(new EnumSetting.Builder<PlaceDelayMode>()
        .name("Place Delay Mode")
        .description(".")
        .defaultValue(PlaceDelayMode.Ticks)
        .build()
    );
    private final Setting<Integer> placeDelayT = sgSpeed.add(new IntSetting.Builder()
        .name("Place Tick Delay")
        .description("Tick delay between places.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> placeDelayMode.get() == PlaceDelayMode.Ticks)
        .build()
    );
    private final Setting<Double> placeDelayS = sgSpeed.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.1)
        .min(0)
        .sliderRange(0, 1)
        .visible(() -> placeDelayMode.get() == PlaceDelayMode.Seconds)
        .build()
    );
    private final Setting<Integer> places = sgSpeed.add(new IntSetting.Builder()
        .name("Places")
        .description("How many blocks to place each time.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> cooldown = sgSpeed.add(new DoubleSetting.Builder()
        .name("Multi Cooldown")
        .description("Waits x seconds before trying to place at the same position if there is more than 1 missing block.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Double> singleCooldown = sgSpeed.add(new DoubleSetting.Builder()
        .name("Single Cooldown")
        .description("Waits x seconds before trying to place at the same position if there is only 1 missing block.")
        .defaultValue(0.02)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );

    //--------------------Blocks--------------------//
    private final Setting<List<Block>> blocks = sgBlocks.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN)
        .build()
    );
    private final Setting<List<Block>> supportBlocks = sgBlocks.add(new BlockListSetting.Builder()
        .name("Support Blocks")
        .description("Blocks to use for support.")
        .defaultValue(Blocks.OBSIDIAN)
        .build()
    );

    //--------------------Attack--------------------//
    private final Setting<Boolean> attack = sgAttack.add(new BoolSetting.Builder()
        .name("Attack")
        .description("Attacks crystals blocking surround.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> attackSpeed = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Speed")
        .description("How many times to attack every second.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> alwaysAttack = sgAttack.add(new BoolSetting.Builder()
        .name("Always Attack")
        .description("Attacks crystals even when surround block isn't broken.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> antiCev = sgAttack.add(new BoolSetting.Builder()
        .name("Anti CEV")
        .description("Attacks crystals placed on surround blocks.")
        .defaultValue(false)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Place Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> attackSwing = sgRender.add(new BoolSetting.Builder()
        .name("Attack Swing")
        .description("Renders swing animation when placing a crystal.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> attackHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Attack Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(attackSwing::get)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
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
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<ShapeMode> supportShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Support Shape Mode")
        .description("Which parts of boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> supportLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Support Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 150))
        .build()
    );
    private final Setting<SettingColor> supportSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Support Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );

    private int tickTimer = 0;
    private double timer = 0;
    private final List<BlockPos> insideBlocks = new ArrayList<>();
    public final List<BlockPos> surroundBlocks = new ArrayList<>();
    private final List<BlockPos> supportPositions = new ArrayList<>();
    private final List<BlockPos> positions = new ArrayList<>();
    private final List<BlockPos> valids = new ArrayList<>();
    private final TimerList<BlockPos> placed = new TimerList<>();
    private final List<Render> render = new ArrayList<>();
    private boolean support = false;
    private Hand hand = null;
    private int blocksLeft = 0;
    private int placesLeft = 0;
    private FindItemResult result = null;
    private boolean switched = false;
    private BlockPos lastPos = null;
    private boolean centered = false;
    private long lastAttack = 0;
    private BlockPos currentPos = null;

    public boolean placing = false;
    private boolean doSneak = false;

    @Override
    public void onActivate() {
        tickTimer = placeDelayT.get();
        timer = placeDelayS.get();
        placesLeft = places.get();
        centered = false;
        lastPos = getPos();
        currentPos = getPos();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (event.oldState.getBlock() != event.newState.getBlock() && !BlockUtils.replaceable(event.pos) && surroundBlocks.contains(event.pos)) {
            render.add(new Render(event.pos, System.currentTimeMillis()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        tickTimer++;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        placed.update();

        placing = false;
        timer += event.frameTime;

        lastPos = currentPos;
        currentPos = getPos();

        setBB();

        if (checkToggle()) return;

        updateBlocks();
        updateSupport();

        surroundBlocks.stream().filter(BlockUtils::replaceable).forEach(block -> event.renderer.box(block, sideColor.get(), lineColor.get(), shapeMode.get(), 0));
        supportPositions.forEach(block -> event.renderer.box(block, supportSideColor.get(), supportLineColor.get(), supportShapeMode.get(), 0));
        render.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

        render.forEach(r -> {
            double progress = 1 - Math.min(System.currentTimeMillis() - r.time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

            event.renderer.box(r.pos, Render2DUtils.injectAlpha(sideColor.get(), (int) Math.round(sideColor.get().a * progress)), Render2DUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
        });

        if (pauseEat.get() && mc.player.isUsingItem()) {
            return;
        }

        placeBlocks();
    }

    private void updateAttack() {
        if (!attack.get()) return;
        if (System.currentTimeMillis() - lastAttack < 1000 / attackSpeed.get()) return;

        Entity blocking = getBlocking();

        if (blocking == null) return;

        if (SettingUtils.shouldRotate(RotationType.Attacking) && !Managers.ROTATION.start(blocking.getBoundingBox(), priority - 0.1, RotationType.Attacking, Objects.hash(name + "attacking")))
            return;

        SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
        sendPacket(PlayerInteractEntityC2SPacket.attack(blocking, mc.player.isSneaking()));
        SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.Attacking)) Managers.ROTATION.end(Objects.hash(name + "attacking"));

        if (attackSwing.get()) clientSwing(attackHand.get(), Hand.MAIN_HAND);

        lastAttack = System.currentTimeMillis();
    }

    private Entity getBlocking() {
        Entity crystal = null;
        double lowest = 1000;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (mc.player.distanceTo(entity) > 5) continue;
            if (!SettingUtils.inAttackRange(entity.getBoundingBox())) continue;

            if (antiCev.get()) {
                for (BlockPos pos : surroundBlocks) {
                    if (entity.getBlockPos().equals(pos.up())) {
                        double dmg = Math.max(10, DamageInfo.crystal(mc.player, mc.player.getBoundingBox(), entity.getPos(), null, false));

                        if (dmg < lowest) {
                            lowest = dmg;
                            crystal = entity;
                        }
                    }
                }
            }

            for (BlockPos pos : alwaysAttack.get() ? surroundBlocks : valids) {
                if (!Box.from(new BlockBox(pos)).intersects(entity.getBoundingBox())) continue;

                double dmg = DamageInfo.crystal(mc.player, mc.player.getBoundingBox(), entity.getPos(), null, false);
                if (dmg < lowest) {
                    crystal = entity;
                    lowest = dmg;
                }
            }
        }
        return crystal;
    }

    private void setBB() {
        if (!centered && center.get() && mc.player.isOnGround() && (!phaseCenter.get() || !CombatInfo.inside(mc.player, mc.player.getBoundingBox().shrink(0.01, 0.01, 0.01)))) {
            double targetX, targetZ;

            if (smartCenter.get()) {
                targetX = MathHelper.clamp(mc.player.getX(), currentPos.getX() + 0.31, currentPos.getX() + 0.69);
                targetZ = MathHelper.clamp(mc.player.getZ(), currentPos.getZ() + 0.31, currentPos.getZ() + 0.69);
            } else {
                targetX = currentPos.getX() + 0.5;
                targetZ = currentPos.getZ() + 0.5;
            }

            double dist = new Vec3d(targetX, 0, targetZ).distanceTo(new Vec3d(mc.player.getX(), 0, mc.player.getZ()));

            if (dist < 0.2873) {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetX, mc.player.getY(), targetZ, Managers.ON_GROUND.isOnGround()));
            }

            double x = mc.player.getX(), z = mc.player.getZ();

            for (int i = 0; i < Math.ceil(dist / 0.2873); i++) {
                double yaw = Rotations.getYaw(new Vec3d(targetX, 0, targetZ)) + 90;

                x += Math.cos(Math.toRadians(yaw)) * 0.2873;
                z += Math.sin(Math.toRadians(yaw)) * 0.2873;

                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, mc.player.getY(), z, Managers.ON_GROUND.isOnGround()));
            }

            mc.player.setPos(targetX, mc.player.getY(), targetZ);
            mc.player.setBoundingBox(new Box(targetX - 0.3, mc.player.getY(), targetZ - 0.3, targetX + 0.3, mc.player.getY() + (mc.player.getBoundingBox().maxY - mc.player.getBoundingBox().minY), targetZ + 0.3));

            centered = true;
        }
    }

    private boolean checkToggle() {
        if (lastPos != null) {
            if (toggleMove.get() && (currentPos.getX() != lastPos.getX() || currentPos.getZ() != lastPos.getZ())) {
                toggle();
                sendToggledMsg("moved horizontally");
                return true;
            }

            if (toggleVertical.get() == VerticalToggleMode.Up || toggleVertical.get() == VerticalToggleMode.Any) {
                if (currentPos.getY() > lastPos.getY()) {
                    toggle();
                    sendToggledMsg("moved up");
                    return true;
                }
            }
            if (toggleVertical.get() == VerticalToggleMode.Down || toggleVertical.get() == VerticalToggleMode.Any) {
                if (currentPos.getY() < lastPos.getY()) {
                    toggle();
                    sendToggledMsg("moved down");
                    return true;
                }
            }
        }

        return false;
    }

    private void placeBlocks() {
        List<BlockPos> positions = new ArrayList<>();
        setSupport();

        if (!TimeBomber.shouldBomb()) {
            if (support) positions.addAll(supportPositions);
            else positions.addAll(surroundBlocks);

            valids.clear();
            valids.addAll(positions.stream().filter(this::validBlock).toList());
        }

        updateAttack();

        updateResult();

        updatePlaces();

        blocksLeft = Math.min(placesLeft, result.count());

        hand = getHand();
        switched = false;

        valids.stream().filter(pos -> !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos)), this::validEntity)).sorted(Comparator.comparingDouble(Rotations::getYaw)).forEach(this::place);

        if (switched && hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> InvUtils.pickSwapBack();
                case InvSwitch -> InvUtils.invSwapBack();
            }
        }
    }

    private void updatePlaces() {
        switch (placeDelayMode.get()) {
            case Ticks -> {
                if (placesLeft >= places.get() || tickTimer >= placeDelayT.get()) {
                    placesLeft = places.get();
                    tickTimer = 0;
                }
            }
            case Seconds -> {
                if (placesLeft >= places.get() || timer >= placeDelayS.get()) {
                    placesLeft = places.get();
                    timer = 0;
                }
            }
        }
    }

    private boolean validBlock(BlockPos pos) {
        if (!BlockUtils.replaceable(pos)) return false;

        PlaceData data = onlyConfirmed.get() ? SettingUtils.getPlaceData(pos) : SettingUtils.getPlaceDataOR(pos, placed::contains);
        if (!data.valid()) return false;

        if (!SettingUtils.inPlaceRange(data.pos())) return false;

        return !placed.contains(pos);
    }

    private void place(BlockPos pos) {
        if (blocksLeft <= 0) {
            return;
        }

        PlaceData data = onlyConfirmed.get() ? SettingUtils.getPlaceData(pos) : SettingUtils.getPlaceDataOR(pos, placed::contains);

        if (data == null || !data.valid()) {
            return;
        }

        placing = true;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return;
        }

        if (!switched && hand == null) {
            switch (switchMode.get()) {
                case Normal, Silent -> {
                    InvUtils.swap(result.slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = InvUtils.pickSwitch(result.slot());
                case InvSwitch -> switched = InvUtils.invSwitch(result.slot());
            }
        }

        if (!switched && hand == null) {
            return;
        }

        positions.addAll(surroundBlocks);
        positions.addAll(supportPositions);

        for (BlockPos esuPos : positions) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos wirePos = esuPos.offset(dir);
                if (mc.world.getBlockState(wirePos).getBlock().equals(Blocks.REDSTONE_WIRE)) {
                    // Mine Wire
                    //mine(wirePos);

                    // Packet Sneak
                    doSneak = true;
                }
            }


            if (mc.world.getBlockState(esuPos).getBlock().equals(Blocks.TNT) || mc.world.getBlockState(esuPos).getBlock().equals(Blocks.REDSTONE_WIRE)) {
                mine(esuPos);
            }
        }

        placeBlock(hand == null ? Hand.MAIN_HAND : hand, data.pos().toCenterPos(), data.dir(), data.pos());

        if (placeSwing.get()) clientSwing(placeHand.get(), hand == null ? Hand.MAIN_HAND : hand);

        if (!packet.get()) {
            setBlock(pos);
        }

        placed.add(pos, oneMissing() ? singleCooldown.get() : cooldown.get());
        blocksLeft--;
        placesLeft--;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }
    }

    public void mine(BlockPos pos) {
        if (SettingUtils.shouldRotate(RotationType.Mining))
            Managers.ROTATION.start(pos, priority, RotationType.Mining, Objects.hash(name + "mining"));

        Direction direction = (mc.player.getY() > pos.getY()) ? Direction.UP : Direction.DOWN;
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));

        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);
        else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private boolean oneMissing() {
        boolean alreadyFound = false;

        for (BlockPos pos : surroundBlocks) {
            if (!BlockUtils.replaceable(pos)) continue;

            if (alreadyFound) return false;
            alreadyFound = true;
        }
        return true;
    }

    private void setBlock(BlockPos pos) {
        Item item = mc.player.getInventory().getStack(result.slot()).getItem();

        if (!(item instanceof BlockItem block)) return;

        mc.world.setBlockState(pos, block.getBlock().getDefaultState());
        mc.world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1, false);
    }

    private void setSupport() {
        support = false;
        double min = 10000;

        for (BlockPos pos : surroundBlocks) {
            if (!validBlock(pos)) {
                continue;
            }

            double y = Rotations.getYaw(pos.toCenterPos());

            if (y < min) {
                support = false;
                min = y;
            }
        }

        for (BlockPos pos : supportPositions) {
            if (!validBlock(pos)) {
                continue;
            }

            double y = Rotations.getYaw(pos.toCenterPos());

            if (y < min) {
                support = true;
                min = y;
            }
        }
    }

    private boolean valid(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block && (support ? supportBlocks : blocks).get().contains(block.getBlock());
    }

    private void updateResult() {
        result = switch (switchMode.get()) {
            case Disabled -> null;
            case Normal, Silent -> InvUtils.findInHotbar(this::valid);
            case PickSilent, InvSwitch -> InvUtils.find(this::valid);
        };
    }

    private Hand getHand() {
        if (valid(Managers.HOLDING.getStack())) {
            return Hand.MAIN_HAND;
        }
        if (valid(mc.player.getOffHandStack())) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    private void updateSupport() {
        supportPositions.clear();

        surroundBlocks.forEach(this::addSupport);
    }

    private void addSupport(BlockPos pos) {
        if (!BlockUtils.replaceable(pos)) return;
        if (hasSupport(pos, true)) return;

        PlaceData data = SettingUtils.getPlaceData(pos);
        if (data.valid()) return;

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) {
                continue;
            }

            if (surroundBlocks.contains(pos.offset(dir)) || insideBlocks.contains(pos.offset(dir))) continue;

            if (EntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos.offset(dir))), entity -> entity instanceof PlayerEntity && !entity.isSpectator()))
                continue;
            if (!SettingUtils.getPlaceData(pos.offset(dir)).valid()) continue;
            if (!SettingUtils.inPlaceRange(pos.offset(dir))) continue;

            supportPositions.add(pos.offset(dir));
            return;
        }
    }

    private boolean hasSupport(BlockPos pos, boolean checkNext) {
        for (Direction dir : Direction.values()) {
            if (supportPositions.contains(pos.offset(dir)) || (checkNext && hasSupport(pos.offset(dir), false))) {
                return true;
            }
        }
        return false;
    }

    private void updateBlocks() {
        updateInsideBlocks();
        getSurroundBlocks();

        insideBlocks.forEach(pos -> surroundBlocks.add(pos.down()));
    }

    private void updateInsideBlocks() {
        insideBlocks.clear();

        addBlocks(getPos(), getSize(mc.player));

        if (extend.get()) {
            mc.world.getPlayers().stream().filter(player -> mc.player.distanceTo(player) < 5 && player != mc.player).sorted(Comparator.comparingDouble(player -> mc.player.distanceTo(player))).forEach(player -> {
                if (!intersects(player)) {
                    return;
                }

                addBlocks(player.getBlockPos(), getSize(player));
            });
        }
    }

    private boolean intersects(PlayerEntity player) {
        getSurroundBlocks();

        for (BlockPos pos : surroundBlocks) {
            if (player.getBoundingBox().intersects(Box.from(new BlockBox(pos)))) {
                return true;
            }
        }

        return false;
    }

    private void getSurroundBlocks() {
        surroundBlocks.clear();

        insideBlocks.forEach(pos -> {
            for (Direction dir : Direction.Type.HORIZONTAL) {

                if (!surroundBlocks.contains(pos.offset(dir)) && !insideBlocks.contains(pos.offset(dir))) {
                    surroundBlocks.add(pos.offset(dir));
                }
            }
        });
    }

    private void addBlocks(BlockPos pos, int[] size) {
        for (int x = size[0]; x <= size[1]; x++) {
            for (int z = size[2]; z <= size[3]; z++) {
                BlockPos p = pos.add(x, 0, z);

                if (mc.world.getBlockState(p).getBlock().getBlastResistance() > 600 && !p.equals(currentPos)) continue;

                if (!insideBlocks.contains(pos.add(x, 0, z).withY(currentPos.getY()))) {
                    insideBlocks.add(pos.add(x, 0, z).withY(currentPos.getY()));
                }
            }
        }
    }

    private boolean validEntity(Entity entity) {
        if (entity instanceof EndCrystalEntity && System.currentTimeMillis() - lastAttack < 100) {
            return false;
        }
        return !(entity instanceof ItemEntity);
    }

    private int[] getSize(PlayerEntity player) {
        int[] size = new int[4];

        double x = player.getX() - player.getBlockX();
        double z = player.getZ() - player.getBlockZ();

        if (x < 0.3) {
            size[0] = -1;
        }
        if (x > 0.7) {
            size[1] = 1;
        }
        if (z < 0.3) {
            size[2] = -1;
        }
        if (z > 0.7) {
            size[3] = 1;
        }

        return size;
    }

    public BlockPos getPos() {
        return new BlockPos(mc.player.getBlockX(), (int) Math.round(mc.player.getY()), mc.player.getBlockZ());
    }

    public boolean doSneak() {
        return isActive() && doSneak;
    }

    public record Render(BlockPos pos, long time) {
    }

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }

    public enum VerticalToggleMode {
        Disabled,
        Up,
        Down,
        Any
    }

    public enum PlaceDelayMode {
        Ticks,
        Seconds
    }
}
