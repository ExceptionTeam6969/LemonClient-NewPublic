package dev.lemonclient.systems.modules.player;

import dev.lemonclient.LemonClient;
import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ShulkerPlacer extends Module {
    public ShulkerPlacer() {
        super(Categories.Player, "Shulker Placer", "Find a location within the place range where the shulker box can be placed and opened to place a shulker box.");

        LemonClient.EVENT_BUS.subscribe(new Renderer());
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Double> minRange = doubleSetting(sgGeneral, "MinRange", 1.0, 0.0, 3.0);
    private final Setting<Boolean> open = boolSetting(sgGeneral, "Open", true);

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
        .description(".")
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
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Lines) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Sides) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );

    private final List<Block> canUseList = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER, Blocks.ACACIA_TRAPDOOR, Blocks.ENCHANTING_TABLE);

    public boolean canUpdate = false;
    private boolean sneaking = false;
    private BlockPos placePos = null;

    private Predicate<ItemStack> shulkers = null;
    private final List<Render> renderBlocks = new ArrayList<>();

    @Override
    public void onActivate() {
        canUpdate = false;
        sneaking = false;
        shulkers = item -> Block.getBlockFromItem(item.getItem()) instanceof ShulkerBoxBlock;

        FindItemResult result = InvUtils.findInHotbar(shulkers);

        if (!result.found()) {
            sendDisableMsg("no shulker found");
            toggle();
            return;
        }

        InvUtils.swap(result.slot(), true);

        double distance = 100.0;
        BlockPos bestPos = null;
        for (BlockPos pos : getSphere(SettingUtils.getPlaceRange())) {
            if (BlockUtils.solid(pos.up()) || PlayerUtils.distanceTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) < minRange.get() || !BlockUtils.canPlace(pos) || bestPos != null && !(PlayerUtils.distanceTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) < distance))
                continue;

            distance = PlayerUtils.distanceTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            bestPos = pos;
        }
        if (bestPos != null) {
            placeShulker(bestPos);
            placePos = bestPos;
        }

        InvUtils.swapBack();

        if (!open.get())
            toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (open.get()) {
            if (!(mc.currentScreen instanceof ShulkerBoxScreen)) {
                if (placePos != null) {
                    if (shulkers.test(mc.world.getBlockState(placePos).getBlock().asItem().getDefaultStack())) {
                        openShulker(placePos);
                    }
                } else {
                    for (BlockPos pos : getSphere(SettingUtils.getPlaceRange())) {
                        if (BlockUtils.solid(pos.up()) || !shulkers.test(mc.world.getBlockState(pos).getBlock().asItem().getDefaultStack()))
                            continue;
                        openShulker(pos);
                        break;
                    }
                }
            } else {
                canUpdate = true;
                toggle();
            }
        }
    }

    private void openShulker(BlockPos blockPos) {
        Direction side = Direction.DOWN;
        blockPos = blockPos.up();
        BlockPos neighbour = blockPos.offset(side);
        Direction opposite = side.getOpposite();

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(blockPos, priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return;
        }

        sneaking = false;
        interactBlock(Hand.MAIN_HAND, neighbour.toCenterPos(), opposite, neighbour);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.end(Objects.hash(name + "placing"));
    }

    private void placeShulker(BlockPos blockPos) {
        Direction side = Direction.DOWN;
        BlockPos neighbour = blockPos.offset(side);
        Direction opposite = side.getOpposite();
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        if (!mc.player.isSneaking() && (canUseList.contains(neighbourBlock) || shulkers.test(neighbourBlock.asItem().getDefaultStack()))) {
            sneaking = true;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(blockPos, priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return;
        }

        placeBlock(Hand.MAIN_HAND, neighbour.toCenterPos(), opposite, neighbour);
        renderBlocks.add(new Render(blockPos, System.currentTimeMillis()));
        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.end(Objects.hash(name + "placing"));

        if (sneaking) {
            sneaking = false;
        }
    }

    private List<BlockPos> getSphere(double range) {
        ArrayList<BlockPos> circleblocks = new ArrayList<>();
        BlockPos blockPos = BlockPos.ofFloored(Math.floor(mc.player.getX()), Math.floor(mc.player.getY()), Math.floor(mc.player.getZ()));
        int cx = blockPos.getX();
        int cy = blockPos.getY();
        int cz = blockPos.getZ();
        int x = cx - (int) range;
        while ((float) x <= (float) cx + range) {
            int z = cz - (int) range;
            while ((float) z <= (float) cz + range) {
                int y = cy - (int) range;
                while (true) {
                    float f2;
                    float f = y;
                    float f3 = f2 = (float) (cy + range);
                    if (!(f < f2)) break;
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + ((cy - y) * (cy - y));
                    if (dist < (range * range)) {
                        BlockPos l = new BlockPos(x, y, z);
                        circleblocks.add(l);
                    }
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return circleblocks;
    }

    public boolean doSneak() {
        return isActive() && sneaking;
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
