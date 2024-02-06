package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.mixin.IClientPlayerInteractionManager;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.hud.elements.ToastNotificationsHud;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.client.Notifications;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.EntityInfo;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.DamageUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.render.NametagUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockInfo;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CevBreaker extends Module {
    public CevBreaker() {
        super(Categories.Combat, "Cev Breaker", "Break crystals over a ppl's head to deal massive damage!");
    }

    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final SettingGroup sgBreaking = settings.createGroup("Breaking");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgSwing = settings.createGroup("Swing");
    private final SettingGroup sgRenderPlace = settings.createGroup("Render Place");
    private final SettingGroup sgRenderBreak = settings.createGroup("Render Break");
    private final SettingGroup sgNone = settings.createGroup("");


    //--------------------General--------------------//
    private final Setting<Boolean> toggleModules = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-modules")
        .description("Turn off other modules when Cev Breaker is activated.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> toggleBack = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-back-on")
        .description("Turn the modules back on when Cev Breaker is deactivated.")
        .defaultValue(false)
        .visible(toggleModules::get)
        .build()
    );
    private final Setting<List<Module>> modules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("modules")
        .description("Which modules to toggle.")
        .visible(toggleModules::get)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("How to switch to obsidian.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );

    //--------------------Breaking--------------------//
    private final Setting<Mode> mode = sgBreaking.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Which mode to use for breaking the obsidian.")
        .defaultValue(Mode.Packet)
        .build()
    );
    private final Setting<Boolean> smartDelay = sgBreaking.add(new BoolSetting.Builder()
        .name("Smart Delay")
        .description("Waits until the target can get damaged again with breaking the block.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Instant)
        .build()
    );
    private final Setting<Integer> switchDelay = sgBreaking.add(new IntSetting.Builder()
        .name("Switch Delay")
        .description("How many ticks to wait before hitting an entity after switching hotbar slots.")
        .defaultValue(1)
        .range(0, 20)
        .sliderRange(0, 20)
        .visible(() -> mode.get().equals(Mode.Packet))
        .build()
    );

    //--------------------Pause--------------------//
    private final Setting<Double> pauseAtHealth = sgPause.add(new DoubleSetting.Builder()
        .name("Pause Health")
        .description("Pauses when you go below a certain health.")
        .defaultValue(5)
        .min(0)
        .build()
    );
    private final Setting<Boolean> eatPause = sgPause.add(new BoolSetting.Builder()
        .name("Pause On Eat")
        .description("Pauses Crystal Aura when eating.")
        .defaultValue(true)
        .build()
    );

    //--------------------Swing--------------------//
    private final Setting<Boolean> swing = sgSwing.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders your swing client-side.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgSwing.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .build()
    );

    //--------------------Render Place--------------------//
    private final Setting<Boolean> renderPlace = sgRenderPlace.add(new BoolSetting.Builder()
        .name("Render Place")
        .description("Renders the block where it is placed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRenderPlace.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Sides)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRenderPlace.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> renderPlace.get() && (shapeMode.get().equals(ShapeMode.Lines) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRenderPlace.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> renderPlace.get() && (shapeMode.get().equals(ShapeMode.Sides) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );

    //--------------------Render Break--------------------//
    private final Setting<Boolean> renderBreak = sgRenderBreak.add(new BoolSetting.Builder()
        .name("Render Break")
        .description("Renders the block where it is breaking.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> breakShapeMode = sgRenderBreak.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> breakLineColor = sgRenderBreak.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 100))
        .build()
    );
    private final Setting<SettingColor> breakSideColor = sgRenderBreak.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 100))
        .build()
    );

    private final Setting<SettingColor> endBreakLineColor = sgRenderPlace.add(new ColorSetting.Builder()
        .name("End Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(0, 255, 0, 0))
        .visible(() -> renderPlace.get() && (shapeMode.get().equals(ShapeMode.Lines) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> endBreakSideColor = sgRenderPlace.add(new ColorSetting.Builder()
        .name("End Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(0, 255, 0, 50))
        .visible(() -> renderPlace.get() && (shapeMode.get().equals(ShapeMode.Sides) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<Boolean> renderProgress = sgRenderBreak.add(new BoolSetting.Builder()
        .name("Render Progress")
        .description("Renders the progress of breaking block.")
        .defaultValue(true)
        .visible(() -> !mode.get().equals(Mode.Instant))
        .build()
    );
    private final Setting<Double> scale = sgRenderBreak.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("The scale of rendered text")
        .defaultValue(1.5)
        .sliderRange(0.01, 3)
        .visible(() -> !mode.get().equals(Mode.Instant) && renderProgress.get())
        .build()
    );
    private final Setting<SettingColor> miningColor = sgRenderBreak.add(new ColorSetting.Builder()
        .name("Mining Color")
        .description("The text color when obsidian is mining.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> endColor = sgRenderBreak.add(new ColorSetting.Builder()
        .name("End Color")
        .description("The text color when obsidian is complete mining.")
        .defaultValue(new SettingColor(106, 255, 78, 255))
        .build()
    );

    //--------------------Notifications--------------------//
    private final Setting<Notifications.Mode> notification = sgNone.add(new EnumSetting.Builder<Notifications.Mode>()
        .name("Notification")
        .defaultValue(Notifications.Mode.Chat)
        .build()
    );

    private BlockPos blockPos = null;
    private PlayerEntity target;
    private boolean startedYet;
    boolean pause = false;
    private double progress;
    private int switchDelayLeft, timer, breakDelayLeft;
    private final List<PlayerEntity> blacklisted = new ArrayList<>();
    private final List<EndCrystalEntity> crystals = new ArrayList<>();
    private final List<Render> renderPlacing = new ArrayList<>();
    public ArrayList<Module> toActivate;

    public enum Mode {
        Normal,
        Packet,
        Instant
    }

    public enum SwitchMode {
        Silent,
        InvSwitch,
        PickSilent
    }

    @EventHandler
    public void onActivate() {
        target = null;
        startedYet = false;
        switchDelayLeft = 0;
        timer = 0;
        blacklisted.clear();

        toActivate = new ArrayList<>();

        if (toggleModules.get() && !modules.get().isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : modules.get()) {
                if (module.isActive()) {
                    module.toggle();
                    toActivate.add(module);
                }
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (toggleBack.get() && !toActivate.isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : toActivate) {
                if (!module.isActive()) {
                    module.toggle();
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        switchDelayLeft--;
        breakDelayLeft--;
        timer--;
        int crystalSlot = InvUtils.findInHotbar(Items.END_CRYSTAL).slot();
        int obsidianSlot = InvUtils.findInHotbar(Items.OBSIDIAN).slot();
        int pickSlot = InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).slot();
        pickSlot = pickSlot == -1 ? InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).slot() : pickSlot;

        if ((crystalSlot == -1 && !(mc.player.getOffHandStack().getItem() instanceof EndCrystalItem)) || obsidianSlot == -1 || pickSlot == -1) {
            String msg = ("No " + (crystalSlot == -1 && !(mc.player.getOffHandStack().getItem() instanceof EndCrystalItem) ? "crystals" : (obsidianSlot == -1 ? "obsidian" : "pickaxe")) + " found, disabling...");

            switch (notification.get()) {
                case Chat -> warning(msg);
                case Toast -> ToastNotificationsHud.addToast(msg);
                case Notification -> Managers.NOTIFICATION.warn(title, msg);
            }
            toggle();
            return;
        }

        getEntities();

        if (target == null) {
            String msg = "No target found, disabling...";

            switch (notification.get()) {
                case Chat -> error(msg);
                case Toast -> ToastNotificationsHud.addToast(msg);
                case Notification -> Managers.NOTIFICATION.error(title, msg);
            }
            toggle();
            return;
        }

        // Check pause settings
        if ((eatPause.get() && mc.player.isUsingItem()) || PlayerUtils.getTotalHealth() <= pauseAtHealth.get()) {
            pause = true;
            return;
        } else {
            pause = false;
        }

        blockPos = getPlacePos(target);
        if (blockPos == null) return;

        BlockState blockState = mc.world.getBlockState(blockPos);
        boolean crystalThere = false;
        for (EndCrystalEntity crystal : crystals) {      //Check for crystal in right pos
            if (crystal.getBlockPos().add(0, -1, 0).equals(blockPos)) {
                crystalThere = true;
                break;
            }
        }

        //Placing obby
        if (!blockState.isOf(Blocks.OBSIDIAN) && !crystalThere && (mc.player.getMainHandStack().getItem().equals(Items.OBSIDIAN) || switchDelayLeft <= 0)) {
            if (BlockUtils.canPlace(blockPos)) {
                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.start(blockPos, progress, RotationType.BlockPlace, Objects.hash(name + "placing"));

                FindItemResult obsidian = (switchMode.get().equals(SwitchMode.InvSwitch) || switchMode.get().equals(SwitchMode.PickSilent)) ? InvUtils.find(Items.OBSIDIAN) : InvUtils.findInHotbar(Items.OBSIDIAN);

                switch (switchMode.get()) {
                    case Silent -> InvUtils.swap(obsidian.slot(), true);
                    case InvSwitch -> InvUtils.invSwitch(obsidian.slot());
                    case PickSilent -> InvUtils.pickSwitch(obsidian.slot());
                }

                for (BlockPos placePos : getValid(blockPos)) {
                    PlaceData data = SettingUtils.getPlaceData(placePos);
                    placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
                }

                switch (switchMode.get()) {
                    case Silent -> InvUtils.swapBack();
                    case InvSwitch -> InvUtils.invSwapBack();
                    case PickSilent -> InvUtils.pickSwapBack();
                }

                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.end(Objects.hash(name + "placing"));
            } else {
                blacklisted.add(target);
                getEntities();
                if (target == null) {
                    String msg = "Can't place obsidian above the target! Disabling...";

                    switch (notification.get()) {
                        case Chat -> warning(msg);
                        case Toast -> ToastNotificationsHud.addToast(msg);
                        case Notification -> Managers.NOTIFICATION.warn(title, msg);
                    }
                    toggle();
                }
                return;
            }
        }

        if (!BlockUtils.solid(blockPos)) progress = 0D;

        // Placing crystal
        boolean offhand = mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
        boolean mainhand = mc.player.getMainHandStack().getItem() instanceof EndCrystalItem;
        if (!crystalThere && blockState.isOf(Blocks.OBSIDIAN)) {
            if (!(offhand || mainhand || switchDelayLeft <= 0)) return;
            double x = blockPos.up().getX();
            double y = blockPos.up().getY();
            double z = blockPos.up().getZ();

            if (!mc.world.getOtherEntities(null, new Box(x, y, z, x + 1D, y + 2D, z + 1D)).isEmpty()
                || !mc.world.getBlockState(blockPos.up()).isAir()) {
                blacklisted.add(target);
                getEntities();
                /*if (target == null) {
                    String msg = "Can't place the crystal! Disabling...";

                    switch (notification.get()) {
                        case Chat -> warning(msg);
                        case Toast -> ToastNotificationsHud.addToast(msg);
                        case Notification -> Managers.NOTIFICATION.warn(title, msg);
                    }
                    toggle();
                }*/
                return;
            } else {
                if (!offhand && !mainhand) mc.player.getInventory().selectedSlot = crystalSlot;
                Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
                if (swing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.start(blockPos, 25, RotationType.BlockPlace, Objects.hash(name + "interact"));

                sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(mc.player.getPos(), blockPos.getY() < mc.player.getY() ? Direction.UP : Direction.DOWN, blockPos, false), 0));

                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.end(Objects.hash(name + "interact"));
            }
        }

        // Breaking obby
        if (blockState.isAir() && mode.get() == Mode.Packet) startedYet = false;
        if ((mc.player.getInventory().selectedSlot == pickSlot || switchDelayLeft <= 0) && crystalThere && blockState.isOf(Blocks.OBSIDIAN)) {
            Direction direction = EntityInfo.rayTraceCheck(blockPos, true);
            switch (mode.get()) {
                case Instant -> {
                    if (!startedYet) {
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
                        startedYet = true;
                    } else {
                        if (smartDelay.get() && target.hurtTime > 0) return;

                        mc.player.getInventory().selectedSlot = pickSlot;
                        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
                    }
                }
                case Normal -> {
                    if (progress < 1)
                        progress = ((IClientPlayerInteractionManager) mc.interactionManager).getBreakingProgress();

                    mc.player.getInventory().selectedSlot = pickSlot;
                    mc.interactionManager.updateBlockBreakingProgress(blockPos, direction);
                }
                case Packet -> {
                    if (progress < 1) progress += BlockUtils.getBreakDelta(pickSlot, blockState);
                    timer = startedYet ? timer : BlockInfo.getBlockBreakingSpeed(blockState, blockPos, pickSlot);

                    if (!startedYet) {
                        mine(blockPos);
                        startedYet = true;
                    } else if (timer <= 0) {
                        mc.player.getInventory().selectedSlot = pickSlot;
                    }
                }
            }
        }

        // Breaking the crystal
        if (mode.get() == Mode.Packet && breakDelayLeft >= 0) return;
        for (EndCrystalEntity crystal : crystals) {
            if (DamageUtils.crystalDamage(target, crystal.getPos()) >= 6) {
                if (swing.get())
                    clientSwing(placeHand.get(), Hand.MAIN_HAND);
                else sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

                if (SettingUtils.shouldRotate(RotationType.Attacking))
                    Managers.ROTATION.start(crystal.getBoundingBox(), priority, RotationType.Attacking, Objects.hash(name + "attacking"));

                sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false));

                if (SettingUtils.shouldRotate(RotationType.Attacking))
                    Managers.ROTATION.end(Objects.hash(name + "attacking"));

                break;
            }
        }
    }

    private void getEntities() {
        target = null;
        crystals.clear();
        for (Entity entity : mc.world.getEntities()) {
            if (entity.isInRange(mc.player, 6))
                if (entity.isAlive())
                    if (entity instanceof PlayerEntity) {
                        if (entity != mc.player)
                            if (Friends.get().shouldAttack((PlayerEntity) entity))
                                if (target == null || mc.player.distanceTo(entity) < mc.player.distanceTo(target))
                                    if (!blacklisted.contains(entity))
                                        target = (PlayerEntity) entity;
                    } else if (entity instanceof EndCrystalEntity)
                        crystals.add((EndCrystalEntity) entity);
        }
    }

    private void mine(BlockPos blockPos) {
        Direction direction = (mc.player.getY() > blockPos.getY()) ? Direction.UP : Direction.DOWN;

        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));

        if (swing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);
        else sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));

        if (SettingUtils.shouldRotate(RotationType.Mining))
            Managers.ROTATION.start(blockPos, 10, RotationType.Mining, Objects.hash(name + "mining"));
    }

    @EventHandler
    private void onUpdateSelectedSlot(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            switchDelayLeft = 1;
            breakDelayLeft = switchDelay.get();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (renderPlace.get()) {
            renderPlacing.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

            renderPlacing.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time, 700) / 700d;

                event.renderer.box(r.pos, new Color(sideColor.get().r, sideColor.get().g, sideColor.get().b, (int) Math.round(sideColor.get().a * progress)), new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
            });
        }

        if (renderBreak.get()) {
            if (mode.get().equals(Mode.Instant)) return;
            if (blockPos == null) return;

            double min = progress / 2;
            Vec3d vec3d = blockPos.toCenterPos();
            Box box = new Box(vec3d.x - min, vec3d.y - min, vec3d.z - min, vec3d.x + min, vec3d.y + min, vec3d.z + min);

            event.renderer.box(
                box,
                (progress >= 0.98 ? endBreakSideColor.get() : breakSideColor.get()),
                (progress >= 0.98 ? endBreakLineColor.get() : breakLineColor.get()),
                breakShapeMode.get(),
                0
            );
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (blockPos == null || !renderProgress.get() || mode.get().equals(Mode.Instant)) return;

        Vector3d pos = new Vector3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

        if (NametagUtils.to2D(pos, scale.get())) {
            TextRenderer textRenderer = TextRenderer.get();

            NametagUtils.begin(pos);
            textRenderer.begin(1.0, false, true);

            String progressText = mode.get().equals(Mode.Instant) ? (!BlockUtils.solid(blockPos) ? "Waiting" : "Mining") : String.format("%.2f", progress) + "%";
            textRenderer.render(
                progressText,
                -textRenderer.getWidth(progressText) / 2.0,
                0.0,
                (progress >= 0.98 ? endColor.get() : miningColor.get())
            );

            textRenderer.end();
            NametagUtils.end();
        }
    }

    @Override
    public String getInfoString() {
        return target != null ? EntityUtils.getName(target) : null;
    }

    private BlockPos getPlacePos(PlayerEntity player) {
        if (player == null) return null;

        BlockPos pos = player.getBlockPos();
        if (mc.world.getBlockState(pos.up(3)).isAir() && (mc.world.getBlockState(pos.up(2)).isOf(Blocks.OBSIDIAN) || mc.world.getBlockState(pos.up(2)).isOf(Blocks.AIR)))
            return pos.up(2);

        List<BlockPos> posList = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;

            if (mc.world.getBlockState(pos.offset(dir).up(2)).isAir() && (mc.world.getBlockState(pos.offset(dir).up(1)).isOf(Blocks.OBSIDIAN) || mc.world.getBlockState(pos.offset(dir).up(1)).isOf(Blocks.AIR))) {
                posList.add(pos.offset(dir).up(1));
            }
        }

        posList.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        return posList.isEmpty() ? null : posList.get(0);
    }

    private List<BlockPos> getValid(BlockPos block) {
        List<BlockPos> list = new ArrayList<>();

        if (!BlockUtils.replaceable(block)) return list;

        PlaceData data = SettingUtils.getPlaceData(block);

        if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
            renderPlacing.add(new Render(block, System.currentTimeMillis()));
            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block)), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity)) /*&&
                !timers.contains(block)*/) {
                list.add(block);
            }
            return list;
        }

        // 1 block support
        Direction support1 = getSupport(block);

        if (support1 != null) {
            renderPlacing.add(new Render(block, System.currentTimeMillis()));
            renderPlacing.add(new Render(block.offset(support1), System.currentTimeMillis()));
            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(support1))), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))/* &&
                !timers.contains(block.offset(support1))*/) {
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
                renderPlacing.add(new Render(block, System.currentTimeMillis()));
                renderPlacing.add(new Render(block.offset(dir), System.currentTimeMillis()));
                renderPlacing.add(new Render(block.offset(dir).offset(support2), System.currentTimeMillis()));
                if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(dir).offset(support2))), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))/* &&
                    !timers.contains(block.offset(dir).offset(support2))*/) {
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

    public record Render(BlockPos pos, long time) {
    }
}
