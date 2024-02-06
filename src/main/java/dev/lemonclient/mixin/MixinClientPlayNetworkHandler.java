package dev.lemonclient.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Commands;
import dev.lemonclient.events.entity.EntityDestroyEvent;
import dev.lemonclient.events.entity.player.PickItemsEvent;
import dev.lemonclient.events.entity.player.PlayerRespawnEvent;
import dev.lemonclient.events.game.GameJoinedEvent;
import dev.lemonclient.events.game.GameLeftEvent;
import dev.lemonclient.events.game.SendCommandEvent;
import dev.lemonclient.events.game.SendMessageEvent;
import dev.lemonclient.events.packets.ContainerSlotUpdateEvent;
import dev.lemonclient.events.packets.InventoryEvent;
import dev.lemonclient.events.packets.PlaySoundPacketEvent;
import dev.lemonclient.events.world.ChunkDataEvent;
import dev.lemonclient.mixininterface.IExplosionS2CPacket;
import dev.lemonclient.pathing.BaritoneUtils;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.movement.Velocity;
import dev.lemonclient.systems.modules.render.NoRender;
import dev.lemonclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ClientWorld world;

    @Shadow
    public abstract void sendChatMessage(String content);

    @Unique
    private boolean ignoreChatMessage;

    @Unique
    private boolean worldNotNull;

    @Inject(method = "onPlayerRespawn", at = @At("TAIL"))
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        LemonClient.EVENT_BUS.post(PlayerRespawnEvent.get());
    }

    @Inject(method = "onEntitySpawn", at = @At("HEAD"), cancellable = true)
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo info) {
        if (packet != null && packet.getEntityType() != null) {
            if (Modules.get().get(NoRender.class).noEntity(packet.getEntityType()) && Modules.get().get(NoRender.class).getDropSpawnPacket()) {
                info.cancel();
            }
        }
    }

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info) {
        worldNotNull = world != null;
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
        if (worldNotNull) {
            LemonClient.EVENT_BUS.post(GameLeftEvent.get());
        }

        LemonClient.EVENT_BUS.post(GameJoinedEvent.get());
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"))
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo info) {
        LemonClient.EVENT_BUS.post(PlaySoundPacketEvent.get(packet));
    }

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo info) {
        WorldChunk chunk = client.world.getChunk(packet.getX(), packet.getZ());
        LemonClient.EVENT_BUS.post(ChunkDataEvent.get(chunk));
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onContainerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo info) {
        LemonClient.EVENT_BUS.post(ContainerSlotUpdateEvent.get(packet));
    }

    @Inject(method = "onInventory", at = @At("TAIL"))
    private void onInventory(InventoryS2CPacket packet, CallbackInfo info) {
        LemonClient.EVENT_BUS.post(InventoryEvent.get(packet));
    }

    @Inject(method = "onEntitiesDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntitiesDestroyS2CPacket;getEntityIds()Lit/unimi/dsi/fastutil/ints/IntList;"))
    private void onEntitiesDestroy(EntitiesDestroyS2CPacket packet, CallbackInfo ci) {
        for (int id : packet.getEntityIds()) {
            LemonClient.EVENT_BUS.post(EntityDestroyEvent.get(client.world.getEntityById(id)));
        }
    }

    @Inject(method = "onExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onExplosionVelocity(ExplosionS2CPacket packet, CallbackInfo ci) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if (!velocity.explosions.get()) return;

        ((IExplosionS2CPacket) packet).setVelocityX((float) (packet.getPlayerVelocityX() * velocity.getHorizontal(velocity.explosionsHorizontal)));
        ((IExplosionS2CPacket) packet).setVelocityY((float) (packet.getPlayerVelocityY() * velocity.getVertical(velocity.explosionsVertical)));
        ((IExplosionS2CPacket) packet).setVelocityZ((float) (packet.getPlayerVelocityZ() * velocity.getHorizontal(velocity.explosionsHorizontal)));
    }

    @Inject(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;", ordinal = 0))
    private void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo info) {
        Entity itemEntity = client.world.getEntityById(packet.getEntityId());
        Entity entity = client.world.getEntityById(packet.getCollectorEntityId());

        if (itemEntity instanceof ItemEntity && entity == client.player) {
            LemonClient.EVENT_BUS.post(PickItemsEvent.get(((ItemEntity) itemEntity).getStack(), packet.getStackAmount()));
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (ignoreChatMessage) return;

        if (!message.startsWith(Config.get().prefix.get()) && (BaritoneUtils.IS_AVAILABLE || !message.startsWith(BaritoneUtils.getPrefix()))) {
            SendMessageEvent event = LemonClient.EVENT_BUS.post(SendMessageEvent.get(message));

            if (!event.isCancelled()) {
                ignoreChatMessage = true;
                sendChatMessage(event.message);
                ignoreChatMessage = false;
            }
            ci.cancel();
            return;
        }

        if (message.startsWith(Config.get().prefix.get())) {
            try {
                Commands.dispatch(message.substring(Config.get().prefix.get().length()));
            } catch (CommandSyntaxException e) {
                ChatUtils.error(e.getMessage());
            }

            client.inGameHud.getChatHud().addToMessageHistory(message);
            ci.cancel();
        }
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void onSendChatCommand(String command, CallbackInfo ci) {
        SendCommandEvent event = LemonClient.EVENT_BUS.post(SendCommandEvent.get(command));
        if (event.isCancelled()) ci.cancel();
    }
}
