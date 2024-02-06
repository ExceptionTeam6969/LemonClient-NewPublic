package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.movement.NoSlow;
import dev.lemonclient.systems.modules.movement.Slippy;
import dev.lemonclient.systems.modules.render.Xray;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public abstract class MixinBlock extends AbstractBlock implements ItemConvertible {
    public MixinBlock(Settings settings) {
        super(settings);
    }

    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
    private static boolean onShouldDrawSide(boolean original, BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos) {
        Xray xray = Modules.get().get(Xray.class);

        if (xray.isActive()) {
            return xray.modifyDrawSide(state, world, pos, side, original);
        }

        return original;
    }

    @ModifyReturnValue(method = "getSlipperiness", at = @At("RETURN"))
    public float getSlipperiness(float original) {
        // For some retarded reason Tweakeroo calls this method before client is initialized
        if (Modules.get() == null) return original;

        Slippy slippy = Modules.get().get(Slippy.class);
        Block block = (Block) (Object) this;

        if (slippy.isActive() && (slippy.listMode.get() == Slippy.ListMode.Whitelist ? slippy.allowedBlocks.get().contains(block) : !slippy.ignoredBlocks.get().contains(block))) {
            return slippy.friction.get().floatValue();
        }

        if (block == Blocks.SLIME_BLOCK && Modules.get().get(NoSlow.class).slimeBlock()) return 0.6F;
        else return original;
    }
}
