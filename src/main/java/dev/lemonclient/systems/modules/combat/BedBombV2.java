package dev.lemonclient.systems.modules.combat;

import com.google.common.util.concurrent.AtomicDouble;
import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.hud.elements.ToastNotificationsHud;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.client.Notifications;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.entity.SortPriority;
import dev.lemonclient.utils.entity.TargetUtils;
import dev.lemonclient.utils.misc.Keybind;
import dev.lemonclient.utils.misc.Pool;
import dev.lemonclient.utils.others.Task;
import dev.lemonclient.utils.player.BedUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.Rotations;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockInfo;
import dev.lemonclient.utils.world.CardinalDirection;
import dev.lemonclient.utils.world.PlaceData;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.lemonclient.utils.misc.Vec3dInfo.closestVec3d;
import static dev.lemonclient.utils.player.BedUtils.*;
import static dev.lemonclient.utils.world.BlockInfo.getBlock;
import static dev.lemonclient.utils.world.BlockInfo.isBlastResist;

public class BedBombV2 extends Module {
    public BedBombV2() {
        super(Categories.Combat, "Bed Bomb V2", "Automatically places and explodes beds in the Nether and End :massivetroll:.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPredict = settings.createGroup("Predict");
    private final SettingGroup sgHelper = settings.createGroup("Helper");
    private final SettingGroup sgTrapBreaker = settings.createGroup("Trap Breaker");
    private final SettingGroup sgBurrowBreaker = settings.createGroup("Burrow Breaker");
    private final SettingGroup sgStringBreaker = settings.createGroup("String Breaker");
    private final SettingGroup sgOther = settings.createGroup("Other");
    private final SettingGroup sgBedRefill = settings.createGroup("Bed Re-fill");
    private final SettingGroup sgRender = settings.createGroup("Render");
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
    public final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Place Delay")
        .description("The delay between placing beds in ticks.")
        .defaultValue(10)
        .sliderRange(0, 20)
        .build()
    );
    public final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Target Range")
        .description("The range at which players can be targeted.")
        .defaultValue(15)
        .sliderRange(1, 25)
        .build()
    );
    public final Setting<Double> minTargetDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("Min Target Damage")
        .description("The minimum damage to inflict on your target.")
        .defaultValue(7)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );
    public final Setting<Double> maxSelfDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Self Damage")
        .description("The maximum damage to inflict on yourself.")
        .defaultValue(4)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );
    public final Setting<Boolean> antiFriendPop = sgGeneral.add(new BoolSetting.Builder()
        .name("Anti Friend Pop")
        .description("Prevents from popping friends.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> maxFriendDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Damage")
        .description("Maximum damage that beds can deal to your friends.")
        .defaultValue(6)
        .range(0, 36)
        .sliderMax(36)
        .visible(antiFriendPop::get)
        .build()
    );
    public final Setting<Boolean> ignoreTerrain = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Terrain")
        .description("Completely ignores terrain if it can be blown up by beds.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("Debug")
        .description("Sends info in chat about calculation.")
        .defaultValue(false)
        .build()
    );

    //--------------------Predict--------------------//
    public final Setting<Boolean> predict = sgPredict.add(new BoolSetting.Builder()
        .name("Predict Position")
        .description("Predicts target position.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Integer> predictIncrease = sgPredict.add(new IntSetting.Builder()
        .name("predict-increase")
        .description("Increasing range from predicted position to target.")
        .defaultValue(2)
        .sliderRange(1, 4)
        .range(1, 4)
        .visible(predict::get)
        .build()
    );
    public final Setting<Boolean> predictCollision = sgPredict.add(new BoolSetting.Builder()
        .name("predict-collision")
        .description("Whether to consider collision when predicting.")
        .defaultValue(true)
        .visible(predict::get)
        .build()
    );

    //--------------------Helper--------------------//
    private final Setting<Boolean> lay = sgHelper.add(new BoolSetting.Builder()
        .name("Auto Lay")
        .description("Auto Lay.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Keybind> forceLay = sgHelper.add(new KeybindSetting.Builder()
        .name("Force Lay")
        .description("AutoLay starts work if the keybind is pressed. Useful agains player with bed instamine.")
        .defaultValue(Keybind.none())
        .build()
    );
    public final Setting<Integer> allowedFails = sgHelper.add(new IntSetting.Builder()
        .name("Fail Times")
        .description("How much AutoLay fails can be dealed.")
        .defaultValue(2)
        .sliderRange(0, 10)
        .range(0, 10)
        .visible(lay::get)
        .build()
    );
    private final Setting<Boolean> zeroTick = sgHelper.add(new BoolSetting.Builder()
        .name("Zero Tick")
        .description("Tries to zero tick your target faster.")
        .defaultValue(true)
        .build()
    );

    //--------------------Trap Breaker--------------------//
    public final Setting<Boolean> tBreakerMain = sgTrapBreaker.add(new BoolSetting.Builder()
        .name("Trap Breaker")
        .description("Breaks targets self trap and prevent re-trapping.")
        .defaultValue(false)
        .build()
    );
    private final Setting<MineMode> tBreakerMode = sgTrapBreaker.add(new EnumSetting.Builder<MineMode>()
        .name("Mine Method")
        .defaultValue(MineMode.Client)
        .visible(tBreakerMain::get)
        .build()
    );
    private final Setting<Boolean> tBreakerSwap = sgTrapBreaker.add(new BoolSetting.Builder()
        .name("Auto Swap")
        .description("Automatically switches to pickaxe slot.")
        .defaultValue(true)
        .visible(tBreakerMain::get)
        .build()
    );
    private final Setting<Boolean> tBreakerOnlySur = sgTrapBreaker.add(new BoolSetting.Builder()
        .name("Surround Only")
        .description("Works only while player is surrounded.")
        .defaultValue(true)
        .visible(tBreakerMain::get)
        .build()
    );
    private final Setting<Boolean> tBreakerGround = sgTrapBreaker.add(new BoolSetting.Builder()
        .name("Only On Ground")
        .description("Works only while player is standing on ground.")
        .defaultValue(true)
        .visible(tBreakerMain::get)
        .build()
    );

    //--------------------Burrow Breaker--------------------//
    public final Setting<Boolean> bBreakerMain = sgBurrowBreaker.add(new BoolSetting.Builder()
        .name("Burrow Breaker")
        .description("Breaks targets burrow and prevent re-burrowing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<MineMode> bBreakerMode = sgBurrowBreaker.add(new EnumSetting.Builder<MineMode>()
        .name("Mine Method")
        .defaultValue(MineMode.Client)
        .visible(bBreakerMain::get)
        .build()
    );
    private final Setting<Boolean> bBreakerSwap = sgBurrowBreaker.add(new BoolSetting.Builder()
        .name("Auto Swap")
        .description("Automatically switches to pickaxe slot.")
        .defaultValue(false)
        .visible(bBreakerMain::get)
        .build()
    );
    private final Setting<Boolean> bBreakerOnlySur = sgBurrowBreaker.add(new BoolSetting.Builder()
        .name("Surround Only")
        .description("Works only while player is surrounded.")
        .defaultValue(true)
        .visible(bBreakerMain::get)
        .build()
    );
    private final Setting<Boolean> bBreakerGround = sgBurrowBreaker.add(new BoolSetting.Builder()
        .name("Only On Ground")
        .description("Works only while player is standing on ground.")
        .defaultValue(true)
        .visible(bBreakerMain::get)
        .build()
    );

    //--------------------String Breaker--------------------//
    public final Setting<Boolean> sBreakerMain = sgStringBreaker.add(new BoolSetting.Builder()
        .name("String Breaker")
        .description("Breaks strings around target.")
        .defaultValue(false)
        .build()
    );
    private final Setting<MineMode> sBreakerMode = sgStringBreaker.add(new EnumSetting.Builder<MineMode>()
        .name("Mine Method")
        .defaultValue(MineMode.Packet)
        .visible(sBreakerMain::get)
        .build()
    );
    private final Setting<Boolean> sBreakerOnlySur = sgStringBreaker.add(new BoolSetting.Builder()
        .name("Surround Only")
        .description("Works only while player is surrounded.")
        .defaultValue(true)
        .visible(sBreakerMain::get)
        .build()
    );
    private final Setting<Boolean> sBreakerGround = sgStringBreaker.add(new BoolSetting.Builder()
        .name("Only On Ground")
        .description("Works only while player is standing on ground.")
        .defaultValue(true)
        .visible(sBreakerMain::get)
        .build()
    );

    //--------------------Other--------------------//
    private final Setting<Boolean> pauseOnUse = sgOther.add(new BoolSetting.Builder()
        .name("Pause On Use")
        .description("Pauses while using items.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pauseOnCA = sgOther.add(new BoolSetting.Builder()
        .name("Pause On CA")
        .description("Pauses while Crystal Aura is activated.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> hurtTime = sgOther.add(new BoolSetting.Builder()
        .name("Hurt Time")
        .description("Place only while target can recieve damage. Not recommended to use this.")
        .defaultValue(false)
        .build()
    );

    //--------------------Bed Re-fill--------------------//
    private final Setting<Boolean> bedRefill = sgBedRefill.add(new BoolSetting.Builder()
        .name("Bed Refill")
        .description("Moves beds into a selected hotbar slot.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> bedSlot = sgBedRefill.add(new IntSetting.Builder()
        .name("Refill Slot")
        .description("The slot auto move moves beds to.")
        .defaultValue(7)
        .range(1, 9)
        .sliderRange(1, 9)
        .visible(bedRefill::get)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders the block where it is placing a bed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("The line color for positions to be placed.")
        .defaultValue(new SettingColor(255, 0, 0, 90))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The side color for positions to be placed.")
        .defaultValue(new SettingColor(255, 0, 0, 10))
        .build()
    );
    private final Setting<SettingColor> textColor = sgRender.add(new ColorSetting.Builder()
        .name("Text Color")
        .description("The text color for positions to be mined.")
        .defaultValue(new SettingColor(255, 0, 0, 10))
        .build()
    );

    //--------------------Notifications--------------------//
    private final Setting<Notifications.Mode> notifications = sgNone.add(new EnumSetting.Builder<Notifications.Mode>()
        .name("Notifications")
        .defaultValue(Notifications.Mode.Chat)
        .build()
    );

    public static ExecutorService cached = Executors.newCachedThreadPool();
    AtomicDouble bestDamage = new AtomicDouble(0);
    private BlockPos finalPos = null;
    int placeTicks, countTicks, failTimes;
    public static PlayerEntity target;
    private CardinalDirection placeDirection;
    double offsetTargetDamage;
    boolean smartLay;
    BlockPos prevBreakPos;
    Boolean Boolean;

    private final Task breakTask = new Task();
    private final Task infoTask = new Task();
    private final Task stageTask = new Task();
    private final Task secondStageTask = new Task();

    private final List<BlockPos> strings = new ArrayList<>();
    private final Pool<RenderText> renderTextPool = new Pool<>(RenderText::new);
    private final List<RenderText> renderTexts = new ArrayList<>();

    private final Pool<RenderBlock> renderBlockPool = new Pool<>(RenderBlock::new);
    private final List<RenderBlock> renderBlocks = new ArrayList<>();

    private final Pool<RenderBreak> renderBreakPool = new Pool<>(RenderBreak::new);
    private final List<RenderBreak> renderBreaks = new ArrayList<>();

    public ArrayList<Module> toActivate;

    public enum MineMode {
        Packet,
        Client
    }

    @Override
    public void onActivate() {
        infoTask.reset();
        breakTask.reset();
        stageTask.reset();
        secondStageTask.reset();
        failTimes = -1;
        finalPos = null;
        placeDirection = null;
        smartLay = true;
        countTicks = placeDelay.get();
        placeTicks = 0;
        bestDamage.set(0);

        for (RenderBlock renderBlock : renderBlocks) renderBlockPool.free(renderBlock);
        renderBlocks.clear();
        for (RenderBreak renderBreak : renderBreaks) renderBreakPool.free(renderBreak);
        renderBreaks.clear();
        for (RenderText renderText : renderTexts) renderTextPool.free(renderText);
        renderTexts.clear();

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
        bestDamage.set(0);

        for (RenderBlock renderBlock : renderBlocks) renderBlockPool.free(renderBlock);
        renderBlocks.clear();
        for (RenderBreak renderBreak : renderBreaks) renderBreakPool.free(renderBreak);
        renderBreaks.clear();
        for (RenderText renderText : renderTexts) renderTextPool.free(renderText);
        renderTexts.clear();

        if (toggleBack.get() && !toActivate.isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : toActivate) {
                if (!module.isActive()) {
                    module.toggle();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1000)
    private void onTick(TickEvent.Pre event) {
        if (mc.world.getDimension().bedWorks()) {
            sendDisableMsg("You can't blow up beds in this dimension.");
            toggle();
            return;
        }

        boolean sHurt;
        countTicks = placeDelay.get();
        placeTicks--;

        renderBlocks.forEach(RenderBlock::tick);
        renderBlocks.removeIf(renderBlock -> renderBlock.ticks <= 0);

        renderBreaks.forEach(RenderBreak::tick);
        renderBreaks.removeIf(renderBreak -> renderBreak.ticks <= 0);

        renderTexts.forEach(RenderText::tick);
        renderTexts.removeIf(renderText -> renderText.ticks <= 0);

        if (pauseOnCA.get() && (Modules.get().get(AutoCrystal.class).isActive() || Modules.get().get(AutoCrystalPlus.class).isActive()))
            return;

        target = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.LowestDistance);
        if (TargetUtils.isBadTarget(target, targetRange.get()) || pauseOnUse.get() && mc.player.isUsingItem()) return;

        if ((lay.get() || forceLay.get().isPressed()) && isSurrounded(target) && !isFaceTrapped(target)) {
            updateCalculateHolePos();

            if (placeDirection != null && finalPos != null) {

                int i = placeDelay.get() <= 9 ? 0 : placeDelay.get() / 2;
                if (failTimes >= allowedFails.get() || smartLay) i = 0;
                countTicks = placeDelay.get() - i;
                if (placeTicks <= 0) {
                    updateBedRefill();
                    updateHolePlace();
                    placeTicks = countTicks;
                }
            }
            return;
        } else if (!isFaceTrapped(target)) {
            smartLay = true;
            failTimes = -1;
        }

        sHurt = target.hurtTime == 0 || !hurtTime.get();

        if (EntityUtils.getTotalHealth(target) <= 11 && zeroTick.get() && !isSurrounded(target)) {
            int i = placeDelay.get() <= 9 ? 0 : placeDelay.get() / 2;
            countTicks = placeDelay.get() - i;
            sHurt = true;
        }

        cached.execute(this::updateCalculatePos);
        if (placeTicks <= 0 && sHurt) {
            if (finalPos == null || placeDirection == null) return;
            updateBedRefill();
            updatePlace();
            placeTicks = countTicks;
        }
    }

    public static boolean isFaceTrapped(LivingEntity entity) {
        return isBlastResist(entity.getBlockPos().south().up())
            && isBlastResist(entity.getBlockPos().west().up())
            && isBlastResist(entity.getBlockPos().east().up())
            && isBlastResist(entity.getBlockPos().north().up());
    }

    @EventHandler
    private void onPreSlowTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        if (TargetUtils.isBadTarget(target, targetRange.get()) || pauseOnUse.get() && mc.player.isUsingItem()) return;

        if (bBreakerMain.get()
            && (!bBreakerGround.get() || mc.player.isOnGround())
            && (!bBreakerOnlySur.get() || isSurrounded(mc.player))
            && BedUtils.shouldBurrowBreak()) {

            BlockPos burrowBp = target.getBlockPos();
            infoTask.run(() -> {
                renderBreaks.add(renderBreakPool.get().set(burrowBp));
                switch (notifications.get()) {
                    case Chat -> info("Burrow Breaker triggered!");
                    case Notification -> Managers.NOTIFICATION.info(title, "Burrow Breaker triggered!");
                    case Toast -> ToastNotificationsHud.addToast("Burrow Breaker triggered!");
                }
            });

            switch (bBreakerMode.get()) {
                case Packet -> packetMine(burrowBp, bBreakerSwap.get(), breakTask);
                case Client -> normalMine(burrowBp, bBreakerSwap.get());
            }
            return;
        } else {
            stageTask.run(() -> {
                infoTask.reset();
                breakTask.reset();
            });

            if (tBreakerMain.get()
                && (!tBreakerGround.get() || mc.player.isOnGround())
                && (!tBreakerOnlySur.get() || isSurrounded(mc.player))
                && BedUtils.shouldTrapBreak()) {

                BlockPos trapBp = BedUtils.getTrapBlock(target, 4.5);

                infoTask.run(() -> {
                    renderBreaks.add(renderBreakPool.get().set(trapBp));
                    switch (notifications.get()) {
                        case Chat -> info("Trap Breaker triggered!");
                        case Notification -> Managers.NOTIFICATION.info(title, "Trap Breaker triggered!");
                        case Toast -> ToastNotificationsHud.addToast("Trap Breaker triggered!");
                    }
                });

                switch (tBreakerMode.get()) {
                    case Packet -> packetMine(trapBp, tBreakerSwap.get(), breakTask);
                    case Client -> normalMine(trapBp, tBreakerSwap.get());
                }
                return;
            } else {
                secondStageTask.run(() -> {
                    infoTask.reset();
                    breakTask.reset();
                });
                if (sBreakerMain.get()
                    && (!sBreakerGround.get() || mc.player.isOnGround())
                    && (!sBreakerOnlySur.get() || isSurrounded(mc.player))
                    && BedUtils.shouldStringBreak()) {

                    strings.clear();
                    for (CardinalDirection dir : CardinalDirection.values()) {
                        BlockPos cPos = target.getBlockPos().up();

                        if (mc.world.getBlockState(cPos).getBlock().asItem().equals(Items.STRING) && mc.player.getPos().distanceTo(cPos.toCenterPos()) < 4.5)
                            strings.add(cPos);

                        if (mc.world.getBlockState(cPos.offset(dir.toDirection())).getBlock().asItem().equals(Items.STRING) && mc.player.getPos().distanceTo(cPos.offset(dir.toDirection()).toCenterPos()) < 4.5)
                            strings.add(cPos.offset(dir.toDirection()));
                    }
                    if (!strings.isEmpty()) {
                        infoTask.run(() -> {
                            switch (notifications.get()) {
                                case Chat -> info("String Breaker triggered!");
                                case Notification -> Managers.NOTIFICATION.info(title, "String Breaker triggered!");
                                case Toast -> ToastNotificationsHud.addToast("String Breaker triggered!");
                            }
                        });
                        for (BlockPos p : strings) {
                            renderTexts.add(renderTextPool.get().set(p, "String"));
                            if (Objects.requireNonNull(sBreakerMode.get()) == MineMode.Packet) {
                                packetMine(p, false, breakTask);
                            } else {
                                normalMine(p, false);
                            }
                        }
                    }
                }
            }
        }

        secondStageTask.reset();
        stageTask.reset();
        infoTask.reset();
        breakTask.reset();
    }

    public static boolean isSurrounded(LivingEntity entity) {
        return isBlastResist(entity.getBlockPos().south())
            && isBlastResist(entity.getBlockPos().west())
            && isBlastResist(entity.getBlockPos().east())
            && isBlastResist(entity.getBlockPos().north())
            && isBlastResist(entity.getBlockPos().down());
    }

    @EventHandler
    public void onBreakPacket(PacketEvent.Receive event) {
        if (!lay.get() || mc.world == null || mc.player == null || target == null || finalPos == null || placeDirection == null)
            return;
        if (event.packet instanceof BlockBreakingProgressS2CPacket packet) {
            BlockPos packetBp = packet.getPos();

            if (packetBp.equals(prevBreakPos) && packet.getProgress() > 0) return;

            Boolean = getBlock(packetBp) instanceof BedBlock;

            if (Boolean && packetBp.equals(finalPos))
                smartLay = false;

            else if (Boolean && packetBp.equals(finalPos.offset(placeDirection.toDirection())))
                smartLay = false;

            prevBreakPos = packetBp;
        }
    }

    private PlaceData getData(BlockPos pos, Direction dir) {
        return SettingUtils.getPlaceDataAND(pos.offset(dir), direction -> direction == Direction.DOWN, pos1 -> !(mc.world.getBlockState(pos1).getBlock() instanceof BedBlock));
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get()) return;
        renderBlocks.sort(Comparator.comparingInt(o -> -o.ticks));
        renderBlocks.forEach(renderBlock -> renderBlock.render(event, sideColor.get(), lineColor.get(), shapeMode.get()));

        renderBreaks.sort(Comparator.comparingInt(o -> -o.ticks));
        renderBreaks.forEach(renderBreak -> renderBreak.render(event, sideColor.get(), lineColor.get(), shapeMode.get()));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!render.get()) return;
        renderTexts.sort(Comparator.comparingInt(o -> -o.ticks));
        renderTexts.forEach(renderText -> renderText.render(event, textColor.get()));
    }

    private void updateBedRefill() {
        if (bedRefill.get()) {
            FindItemResult bedItem = InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem);
            if (bedItem.found() && bedItem.slot() != bedSlot.get() - 1) {
                InvUtils.move().from(bedItem.slot()).toHotbar(bedSlot.get() - 1);
                sendPacket(new UpdateSelectedSlotC2SPacket(bedSlot.get() - 1));
            }
        }
    }

    private void updatePlace() {
        FindItemResult bedItem = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BedItem);
        assert bedItem.isHotbar();

        double yaw = switch (placeDirection) {
            case North -> 180;
            case East -> -90;
            case West -> 90;
            case South -> 0;
        };
        double pitch = Rotations.getPitch(closestVec3d(finalPos));

        Managers.ROTATION.start(yaw, pitch, 8, RotationType.Other, Objects.hash(name + "rotate"));
        //Rotations.rotate(yaw, pitch, 1000000, () -> {
        int prevSlot = mc.player.getInventory().selectedSlot;
        InvUtils.swap(bedItem.slot(), false);
        PlaceData data = getData(finalPos, placeDirection.toDirection());
        if (!data.valid()) return;

        interactBlock(Hand.MAIN_HAND, closestVec3d(finalPos.offset(getcounter(data.dir()))), Direction.UP, finalPos.offset(getcounter(data.dir())));
        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        interactBlock(Hand.OFF_HAND, closestVec3d(finalPos), Direction.UP, finalPos);
        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.OFF_HAND);

        mc.player.getInventory().selectedSlot = prevSlot;

        renderBlocks.add(renderBlockPool.get().set(finalPos, placeDirection));
        bestDamage.set(0);
        finalPos = null;
        placeDirection = null;
        //});
        Managers.ROTATION.end(Objects.hash(name + "rotate"));
    }

    private Direction getcounter(Direction direction) {
        switch (direction) {
            case DOWN -> {
                return Direction.UP;
            }
            case UP -> {
                return Direction.DOWN;
            }
            case NORTH -> {
                return Direction.SOUTH;
            }
            case SOUTH -> {
                return Direction.NORTH;
            }
            case WEST -> {
                return Direction.EAST;
            }
            case EAST -> {
                return Direction.WEST;
            }
        }
        return direction;
    }

    private void updateHolePlace() {
        FindItemResult bedItem = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BedItem);
        if (finalPos == null || !bedItem.isHotbar()) return;

        if (!(mc.world.getBlockState(finalPos).getBlock() instanceof BedBlock)) failTimes++;
        BlockHitResult placeResult = new BlockHitResult(closestVec3d(finalPos), Direction.UP, finalPos, false);
        BlockHitResult breakResult = new BlockHitResult(closestVec3d(finalPos), Direction.UP, finalPos, false);

        double y = switch (placeDirection) {
            case North -> 180;
            case East -> -90;
            case West -> 90;
            case South -> 0;
        };

        Managers.ROTATION.start(y, Rotations.getPitch(closestVec3d(finalPos)), 8, RotationType.Other, Objects.hash(name + "holeplace"));

        int prevSlot = mc.player.getInventory().selectedSlot;
        InvUtils.swap(bedItem.slot(), false);

        if (failTimes >= allowedFails.get()) {
            sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, placeResult, 0));
            if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

            mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, breakResult);
            if (placeSwing.get()) clientSwing(placeHand.get(), Hand.OFF_HAND);
        } else {
            mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, breakResult);
            if (placeSwing.get()) clientSwing(placeHand.get(), Hand.OFF_HAND);

            sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, placeResult, 0));
            if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);
        }

        mc.player.getInventory().selectedSlot = prevSlot;

        renderBlocks.add(renderBlockPool.get().set(finalPos, placeDirection));
        bestDamage.set(0);
        finalPos = null;
        placeDirection = null;

        Managers.ROTATION.end(Objects.hash(name + "holeplace"));
    }

    private void updateCalculateHolePos() {
        long startTime = System.currentTimeMillis();
        if (debug.get()) debug("thread started");
        BlockPos p = target.getBlockPos().up();

        double selfDMG = BedUtils.getDamage(mc.player, p.toCenterPos(), false, false, 0, true);
        double targetDMG = BedUtils.getDamage(target, p.toCenterPos(), false, false, 0, true);

        if (canBed(p.north(), p) && mc.player.getBlockPos().isWithinDistance(p.north(), SettingUtils.getPlaceRange()) && selfDMG < maxSelfDamage.get() && targetDMG > minTargetDamage.get()) {
            finalPos = p.north();
            placeDirection = CardinalDirection.South;
        } else if (canBed(p.south(), p) && mc.player.getBlockPos().isWithinDistance(p.south(), SettingUtils.getPlaceRange()) && selfDMG < maxSelfDamage.get() && targetDMG > minTargetDamage.get()) {
            finalPos = p.south();
            placeDirection = CardinalDirection.North;
        } else if (canBed(p.east(), p) && mc.player.getBlockPos().isWithinDistance(p.east(), SettingUtils.getPlaceRange()) && selfDMG < maxSelfDamage.get() && targetDMG > minTargetDamage.get()) {
            finalPos = p.east();
            placeDirection = CardinalDirection.West;
        } else if (canBed(p.west(), p) && mc.player.getBlockPos().isWithinDistance(p.west(), SettingUtils.getPlaceRange()) && selfDMG < maxSelfDamage.get() && targetDMG > minTargetDamage.get()) {
            finalPos = p.west();
            placeDirection = CardinalDirection.East;
        }
        if (debug.get()) {
            debug("thread shutdown in " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    private void updateCalculatePos() {
        long startTime = System.currentTimeMillis();
        int radius = (int) mc.player.distanceTo(target);
        radius -= 2;
        if (radius < 2) radius = 2;
        if (radius > 6) radius = 6;
        ArrayList<BlockPos> sphere = new ArrayList<>(getTargetSphere(target, radius, 3));
        CardinalDirection localDirection = null;
        BlockPos localPos = null;

        try {
            for (BlockPos p : sphere) {
                offsetTargetDamage = 0;
                //removing bad blocks, better for optimization
                if (intersectsWithEntities(p)) continue;
                if (!mc.player.getBlockPos().isWithinDistance(BlockInfo.closestVec3d(p), SettingUtils.getPlaceRange()) || !mc.world.getBlockState(p).isReplaceable())
                    continue;

                //4 times loop for every direction
                for (CardinalDirection d : CardinalDirection.values()) {
                    PlaceData data = getData(p, d.toDirection());
                    if (!data.valid()) continue;

                    double targetDMG = BedUtils.getDamage(target, p.offset(d.toDirection()).toCenterPos(), predict.get(), predictCollision.get(), predictIncrease.get(), ignoreTerrain.get());
                    double selfDMG = BedUtils.getDamage(mc.player, p.offset(d.toDirection()).toCenterPos(), predict.get(), predictCollision.get(), predictIncrease.get(), ignoreTerrain.get());
                    double friendDMG = 0;

                    if (antiFriendPop.get()) {
                        for (Entity entity : mc.world.getEntities()) {
                            if (entity instanceof PlayerEntity friend && Friends.get().isFriend(friend)) {
                                friendDMG = BedUtils.getDamage(friend, p.offset(d.toDirection()).toCenterPos(), false, false, 1, false);
                            }
                        }
                    }

                    if (!canBed(p, p.offset(d.toDirection()))
                        || selfDMG > maxSelfDamage.get()
                        || targetDMG < minTargetDamage.get()
                        || (friendDMG != 0 && friendDMG > maxFriendDamage.get())) continue;

                    offsetTargetDamage = targetDMG;
                    if (offsetTargetDamage > bestDamage.get()) {
                        bestDamage.set(offsetTargetDamage);
                        localDirection = d;
                        localPos = p.toImmutable();
                    }
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }

        if (localPos == null) return;
        finalPos = localPos;
        placeDirection = localDirection;
        if (debug.get()) {
            debug("thread shutdown in " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    private boolean intersectsWithEntities(BlockPos blockPos) {
        Box box = new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 0.6, blockPos.getZ() + 1);

        return EntityUtils.intersectsWithEntity(box, entity -> entity instanceof PlayerEntity || entity instanceof EndCrystalEntity || entity instanceof TntEntity);
    }

    @Override
    public String getInfoString() {
        return target != null ? target.getGameProfile().getName() : null;
    }
}
