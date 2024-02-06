package dev.lemonclient.systems.modules.world;

import dev.lemonclient.events.entity.player.PlayerMoveEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.CollisionShapeEvent;
import dev.lemonclient.mixininterface.IVec3d;
import dev.lemonclient.settings.BlockListSetting;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.shape.VoxelShapes;

import java.util.List;

public class Collisions extends Module {
    public Collisions() {
        super(Categories.World, "Collisions", "Adds collision boxes to certain blocks/areas.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("What blocks should be added collision box.")
        .filter(this::blockFilter)
        .build()
    );

    private final Setting<Boolean> magma = sgGeneral.add(new BoolSetting.Builder()
        .name("magma")
        .description("Prevents you from walking over magma blocks.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> unloadedChunks = sgGeneral.add(new BoolSetting.Builder()
        .name("unloaded-chunks")
        .description("Stops you from going into unloaded chunks.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreBorder = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-border")
        .description("Removes world border collision.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onCollisionShape(CollisionShapeEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (!event.state.getFluidState().isEmpty()) return;
        if (blocks.get().contains(event.state.getBlock())) {
            event.shape = VoxelShapes.fullCube();
        } else if (magma.get() && !mc.player.isSneaking()
            && event.state.isAir()
            && mc.world.getBlockState(event.pos.down()).getBlock() == Blocks.MAGMA_BLOCK) {
            event.shape = VoxelShapes.fullCube();
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        int x = (int) (mc.player.getX() + event.movement.x) >> 4;
        int z = (int) (mc.player.getZ() + event.movement.z) >> 4;
        if (unloadedChunks.get() && !mc.world.getChunkManager().isChunkLoaded(x, z)) {
            ((IVec3d) event.movement).set(0, event.movement.y, 0);
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!unloadedChunks.get()) return;
        if (event.packet instanceof VehicleMoveC2SPacket packet) {
            if (!mc.world.getChunkManager().isChunkLoaded((int) packet.getX() >> 4, (int) packet.getZ() >> 4)) {
                mc.player.getVehicle().updatePosition(mc.player.getVehicle().prevX, mc.player.getVehicle().prevY, mc.player.getVehicle().prevZ);
                event.cancel();
            }
        } else if (event.packet instanceof PlayerMoveC2SPacket packet) {
            if (!mc.world.getChunkManager().isChunkLoaded((int) packet.getX(mc.player.getX()) >> 4, (int) packet.getZ(mc.player.getZ()) >> 4)) {
                event.cancel();
            }
        }
    }

    private boolean blockFilter(Block block) {
        return (block instanceof AbstractFireBlock
            || block instanceof AbstractPressurePlateBlock
            || block instanceof TripwireBlock
            || block instanceof TripwireHookBlock
            || block instanceof CobwebBlock
            || block instanceof CampfireBlock
            || block instanceof SweetBerryBushBlock
            || block instanceof CactusBlock
            || block instanceof AbstractRailBlock
            || block instanceof TrapdoorBlock
            || block instanceof PowderSnowBlock
            || block instanceof AbstractCauldronBlock
            || block instanceof HoneyBlock
        );
    }

    public boolean ignoreBorder() {
        return isActive() && ignoreBorder.get();
    }
}
