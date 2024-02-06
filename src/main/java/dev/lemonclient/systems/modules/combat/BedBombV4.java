package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.DamageInfo;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.RotationUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BedBombV4 extends Module {
    public BedBombV4() {
        super(Categories.Combat, "Bed Bomb V4", "Automatically places and breaks beds to cause damage to your opponents.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlacing = settings.createGroup("Placing");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgBedRefill = settings.createGroup("Bed Re-fill");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> toggleModules = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle Modules")
        .description("Turn off other modules when Cev Breaker is activated.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> toggleBack = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle Back On")
        .description("Turn the modules back on when Cev Breaker is deactivated.")
        .defaultValue(false)
        .visible(toggleModules::get)
        .build()
    );
    private final Setting<List<Module>> modules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("Modules")
        .description("Which modules to toggle.")
        .visible(toggleModules::get)
        .build()
    );
    private final Setting<Boolean> fiveBfiveT = sgGeneral.add(new BoolSetting.Builder()
        .name("5B5T")
        .description("For example requires floor for both bed blocks and allows placing inside entities.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> doubleInteract = sgGeneral.add(new BoolSetting.Builder()
        .name("Double Interact")
        .description("Clicks both bed blocks every time.")
        .defaultValue(true)
        .build()
    );
    private final Setting<LogicMode> logicMode = sgGeneral.add(new EnumSetting.Builder<LogicMode>()
        .name("Logic Mode")
        .description("Logic for bullying kids.")
        .defaultValue(LogicMode.BreakPlace)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<RotationMode> rotMode = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
        .name("Rotation Mode")
        .description("Packet- Sends 1 rotation packet for each bed. Manager- Modifies movement packets to set rotation.")
        .defaultValue(RotationMode.Manager)
        .build()
    );

    //--------------------Placing--------------------//
    private final Setting<SpeedMode> speedMode = sgPlacing.add(new EnumSetting.Builder<SpeedMode>()
        .name("Speed Mode")
        .description("Normal mode should be used in everywhere else than 5B.")
        .defaultValue(SpeedMode.Normal)
        .build()
    );
    private final Setting<Double> speed = sgPlacing.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("How many beds to blow up every second.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> speedMode.get() == SpeedMode.Normal)
        .build()
    );
    private final Setting<Double> damageSpeed = sgPlacing.add(new DoubleSetting.Builder()
        .name("Damage Speed Factor")
        .description("Sets speed to damage multiplied by factor.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> speedMode.get() == SpeedMode.Damage)
        .build()
    );
    private final Setting<Double> maxSpeed = sgPlacing.add(new DoubleSetting.Builder()
        .name("Damage Speed")
        .description("Maximum speed for damage mode.")
        .defaultValue(12)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> speedMode.get() == SpeedMode.Damage)
        .build()
    );

    //--------------------Damage--------------------//
    private final Setting<Double> minDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Damage")
        .description("Minimum damage to place.")
        .defaultValue(8)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Damage")
        .description("Maximum self damage to place.")
        .defaultValue(6)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> maxFriendDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Friend Damage")
        .description("Maximum friend damage to place.")
        .defaultValue(6)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Damage Ratio")
        .description("Minimum damage ratio between self damage and enemy damage.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> minFriendRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Friend Damage Ratio")
        .description("Minimum damage ratio between friend damage and enemy damage.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> forcePop = sgDamage.add(new DoubleSetting.Builder()
        .name("Force Pop")
        .description("Ignores damage checks if enemy would pop after x explodes.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Pop")
        .description("Cancels actions if you would pop after x explodes.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> antiFriendPop = sgDamage.add(new DoubleSetting.Builder()
        .name("Anti Friend Pop")
        .description("Cancels actions if any friend would pop after x explodes.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> friendSacrifice = sgDamage.add(new BoolSetting.Builder()
        .name("Friend Sacrifice")
        .description("Kills your friend if you can also kill any enemy with same bed.")
        .defaultValue(true)
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
        .name("Place Swing")
        .description("Renders swing animation when placing the crafting table.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Place Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> interactSwing = sgRender.add(new BoolSetting.Builder()
        .name("Interact Swing")
        .description("Renders swing animation when interacting with a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> interactHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Interact Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(interactSwing::get)
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
    public final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of the render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of head block.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of head block.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    private int lastIndex = 0;
    private int length = 0;
    private long tickTime = -1;
    private double bestDmg = 0;
    private long lastTime = 0;
    private long lastPlace = 0;

    private BlockPos placePos = null;
    private Direction bedDir = null;
    private PlaceData placeData = null;
    private BlockPos calcPos = null;
    private Direction calcDir = null;
    private PlaceData calcData = null;
    private BlockPos renderPos = null;
    private Direction renderDir = null;
    private BlockPos[] blocks = new BlockPos[]{};
    private final List<PlayerEntity> targets = new ArrayList<>();
    private final List<PlayerEntity> friends = new ArrayList<>();
    private final List<Bed> beds = new ArrayList<>();
    public ArrayList<Module> toActivate;

    private double timer = 0;

    private double dmg;
    private double enemyHP;
    private double self;
    private double selfHP;
    private double friend;
    private double friendHP;

    @EventHandler
    public void onActivate() {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPre(TickEvent.Post event) {
        calculate(length - 1);
        renderPos = calcPos;
        placePos = calcPos;
        renderDir = calcDir;
        bedDir = calcDir;
        placeData = calcData;

        blocks = getBlocks(mc.player.getEyePos(), Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));

        // Reset stuff
        tickTime = System.currentTimeMillis();
        length = blocks.length;
        lastIndex = 0;
        bestDmg = 0;
        calcPos = null;
        calcDir = null;
        calcData = null;

        updateTargets();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        double delta = (System.currentTimeMillis() - lastTime) / 1000f;
        timer += delta;
        lastTime = System.currentTimeMillis();

        List<Bed> toRemove = new ArrayList<>();
        beds.forEach(bed -> {
            if (System.currentTimeMillis() - bed.time > 500) {
                toRemove.add(bed);
            }
        });
        toRemove.forEach(beds::remove);

        if (tickTime < 0) return;

        if (pauseCheck()) {
            updateBedRefill();
            update();
        }

        int index = Math.min((int) Math.ceil((System.currentTimeMillis() - tickTime) / 50f * length), length - 1);
        calculate(index);

        if (renderPos != null && pauseCheck() && renderDir != null) {
            double progress = 1 - Math.min(System.currentTimeMillis() - lastPlace + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

            renderBed(
                event.renderer,
                Render2DUtils.injectAlpha(sideColor.get(), (int) Math.round(sideColor.get().a * progress)),
                Render2DUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * progress))
            );
        }
    }

    private void renderBed(Renderer3D renderer, Color sideColor, Color lineColor) {
        int x = renderPos.getX();
        int y = renderPos.getY();
        int z = renderPos.getZ();

        switch (renderDir) {
            case SOUTH -> renderer.box(x, y, z, x + 1, y + 0.56, z + 2, sideColor, lineColor, shapeMode.get(), 0);
            case NORTH -> renderer.box(x, y, z - 1, x + 1, y + 0.56, z + 1, sideColor, lineColor, shapeMode.get(), 0);
            case WEST -> renderer.box(x - 1, y, z, x + 1, y + 0.56, z + 1, sideColor, lineColor, shapeMode.get(), 0);
            case EAST -> renderer.box(x, y, z, x + 2, y + 0.56, z + 1, sideColor, lineColor, shapeMode.get(), 0);
        }
    }

    private boolean pauseCheck() {
        return !pauseEat.get() || !mc.player.isUsingItem();
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

    private void calculate(int index) {
        BlockPos pos;

        for (int i = lastIndex; i < index; i++) {
            pos = blocks[i];

            damageCalc(pos);

            if (!dmgCheck()) continue;

            for (Direction dir : Direction.Type.HORIZONTAL) {
                PlaceData data = getData(pos, dir);

                if (!data.valid()) continue;

                if (!BlockUtils.replaceable(pos.offset(dir)) && !(mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof BedBlock))
                    continue;

                if (!SettingUtils.inPlaceRange(data.pos())) continue;

                if (!fiveBfiveT.get() && EntityUtils.intersectsWithEntity(new Box(pos.offset(dir)), entity -> !(entity instanceof ItemEntity)))
                    continue;

                calcData = data;
                calcPos = pos;
                calcDir = dir;
                bestDmg = dmg;
            }
        }
        lastIndex = index;
    }

    private void updateTargets() {
        friends.clear();
        targets.clear();

        List<PlayerEntity> players = new ArrayList<>();

        double closestDist = 1000;
        PlayerEntity closest;
        double dist;

        for (int i = 3; i > 0; i--) {

            closest = null;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (players.contains(player) || Friends.get().isFriend(player) || player == mc.player) continue;

                dist = player.distanceTo(mc.player);

                if (dist > 15) continue;

                if (closest == null || dist < closestDist) {
                    closestDist = dist;
                    closest = player;
                }
            }
            if (closest != null) {
                players.add(closest);
                if (Friends.get().isFriend(closest)) {
                    friends.add(closest);
                } else {
                    targets.add(closest);
                }
            }
        }
    }

    private BlockPos[] getBlocks(Vec3d middle, double radius) {
        ArrayList<BlockPos> result = new ArrayList<>();
        int i = (int) Math.ceil(radius);
        BlockPos pos;

        for (int x = -i; x <= i; x++) {
            for (int y = -i; y <= i; y++) {
                for (int z = -i; z <= i; z++) {
                    pos = BlockPos.ofFloored(middle).add(x, y, z);

                    if (!BlockUtils.replaceable(pos) && !(mc.world.getBlockState(pos).getBlock() instanceof BedBlock))
                        continue;

                    if (fiveBfiveT.get() && (mc.world.getBlockState(pos.down()).getBlock() == Blocks.AIR || mc.world.getBlockState(pos.down()).hasBlockEntity()))
                        continue;

                    if (!inRangeToTargets(pos)) continue;
                    result.add(pos);
                }
            }
        }
        return result.toArray(new BlockPos[0]);
    }

    private boolean inRangeToTargets(BlockPos pos) {
        for (PlayerEntity target : targets) {
            if (target.getPos().add(0, 1, 0).distanceTo(pos.toCenterPos()) < 3.5) {
                return true;
            }
        }
        return false;
    }

    private void update() {
        if (placePos == null || placeData == null || !placeData.valid() || bedDir == null) return;

        if (logicMode.get() == LogicMode.PlaceBreak) {
            List<BlockPos> in = interactUpdate();
            if (in != null && !in.isEmpty()) {
                in.forEach(this::removeBedAll);
            }

            if (timer <= 1 / getSpeed()) return;

            if (BlockUtils.replaceable(placePos) && BlockUtils.replaceable(placePos.offset(bedDir)) && placeUpdate()) {
                removeBed(placePos);
                beds.add(new Bed(placePos, placePos.offset(bedDir), true, System.currentTimeMillis()));
                timer = 0;
            }
        } else {
            if (!isBed(placePos) && !isBed(placePos.offset(bedDir)) && placeUpdate()) {
                removeBed(placePos);
                beds.add(new Bed(placePos, placePos.offset(bedDir), true, System.currentTimeMillis()));
            }

            if (timer <= 1 / getSpeed()) return;

            List<BlockPos> in = interactUpdate();
            if (in != null && !in.isEmpty()) {
                in.forEach(this::removeBedAll);
                timer = 0;
            }
        }
    }

    private void removeBedAll(BlockPos pos) {
        List<Bed> toRemove = new ArrayList<>();
        beds.forEach(bed -> {
            if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
                toRemove.add(bed);
            }
        });
        toRemove.forEach(bed -> {
            beds.remove(bed);
            beds.add(new Bed(bed.feetBlock, bed.headBlock, false, System.currentTimeMillis()));
        });
    }

    private void removeBed(BlockPos pos) {
        List<Bed> toRemove = new ArrayList<>();
        beds.forEach(bed -> {
            if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
                toRemove.add(bed);
            }
        });
        toRemove.forEach(beds::remove);
    }

    private void place(Hand hand) {
        placeBlock(hand, placeData.pos().toCenterPos(), placeData.dir(), placeData.pos());
        lastPlace = System.currentTimeMillis();

        if (placeSwing.get()) clientSwing(placeHand.get(), hand);
    }

    private List<BlockPos> interactUpdate() {
        if (doubleInteract.get()) {
            if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(placePos, priority, RotationType.Interact, Objects.hash(name + "explode"))) {
                return null;
            }

            List<BlockPos> list = new ArrayList<>();

            if (isBed(placePos) || isBed(placePos.offset(bedDir))) {
                if (SettingUtils.inPlaceRange(placePos) && interact(placePos)) {
                    list.add(placePos);
                }
                if (SettingUtils.inPlaceRange(placePos.offset(bedDir)) && interact(placePos.offset(bedDir))) {
                    list.add(placePos.offset(bedDir));
                }
            }

            if (SettingUtils.shouldRotate(RotationType.Interact)) {
                Managers.ROTATION.end(Objects.hash(name + "explode"));
            }

            return list;
        }

        BlockPos interactPos = getInteractPos();

        if (interactPos == null) {
            return null;
        }

        Direction interactDir = SettingUtils.getPlaceOnDirection(interactPos);

        if (interactDir == null) {
            return null;
        }

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(interactPos, priority, RotationType.Interact, Objects.hash(name + "explode"))) {
            return null;
        }

        interactBlock(Hand.MAIN_HAND, interactPos.toCenterPos(), interactDir, interactPos);

        if (interactSwing.get()) clientSwing(interactHand.get(), Hand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end(Objects.hash(name + "explode"));
        }
        List<BlockPos> list = new ArrayList<>();
        list.add(interactPos);
        return list;
    }

    private boolean interact(BlockPos pos) {
        Direction dir = SettingUtils.getPlaceOnDirection(pos);

        if (dir == null) {
            return false;
        }

        interactBlock(Hand.MAIN_HAND, pos.toCenterPos(), dir, pos);

        if (interactSwing.get()) clientSwing(interactHand.get(), Hand.MAIN_HAND);
        return true;
    }

    private BlockPos getInteractPos() {
        if (isBed(placePos.offset(bedDir)) && SettingUtils.inPlaceRange(placePos.offset(bedDir)) && SettingUtils.getPlaceOnDirection(placePos.offset(bedDir)) != null) {
            return placePos.offset(bedDir);
        }
        if (isBed(placePos) && SettingUtils.inPlaceRange(placePos) && SettingUtils.getPlaceOnDirection(placePos) != null) {
            return placePos;
        }
        return null;
    }

    private boolean isBed(BlockPos pos) {
        for (Bed bed : beds) {
            if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
                return bed.isBed;
            }
        }
        return mc.world.getBlockState(pos).getBlock() instanceof BedBlock;
    }

    private boolean placeUpdate() {
        Hand hand = Managers.HOLDING.getStack().getItem() instanceof BedItem ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() instanceof BedItem ? Hand.OFF_HAND : null;

        int beds = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() :
            hand == Hand.OFF_HAND ? mc.player.getOffHandStack().getCount() : 0;

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(item -> item.getItem() instanceof BedItem);
                    beds = result.count();
                }
                case PickSilent, InvSwitch -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() instanceof BedItem);
                    beds = result.slot() >= 0 ? result.count() : -1;
                }
            }
        }

        if (beds <= 0) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(placeData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return false;
        }

        boolean switched = hand != null;

        if (rotMode.get() == RotationMode.Packet) {
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(bedDir.getOpposite().asRotation(), Managers.ROTATION.lastDir[1], Managers.ON_GROUND.isOnGround()));
        } else {
            Managers.ROTATION.startYaw(bedDir.getOpposite().asRotation(), priority, RotationType.Other, Objects.hash(name + "placing"));
            if (Math.abs(RotationUtils.yawAngle(Managers.ROTATION.lastDir[0], bedDir.getOpposite().asRotation())) > 45) {
                return false;
            }
        }

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(item -> item.getItem() instanceof BedItem);
                    InvUtils.swap(result.slot(), true);
                    switched = true;
                }
                case PickSilent -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() instanceof BedItem);
                    switched = InvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() instanceof BedItem);
                    switched = InvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {
            return false;
        }

        place(hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> InvUtils.pickSwapBack();
                case InvSwitch -> InvUtils.invSwapBack();
            }
        }
        return true;
    }

    private boolean dmgCheck() {
        if (dmg < bestDmg) {
            return false;
        }

        if (self * antiPop.get() >= selfHP) {
            return false;
        }

        if (!friendSacrifice.get() && friendHP >= 0 && friend * antiFriendPop.get() >= friendHP) {
            return false;
        }
        if (enemyHP >= 0 && dmg * forcePop.get() >= enemyHP) {
            return true;
        }
        if (friendHP >= 0 && friend * antiFriendPop.get() >= friendHP) {
            return false;
        }

        if (dmg < minDmg.get()) {
            return false;
        }

        if (self > maxDmg.get()) {
            return false;
        }
        if (friend > maxFriendDmg.get()) {
            return false;
        }

        if (dmg / self < minRatio.get()) {
            return false;
        }

        return !(friendHP >= 0) || !(dmg / friend < minFriendRatio.get());
    }

    private double getDmg(BlockPos pos) {
        double highest = -1;
        for (PlayerEntity target : targets) {
            highest = Math.max(highest, DamageInfo.bedDamage(target, target.getBoundingBox(), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), null));
        }
        return highest;
    }

    private void damageCalc(BlockPos pos) {
        // Enemy
        double highest = -1;
        double highestHP = -1;
        for (PlayerEntity target : targets) {
            if (target.getHealth() <= 0) continue;

            highest = Math.max(highest, DamageInfo.bedDamage(target, target.getBoundingBox(), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), null));
            highestHP = target.getHealth() + target.getAbsorptionAmount();
        }
        dmg = highest;
        enemyHP = highestHP;

        // Self
        self = DamageInfo.bedDamage(mc.player, mc.player.getBoundingBox(), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), null);
        selfHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();

        // Friend
        highest = -1;
        highestHP = -1;
        for (PlayerEntity friend : friends) {
            if (friend.getHealth() <= 0) continue;

            highest = Math.max(highest, DamageInfo.bedDamage(friend, friend.getBoundingBox(), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), null));
            highestHP = friend.getHealth() + friend.getAbsorptionAmount();
        }
        friend = highest;
        friendHP = highestHP;
    }

    private PlaceData getData(BlockPos pos, Direction dir) {
        if (fiveBfiveT.get()) {
            return SettingUtils.getPlaceDataAND(pos.offset(dir), direction -> direction == Direction.DOWN, pos1 -> !(mc.world.getBlockState(pos1).getBlock() instanceof BedBlock));
        } else {
            return SettingUtils.getPlaceDataAND(pos.offset(dir), direction -> direction != dir, pos1 -> !(mc.world.getBlockState(pos1).getBlock() instanceof BedBlock));
        }
    }

    private double getSpeed() {
        switch (speedMode.get()) {
            case Normal -> {
                return speed.get();
            }
            case Damage -> {
                if (placePos == null) {
                    return maxSpeed.get();
                }

                return Math.min(getDmg(placePos) * damageSpeed.get(), maxSpeed.get());
            }
        }
        return 2;
    }

    public enum LogicMode {
        PlaceBreak,
        BreakPlace
    }

    public enum RotationMode {
        Packet,
        Manager
    }

    public enum SwitchMode {
        Silent,
        Normal,
        PickSilent,
        InvSwitch,
        Disabled
    }

    public enum SpeedMode {
        Normal,
        Damage
    }

    private record Bed(BlockPos feetBlock, BlockPos headBlock, boolean isBed, long time) {
    }
}
