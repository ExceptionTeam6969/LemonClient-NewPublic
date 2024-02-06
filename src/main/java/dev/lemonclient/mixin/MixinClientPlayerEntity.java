package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.DamageEvent;
import dev.lemonclient.events.entity.DropItemsEvent;
import dev.lemonclient.events.entity.player.MoveEvent;
import dev.lemonclient.events.entity.player.SendMovementPacketsEvent;
import dev.lemonclient.events.entity.player.SprintEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.combat.SurroundPlus;
import dev.lemonclient.systems.modules.movement.NoSlow;
import dev.lemonclient.systems.modules.movement.Sneak;
import dev.lemonclient.systems.modules.movement.TickShift;
import dev.lemonclient.systems.modules.movement.Velocity;
import dev.lemonclient.systems.modules.player.Portals;
import dev.lemonclient.systems.modules.player.ShulkerPlacer;
import dev.lemonclient.systems.modules.player.Twerk;
import dev.lemonclient.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    @Unique
    private static boolean sent = false;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onDropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> info) {
        if (LemonClient.EVENT_BUS.post(DropItemsEvent.get(getMainHandStack())).isCancelled())
            info.setReturnValue(false);
    }

    @Redirect(method = "updateNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    private Screen updateNauseaGetCurrentScreenProxy(MinecraftClient client) {
        if (Modules.get().isActive(Portals.class)) return null;
        return client.currentScreen;
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean redirectUsingItem(boolean isUsingItem) {
        if (Modules.get().get(NoSlow.class).items()) return false;
        return isUsingItem;
    }

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    private void onShouldSlowDown(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(NoSlow.class).sneaking()) {
            info.setReturnValue(isCrawling());
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double d, CallbackInfo info) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if (velocity.isActive() && velocity.blocks.get()) {
            info.cancel();
        }
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (Utils.canUpdate() && getWorld().isClient && canTakeDamage())
            LemonClient.EVENT_BUS.post(DamageEvent.get(this, source, amount));
    }

    @ModifyConstant(method = "canSprint", constant = @Constant(floatValue = 6.0f))
    private float onHunger(float constant) {
        if (Modules.get().get(NoSlow.class).hunger()) return -1;
        return constant;
    }

    // Rotations

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void onSendMovementPacketsHead(CallbackInfo info) {
        LemonClient.EVENT_BUS.post(SendMovementPacketsEvent.Pre.get());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
    private void onTickHasVehicleBeforeSendPackets(CallbackInfo info) {
        LemonClient.EVENT_BUS.post(SendMovementPacketsEvent.Pre.get());
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void onSendMovementPacketsTail(CallbackInfo info) {
        LemonClient.EVENT_BUS.post(SendMovementPacketsEvent.Post.get());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void onTickHasVehicleAfterSendPackets(CallbackInfo info) {
        LemonClient.EVENT_BUS.post(SendMovementPacketsEvent.Post.get());
    }

    // Sneak
    @ModifyExpressionValue(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSneaking()Z"))
    private boolean isSneaking(boolean sneaking) {
        return Modules.get().get(Sneak.class).doPacket() || Modules.get().get(NoSlow.class).airStrict() || Modules.get().get(SurroundPlus.class).doSneak() || Modules.get().get(ShulkerPlacer.class).doSneak() || sneaking;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true, require = 0)
    public void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        MoveEvent event = MoveEvent.get(movementType, movement.x, movement.y, movement.z);
        LemonClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            super.move(movementType, new Vec3d(event.x, event.y, event.z));
            ci.cancel();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), require = 0)
    private void sendPacketsHead(CallbackInfo ci) {
        sent = false;
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"), require = 0)
    private void onSendPacket(CallbackInfo ci) {
        sent = true;
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"), require = 0)
    private void sendPacketsTail(CallbackInfo ci) {
        if (!sent) {
            TickShift tickShift = Modules.get().get(TickShift.class);

            if (tickShift.isActive()) {
                tickShift.unSent = Math.min(tickShift.packets.get(), tickShift.unSent + 1);
            }
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 2), require = 0)
    private void sendPacketFull(ClientPlayNetworkHandler instance, Packet<?> packet) {
        networkHandler.sendPacket(Managers.ROTATION.onFull((PlayerMoveC2SPacket.Full) packet));
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 3), require = 0)
    private void sendPacketPosGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
        networkHandler.sendPacket(Managers.ROTATION.onPositionOnGround((PlayerMoveC2SPacket.PositionAndOnGround) packet));
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 4), require = 0)
    private void sendPacketLookGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
        PlayerMoveC2SPacket toSend = Managers.ROTATION.onLookAndOnGround((PlayerMoveC2SPacket.LookAndOnGround) packet);
        if (toSend != null) {
            networkHandler.sendPacket(toSend);
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 5), require = 0)
    private void sendPacketGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
        networkHandler.sendPacket(Managers.ROTATION.onOnlyOnground((PlayerMoveC2SPacket.OnGroundOnly) packet));
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSneaking()Z"), require = 0)
    private boolean isSneaking(ClientPlayerEntity clientPlayerEntity) {
        return (clientPlayerEntity.isSneaking()) || (Modules.get().get(Twerk.class).doPacket());
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAbilities()Lnet/minecraft/entity/player/PlayerAbilities;", shift = At.Shift.AFTER, ordinal = 1), require = 0)
    private void onSprint(CallbackInfo ci) {
        SprintEvent event = SprintEvent.get(isSprinting());
        LemonClient.EVENT_BUS.post(event);
        LemonClient.mc.player.setSprinting(event.isSprint);
    }
}
