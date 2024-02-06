package dev.lemonclient.systems.modules.player;

import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AirPlace extends Module {
    public AirPlace() {
        super(Categories.Player, "Air Place", "Places a block where your crosshair is pointing at.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRange = settings.createGroup("Range");

    // General

    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders a block overlay where the obsidian will be placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the sides of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 10))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the lines of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 255))
        .build()
    );

    // Range

    private final Setting<Boolean> customRange = sgRange.add(new BoolSetting.Builder()
        .name("custom-range")
        .description("Use custom range for air place.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> range = sgRange.add(new DoubleSetting.Builder()
        .name("range")
        .description("Custom range to place at.")
        .visible(customRange::get)
        .defaultValue(5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private HitResult hitResult;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        double r = customRange.get() ? range.get() : mc.interactionManager.getReachDistance();
        hitResult = mc.getCameraEntity().raycast(r, 0, false);

        if (!(hitResult instanceof BlockHitResult) || !(mc.player.getMainHandStack().getItem() instanceof BlockItem) && !(mc.player.getMainHandStack().getItem() instanceof SpawnEggItem))
            return;

        if (mc.options.useKey.isPressed()) {
            place(((BlockHitResult) hitResult).getBlockPos(), mc.player.getInventory().selectedSlot);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!(hitResult instanceof BlockHitResult)
            || !mc.world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).isReplaceable()
            || !(mc.player.getMainHandStack().getItem() instanceof BlockItem) && !(mc.player.getMainHandStack().getItem() instanceof SpawnEggItem)
            || !render.get()) return;

        event.renderer.box(((BlockHitResult) hitResult).getBlockPos(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    private void place(BlockPos blockPos, int slot) {
        if (slot < 0 || slot > 8) return;
        if (!BlockUtils.canPlace(blockPos, true)) return;

        Vec3d hitPos = blockPos.toCenterPos();

        BlockPos neighbour;
        Direction side = BlockUtils.getPlaceSide(blockPos);

        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        placeBlock(Hand.MAIN_HAND, hitPos, side.getOpposite(), neighbour);
    }
}
