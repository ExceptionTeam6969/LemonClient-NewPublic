package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.LemonClient;
import dev.lemonclient.TimeBomber;
import dev.lemonclient.enums.*;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.EntityInfo;
import dev.lemonclient.utils.player.DamageInfo;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.*;

public class Burrow extends Module {
    public Burrow() {
        super(Categories.Combat, "Burrow", "Places a block inside your feet.");

        LemonClient.EVENT_BUS.subscribe(new Renderer());
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> autoMoveUp = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Move Up")
        .description("Pop you out of the box if you're already in the box.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("Only On Ground")
        .description("Only burrow on ground.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("The mode to switch obsidian.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Block To Use")
        .description("Which blocks used for burrow.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.ENDER_CHEST)
        .build()
    );
    private final Setting<LagBackMode> lagBackMode = sgGeneral.add(new EnumSetting.Builder<LagBackMode>()
        .name("LagBack Mode")
        .description(".")
        .defaultValue(LagBackMode.OBS)
        .build()
    );
    private final Setting<Boolean> autoScaffold = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Scaffold")
        .description("Automatically placed when there is no block under the block you are going to place.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> multiPlace = sgGeneral.add(new IntSetting.Builder()
        .name("Multi Place")
        .description("How many blocks should be placed in 1 tick.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0, 4)
        .build()
    );
    private final Setting<Boolean> fillHead = sgGeneral.add(new BoolSetting.Builder()
        .name("Fill Head")
        .description("Place blocks on your upper body.")
        .defaultValue(false)
        .build()
    );

    //--------------------Attack--------------------//
    private final Setting<Boolean> attack = sgAttack.add(new BoolSetting.Builder()
        .name("Attack")
        .description("Attacks crystals blocking surround.")
        .defaultValue(true)
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
    private final Setting<Double> minHealth = sgAttack.add(new DoubleSetting.Builder()
        .name("Min Health")
        .description("Minimum health for attacking end crystals.")
        .defaultValue(12.0)
        .visible(attack::get)
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
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders a box where the block is placed.")
        .defaultValue(true)
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
        .description("Which parts of the boxes should be rendered.")
        .defaultValue(ShapeMode.Sides)
        .visible(render::get)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> render.get() && shapeMode.get().lines())
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> render.get() && shapeMode.get().sides())
        .build()
    );

    private int count = 0;
    private long lastAttack = 0;
    private boolean burrowed = false;
    private final List<Render> renderBlocks = new ArrayList<>();

    public enum LagBackMode {
        OBS,
        Xin,
        Seija,
        Troll
    }

    public enum SwitchMode {
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }

    @Override
    public void onActivate() {
        count = 0;

        if (autoMoveUp.get() && (burrowed = EntityInfo.isBurrowed(mc.player))) {
            double y = 0;
            double velocity = 0.42;

            while (y < 1.1) {
                y += velocity;
                velocity = (velocity - 0.08) * 0.98;

                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y, mc.player.getZ(), false));
            }

            for (int i = 0; i < 1; i++) {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y + 9, mc.player.getZ(), false));
            }
            toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        count = 0;

        if ((autoMoveUp.get() && burrowed) || (onlyOnGround.get() && !mc.player.isOnGround())) return;

        BlockPos selfPos = null;
        if (!TimeBomber.shouldBomb()) {
            selfPos = getFillBlock();
            if (selfPos == null) {
                toggle();
                return;
            }
        }
        PlaceData data = SettingUtils.getPlaceData(selfPos);
        if (!data.valid()) return;

        boolean headFillMode = selfPos.getY() > mc.player.getY();
        List<Vec3d> fakeJumpOffsets = getFakeJumpOffset(selfPos, headFillMode);
        if (fakeJumpOffsets.size() != 4) {
            toggle();
            return;
        }

        FindItemResult item = switchMode.get().equals(SwitchMode.Normal) || switchMode.get().equals(SwitchMode.Silent) ? InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem()))) : InvUtils.find(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));

        updateAttack(selfPos);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return;
        }

        boolean switched = switch (switchMode.get()) {
            case Normal, Silent -> InvUtils.swap(item.slot(), true);
            case PickSilent -> InvUtils.pickSwitch(item.slot());
            case InvSwitch -> InvUtils.invSwitch(item.slot());
        };

        if (!switched) return;

        doFakeJump(fakeJumpOffsets);

        if (multiPlace.get() == 1) {
            BlockPos supportPos = selfPos.down();
            if (autoScaffold.get() && !BlockUtils.solid(supportPos) && SettingUtils.getPlaceOnDirection(supportPos) != null) {
                dataPlace(Hand.MAIN_HAND, supportPos);
            }

            placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
            addRender(selfPos);
        } else {
            double[] offsets = {0.0d, 0.3d, -0.3d};

            for (double yOffset : headFillMode ? new double[]{0.0d, 1.0d} : new double[]{0.0d}) {
                for (double xOffset : offsets) {
                    for (double zOffset : offsets) {
                        BlockPos blockPos = BlockPos.ofFloored(mc.player.getPos().add(xOffset, yOffset, zOffset));

                        BlockPos supportPos = blockPos.down();
                        if (autoScaffold.get() && !BlockUtils.solid(supportPos) && SettingUtils.getPlaceOnDirection(supportPos) != null) {
                            dataPlace(Hand.MAIN_HAND, supportPos);
                        }

                        if (!BlockUtils.solid(blockPos) && count < multiPlace.get()) {
                            dataPlace(Hand.MAIN_HAND, blockPos);
                            count++;
                        }
                    }
                }
            }
        }

        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        updateLagBack();

        switch (switchMode.get()) {
            case Silent -> InvUtils.swapBack();
            case PickSilent -> InvUtils.pickSwapBack();
            case InvSwitch -> InvUtils.invSwapBack();
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }

        toggle();
    }

    private boolean isAir(Vec3d vec3d) {
        return !BlockUtils.solid(BlockPos.ofFloored(vec3d));
    }

    private void updateAttack(BlockPos blockPos) {
        if (!attack.get()) return;
        if (mc.player.getHealth() < minHealth.get()) return;
        if (System.currentTimeMillis() - lastAttack < 1000 / attackSpeed.get()) return;

        Entity blocking = getBlocking(blockPos);

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

    private BlockPos playerPos(PlayerEntity targetEntity) {
        return new BlockPos((int) Math.floor(targetEntity.getX()), (int) Math.round(targetEntity.getY()), (int) Math.floor(targetEntity.getZ()));
    }

    public double ez() {
        if (BlockUtils.solid(playerPos(mc.player).multiply(3))) {
            return 1.2d;
        }
        for (int i = 4; i < 6; i++) {
            if (BlockUtils.solid(playerPos(mc.player).multiply(i))) {
                return (2.2d + i) - 4.0d;
            }
        }
        return 10.0d;
    }

    private void updateLagBack() {
        switch (lagBackMode.get()) {
            case OBS -> {
                for (int i = 10; i > 0; i--) {
                    if (!BlockUtils.solid(mc.player.getBlockPos().add(0, i, 0)) && isAir(mc.player.getBlockPos().add(0, i, 0).toCenterPos())) {
                        BlockPos lagPos = mc.player.getBlockPos().add(0, i, 0);
                        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(lagPos.getX() + 0.5d, lagPos.getY(), lagPos.getZ() + 0.5d, true));
                    }
                }
            }
            case Xin ->
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 2.0d, this.mc.player.getZ(), true));
            case Seija -> {
                if (mc.player.getY() >= 3.0) {
                    for (int i = -10; i < 10; ++i) {
                        if (i == -1) {
                            i = 4;
                        }
                        if (mc.world.getBlockState(mc.player.getBlockPos().add(0, i, 0)).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(mc.player.getBlockPos().add(0, i + 1, 0)).getBlock().equals(Blocks.AIR)) {
                            final BlockPos pos = mc.player.getBlockPos().add(0, i, 0);
                            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.getX() + 0.3, pos.getY(), pos.getZ() + 0.3, false));
                            return;
                        }
                    }
                }
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 5.0, mc.player.getZ(), false));
            }
            case Troll -> {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 3.3400880035762786, mc.player.getZ(), false));
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 1.0, mc.player.getZ(), false));
            }
        }
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + ez(), this.mc.player.getZ(), true));
    }

    private void doFakeJump(List<Vec3d> offsets) {
        if (offsets == null) {
            return;
        }
        offsets.forEach(vec -> {
            if (vec == null || vec.equals(new Vec3d(0.0d, 0.0d, 0.0d))) {
                return;
            }
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, true));
        });
    }

    private boolean fakeBoxCheckFeet(PlayerEntity player, Vec3d offset) {
        Vec3d futurePos = player.getPos().add(offset);
        return isAir(futurePos.add(0.3, 0.0, 0.3)) && isAir(futurePos.add(-0.3, 0.0, 0.3)) && isAir(futurePos.add(0.3, 0.0, -0.3)) && isAir(futurePos.add(-0.3, 0.0, 0.3));
    }

    private List<Vec3d> getFakeJumpOffset(BlockPos burBlock, boolean headFillMode) {
        List<Vec3d> offsets = new LinkedList<>();
        if (headFillMode) {
            if (fakeBoxCheckFeet(this.mc.player, new Vec3d(0.0d, 2.0d, 0.0d))) {
                Vec3d offVec = getVec3dDirection(burBlock);
                offsets.add(new Vec3d(this.mc.player.getX() + (offVec.x * 0.42132d), this.mc.player.getY() + 0.4199999868869781d, this.mc.player.getZ() + (offVec.z * 0.42132d)));
                offsets.add(new Vec3d(this.mc.player.getX() + (offVec.x * 0.95d), this.mc.player.getY() + 0.7531999805212017d, this.mc.player.getZ() + (offVec.z * 0.95d)));
                offsets.add(new Vec3d(this.mc.player.getX() + (offVec.x * 1.03d), this.mc.player.getY() + 0.9999957640154541d, this.mc.player.getZ() + (offVec.z * 1.03d)));
                offsets.add(new Vec3d(this.mc.player.getX() + (offVec.x * 1.0933d), this.mc.player.getY() + 1.1661092609382138d, this.mc.player.getZ() + (offVec.z * 1.0933d)));
            } else {
                Vec3d offVec2 = getVec3dDirection(burBlock);
                offsets.add(new Vec3d(this.mc.player.getX() + (offVec2.x * 0.42132d), this.mc.player.getY() + 0.12160004615784d, this.mc.player.getZ() + (offVec2.z * 0.42132d)));
                offsets.add(new Vec3d(this.mc.player.getX() + (offVec2.x * 0.95d), this.mc.player.getY() + 0.200000047683716d, this.mc.player.getZ() + (offVec2.z * 0.95d)));
                offsets.add(new Vec3d(this.mc.player.getX() + (offVec2.x * 1.03d), this.mc.player.getY() + 0.200000047683716d, this.mc.player.getZ() + (offVec2.z * 1.03d)));
                offsets.add(new Vec3d(this.mc.player.getX() + (offVec2.x * 1.0933d), this.mc.player.getY() + 0.12160004615784d, this.mc.player.getZ() + (offVec2.z * 1.0933d)));
            }
        } else if (fakeBoxCheckFeet(this.mc.player, new Vec3d(0.0d, 2.0d, 0.0d))) {
            offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.4199999868869781d, this.mc.player.getZ()));
            offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.7531999805212017d, this.mc.player.getZ()));
            offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.9999957640154541d, this.mc.player.getZ()));
            offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 1.1661092609382138d, this.mc.player.getZ()));
        } else {
            Vec3d offVec3 = getVec3dDirection(burBlock);
            offsets.add(new Vec3d(mc.player.getX() + (offVec3.x * 0.42132d), mc.player.getY() + 0.12160004615784d, mc.player.getZ() + (offVec3.z * 0.42132d)));
            offsets.add(new Vec3d(mc.player.getX() + (offVec3.x * 0.95d), mc.player.getY() + 0.200000047683716d, mc.player.getZ() + (offVec3.z * 0.95d)));
            offsets.add(new Vec3d(mc.player.getX() + (offVec3.x * 1.03d), mc.player.getY() + 0.200000047683716d, mc.player.getZ() + (offVec3.z * 1.03d)));
            offsets.add(new Vec3d(mc.player.getX() + (offVec3.x * 1.0933d), mc.player.getY() + 0.12160004615784d, mc.player.getZ() + (offVec3.z * 1.0933d)));
        }
        return offsets;
    }

    public Vec3d getVec3dDirection(BlockPos burBlockPos) {
        BlockPos playerPos = mc.player.getBlockPos();
        Vec3d centerPos = burBlockPos.toCenterPos();
        Vec3d subtracted = mc.player.getPos().subtract(centerPos);
        Vec3d off = Vec3d.ZERO;
        if (Math.abs(subtracted.x) >= Math.abs(subtracted.z) && Math.abs(subtracted.x) > 0.2) {
            if (subtracted.x > 0.0) {
                off = new Vec3d(0.8 - subtracted.x, 0.0, 0.0);
            } else {
                off = new Vec3d(-0.8 - subtracted.x, 0.0, 0.0);
            }
        } else if (Math.abs(subtracted.z) >= Math.abs(subtracted.x) && Math.abs(subtracted.z) > 0.2) {
            if (subtracted.z > 0.0) {
                off = new Vec3d(0.0, 0.0, 0.8 - subtracted.z);
            } else {
                off = new Vec3d(0.0, 0.0, -0.8 - subtracted.z);
            }
        } else if (burBlockPos.equals(playerPos)) {
            List<Direction> facList = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                if (dir == Direction.UP || dir == Direction.DOWN) continue;

                if (!BlockUtils.solid(playerPos.offset(dir)) && !BlockUtils.solid(playerPos.offset(dir).offset(Direction.UP))) {
                    facList.add(dir);
                }
            }
            Vec3d vec3d = Vec3d.ZERO;
            Vec3d[] offVec1 = new Vec3d[1];
            Vec3d[] offVec2 = new Vec3d[1];
            facList.sort((dir1, dir2) -> {
                offVec1[0] = vec3d.add(new Vec3d(dir1.getOffsetX(), dir1.getOffsetY(), dir1.getOffsetZ()).multiply(0.5));
                offVec2[0] = vec3d.add(new Vec3d(dir2.getOffsetX(), dir2.getOffsetY(), dir2.getOffsetZ()).multiply(0.5));
                return (int) (Math.sqrt(mc.player.squaredDistanceTo(offVec1[0].x, mc.player.getY(), offVec1[0].z)) - Math.sqrt(mc.player.squaredDistanceTo(offVec2[0].x, mc.player.getY(), offVec2[0].z)));
            });
            if (facList.size() > 0) {
                off = new Vec3d(facList.get(0).getOffsetX(), facList.get(0).getOffsetY(), facList.get(0).getOffsetZ());
            }
        }
        return off;
    }

    private Entity getBlocking(BlockPos blockPos) {
        Entity crystal = null;
        double lowest = 1000;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (mc.player.distanceTo(entity) > 5) continue;
            if (!SettingUtils.inAttackRange(entity.getBoundingBox())) continue;

            if (!Box.from(new BlockBox(blockPos)).intersects(entity.getBoundingBox())) continue;

            double dmg = DamageInfo.crystal(mc.player, mc.player.getBoundingBox(), entity.getPos(), null, false);
            if (dmg < lowest) {
                crystal = entity;
                lowest = dmg;
            }
        }
        return crystal;
    }

    private BlockPos getFillBlock() {
        List<BlockPos> collect = getFeetBlock(0).stream().filter(blockPos -> !BlockUtils.solid(blockPos)).filter(p -> !cantBlockPlace(p)).limit(1L).toList();
        if (collect.isEmpty()) {
            return null;
        }
        return collect.get(0);
    }

    private boolean cantBlockPlace(BlockPos blockPos) {
        if (mc.world.getBlockState(blockPos.add(0, 0, 1)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(0, 0, -1)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(1, 0, 0)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(-1, 0, 0)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() == Blocks.AIR) {
            return true;
        }
        return !mc.world.getBlockState(blockPos).isAir() && mc.world.getBlockState(blockPos).getBlock() != Blocks.FIRE;
    }

    public LinkedHashSet<BlockPos> getFeetBlock(int yOff) {
        LinkedHashSet<BlockPos> set = new LinkedHashSet<>();
        set.add(BlockPos.ofFloored(mc.player.getPos().add(0.0d, yOff, 0.0d)));
        set.add(BlockPos.ofFloored(mc.player.getPos().add(0.3d, yOff, 0.3d)));
        set.add(BlockPos.ofFloored(mc.player.getPos().add(-0.3d, yOff, 0.3d)));
        set.add(BlockPos.ofFloored(mc.player.getPos().add(0.3d, yOff, -0.3d)));
        set.add(BlockPos.ofFloored(mc.player.getPos().add(-0.3d, yOff, -0.3d)));
        if (fillHead.get() && yOff == 0) {
            set.addAll(getFeetBlock(1));
        }
        return set;
    }

    private void dataPlace(Hand hand, BlockPos blockPos) {
        PlaceData data = SettingUtils.getPlaceData(blockPos);
        if (!data.valid()) return;

        placeBlock(hand, data.pos().toCenterPos(), data.dir(), data.pos());
        addRender(blockPos);
    }

    private void addRender(BlockPos blockPos) {
        renderBlocks.add(new Render(blockPos, System.currentTimeMillis()));
    }

    public record Render(BlockPos blockPos, long time) {
    }

    private class Renderer {
        @EventHandler
        private void onRender(Render3DEvent event) {
            if (!render.get()) return;

            renderBlocks.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

            renderBlocks.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

                event.renderer.box(r.blockPos, Render2DUtils.injectAlpha(sideColor.get(), (int) Math.round(sideColor.get().a * progress)), Render2DUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
            });
        }
    }
}
