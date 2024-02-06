package dev.lemonclient.mixin;

import dev.lemonclient.mixininterface.IPlayerInteractEntityC2SPacket;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.combat.SurroundPlus;
import dev.lemonclient.systems.modules.movement.NoSlow;
import dev.lemonclient.systems.modules.movement.Sneak;
import dev.lemonclient.systems.modules.player.ShulkerPlacer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static dev.lemonclient.LemonClient.mc;

@Mixin(PlayerInteractEntityC2SPacket.class)
public class MixinPlayerInteractEntityC2SPacket implements IPlayerInteractEntityC2SPacket {
    @Shadow
    @Final
    private PlayerInteractEntityC2SPacket.InteractTypeHandler type;
    @Shadow
    @Final
    private int entityId;

    @Override
    public PlayerInteractEntityC2SPacket.InteractType getType() {
        return type.getType();
    }

    @Override
    public Entity getEntity() {
        return mc.world.getEntityById(entityId);
    }

    @ModifyVariable(method = "<init>(IZLnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket$InteractTypeHandler;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static boolean setSneaking(boolean sneaking) {
        return Modules.get().get(Sneak.class).doPacket() || Modules.get().get(NoSlow.class).airStrict() || Modules.get().get(SurroundPlus.class).doSneak() || Modules.get().get(ShulkerPlacer.class).doSneak() || sneaking;
    }
}
