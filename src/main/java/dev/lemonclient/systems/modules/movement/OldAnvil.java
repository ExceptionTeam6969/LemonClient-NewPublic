package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class OldAnvil extends Module {
    public OldAnvil() {
        super(Categories.Movement, "Old Anvil", "Allows you to move while you burrowed with anvil.");
    }

    private final VoxelShape BASE_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    private final VoxelShape X_STEP_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 2.0D, 16.0D, 4.0D, 14.0D);
    private final VoxelShape X_STEM_SHAPE = Block.createCuboidShape(0.0D, 5.0D, 2.0D, 16.0D, 10.0D, 14.0D);
    private final VoxelShape X_FACE_SHAPE = Block.createCuboidShape(0.0D, 10.0D, 2.0D, 16.0D, 16.0D, 14.0D);
    private final VoxelShape Z_STEP_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 0.0D, 14.0D, 4.0D, 16.0D);
    private final VoxelShape Z_STEM_SHAPE = Block.createCuboidShape(2.0D, 5.0D, 0.0D, 14.0D, 10.0D, 16.0D);
    private final VoxelShape Z_FACE_SHAPE = Block.createCuboidShape(2.0D, 10.0D, 0.0D, 14.0D, 16.0D, 16.0D);
    private final VoxelShape X_AXIS_SHAPE = VoxelShapes.union(BASE_SHAPE, X_STEP_SHAPE, X_STEM_SHAPE, X_FACE_SHAPE);
    private final VoxelShape Z_AXIS_SHAPE = VoxelShapes.union(BASE_SHAPE, Z_STEP_SHAPE, Z_STEM_SHAPE, Z_FACE_SHAPE);

    public VoxelShape getVoxelShape(BlockState state) {
        Direction direction = state.get(HorizontalFacingBlock.FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_SHAPE : Z_AXIS_SHAPE;
    }
}
