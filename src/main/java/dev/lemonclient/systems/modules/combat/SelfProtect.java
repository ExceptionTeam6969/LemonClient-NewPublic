package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.*;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.BlockUpdateEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.DamageInfo;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.TimerList;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import dev.lemonclient.utils.world.hole.HoleUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SelfProtect extends Module {
    public SelfProtect() {
        super(Categories.Combat, "Self Protect", "Covers you if any enemy tries to attack you.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgProtection = settings.createGroup("Protection");
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgBlocks = settings.createGroup("Blocks");
    private final SettingGroup sgAttack = settings.createGroup("Attack");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> oldVer = sgGeneral.add(new BoolSetting.Builder()
        .name("1.12 Crystals")
        .description("Uses 1.12.2 crystal mechanics.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Eat")
        .description("Pause while eating.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Double> mineTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Mine Time")
        .description("How long do we let enemies mine our surround for before protecting it.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> maxMineTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Mine Time")
        .description("Ignores mining after x seconds.")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> packet = sgGeneral.add(new BoolSetting.Builder()
        .name("Packet")
        .description("Send packets to place blocks.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> onlyHole = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Hole")
        .description("Only protects when you are in a hole.")
        .defaultValue(true)
        .build()
    );

    //--------------------Protection--------------------//
    private final Setting<Boolean> surroundFloor = sgProtection.add(new BoolSetting.Builder()
        .name("Surround Floor")
        .description("Places blocks around to surround floor blocks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> surroundFloorBottom = sgProtection.add(new BoolSetting.Builder()
        .name("Surround Floor Bottom")
        .description("Places blocks under surround floor blocks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> surroundSides = sgProtection.add(new BoolSetting.Builder()
        .name("Surround Sides")
        .description("Places blocks next to surround blocks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> surroundTop = sgProtection.add(new BoolSetting.Builder()
        .name("Surround Side Top")
        .description("Places a block on top of surround.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> surroundBottom = sgProtection.add(new BoolSetting.Builder()
        .name("Surround Side Bottom")
        .description("Places a block on bottom of surround.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> trapCev = sgProtection.add(new BoolSetting.Builder()
        .name("Trap Cev")
        .description("Places on top of trap side block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> anvil = sgProtection.add(new BoolSetting.Builder()
        .name("Anvil")
        .description("Automatically prevents Auto Anvil by placing between you and the anvil.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> cev = sgProtection.add(new BoolSetting.Builder()
        .name("Cev")
        .description("Places on top of trap top blocks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> piston = sgProtection.add(new BoolSetting.Builder()
        .name("Piston Push")
        .description("Automatically prevents Auto Anvil by placing between you and the anvil.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> tntAura = sgProtection.add(new BoolSetting.Builder()
        .name("TNT Aura")
        .description("Prevents TNT Aura.")
        .defaultValue(true)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<SurroundPlus.PlaceDelayMode> placeDelayMode = sgSpeed.add(new EnumSetting.Builder<SurroundPlus.PlaceDelayMode>()
        .name("Place Delay Mode")
        .description(".")
        .defaultValue(SurroundPlus.PlaceDelayMode.Ticks)
        .build()
    );
    private final Setting<Integer> placeDelayT = sgSpeed.add(new IntSetting.Builder()
        .name("Place Tick Delay")
        .description("Tick delay between places.")
        .defaultValue(1)
        .min(1)
        .sliderRange(0, 20)
        .visible(() -> placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Ticks)
        .build()
    );
    private final Setting<Double> placeDelayS = sgSpeed.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.1)
        .min(0)
        .sliderRange(0, 1)
        .visible(() -> placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Seconds)
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
        .name("Cooldown")
        .description("Waits x seconds before trying to place at the same position if there is only 1 missing block.")
        .defaultValue(0.5)
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

    //--------------------Damage--------------------//
    private final Setting<Boolean> always = sgGeneral.add(new BoolSetting.Builder()
        .name("Always")
        .description("Doesn't check for damages.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> maxDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Damage")
        .description("Doesn't place if you would take less damage than this.")
        .defaultValue(6)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> !always.get())
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

    private final List<MineStart> mining = new ArrayList<>();
    private MineStart mineStart = null;

    private final List<ProtectBlock> toProtect = new ArrayList<>();
    private final List<BlockPos> placePositions = new ArrayList<>();
    private final List<Render> render = new ArrayList<>();

    private final TimerList<BlockPos> placed = new TimerList<>();
    private int blocksLeft = 0;
    private int placesLeft = 0;
    private FindItemResult result = null;
    private FindItemResult pushResult = null;
    private boolean switched = false;
    private boolean tntAured = false;
    private boolean ceved = false;
    private Hand hand = null;
    private int tickTimer = 0;
    private double timer = 0;
    private long lastTime = 0;
    private long lastAttack = 0;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (event.oldState.getBlock() != event.newState.getBlock() && !BlockUtils.replaceable(event.pos) && placePositions.contains(event.pos)) {
            render.add(new Render(event.pos, System.currentTimeMillis()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPre(TickEvent.Pre event) {
        tickTimer++;
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (!piston.get() || !mc.player.isOnGround()) return;

        switch (switchMode.get()) {
            case Silent -> pushResult = InvUtils.findInHotbar(Items.OBSIDIAN);
            case InvSwitch, PickSilent -> pushResult = InvUtils.find(Items.OBSIDIAN);
        }
        if (!pushResult.found()) return;

        BlockPos eyePos = BlockPos.ofFloored(mc.player.getEyePos());
        if (BlockUtils.solid(eyePos.up())) return;

        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN || dir == Direction.UP || !(mc.world.getBlockState(eyePos.offset(dir)).getBlock() instanceof PistonBlock) && mc.world.getBlockState(eyePos.offset(dir)).getBlock() != Blocks.MOVING_PISTON && mc.world.getBlockState(eyePos.offset(dir)).getBlock() != Blocks.PISTON_HEAD) {
                continue;
            }

            updatePlace(Hand.MAIN_HAND, eyePos.offset(dir.getOpposite()));
            updatePlace(Hand.MAIN_HAND, eyePos.offset(dir).up());
        }
    }

    @EventHandler
    private void onSlowTickPre(TickEvent.Pre event) {
        if (anvil.get()) {
            for (int i = 0; i <= mc.interactionManager.getReachDistance(); i++) {
                BlockPos pos = mc.player.getBlockPos().add(0, i + 3, 0);

                if (mc.world.getBlockState(pos).getBlock() == Blocks.ANVIL && mc.world.getBlockState(pos.down()).isAir()) {
                    BlockPos placePos = pos.down();

                    if (!SettingUtils.shouldAirPlace() && !updateHelper(placePos)) {
                        return;
                    }

                    FindItemResult result = switchMode.get().equals(SwitchMode.Silent) ? InvUtils.findInHotbar(Items.OBSIDIAN) : InvUtils.find(Items.OBSIDIAN);

                    if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(placePos, 15, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
                        return;
                    }

                    boolean switched = switch (switchMode.get()) {
                        case Silent, Normal -> InvUtils.swap(result.slot(), true);
                        case InvSwitch -> InvUtils.invSwitch(result.slot());
                        case PickSilent -> InvUtils.pickSwitch(result.slot());
                        case Disabled -> true;
                    };

                    if (!switched) {
                        return;
                    }

                    placeBlock(placePos, result, true);
                    if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

                    switch (switchMode.get()) {
                        case Silent -> InvUtils.swapBack();
                        case InvSwitch -> InvUtils.invSwapBack();
                        case PickSilent -> InvUtils.pickSwapBack();
                    }

                    if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        Managers.ROTATION.end(Objects.hash(name + "placing"));
                    }

                    break;
                }
            }
        }

        BlockPos top = mc.player.getBlockPos().up(2);

        if (tntAura.get()) {
            if (mc.world.getBlockState(top).getBlock().equals(Blocks.TNT)) {
                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.start(top, priority, RotationType.BlockPlace, Objects.hash(name + "placing"));

                mine(top);

                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.end(Objects.hash(name + "placing"));

                tntAured = true;
            } else if (tntAured) {
                FindItemResult result = InvUtils.findInHotbar(Items.OBSIDIAN);
                PlaceData data = SettingUtils.getPlaceData(top);

                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.start(top, 50, RotationType.BlockPlace, Objects.hash(name + "placing"));

                switch (switchMode.get()) {
                    case Normal, Silent -> {
                        InvUtils.swap(result.slot(), true);
                        switched = true;
                    }
                    case PickSilent -> switched = InvUtils.pickSwitch(result.slot());
                    case InvSwitch -> switched = InvUtils.invSwitch(result.slot());
                }

                placeBlock(hand == null ? Hand.MAIN_HAND : hand, data.pos().toCenterPos(), data.dir(), data.pos());

                switch (switchMode.get()) {
                    case Silent -> InvUtils.swapBack();
                    case PickSilent -> InvUtils.pickSwapBack();
                    case InvSwitch -> InvUtils.invSwapBack();
                }

                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.end(Objects.hash(name + "placing"));

                tntAured = false;
            }
        }

        if (cev.get()) {
            if (mc.world.getBlockState(top).getBlock() == Blocks.OBSIDIAN) {
                Iterator<Entity> iterator = mc.world.getEntities().iterator();

                ceved:
                while (true) {
                    while (true) {
                        if (!iterator.hasNext()) break ceved;

                        Entity crystal = iterator.next();
                        if (crystal instanceof EndCrystalEntity && crystal.getBlockPos().equals(top.up())) {
                            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
                            ceved = true;
                        } else if (ceved) {
                            FindItemResult result = InvUtils.findInHotbar(Items.OBSIDIAN);

                            if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                                Managers.ROTATION.start(top.up(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"));

                            switch (switchMode.get()) {
                                case Normal, Silent -> {
                                    InvUtils.swap(result.slot(), true);
                                    switched = true;
                                }
                                case PickSilent -> switched = InvUtils.pickSwitch(result.slot());
                                case InvSwitch -> switched = InvUtils.invSwitch(result.slot());
                            }

                            placeBlock(top.up(), result, true);

                            switch (switchMode.get()) {
                                case Silent -> InvUtils.swapBack();
                                case PickSilent -> InvUtils.pickSwapBack();
                                case InvSwitch -> InvUtils.invSwapBack();
                            }

                            if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                                Managers.ROTATION.end(Objects.hash(name + "placing"));

                            ceved = false;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        placed.update();

        if (mc.player == null || mc.world == null) return;

        timer += (System.currentTimeMillis() - lastTime) / 1000d;
        lastTime = System.currentTimeMillis();
        updateBlocks();

        if (mineStart != null && contains()) {
            mineStart = null;
        }

        mining.removeIf(m -> System.currentTimeMillis() > m.time + maxMineTime.get() * 1000 || (mineStart != null && m.id == mineStart.id) || !BlockUtils.solid2(m.pos));

        if (mineStart != null) {
            mining.add(mineStart);
            mineStart = null;
        }

        updatePlacing();

        render.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

        render.forEach(r -> {
            double progress = 1 - Math.min(System.currentTimeMillis() - r.time, 500) / 500d;

            event.renderer.box(r.pos, new Color(sideColor.get().r, sideColor.get().g, sideColor.get().b, (int) Math.round(sideColor.get().a * progress)), new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockBreakingProgressS2CPacket p) {
            mineStart = new MineStart(p.getPos(), p.getEntityId(), System.currentTimeMillis());
        }
    }

    private void updatePlacing() {
        if (pauseEat.get() && mc.player.isUsingItem()) return;

        updateResult();

        updatePlaces();

        blocksLeft = Math.min(placesLeft, result.count());

        hand = getHand();
        switched = false;

        placePositions.clear();

        toProtect.stream().filter(this::shouldProtect).forEach(this::addPlacePositions);

        updateAttack();

        placePositions.stream().filter(pos -> !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos)), entity -> entity instanceof EndCrystalEntity && System.currentTimeMillis() - lastAttack > 100)).forEach(this::place);

        if (switched && hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> InvUtils.pickSwapBack();
                case InvSwitch -> InvUtils.invSwapBack();
            }
        }
    }

    private void addPlacePositions(ProtectBlock p) {
        switch (p.type) {
            case 0, 1 -> {
                for (Direction dir : Direction.values()) {
                    if (p.type == 1) {
                        if (!surroundSides.get() && dir.getAxis().isHorizontal()) continue;
                        if (!surroundTop.get() && dir == Direction.UP) continue;
                        if (!surroundBottom.get() && dir == Direction.DOWN) continue;
                    } else {
                        if (dir == Direction.UP) continue;
                        if (!surroundFloor.get() && dir.getAxis().isHorizontal()) continue;
                        if (!surroundFloorBottom.get() && dir == Direction.DOWN) continue;
                    }

                    BlockPos pos = p.pos.offset(dir);

                    if (!BlockUtils.replaceable(pos)) continue;

                    PlaceData data = SettingUtils.getPlaceData(pos);
                    if (!data.valid()) continue;

                    if (!SettingUtils.inPlaceRange(data.pos())) continue;
                    if (EntityUtils.intersectsWithEntity(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), this::validForIntersects))
                        continue;

                    placePositions.add(pos);
                }
            }
            case 2, 3 -> {
                BlockPos pos = p.pos.up();

                if (!BlockUtils.replaceable(pos)) return;

                PlaceData data = SettingUtils.getPlaceData(pos);
                if (!data.valid()) return;

                if (!SettingUtils.inPlaceRange(data.pos())) return;
                if (EntityUtils.intersectsWithEntity(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), this::validForIntersects))
                    return;

                placePositions.add(pos);
            }
        }
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

            for (BlockPos pos : placePositions) {
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

    private Hand getHand() {
        if (valid(Managers.HOLDING.getStack())) {
            return Hand.MAIN_HAND;
        }
        if (valid(mc.player.getOffHandStack())) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    private void updateResult() {
        result = switch (switchMode.get()) {
            case Disabled -> null;
            case Normal, Silent -> InvUtils.findInHotbar(this::valid);
            case PickSilent, InvSwitch -> InvUtils.find(this::valid);
        };
    }

    private boolean valid(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block && blocks.get().contains(block.getBlock());
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

    private void place(BlockPos pos) {
        if (blocksLeft <= 0) {
            return;
        }

        PlaceData data = SettingUtils.getPlaceDataOR(pos, placed::contains);

        if (data == null || !data.valid()) {
            return;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing")))
            return;

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

        if (!switched && hand == null) return;

        placeBlock(hand == null ? Hand.MAIN_HAND : hand, data.pos().toCenterPos(), data.dir(), data.pos());

        if (placeSwing.get()) clientSwing(placeHand.get(), hand == null ? Hand.MAIN_HAND : hand);

        if (!packet.get()) setBlock(pos);

        placed.add(pos, cooldown.get());
        blocksLeft--;
        placesLeft--;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "placing"));
    }

    private void setBlock(BlockPos pos) {
        Item item = mc.player.getInventory().getStack(result.slot()).getItem();

        if (!(item instanceof BlockItem block)) {
            return;
        }

        mc.world.setBlockState(pos, block.getBlock().getDefaultState());
        mc.world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1, false);
    }

    private boolean shouldProtect(ProtectBlock p) {
        BlockPos pos = p.pos;
        switch (p.type) {
            case 1 -> {
                if (!BlockUtils.solid2(pos) || mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) return false;
            }
            case 2, 3 -> {
                if (mc.world.getBlockState(p.pos).getBlock() != Blocks.OBSIDIAN) return false;
                if (!(mc.world.getBlockState(p.pos.up()).getBlock() instanceof AirBlock)) return false;
                if (oldVer.get() && !(mc.world.getBlockState(p.pos.up(2)).getBlock() instanceof AirBlock)) return false;
            }
        }

        if (!containsPos(pos)) return false;
        if (!damageCheck(pos, p.type)) return false;

        return true;
    }

    private void mine(BlockPos blockPos) {
        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);
        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
    }

    private boolean cantBlockPlace(BlockPos blockPos) {
        if (mc.world.getBlockState(blockPos.add(0, 0, 1)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(0, 0, -1)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(1, 0, 0)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(-1, 0, 0)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() == Blocks.AIR) {
            return true;
        }
        return BlockUtils.solid(blockPos);
    }

    private void updatePlace(Hand hand, BlockPos blockPos) {
        if (BlockUtils.solid(blockPos) || cantBlockPlace(blockPos)) return;

        PlaceData data = SettingUtils.getPlaceData(blockPos);
        if (!data.valid()) return;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return;
        }

        boolean switched = switch (switchMode.get()) {
            case Normal, Silent -> InvUtils.swap(pushResult.slot(), true);
            case InvSwitch -> InvUtils.invSwitch(pushResult.slot());
            case PickSilent -> InvUtils.pickSwitch(pushResult.slot());
            case Disabled -> true;
        };

        if (!switched) {
            return;
        }

        placeBlock(hand, data.pos().toCenterPos(), data.dir(), data.pos());
        render.add(new Render(blockPos, System.currentTimeMillis()));

        if (placeSwing.get()) clientSwing(placeHand.get(), hand);

        switch (switchMode.get()) {
            case Silent -> InvUtils.swapBack();
            case InvSwitch -> InvUtils.invSwapBack();
            case PickSilent -> InvUtils.pickSwapBack();
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }
    }

    private void updateBlocks() {
        toProtect.clear();

        if (onlyHole.get() && !HoleUtils.inHole(mc.player)) return;

        BlockPos e = BlockPos.ofFloored(mc.player.getX(), mc.player.getBoundingBox().maxY, mc.player.getZ());
        BlockPos pos = new BlockPos(mc.player.getBlockX(), (int) Math.round(mc.player.getY()), mc.player.getBlockZ());
        int[] size = new int[4];

        double xOffset = mc.player.getX() - mc.player.getBlockX();
        double zOffset = mc.player.getZ() - mc.player.getBlockZ();

        if (xOffset < 0.3) {
            size[0] = -1;
        }
        if (xOffset > 0.7) {
            size[1] = 1;
        }
        if (zOffset < 0.3) {
            size[2] = -1;
        }
        if (zOffset > 0.7) {
            size[3] = 1;
        }

        updateSurround(pos, size);
        if (trapCev.get()) updateEyes(e, size);
        if (cev.get()) updateTop(mc.player.getBlockPos().up(2));
    }

    private void updateTop(BlockPos pos) {
        toProtect.add(new ProtectBlock(pos, 3));
    }

    private void updateEyes(BlockPos pos, int[] size) {
        for (int x = size[0] - 1; x <= size[1] + 1; x++) {
            for (int z = size[2] - 1; z <= size[3] + 1; z++) {
                if (!(x == size[0] - 1 || x == size[1] + 1) || !(z == size[2] - 1 || z == size[3] + 1))
                    toProtect.add(new ProtectBlock(pos.add(x, 0, z), 2));
            }
        }
    }

    private void updateSurround(BlockPos pos, int[] size) {
        for (int y = -1; y <= 0; y++) {
            for (int x = size[0] - 1; x <= size[1] + 1; x++) {
                for (int z = size[2] - 1; z <= size[3] + 1; z++) {
                    boolean bx = x == size[0] - 1 || x == size[1] + 1;
                    boolean by = y == -1;
                    boolean bz = z == size[2] - 1 || z == size[3] + 1;

                    if (by) {
                        if (!bx && !bz) {
                            toProtect.add(new ProtectBlock(pos.add(x, y, z), 0));
                        }
                    } else if (!bx || !bz) toProtect.add(new ProtectBlock(pos.add(x, y, z), 1));
                }
            }
        }
    }

    private boolean validForIntersects(Entity entity) {
        return !(entity instanceof ItemEntity) && !(entity instanceof EndCrystalEntity);
    }

    private boolean damageCheck(BlockPos blockPos, int type) {
        if (always.get()) return true;

        switch (type) {
            case 1 -> {
                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            BlockPos pos = blockPos.add(x, y, z);

                            if (!(mc.world.getBlockState(pos).getBlock() instanceof AirBlock)) continue;
                            if (oldVer.get() && !(mc.world.getBlockState(pos.up()).getBlock() instanceof AirBlock))
                                continue;

                            double self = DamageInfo.crystal(mc.player, mc.player.getBoundingBox(), feet(pos), blockPos, true);
                            if (self >= maxDmg.get()) return true;
                        }
                    }
                }
            }
            case 2, 3 -> {
                if (!(mc.world.getBlockState(blockPos).getBlock() instanceof AirBlock)) return false;
                if (oldVer.get() && !(mc.world.getBlockState(blockPos.up()).getBlock() instanceof AirBlock))
                    return false;

                double self = DamageInfo.crystal(mc.player, mc.player.getBoundingBox(), feet(blockPos.up()), blockPos, true);
                if (self >= maxDmg.get()) return true;
            }
        }

        return false;
    }

    private boolean updateHelper(BlockPos helpPos) {
        updatePlaces();

        if (blocksLeft <= 0) {
            return false;
        }

        for (BlockPos blockPos : getHelper(helpPos)) {
            FindItemResult result = switchMode.get().equals(SwitchMode.Silent) ? InvUtils.findInHotbar(Items.OBSIDIAN) : InvUtils.find(Items.OBSIDIAN);

            if (!result.found()) {
                return true;
            }

            PlaceData data = SettingUtils.getPlaceDataOR(blockPos, placed::contains);

            if (data != null && data.valid()) {
                if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
                    return false;
                }

                boolean switched = switch (switchMode.get()) {
                    case Normal, Silent -> InvUtils.swap(result.slot(), true);
                    case InvSwitch -> InvUtils.invSwitch(result.slot());
                    case PickSilent -> InvUtils.pickSwitch(result.slot());
                    case Disabled -> true;
                };

                if (!switched) {
                    return false;
                }

                placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
                if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

                if (!packet.get()) setBlock(helpPos);

                placed.add(helpPos, cooldown.get());
                blocksLeft--;
                placesLeft--;

                switch (switchMode.get()) {
                    case Silent -> InvUtils.swapBack();
                    case InvSwitch -> InvUtils.invSwapBack();
                    case PickSilent -> InvUtils.pickSwapBack();
                }

                if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                    Managers.ROTATION.end(Objects.hash(name + "placing"));
                }
            }
        }
        return true;
    }

    private List<BlockPos> getHelper(BlockPos block) {
        List<BlockPos> list = new ArrayList<>();
        if (!BlockUtils.replaceable(block)) return list;
        if (SettingUtils.getPlaceData(block).valid()) return list;

        // 1 block support
        Direction support1 = getSupport(block);

        if (support1 != null) {
            render.add(new Render(block.offset(support1), System.currentTimeMillis()));
            if (block.offset(support1) != block && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(support1))), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                list.add(block.offset(support1));
            }
            return list;
        }

        // 2 block support
        for (Direction dir : Direction.values()) {
            if (!BlockUtils.replaceable(block.offset(dir)) || !SettingUtils.inPlaceRange(block.offset(dir))) {
                continue;
            }

            Direction support2 = getSupport(block.offset(dir));

            if (support2 != null) {
                render.add(new Render(block.offset(dir), System.currentTimeMillis()));
                render.add(new Render(block.offset(dir).offset(support2), System.currentTimeMillis()));
                if (block.offset(dir).offset(support2) != block && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(dir).offset(support2))), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                    list.add(block.offset(dir).offset(support2));
                }
                return list;
            }
        }

        return list;
    }

    private Direction getSupport(BlockPos position) {
        Direction cDir = null;
        double cDist = 1000;
        int value = -1;

        for (Direction dir : Direction.values()) {
            PlaceData data = SettingUtils.getPlaceData(position.offset(dir));

            if (!data.valid() || !SettingUtils.inPlaceRange(data.pos())) {
                continue;
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                double dist = mc.player.getEyePos().distanceTo(position.offset(dir).toCenterPos());

                if (dist < cDist || value < 2) {
                    value = 2;
                    cDir = dir;
                    cDist = dist;
                }
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM && entity.getType() != EntityType.END_CRYSTAL)) {
                double dist = mc.player.getEyePos().distanceTo(position.offset(dir).toCenterPos());

                if (dist < cDist || value < 1) {
                    value = 1;
                    cDir = dir;
                    cDist = dist;
                }
            }

        }
        return cDir;
    }

    private Vec3d feet(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    private boolean contains() {
        for (MineStart m : mining) {
            if (m.id == mineStart.id && m.pos.equals(mineStart.pos)) return true;
        }
        return false;
    }

    private boolean containsPos(BlockPos pos) {
        for (MineStart m : mining) {
            if (System.currentTimeMillis() > m.time + mineTime.get() * 1000 && m.pos.equals(pos)) return true;
        }
        return false;
    }

    private record MineStart(BlockPos pos, int id, long time) {
    }

    private record ProtectBlock(BlockPos pos, int type) {
    }

    public record Render(BlockPos pos, long time) {
    }

    public enum SwitchMode {
        Silent,
        Normal,
        PickSilent,
        InvSwitch,
        Disabled
    }
}
