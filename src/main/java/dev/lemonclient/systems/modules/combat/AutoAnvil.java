package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.Render3DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.TimerUtils;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoAnvil extends Module {
    public AutoAnvil() {
        super(Categories.Combat, "Auto Anvil", "Automatically places anvils above players to destroy helmets.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgDelay = settings.createGroup("Delay");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("Height")
        .description("The height to place anvils at.")
        .defaultValue(2)
        .range(0, 5)
        .sliderMax(5)
        .build()
    );
    private final Setting<Boolean> placeButton = sgGeneral.add(new BoolSetting.Builder()
        .name("Place At Feet")
        .description("Automatically places a button or pressure plate at the targets feet to break the anvils.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> multiPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("Multi Place")
        .description("Places multiple anvils at once.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> toggleOnBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle On Break")
        .description("Toggles when the target's helmet slot is empty.")
        .defaultValue(false)
        .build()
    );

    //--------------------Switch--------------------//
    private final Setting<SwitchMode> switchMode = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Switching method. Silent is the most reliable but doesn't work everywhere.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<HelperSwitchMode> helperSwitchMode = sgSwitch.add(new EnumSetting.Builder<HelperSwitchMode>()
        .name("Switch Mode")
        .description("Switching method. Silent is the most reliable but doesn't work everywhere.")
        .defaultValue(HelperSwitchMode.Silent)
        .build()
    );

    //--------------------Delay--------------------//
    private final Setting<Integer> delay = sgDelay.add(new IntSetting.Builder()
        .name("Delay")
        .description("The delay in between anvil placements.")
        .defaultValue(10)
        .min(0)
        .sliderMax(50)
        .build()
    );
    private final Setting<Integer> helperDelay = sgDelay.add(new IntSetting.Builder()
        .name("Helper Delay")
        .description("The delay in between helper block placements.")
        .defaultValue(1)
        .min(0)
        .sliderMax(50)
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
        .name("Place Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> renderTargetEsp = sgRender.add(new BoolSetting.Builder()
        .name("Render Target")
        .description("Render on target.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Color")
        .description(COLOR)
        .defaultValue(new SettingColor(149, 149, 149, 170))
        .visible(renderTargetEsp::get)
        .build()
    );
    private final Setting<Boolean> renderAnvil = sgRender.add(new BoolSetting.Builder()
        .name("Render Anvil")
        .description("Renders the anvil where it is placed.")
        .defaultValue(true)
        .visible(() -> !SettingUtils.shouldAirPlace())
        .build()
    );
    private final Setting<Double> anvilRenderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Anvil Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .visible(renderAnvil::get)
        .build()
    );
    private final Setting<Double> anvilFadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Helper Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(renderAnvil::get)
        .build()
    );
    private final Setting<ShapeMode> anvilShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Anvil Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Sides)
        .visible(renderAnvil::get)
        .build()
    );
    private final Setting<SettingColor> anvilLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Anvil Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> renderAnvil.get() && anvilShapeMode.get().lines())
        .build()
    );
    private final Setting<SettingColor> anvilSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Anvil Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> renderAnvil.get() && anvilShapeMode.get().sides())
        .build()
    );
    private final Setting<Boolean> renderHelper = sgRender.add(new BoolSetting.Builder()
        .name("Render Helper")
        .description("Renders the block where it is placed.")
        .defaultValue(true)
        .visible(() -> !SettingUtils.shouldAirPlace())
        .build()
    );
    private final Setting<Double> helperRenderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Helper Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> !SettingUtils.shouldAirPlace() && renderHelper.get())
        .build()
    );
    private final Setting<Double> helperFadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Helper Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> !SettingUtils.shouldAirPlace() && renderHelper.get())
        .build()
    );
    private final Setting<ShapeMode> helperShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Helper Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Sides)
        .visible(() -> !SettingUtils.shouldAirPlace() && renderHelper.get())
        .build()
    );
    private final Setting<SettingColor> helperLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Helper Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> !SettingUtils.shouldAirPlace() && renderHelper.get() && helperShapeMode.get().lines())
        .build()
    );
    private final Setting<SettingColor> helperSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Helper Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> !SettingUtils.shouldAirPlace() && renderHelper.get() && helperShapeMode.get().sides())
        .build()
    );

    private PlayerEntity target;
    private boolean canRenderTarget;

    private int timer;
    private final TimerUtils helperTimer = new TimerUtils();

    private final List<Render> renderAnvilPlacing = new ArrayList<>();
    private final List<Render> renderHelperPlacing = new ArrayList<>();

    public enum SwitchMode {
        Silent,
        InvSwitch,
        PickSilent
    }

    public enum HelperSwitchMode {
        Silent,
        InvSwitch,
        PickSilent
    }

    @Override
    public void onActivate() {
        timer = 0;
        target = null;
        canRenderTarget = false;
        helperTimer.reset();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof AnvilScreen) {
            event.cancel();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (toggleOnBreak.get() && target != null && target.getInventory().getArmorStack(3).isEmpty()) {
            error("Target head slot is empty... disabling.");
            toggle();
            return;
        }

        updateTargets();

        if (placeButton.get()) {
            placeButton();
        }

        if (timer >= delay.get()) {
            timer = 0;

            FindItemResult anvil = InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AnvilBlock);
            if (!anvil.found()) return;

            for (int i = height.get(); i > 1; i--) {
                BlockPos blockPos = target.getBlockPos().up().add(0, i, 0);

                if (!(canRenderTarget = SettingUtils.inPlaceRange(blockPos))) {
                    continue;
                }

                for (int j = 0; j < i; j++) {
                    if (!mc.world.getBlockState(target.getBlockPos().up(j + 1)).isReplaceable()) {
                        break;
                    }
                }

                if (placeAnvil(blockPos, anvil) && !multiPlace.get()) break;
            }
        } else {
            timer++;
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        // JELLO TARGET ESP
        if (renderTargetEsp.get() && target != null && canRenderTarget) {
            Render3DUtils.drawJello(event.matrices, target, color.get());
        }

        // HELPER FADE BOX
        if (renderHelper.get()) {
            renderHelperPlacing.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

            renderHelperPlacing.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time + helperRenderTime.get() * 1000, helperFadeTime.get() * 1000) / (helperFadeTime.get() * 1000d);

                event.renderer.box(r.pos, Render2DUtils.injectAlpha(helperSideColor.get(), (int) Math.round(helperSideColor.get().a * progress)), Render2DUtils.injectAlpha(helperLineColor.get(), (int) Math.round(helperLineColor.get().a * progress)), helperShapeMode.get(), 0);
            });
        }

        // ANVIL FADE BOX
        if (renderAnvil.get()) {
            renderAnvilPlacing.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

            renderAnvilPlacing.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time + anvilRenderTime.get() * 1000, anvilFadeTime.get() * 1000) / (anvilFadeTime.get() * 1000d);

                event.renderer.box(r.pos, Render2DUtils.injectAlpha(anvilSideColor.get(), (int) Math.round(anvilSideColor.get().a * progress)), Render2DUtils.injectAlpha(anvilLineColor.get(), (int) Math.round(anvilLineColor.get().a * progress)), anvilShapeMode.get(), 0);
            });
        }
    }

    private void placeButton() {
        FindItemResult result = !switchMode.get().equals(SwitchMode.Silent) ? InvUtils.find(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractPressurePlateBlock || Block.getBlockFromItem(itemStack.getItem()) instanceof ButtonBlock) : InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractPressurePlateBlock || Block.getBlockFromItem(itemStack.getItem()) instanceof ButtonBlock);
        if (!result.found()) return;

        BlockPos placePos = target.getBlockPos();
        if (!(canRenderTarget = SettingUtils.inPlaceRange(placePos))) {
            return;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(placePos, 0, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return;
        }

        switch (switchMode.get()) {
            case Silent -> InvUtils.swap(result.slot(), true);
            case InvSwitch -> InvUtils.invSwitch(result.slot());
            case PickSilent -> InvUtils.pickSwitch(result.slot());
        }

        placeBlock(placePos, result, false);
        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        switch (switchMode.get()) {
            case Silent -> InvUtils.swapBack();
            case InvSwitch -> InvUtils.invSwapBack();
            case PickSilent -> InvUtils.pickSwapBack();
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }
    }

    private boolean placeAnvil(BlockPos blockPos, FindItemResult result) {
        if (!SettingUtils.shouldAirPlace() && !placeHelper(blockPos)) {
            return false;
        }

        PlaceData data = SettingUtils.getPlaceData(blockPos);
        if (data.valid()) {
            if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
                return false;
            }

            boolean switched = switch (switchMode.get()) {
                case Silent -> InvUtils.swap(result.slot(), true);
                case InvSwitch -> InvUtils.invSwitch(result.slot());
                case PickSilent -> InvUtils.pickSwitch(result.slot());
            };

            if (!switched) {
                return false;
            }

            placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());

            renderAnvilPlacing.add(new Render(blockPos, System.currentTimeMillis()));

            if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case InvSwitch -> InvUtils.invSwapBack();
                case PickSilent -> InvUtils.pickSwapBack();
            }

            if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                Managers.ROTATION.end(Objects.hash(name + "placing"));
            }
        }

        return true;
    }

    private boolean placeHelper(BlockPos helpBlockPos) {
        if (!helperTimer.passedMs(helperDelay.get().longValue())) return false;

        for (BlockPos blockPos : getHelper(helpBlockPos)) {
            FindItemResult result = !helperSwitchMode.get().equals(HelperSwitchMode.Silent) ? InvUtils.find(Items.OBSIDIAN) : InvUtils.findInHotbar(Items.OBSIDIAN);

            if (!result.found()) {
                return true;
            }

            PlaceData data = SettingUtils.getPlaceData(blockPos);

            if (data.valid()) {
                if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
                    return false;
                }

                boolean switched = switch (switchMode.get()) {
                    case Silent -> InvUtils.swap(result.slot(), true);
                    case InvSwitch -> InvUtils.invSwitch(result.slot());
                    case PickSilent -> InvUtils.pickSwitch(result.slot());
                };

                if (!switched) {
                    return false;
                }

                placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
                if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

                helperDelay.reset();

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
            renderHelperPlacing.add(new Render(block.offset(support1), System.currentTimeMillis()));
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
                renderHelperPlacing.add(new Render(block.offset(dir), System.currentTimeMillis()));
                renderHelperPlacing.add(new Render(block.offset(dir).offset(support2), System.currentTimeMillis()));
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

    private void updateTargets() {
        double closestDist = 1000;
        PlayerEntity closest;
        double dist;
        for (int i = 3; i > 0; i--) {
            closest = null;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (closest == player || Friends.get().isFriend(player) || player == mc.player) {
                    continue;
                }

                dist = player.distanceTo(mc.player);

                if (dist > 15) {
                    continue;
                }

                if (closest == null || dist < closestDist) {
                    closestDist = dist;
                    closest = player;
                }
            }
            if (closest != null) {
                target = closest;
            }
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }

    public record Render(BlockPos pos, long time) {
    }
}
