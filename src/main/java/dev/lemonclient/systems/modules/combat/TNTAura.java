package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.TimeBomber;
import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.entity.player.StartBreakingBlockEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.EntityInfo;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import dev.lemonclient.utils.world.hole.HoleUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.lemonclient.utils.world.BlockInfo.getBlock;

public class TNTAura extends Module {
    public TNTAura() {
        super(Categories.Combat, "TNT Aura", "Placing & igniting TNT around enemy");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutoBreak = settings.createGroup("Auto Break");
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgSwing = settings.createGroup("Swing");
    private final SettingGroup sgObsidianRender = settings.createGroup("Obsidian Render");
    private final SettingGroup sgTNTRender = settings.createGroup("TNT Render");
    private final SettingGroup sgBreakRender = settings.createGroup("Break Render");

    //--------------------General--------------------//
    private final Setting<Boolean> face = sgGeneral.add(new BoolSetting.Builder()
        .name("Face Place")
        .description("Places obsidian around target's face.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Place Delay")
        .description("How many ticks between obsidian placement.")
        .defaultValue(1)
        .build()
    );

    //--------------------Auto Break--------------------//
    private final Setting<Boolean> autoBreak = sgAutoBreak.add(new BoolSetting.Builder()
        .name("Auto Break")
        .description("attemps to auto break.")
        .defaultValue(true)
        .build()
    );
    private final Setting<MineMode> breakMode = sgAutoBreak.add(new EnumSetting.Builder<MineMode>()
        .name("Break Mode")
        .defaultValue(MineMode.Normal)
        .visible(autoBreak::get)
        .build()
    );

    //--------------------Switch--------------------//
    private final Setting<Pickaxe> pickaxeSwitch = sgSwitch.add(new EnumSetting.Builder<Pickaxe>()
        .name("Pickaxe Switch")
        .description("Which mode to swap pickaxe.")
        .defaultValue(Pickaxe.Silent)
        .visible(autoBreak::get)
        .build()
    );
    private final Setting<Obsidian> obsidianSwitch = sgSwitch.add(new EnumSetting.Builder<Obsidian>()
        .name("Obsidian Switch")
        .description("Which mode to swap obsidian.")
        .defaultValue(Obsidian.Silent)
        .build()
    );
    private final Setting<TNT> tntSwitch = sgSwitch.add(new EnumSetting.Builder<TNT>()
        .name("TNT Switch")
        .description("Which mode to swap tnt.")
        .defaultValue(TNT.Silent)
        .build()
    );
    private final Setting<Ignite> igniteSwitch = sgSwitch.add(new EnumSetting.Builder<Ignite>()
        .name("Ignite Switch")
        .description("Which mode to swap flint.")
        .defaultValue(Ignite.Silent)
        .build()
    );

    //--------------------Pause--------------------//
    private final Setting<Boolean> burrowPause = sgPause.add(new BoolSetting.Builder()
        .name("Pause On Burrow")
        .description("Pause while target is burrowed")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> antiSelf = sgPause.add(new BoolSetting.Builder()
        .name("Anti Self")
        .description("Pause if target was in your hole")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> holePause = sgPause.add(new BoolSetting.Builder()
        .name("Only In Hole")
        .description("Pause if enemy isnt in hole")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder()
        .name("Pause On Eat")
        .description("Pause while eating.")
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
        .visible(swing::get)
        .build()
    );

    //--------------------Obsidian Render--------------------//
    private final Setting<Boolean> obsidianRender = sgObsidianRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders an overlay where blocks will be placed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> obsidianShapeMode = sgObsidianRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Sides)
        .build()
    );
    private final Setting<SettingColor> obsidianSideColor = sgObsidianRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The side color of the target block rendering.")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> obsidianRender.get() && (obsidianShapeMode.get().equals(ShapeMode.Sides) || obsidianShapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> obsidianLineColor = sgObsidianRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("The line color of the target block rendering.")
        .defaultValue(new SettingColor(255, 255, 255, 190))
        .visible(() -> obsidianRender.get() && (obsidianShapeMode.get().equals(ShapeMode.Lines) || obsidianShapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    /*private final Setting<SettingColor> obsidianNextSideColor = sgObsidianRender.add(new ColorSetting.Builder()
        .name("Next Side Color")
        .description("The side color of the next block to be placed.")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> obsidianRender.get() && (obsidianShapeMode.get().equals(ShapeMode.Sides) || obsidianShapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> obsidianNextLineColor = sgObsidianRender.add(new ColorSetting.Builder()
        .name("Next Line Color")
        .description("The line color of the next block to be placed.")
        .defaultValue(new SettingColor(255, 255, 255, 190))
        .visible(() -> obsidianRender.get() && (obsidianShapeMode.get().equals(ShapeMode.Lines) || obsidianShapeMode.get().equals(ShapeMode.Both)))
        .build()
    );*/

    //--------------------TNT Render--------------------//
    private final Setting<Boolean> tntRender = sgTNTRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders an overlay where blocks will be placed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> tntShapeMode = sgTNTRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> tntSideColor = sgTNTRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The side color of the target block rendering.")
        .defaultValue(new SettingColor(255, 0, 0, 60))
        .visible(() -> tntRender.get() && (tntShapeMode.get().equals(ShapeMode.Sides) || tntShapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> tntLineColor = sgTNTRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("The line color of the target block rendering.")
        .defaultValue(new SettingColor(255, 0, 0, 190))
        .visible(() -> tntRender.get() && (tntShapeMode.get().equals(ShapeMode.Lines) || tntShapeMode.get().equals(ShapeMode.Both)))
        .build()
    );

    //--------------------Break Render--------------------//
    private final Setting<Boolean> breakRender = sgBreakRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders an overlay where blocks will be placed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> breakShapeMode = sgBreakRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> breakSideColor = sgBreakRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The side color of the target block rendering.")
        .defaultValue(new SettingColor(0, 0, 255, 60))
        .build()
    );
    private final Setting<SettingColor> breakLineColor = sgBreakRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("The line color of the target block rendering.")
        .defaultValue(new SettingColor(0, 0, 255, 190))
        .build()
    );

    private PlayerEntity target;
    private final List<BlockPos> obsidianPos = new ArrayList<>();
    private Direction direction;
    private boolean rofl;
    private int ticks;
    private float progress;

    private final List<Render> renderObsidian = new ArrayList<>();
    private final List<Render> renderTNT = new ArrayList<>();

    public enum Pickaxe {
        Normal,
        Silent,
        InvSwitch,
        PickSilent
    }

    public enum Obsidian {
        Silent,
        InvSwitch,
        PickSilent
    }

    public enum TNT {
        Silent,
        InvSwitch,
        PickSilent
    }

    public enum Ignite {
        Silent,
        InvSwitch,
        PickSilent
    }

    public enum MineMode {
        Normal,
        NSilent,
        Instant
    }

    @Override
    public void onActivate() {
        target = null;
        obsidianPos.clear();
        ticks = 0;
        rofl = false;
    }

    @Override
    public void onDeactivate() {
        obsidianPos.clear();
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        direction = event.direction;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult obsidian = (obsidianSwitch.get() == Obsidian.InvSwitch || obsidianSwitch.get() == Obsidian.PickSilent) ? InvUtils.find(Items.OBSIDIAN) : InvUtils.findInHotbar(Items.OBSIDIAN);

        if (!obsidian.found()) {
            obsidianPos.clear();
            sendDisableMsg("No obsidian found");
            toggle();
        }

        FindItemResult flint = (igniteSwitch.get().equals(Ignite.InvSwitch) || igniteSwitch.get().equals(Ignite.PickSilent)) ? InvUtils.find(Items.FLINT_AND_STEEL) : InvUtils.findInHotbar(Items.FLINT_AND_STEEL);

        if (!flint.found()) {
            obsidianPos.clear();
            sendDisableMsg("No flint and steel found");
            toggle();
        }

        FindItemResult tnt = (tntSwitch.get() == TNT.InvSwitch || tntSwitch.get() == TNT.PickSilent) ? InvUtils.find(Items.TNT) : InvUtils.findInHotbar(Items.TNT);

        if (!tnt.found()) {
            obsidianPos.clear();
            sendDisableMsg("No TNT found");
            toggle();
        }

        FindItemResult pickaxe = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);

        if (!pickaxe.found()) {
            obsidianPos.clear();
            sendDisableMsg("No pickaxe found");
            toggle();
        }

        for (PlayerEntity target : mc.world.getPlayers()) {
            if (target == mc.player) continue;
            if (!Friends.get().isFriend(target)) continue;

            this.target = target;

            if (burrowPause.get() && EntityInfo.isBurrowed(target)) {
                obsidianPos.clear();
                sendDisableMsg("Target is burrowed");
                toggle();
            }

            if (antiSelf.get() && isSelf(target)) {
                obsidianPos.clear();
                sendDisableMsg("Target in your hole");
                toggle();
            }

            if (holePause.get() && !HoleUtils.inHole(target)) {
                obsidianPos.clear();
                sendDisableMsg("Target is not surrounded");
                toggle();
            }


            if (pauseOnEat.get() && mc.player.isUsingItem()) return;

            if (allowTNT(target)) {
                placeTNT(target);
                igniteTNT(target.getBlockPos().up(2), flint);
            }

            if (checkMinePos(target.getBlockPos().up(2)) && autoBreak.get()) {
                mine(target.getBlockPos().up(2), pickaxe);
            }

            if (!TimeBomber.shouldBomb()) getObsidianPos(target);
            List<BlockPos> placeList = getValid(obsidianPos);

            if (ticks >= delay.get() && obsidianPos.size() > 0) {
                for (BlockPos blockPos : placeList) {
                    BlockPos shouldRemovePos = obsidianPos.get(obsidianPos.size() - 1);
                    PlaceData data = SettingUtils.getPlaceData(blockPos);

                    if (!SettingUtils.inPlaceRange(blockPos)) return;

                    if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                        Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"));

                    switch (obsidianSwitch.get()) {
                        case Silent -> InvUtils.swap(obsidian.slot(), true);
                        case InvSwitch -> InvUtils.invSwitch(obsidian.slot());
                        case PickSilent -> InvUtils.pickSwitch(obsidian.slot());
                    }

                    placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());

                    if (swing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

                    switch (obsidianSwitch.get()) {
                        case Silent -> InvUtils.swapBack();
                        case InvSwitch -> InvUtils.invSwapBack();
                        case PickSilent -> InvUtils.pickSwapBack();
                    }

                    obsidianPos.remove(shouldRemovePos);

                    ticks = 0;
                }
            } else {
                ticks++;
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (obsidianRender.get()) {
            //for (BlockPos pos : placeList) {
                /*boolean isFirst = pos.equals(obsidianPos.get(placeList.size() - 1));

                Color side = isFirst ? obsidianNextSideColor.get() : obsidianSideColor.get();
                Color line = isFirst ? obsidianNextLineColor.get() : obsidianLineColor.get();*/

            renderObsidian.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

            renderObsidian.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time, 1200) / 1200d;

                event.renderer.box(r.pos, new Color(obsidianSideColor.get().r, obsidianSideColor.get().g, obsidianSideColor.get().b, (int) Math.round(obsidianSideColor.get().a * progress)), new Color(obsidianLineColor.get().r, obsidianLineColor.get().g, obsidianLineColor.get().b, (int) Math.round(obsidianLineColor.get().a * progress)), obsidianShapeMode.get(), 0);
            });
            //}
        }

        if (tntRender.get() && target != null && allowTNT(target)) {
            if (tntBlockstate(target.getBlockPos().add(0, 2, 0))) {
                //event.renderer.box(target.getBlockPos().add(0, 2, 0), tntSideColor.get(), tntLineColor.get(), tntShapeMode.get(), 0);
                renderTNT.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

                renderTNT.forEach(r -> {
                    double progress = 1 - Math.min(System.currentTimeMillis() - r.time, 1200) / 1200d;

                    event.renderer.box(r.pos, new Color(tntSideColor.get().r, tntSideColor.get().g, tntSideColor.get().b, (int) Math.round(tntSideColor.get().a * progress)), new Color(tntLineColor.get().r, tntLineColor.get().g, tntLineColor.get().b, (int) Math.round(tntLineColor.get().a * progress)), tntShapeMode.get(), 0);
                });
            }
        }

        if (breakRender.get() && target != null && autoBreak.get()) {
            BlockPos renderPos = target.getBlockPos().add(0, 2, 0);

            if (checkMinePos(renderPos)) {
                if (!breakMode.get().equals(MineMode.NSilent)) {
                    event.renderer.box(renderPos, breakSideColor.get(), breakLineColor.get(), breakShapeMode.get(), 0);
                } else {
                    double min = progress / 2;
                    Vec3d vec3d = renderPos.toCenterPos();
                    Box box = new Box(vec3d.x - min, vec3d.y - min, vec3d.z - min, vec3d.x + min, vec3d.y + min, vec3d.z + min);

                    event.renderer.box(box, breakSideColor.get(), breakLineColor.get(), breakShapeMode.get(), 0);
                }
            }
        }
    }

    private void getObsidianPos(PlayerEntity target) {
        obsidianPos.clear();
        BlockPos targetPos = target.getBlockPos();

        if (face.get()) {
            add(targetPos.up().offset(Direction.SOUTH));
            add(targetPos.up().offset(Direction.NORTH));
            add(targetPos.up().offset(Direction.EAST));
            add(targetPos.up().offset(Direction.WEST));
        }
        add(targetPos.add(0, 3, 0));
        add(targetPos.add(1, 2, 0));
        add(targetPos.add(-1, 2, 0));
        add(targetPos.add(0, 2, 1));
        add(targetPos.add(0, 2, -1));
    }

    private void placeTNT(PlayerEntity target) {
        FindItemResult tnt = InvUtils.findInHotbar(Items.TNT);
        FindItemResult tntInv = InvUtils.find(Items.TNT);
        BlockPos targetPos = target.getBlockPos();
        BlockPos tntPos = targetPos.add(0, 2, 0);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.start(tntPos, priority, RotationType.BlockPlace, Objects.hash(name + "placing"));

        if (BlockUtils.canPlace(tntPos)) {
            PlaceData data = SettingUtils.getPlaceData(tntPos);

            switch (tntSwitch.get()) {
                case Silent -> InvUtils.swap(tnt.slot(), true);
                case InvSwitch -> InvUtils.invSwitch(tntInv.slot());
                case PickSilent -> InvUtils.pickSwitch(tntInv.slot());
            }

            renderTNT.add(new Render(tntPos, System.currentTimeMillis()));
            placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
            if (swing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

            switch (tntSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case InvSwitch -> InvUtils.invSwapBack();
                case PickSilent -> InvUtils.pickSwapBack();
            }
        }
    }

    private void add(BlockPos blockPos) {
        if (!obsidianPos.contains(blockPos) && BlockUtils.canPlace(blockPos)) obsidianPos.add(blockPos);
    }

    private void igniteTNT(BlockPos pos, FindItemResult item) {
        switch (igniteSwitch.get()) {
            case Silent -> InvUtils.swap(item.slot(), true);
            case InvSwitch -> InvUtils.invSwitch(item.slot());
            case PickSilent -> InvUtils.pickSwitch(item.slot());
        }

        interactBlock(Hand.MAIN_HAND, pos.toCenterPos(), Direction.UP, pos);

        switch (igniteSwitch.get()) {
            case Silent -> InvUtils.swapBack();
            case InvSwitch -> InvUtils.invSwapBack();
            case PickSilent -> InvUtils.pickSwapBack();
        }
    }

    public boolean tntBlockstate(BlockPos pos) {
        return getBlock(pos) == Blocks.AIR || getBlock(pos) == Blocks.TNT;
    }

    public boolean allowTNT(LivingEntity target) {
        assert mc.world != null;

        return BlockUtils.solid(target.getBlockPos().add(1, 2, 0)) && BlockUtils.solid(target.getBlockPos().add(-1, 2, 0)) && !mc.world.isAir(target.getBlockPos().add(0, 2, 1)) && !mc.world.isAir(target.getBlockPos().add(0, 2, -1)) && !mc.world.isAir(target.getBlockPos().add(0, 3, 0));
    }

    public boolean checkMinePos(BlockPos Pos) {
        return getBlock(Pos) != Blocks.AIR
            && getBlock(Pos) != Blocks.TNT
            && getBlock(Pos) != Blocks.BEDROCK;
    }

    public void mine(BlockPos blockPos, FindItemResult item) {
        switch (breakMode.get()) {
            case Normal -> {
                InvUtils.swap(item.slot(), false);
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                if (swing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
            }
            case NSilent -> {
                progress = 0.0f;
                silentMine(false, blockPos, item.slot());

                if (progress < 1.0f) {
                    progress += BlockUtils.getBreakDelta(item.slot(), mc.world.getBlockState(blockPos));
                    if (progress < 1.0f) return;
                }

                silentMine(true, blockPos, item.slot());

                progress = 0.0f;
            }
            case Instant -> {
                InvUtils.swap(item.slot(), false);

                if (!rofl) {
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                    rofl = true;
                }
                if (SettingUtils.shouldRotate(RotationType.Mining)) {
                    Managers.ROTATION.start(blockPos, priority, RotationType.Mining, Objects.hash(name + "mining"));
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
                } else {
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
                }
                sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
    }

    private void silentMine(boolean done, BlockPos blockPos, int slot) {
        switch (pickaxeSwitch.get()) {
            case Normal -> InvUtils.swap(slot, false);
            case Silent -> InvUtils.swap(slot, true);
            case InvSwitch -> InvUtils.invSwitch(slot);
            case PickSilent -> InvUtils.pickSwitch(slot);
        }

        if (SettingUtils.shouldRotate(RotationType.Mining))
            Managers.ROTATION.start(blockPos, priority, RotationType.Mining, Objects.hash(name + "mining"));

        Direction direction = (mc.player.getY() > blockPos.getY()) ? Direction.UP : Direction.DOWN;

        if (!done)
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));

        if (swing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);
        else sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        if (SettingUtils.shouldRotate(RotationType.Mining)) Managers.ROTATION.end(Objects.hash(name + "mining"));

        switch (pickaxeSwitch.get()) {
            case Silent -> InvUtils.swapBack();
            case InvSwitch -> InvUtils.invSwapBack();
            case PickSilent -> InvUtils.pickSwapBack();
        }
    }

    private boolean isSelf(LivingEntity target) {
        return mc.player.getBlockPos().getX() == target.getBlockPos().getX() && mc.player.getBlockPos().getZ() == target.getBlockPos().getZ() && mc.player.getBlockPos().getY() == target.getBlockPos().getY();
    }

    private List<BlockPos> getValid(List<BlockPos> blocks) {
        List<BlockPos> list = new ArrayList<>();

        if (blocks.isEmpty()) return list;

        blocks.forEach(block -> {
            if (!BlockUtils.replaceable(block)) return;

            PlaceData data = SettingUtils.getPlaceData(block);

            if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
                renderObsidian.add(new Render(block, System.currentTimeMillis()));
                if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block)), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                    list.add(block);
                }
                return;
            }

            // 1 block support
            Direction support1 = getSupport(block);

            if (support1 != null) {
                renderObsidian.add(new Render(block, System.currentTimeMillis()));
                renderObsidian.add(new Render(block.offset(support1), System.currentTimeMillis()));
                if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(support1))), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                    list.add(block.offset(support1));
                }
                return;
            }

            // 2 block support
            for (Direction dir : Direction.values()) {
                if (!BlockUtils.replaceable(block.offset(dir)) || !SettingUtils.inPlaceRange(block.offset(dir))) {
                    continue;
                }

                Direction support2 = getSupport(block.offset(dir));

                if (support2 != null) {
                    renderObsidian.add(new Render(block, System.currentTimeMillis()));
                    renderObsidian.add(new Render(block.offset(dir), System.currentTimeMillis()));
                    renderObsidian.add(new Render(block.offset(dir).offset(support2), System.currentTimeMillis()));
                    if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(dir).offset(support2))), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                        list.add(block.offset(dir).offset(support2));
                    }
                    return;
                }
            }
        });
        return list;
    }

    private Direction getSupport(BlockPos position) {
        Direction cDir = null;
        double cDist = 1000;
        int value = -1;

        for (Direction dir : Direction.values()) {
            if (position.offset(dir).equals(target.getBlockPos().up(2))) continue;

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

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }

    public record Render(BlockPos pos, long time) {
    }
}
