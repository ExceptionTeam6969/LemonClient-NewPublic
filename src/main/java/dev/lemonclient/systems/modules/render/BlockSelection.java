package dev.lemonclient.systems.modules.render;

import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.mixininterface.IBox;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockSelection extends Module {
    public BlockSelection() {
        super(Categories.Render, "Block Selection", "Modifies how your block selection is rendered.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> advanced = sgGeneral.add(new BoolSetting.Builder()
        .name("Advanced")
        .description("Shows a more advanced outline on different types of shape blocks.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> oneSide = sgGeneral.add(new BoolSetting.Builder()
        .name("Single Side")
        .description("Only renders the side you are looking at.")
        .defaultValue(false)
        .build()
    );
    private final Setting<RenderMode> renderMode = sgGeneral.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description("The mode to render in.")
        .defaultValue(RenderMode.Normal)
        .build()
    );
    private final Setting<Integer> smoothness = sgGeneral.add(new IntSetting.Builder()
        .name("Smoothness")
        .description("How smoothly the render should move around.")
        .defaultValue(10)
        .min(0)
        .sliderMax(20)
        .visible(() -> renderMode.get() == RenderMode.Smooth)
        .build()
    );
    private final Setting<Double> renderTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha value.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> renderMode.get().equals(RenderMode.Fading))
        .build()
    );
    private final Setting<Double> fadeTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(0.2)
        .min(0)
        .sliderRange(0, 5)
        .visible(() -> renderMode.get().equals(RenderMode.Fading))
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Boolean> hideInside = sgGeneral.add(new BoolSetting.Builder()
        .name("Hide When Inside Block")
        .description("Hide selection when inside target block.")
        .defaultValue(true)
        .build()
    );

    private double d = 0.0;
    private long lastMillis = System.currentTimeMillis();
    private Box renderBoxOne, renderBoxTwo;
    private final Map<BlockPos, Double[]> earthMap = new HashMap<>();

    public enum RenderMode {
        Normal,
        Smooth,
        Fading
    }

    @Override
    public void onActivate() {
        earthMap.clear();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.crosshairTarget == null || !(mc.crosshairTarget instanceof BlockHitResult result)) return;

        if (hideInside.get() && result.isInsideBlock()) return;

        d = (System.currentTimeMillis() - lastMillis) / 1000f;
        lastMillis = System.currentTimeMillis();

        BlockPos bp = result.getBlockPos();
        Direction side = result.getSide();

        BlockState state = mc.world.getBlockState(bp);
        VoxelShape shape = state.getOutlineShape(mc.world, bp);

        if (shape.isEmpty()) return;
        Box box = shape.getBoundingBox();

        if (renderMode.get().equals(RenderMode.Fading)) {
            if (!earthMap.containsKey(bp)) {
                earthMap.put(bp, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
            } else {
                earthMap.replace(bp, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
            }
        }

        if (oneSide.get()) {
            if (side == Direction.UP || side == Direction.DOWN) {
                event.renderer.sideHorizontal(bp.getX() + box.minX, bp.getY() + (side == Direction.DOWN ? box.minY : box.maxY), bp.getZ() + box.minZ, bp.getX() + box.maxX, bp.getZ() + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get());
            } else if (side == Direction.SOUTH || side == Direction.NORTH) {
                double z = side == Direction.NORTH ? box.minZ : box.maxZ;
                event.renderer.sideVertical(bp.getX() + box.minX, bp.getY() + box.minY, bp.getZ() + z, bp.getX() + box.maxX, bp.getY() + box.maxY, bp.getZ() + z, sideColor.get(), lineColor.get(), shapeMode.get());
            } else {
                double x = side == Direction.WEST ? box.minX : box.maxX;
                event.renderer.sideVertical(bp.getX() + x, bp.getY() + box.minY, bp.getZ() + box.minZ, bp.getX() + x, bp.getY() + box.maxY, bp.getZ() + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get());
            }
        } else {
            if (advanced.get()) {
                if (shapeMode.get() == ShapeMode.Both || shapeMode.get() == ShapeMode.Lines) {
                    shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
                        event.renderer.line(bp.getX() + minX, bp.getY() + minY, bp.getZ() + minZ, bp.getX() + maxX, bp.getY() + maxY, bp.getZ() + maxZ, lineColor.get());
                    });
                }

                if (shapeMode.get().sides()) {
                    for (Box b : shape.getBoundingBoxes()) {
                        render(event, bp, b);
                    }
                }
            } else {
                render(event, bp, box);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (renderMode.get().equals(RenderMode.Fading)) {
            List<BlockPos> toRemove = new ArrayList<>();
            for (Map.Entry<BlockPos, Double[]> entry : earthMap.entrySet()) {
                BlockPos pos = entry.getKey();
                Double[] alpha = entry.getValue();
                if (alpha[0] <= d) {
                    toRemove.add(pos);
                } else {
                    event.renderer.box(
                        new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1),
                        Render2DUtils.injectAlpha(sideColor.get(), (int) Math.round(sideColor.get().a * Math.min(1, alpha[0] / alpha[1]))),
                        Render2DUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * Math.min(1, alpha[0] / alpha[1]))),
                        shapeMode.get(),
                        0
                    );
                    entry.setValue(new Double[]{alpha[0] - d, alpha[1]});
                }
            }
            toRemove.forEach(earthMap::remove);
        }
    }

    private void render(Render3DEvent event, BlockPos blockPos, Box box) {
        switch (renderMode.get()) {
            case Normal ->
                event.renderer.box(blockPos.getX() + box.minX, blockPos.getY() + box.minY, blockPos.getZ() + box.minZ, blockPos.getX() + box.maxX, blockPos.getY() + box.maxY, blockPos.getZ() + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            case Smooth -> {
                if (renderBoxOne == null) renderBoxOne = new Box(blockPos);
                if (renderBoxTwo == null) renderBoxTwo = new Box(blockPos);
                else ((IBox) renderBoxTwo).set(blockPos);

                double offsetX = (renderBoxTwo.minX - renderBoxOne.minX) / smoothness.get();
                double offsetY = (renderBoxTwo.minY - renderBoxOne.minY) / smoothness.get();
                double offsetZ = (renderBoxTwo.minZ - renderBoxOne.minZ) / smoothness.get();

                ((IBox) renderBoxOne).set(
                    renderBoxOne.minX + offsetX,
                    renderBoxOne.minY + offsetY,
                    renderBoxOne.minZ + offsetZ,
                    renderBoxOne.maxX + offsetX,
                    renderBoxOne.maxY + offsetY,
                    renderBoxOne.maxZ + offsetZ
                );

                event.renderer.box(renderBoxOne, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }
        }
    }
}
