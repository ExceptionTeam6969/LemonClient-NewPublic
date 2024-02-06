package dev.lemonclient.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.world.BlockActivateEvent;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class MixinBlockState extends AbstractBlock.AbstractBlockState {
    public MixinBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> mapCodec) {
        super(block, propertyMap, mapCodec);
    }

    @Override
    public ActionResult onUse(World world, PlayerEntity player, Hand hand, BlockHitResult hit) {
        LemonClient.EVENT_BUS.post(BlockActivateEvent.get((BlockState) (Object) this));
        return super.onUse(world, player, hand, hit);
    }
}
