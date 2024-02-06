package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class GhostHand extends Module {
    public GhostHand() {
        super(Categories.Player, "Ghost Hand", "Opens containers through walls.");
    }

    private final Set<BlockPos> posList = new ObjectOpenHashSet<>();

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.options.useKey.isPressed() || mc.player.isSneaking()) return;

        if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.raycast(mc.interactionManager.getReachDistance(), mc.getTickDelta(), false).getPos())).hasBlockEntity())
            return;

        Vec3d direction = new Vec3d(0, 0, 0.1)
            .rotateX(-(float) Math.toRadians(mc.player.getPitch()))
            .rotateY(-(float) Math.toRadians(mc.player.getYaw()));

        posList.clear();

        for (int i = 1; i < mc.interactionManager.getReachDistance() * 10; i++) {
            BlockPos pos = BlockPos.ofFloored(mc.player.getCameraPosVec(mc.getTickDelta()).add(direction.multiply(i)));

            if (posList.contains(pos)) continue;
            posList.add(pos);

            if (mc.world.getBlockState(pos).hasBlockEntity()) {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
                return;
            }
        }
    }
}
