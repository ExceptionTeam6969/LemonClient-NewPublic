package dev.lemonclient.mixin.lithium;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.world.CollisionShapeEvent;
import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChunkAwareBlockCollisionSweeper.class)
public abstract class MixinChunkAwareBlockCollisionSweeper {
    @Redirect(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape onComputeNextCollisionBox(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = state.getCollisionShape(world, pos, context);

        if (world != MinecraftClient.getInstance().world)
            return shape;

        CollisionShapeEvent event = LemonClient.EVENT_BUS.post(CollisionShapeEvent.get(state, pos, shape));
        return event.isCancelled() ? VoxelShapes.empty() : event.shape;
    }
}
