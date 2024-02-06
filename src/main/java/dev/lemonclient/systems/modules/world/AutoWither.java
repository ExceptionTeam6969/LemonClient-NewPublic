package dev.lemonclient.systems.modules.world;

import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.Pool;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.player.Rotations;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class AutoWither extends Module {
    public AutoWither() {
        super(Categories.World, "Auto Wither", "Automatically builds withers.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("horizontal-radius")
        .description("Horizontal radius for placement")
        .defaultValue(4)
        .min(0)
        .sliderMax(6)
        .build()
    );
    private final Setting<Integer> verticalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("vertical-radius")
        .description("Vertical radius for placement")
        .defaultValue(3)
        .min(0)
        .sliderMax(6)
        .build()
    );
    private final Setting<Priority> priorityMode = sgGeneral.add(new EnumSetting.Builder<Priority>()
        .name("priority")
        .description("Priority")
        .defaultValue(Priority.Random)
        .build()
    );
    private final Setting<Integer> witherDelay = sgGeneral.add(new IntSetting.Builder()
        .name("wither-delay")
        .description("Delay in ticks between wither placements")
        .defaultValue(1)
        .min(1)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> blockDelay = sgGeneral.add(new IntSetting.Builder()
        .name("block-delay")
        .description("Delay in ticks between block placements")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> turnOff = sgGeneral.add(new BoolSetting.Builder()
        .name("turn-off")
        .description("Turns off automatically after building a single wither.")
        .defaultValue(true)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the target block rendering.")
        .defaultValue(new SettingColor(197, 137, 232, 10))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the target block rendering.")
        .defaultValue(new SettingColor(197, 137, 232))
        .build()
    );

    private final Pool<Wither> witherPool = new Pool<>(Wither::new);
    private final ArrayList<Wither> withers = new ArrayList<>();
    private Wither wither;

    private int witherTicksWaited, blockTicksWaited;

    @Override
    public void onDeactivate() {
        wither = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        if (wither == null) {
            // Delay
            if (witherTicksWaited < witherDelay.get() - 1) {
                return;
            }

            // Clear pool and list
            for (Wither wither : withers) witherPool.free(wither);
            withers.clear();

            // Register
            BlockIterator.register(horizontalRadius.get(), verticalRadius.get(), (blockPos, blockState) -> {
                Direction dir = Direction.fromRotation(Rotations.getYaw(blockPos)).getOpposite();
                if (isValidSpawn(blockPos, dir)) withers.add(witherPool.get().set(blockPos, dir));
            });
        }
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        if (wither == null) {
            // Delay
            if (witherTicksWaited < witherDelay.get() - 1) {
                witherTicksWaited++;
                return;
            }


            if (withers.isEmpty()) return;

            // Sorting
            switch (priorityMode.get()) {
                case Closest:
                    withers.sort(Comparator.comparingDouble(w -> PlayerUtils.distanceTo(w.foot)));
                case Furthest:
                    withers.sort((w1, w2) -> {
                        int sort = Double.compare(PlayerUtils.distanceTo(w1.foot), PlayerUtils.distanceTo(w2.foot));
                        if (sort == 0) return 0;
                        return sort > 0 ? -1 : 1;
                    });
                case Random:
                    Collections.shuffle(withers);
            }

            wither = withers.get(0);
        }

        // Soul sand/soil and skull slot
        FindItemResult findSoulSand = InvUtils.findInHotbar(Items.SOUL_SAND);
        if (!findSoulSand.found()) findSoulSand = InvUtils.findInHotbar(Items.SOUL_SOIL);
        FindItemResult findWitherSkull = InvUtils.findInHotbar(Items.WITHER_SKELETON_SKULL);

        // Check for enough resources
        if (!findSoulSand.found() || !findWitherSkull.found()) {
            error("Not enough resources in hotbar");
            toggle();
            return;
        }


        // Build
        if (blockDelay.get() == 0) {
            // Body
            place(wither.foot, findSoulSand);
            place(wither.foot.up(), findSoulSand);
            place(wither.foot.up().offset(wither.axis, -1), findSoulSand);
            place(wither.foot.up().offset(wither.axis, 1), findSoulSand);

            // Head
            place(wither.foot.up().up(), findWitherSkull);
            place(wither.foot.up().up().offset(wither.axis, -1), findWitherSkull);
            place(wither.foot.up().up().offset(wither.axis, 1), findWitherSkull);


            // Auto turnoff
            if (turnOff.get()) {
                wither = null;
                toggle();
            }

        } else {
            // Delay
            if (blockTicksWaited < blockDelay.get() - 1) {
                blockTicksWaited++;
                return;
            }

            switch (wither.stage) {
                case 0 -> {
                    if (place(wither.foot, findSoulSand))
                        wither.stage++;
                }
                case 1 -> {
                    if (place(wither.foot.up(), findSoulSand))
                        wither.stage++;
                }
                case 2 -> {
                    if (place(wither.foot.up().offset(wither.axis, -1), findSoulSand))
                        wither.stage++;
                }
                case 3 -> {
                    if (place(wither.foot.up().offset(wither.axis, 1), findSoulSand))
                        wither.stage++;
                }
                case 4 -> {
                    if (place(wither.foot.up().up(), findWitherSkull))
                        wither.stage++;
                }
                case 5 -> {
                    if (place(wither.foot.up().up().offset(wither.axis, -1), findWitherSkull))
                        wither.stage++;
                }
                case 6 -> {
                    if (place(wither.foot.up().up().offset(wither.axis, 1), findWitherSkull))
                        wither.stage++;
                }
                case 7 -> {
                    // Auto turnoff
                    if (turnOff.get()) {
                        wither = null;
                        toggle();
                    }
                }
            }
        }


        witherTicksWaited = 0;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (wither == null) return;

        // Body
        event.renderer.box(wither.foot, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        event.renderer.box(wither.foot.up(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        event.renderer.box(wither.foot.up().offset(wither.axis, -1), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        event.renderer.box(wither.foot.up().offset(wither.axis, 1), sideColor.get(), lineColor.get(), shapeMode.get(), 0);

        // Head
        BlockPos midHead = wither.foot.up().up();
        BlockPos leftHead = wither.foot.up().up().offset(wither.axis, -1);
        BlockPos rightHead = wither.foot.up().up().offset(wither.axis, 1);

        event.renderer.box((double) midHead.getX() + 0.2, midHead.getX(), (double) midHead.getX() + 0.2,
            (double) midHead.getX() + 0.8, (double) midHead.getX() + 0.7, (double) midHead.getX() + 0.8,
            sideColor.get(), lineColor.get(), shapeMode.get(), 0);

        event.renderer.box((double) leftHead.getX() + 0.2, leftHead.getX(), (double) leftHead.getX() + 0.2,
            (double) leftHead.getX() + 0.8, (double) leftHead.getX() + 0.7, (double) leftHead.getX() + 0.8,
            sideColor.get(), lineColor.get(), shapeMode.get(), 0);

        event.renderer.box((double) rightHead.getX() + 0.2, rightHead.getX(), (double) rightHead.getX() + 0.2,
            (double) rightHead.getX() + 0.8, (double) rightHead.getX() + 0.7, (double) rightHead.getX() + 0.8,
            sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    private boolean place(BlockPos blockPos, FindItemResult result) {
        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.start(blockPos, priority, RotationType.BlockPlace, Objects.hash(name + "placing"));

        InvUtils.swap(result.slot(), true);

        boolean place = placeBlock(blockPos, result, true);

        InvUtils.swapBack();

        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.end(Objects.hash(name + "placing"));

        return place;
    }

    private boolean isValidSpawn(BlockPos blockPos, Direction direction) {
        // Withers are 3x3x1

        // Check if y > (255 - 3)
        // Because withers are 3 blocks tall
        if (blockPos.getY() > 252) return false;

        // Determine width from direction
        int widthX = 0;
        int widthZ = 0;

        if (direction == Direction.EAST || direction == Direction.WEST) widthZ = 1;
        if (direction == Direction.NORTH || direction == Direction.SOUTH) widthX = 1;


        // Check for non air blocks and entities
        BlockPos.Mutable bp = new BlockPos.Mutable();
        for (int x = blockPos.getX() - widthX; x <= blockPos.getX() + widthX; x++) {
            for (int z = blockPos.getZ() - widthZ; z <= blockPos.getZ(); z++) {
                for (int y = blockPos.getY(); y <= blockPos.getY() + 2; y++) {
                    bp.set(x, y, z);
                    if (!mc.world.getBlockState(bp).isReplaceable()) return false;
                    if (!mc.world.canPlace(Blocks.STONE.getDefaultState(), bp, ShapeContext.absent())) return false;
                }
            }
        }

        return true;
    }

    public enum Priority {
        Closest,
        Furthest,
        Random
    }

    private static class Wither {
        public int stage;
        // 0 = foot
        // 1 = mid body
        // 2 = left arm
        // 3 = right arm
        // 4 = mid head
        // 5 = left head
        // 6 = right head
        // 7 = end
        public BlockPos.Mutable foot = new BlockPos.Mutable();
        public Direction facing;
        public Direction.Axis axis;

        public Wither set(BlockPos pos, Direction dir) {
            this.stage = 0;
            this.foot.set(pos);
            this.facing = dir;
            this.axis = dir.getAxis();

            return this;
        }
    }
}
