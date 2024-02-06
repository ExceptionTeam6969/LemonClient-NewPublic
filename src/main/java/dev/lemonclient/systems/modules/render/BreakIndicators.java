package dev.lemonclient.systems.modules.render;

import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.mixin.IClientPlayerInteractionManager;
import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.settings.ColorSetting;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class BreakIndicators extends Module {
    public BreakIndicators() {
        super(Categories.Render, "Break Indicators", "Renders the progress of a block being broken.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<RenderMode> renderMode = sgGeneral.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description(".")
        .defaultValue(RenderMode.Normal)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> startColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Start Color")
        .description("The color for the non-broken block.")
        .defaultValue(new SettingColor(25, 252, 25, 150))
        .build()
    );
    private final Setting<SettingColor> endColor = sgGeneral.add(new ColorSetting.Builder()
        .name("End Color")
        .description("The color for the fully-broken block.")
        .defaultValue(new SettingColor(255, 25, 25, 150))
        .build()
    );

    private final Color cSides = new Color();
    private final Color cLines = new Color();

    public enum RenderMode {
        Normal,
        Shrink,
        Grow
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        //Map<Integer, BlockBreakingInfo> blocks = ((IWorldRenderer) mc.worldRenderer).getBlockBreakingInfos();

        float ownBreakingProgress = ((IClientPlayerInteractionManager) mc.interactionManager).getBreakingProgress();
        BlockPos ownBreakingPos = ((IClientPlayerInteractionManager) mc.interactionManager).getCurrentBreakingBlockPos();

        if (ownBreakingPos != null && ownBreakingProgress > 0) {
            switch (renderMode.get()) {
                case Normal -> {
                    BlockState state = mc.world.getBlockState(ownBreakingPos);
                    VoxelShape shape = state.getOutlineShape(mc.world, ownBreakingPos);
                    if (shape == null || shape.isEmpty()) return;

                    Box orig = shape.getBoundingBox();

                    double shrinkFactor = 1d - ownBreakingProgress;

                    renderBlock(event.renderer, orig, ownBreakingPos, shrinkFactor, ownBreakingProgress);
                }
                case Shrink -> {
                    double progress = 1d - ownBreakingProgress;
                    double max = ((double) Math.round(progress * 100) / 100);
                    double min = 1 - max;

                    renderBlock(
                        event.renderer,
                        new Box(
                            ownBreakingPos.getX() + min, ownBreakingPos.getY() + min, ownBreakingPos.getZ() + min,
                            ownBreakingPos.getX() + max, ownBreakingPos.getY() + max, ownBreakingPos.getZ() + max
                        ),
                        progress
                    );
                }
                case Grow -> renderBlock(
                    event.renderer,
                    new Box(
                        ownBreakingPos.getX(), ownBreakingPos.getY(), ownBreakingPos.getZ(),
                        ownBreakingPos.getX() + 1, ownBreakingPos.getY() + ownBreakingProgress, ownBreakingPos.getZ() + 1
                    ),
                    1d - ownBreakingProgress
                );
            }
        }

/*        blocks.values().forEach(info -> {
            BlockPos pos = info.getPos();
            int stage = info.getStage();
            if (pos.equals(ownBreakingPos)) return;

            VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
            if (shape == null || shape.isEmpty()) return;

            Box orig = shape.getBoundingBox();

            double shrinkFactor = (9 - (stage + 1)) / 9d;
            double progress = 1d - shrinkFactor;

            renderBlock(event.renderer, orig, pos, shrinkFactor, progress);
        });*/
    }

    private void renderBlock(Renderer3D renderer, Box box, double progress) {
        Color c1Sides = startColor.get().copy().a(startColor.get().a / 2);
        Color c2Sides = endColor.get().copy().a(endColor.get().a / 2);

        cSides.set(
            (int) Math.round(c1Sides.r + (c2Sides.r - c1Sides.r) * progress),
            (int) Math.round(c1Sides.g + (c2Sides.g - c1Sides.g) * progress),
            (int) Math.round(c1Sides.b + (c2Sides.b - c1Sides.b) * progress),
            (int) Math.round(c1Sides.a + (c2Sides.a - c1Sides.a) * progress)
        );

        Color c1Lines = startColor.get();
        Color c2Lines = endColor.get();

        cLines.set(
            (int) Math.round(c1Lines.r + (c2Lines.r - c1Lines.r) * progress),
            (int) Math.round(c1Lines.g + (c2Lines.g - c1Lines.g) * progress),
            (int) Math.round(c1Lines.b + (c2Lines.b - c1Lines.b) * progress),
            (int) Math.round(c1Lines.a + (c2Lines.a - c1Lines.a) * progress)
        );

        renderer.box(box, cSides, cLines, shapeMode.get(), 0);
    }

    private void renderBlock(Renderer3D renderer, Box orig, BlockPos pos, double shrinkFactor, double progress) {
        Box box = orig.shrink(orig.getXLength() * shrinkFactor, orig.getYLength() * shrinkFactor, orig.getZLength() * shrinkFactor);

        double xShrink = (orig.getXLength() * shrinkFactor) / 2;
        double yShrink = (orig.getYLength() * shrinkFactor) / 2;
        double zShrink = (orig.getZLength() * shrinkFactor) / 2;

        double x1 = pos.getX() + box.minX + xShrink;
        double y1 = pos.getY() + box.minY + yShrink;
        double z1 = pos.getZ() + box.minZ + zShrink;
        double x2 = pos.getX() + box.maxX + xShrink;
        double y2 = pos.getY() + box.maxY + yShrink;
        double z2 = pos.getZ() + box.maxZ + zShrink;

        Color c1Sides = startColor.get().copy().a(startColor.get().a / 2);
        Color c2Sides = endColor.get().copy().a(endColor.get().a / 2);

        cSides.set(
            (int) Math.round(c1Sides.r + (c2Sides.r - c1Sides.r) * progress),
            (int) Math.round(c1Sides.g + (c2Sides.g - c1Sides.g) * progress),
            (int) Math.round(c1Sides.b + (c2Sides.b - c1Sides.b) * progress),
            (int) Math.round(c1Sides.a + (c2Sides.a - c1Sides.a) * progress)
        );

        Color c1Lines = startColor.get();
        Color c2Lines = endColor.get();

        cLines.set(
            (int) Math.round(c1Lines.r + (c2Lines.r - c1Lines.r) * progress),
            (int) Math.round(c1Lines.g + (c2Lines.g - c1Lines.g) * progress),
            (int) Math.round(c1Lines.b + (c2Lines.b - c1Lines.b) * progress),
            (int) Math.round(c1Lines.a + (c2Lines.a - c1Lines.a) * progress)
        );

        renderer.box(x1, y1, z1, x2, y2, z2, cSides, cLines, shapeMode.get(), 0);
    }
}
