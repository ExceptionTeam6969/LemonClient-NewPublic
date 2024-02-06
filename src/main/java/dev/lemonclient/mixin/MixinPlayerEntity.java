package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.DropItemsEvent;
import dev.lemonclient.events.entity.player.ClipAtLedgeEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.SoundModifier;
import dev.lemonclient.systems.modules.movement.Anchor;
import dev.lemonclient.systems.modules.movement.Flight;
import dev.lemonclient.systems.modules.movement.NoSlow;
import dev.lemonclient.systems.modules.player.SpeedMine;
import dev.lemonclient.systems.modules.render.OldHitting;
import dev.lemonclient.utils.world.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.lemonclient.LemonClient.mc;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    @Shadow
    public abstract PlayerAbilities getAbilities();

    @Unique
    Entity attackEntity = null;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    protected void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
        if (!getWorld().isClient) return;

        ClipAtLedgeEvent event = LemonClient.EVENT_BUS.post(ClipAtLedgeEvent.get());
        if (event.isSet()) info.setReturnValue(event.isClip());
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropItem(ItemStack stack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> info) {
        if (getWorld().isClient && !stack.isEmpty()) {
            if (LemonClient.EVENT_BUS.post(DropItemsEvent.get(stack)).isCancelled()) info.cancel();
        }
    }

    @ModifyReturnValue(method = "getBlockBreakingSpeed", at = @At(value = "RETURN"))
    public float onGetBlockBreakingSpeed(float breakSpeed, BlockState block) {
        if (!getWorld().isClient) return breakSpeed;

        SpeedMine speedMine = Modules.get().get(SpeedMine.class);
        if (!speedMine.isActive() || speedMine.mode.get() != SpeedMine.Mode.Normal || !speedMine.filter(block.getBlock()))
            return breakSpeed;

        float breakSpeedMod = (float) (breakSpeed * speedMine.modifier.get());

        if (mc.crosshairTarget instanceof BlockHitResult bhr) {
            BlockPos pos = bhr.getBlockPos();
            if (speedMine.modifier.get() < 1 || (BlockUtils.canInstaBreak(pos, breakSpeed) == BlockUtils.canInstaBreak(pos, breakSpeedMod))) {
                return breakSpeedMod;
            } else {
                return 0.9f / BlockUtils.calcBlockBreakingDelta2(pos, 1);
            }
        }

        return breakSpeed;
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void dontJump(CallbackInfo info) {
        if (!getWorld().isClient) return;

        Anchor module = Modules.get().get(Anchor.class);
        if (module.isActive() && module.cancelJump) info.cancel();
    }

    @ModifyReturnValue(method = "getMovementSpeed", at = @At("RETURN"))
    private float onGetMovementSpeed(float original) {
        if (!getWorld().isClient) return original;
        if (!Modules.get().get(NoSlow.class).slowness()) return original;

        float walkSpeed = getAbilities().getWalkSpeed();

        if (original < walkSpeed) {
            if (isSprinting()) return (float) (walkSpeed * 1.30000001192092896);
            else return walkSpeed;
        }

        return original;
    }

    @Inject(method = "getOffGroundSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetOffGroundSpeed(CallbackInfoReturnable<Float> info) {
        if (!getWorld().isClient) return;

        float speed = Modules.get().get(Flight.class).getOffGroundSpeed();
        if (speed != -1) info.setReturnValue(speed);
    }

    @Inject(method = "attack", at = @At(value = "HEAD"))
    private void inject(Entity target, CallbackInfo ci) {
        attackEntity = target;
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private void poseNotCollide(World instance, PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        SoundModifier m = Modules.get().get(SoundModifier.class);

        if (m.isActive()) {
            if (m.crystalHits.get()) {
                instance.playSound(except, x, y, z, sound, category, (float) (volume * m.crystalHitVolume.get()), (float) (pitch * m.crystalHitPitch.get()));
            }
            return;
        }
        instance.playSound(except, x, y, z, sound, category, volume, pitch);
    }

    @Inject(method = "getAttackCooldownProgress", at = @At("HEAD"), cancellable = true)
    private void onCooldown(float baseTime, CallbackInfoReturnable<Float> cir) {
        if (Modules.get().isActive(OldHitting.class)) {
            cir.setReturnValue(1f);
        }
    }

    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("HEAD"), cancellable = true)
    private void onCooldownPreTick(CallbackInfoReturnable<Float> cir) {
        if (Modules.get().isActive(OldHitting.class)) {
            cir.setReturnValue(1f);
        }
    }
}
