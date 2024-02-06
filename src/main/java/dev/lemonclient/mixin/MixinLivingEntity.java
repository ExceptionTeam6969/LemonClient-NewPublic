package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.DamageEvent;
import dev.lemonclient.events.entity.player.CanWalkOnFluidEvent;
import dev.lemonclient.events.entity.player.StrafeJumpEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import dev.lemonclient.systems.modules.movement.elytrafly.ElytraFly;
import dev.lemonclient.systems.modules.movement.elytrafly.modes.Bounce;
import dev.lemonclient.systems.modules.player.PotionSpoof;
import dev.lemonclient.systems.modules.render.HandView;
import dev.lemonclient.systems.modules.render.NoRender;
import dev.lemonclient.systems.modules.render.SwingAnimation;
import dev.lemonclient.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static dev.lemonclient.LemonClient.mc;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    @Shadow
    @Final
    private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamageHead(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (Utils.canUpdate() && getWorld().isClient)
            LemonClient.EVENT_BUS.post(DamageEvent.get((LivingEntity) (Object) this, source, amount));
    }

    @Inject(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V", shift = At.Shift.AFTER), cancellable = true)
    private void onJump(CallbackInfo ci) {
        StrafeJumpEvent event = LemonClient.EVENT_BUS.post(StrafeJumpEvent.get(isSprinting(), getYaw()));
        if (event.sprint) {
            float fac = event.yaw * 0.017453292F;
            this.setVelocity(this.getVelocity().add(-MathHelper.sin(fac) * 0.2F, 0.0, MathHelper.cos(fac) * 0.2F));
        }
        this.velocityDirty = true;
        ci.cancel();
    }

    @ModifyReturnValue(method = "canWalkOnFluid", at = @At("RETURN"))
    private boolean onCanWalkOnFluid(boolean original, FluidState fluidState) {
        if ((Object) this != mc.player) return original;
        CanWalkOnFluidEvent event = LemonClient.EVENT_BUS.post(CanWalkOnFluidEvent.get(fluidState));

        return event.walkOnFluid;
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasNoGravity()Z"))
    private boolean travelHasNoGravityProxy(LivingEntity self) {
        if (activeStatusEffects.containsKey(StatusEffects.LEVITATION) && Modules.get().get(PotionSpoof.class).shouldBlock(StatusEffects.LEVITATION)) {
            return !Modules.get().get(PotionSpoof.class).applyGravity.get();
        }
        return self.hasNoGravity();
    }

    @Inject(method = "spawnItemParticles", at = @At("HEAD"), cancellable = true)
    private void spawnItemParticles(ItemStack stack, int count, CallbackInfo info) {
        NoRender noRender = Modules.get().get(NoRender.class);
        if (noRender.noEatParticles() && stack.isFood()) info.cancel();
    }

    @ModifyArg(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;swingHand(Lnet/minecraft/util/Hand;Z)V"))
    private Hand setHand(Hand hand) {
        HandView handView = Modules.get().get(HandView.class);
        if ((Object) this == mc.player && handView.isActive()) {
            if (handView.swingMode.get() == HandView.SwingMode.None) return hand;
            return handView.swingMode.get() == HandView.SwingMode.Offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
        }
        return hand;
    }

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void getArmSwingAnimationEnd(CallbackInfoReturnable<Integer> cir) {
        SwingAnimation swingAnimation = Modules.get().get(SwingAnimation.class);
        if (swingAnimation.isActive() && mc.options.getPerspective().isFirstPerson()) {
            cir.setReturnValue(swingAnimation.swingSpeed.get());
        }
    }

    @ModifyReturnValue(method = "isFallFlying", at = @At("RETURN"))
    private boolean isFallFlyingHook(boolean original) {
        if ((Object) this == mc.player && Modules.get().get(ElytraFly.class).canPacketEfly()) {
            return true;
        }

        return original;
    }

    private boolean previousElytra = false;

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = cir.getReturnValue();
        ElytraFly elytraFly = Modules.get().get(ElytraFly.class);
        if (previousElytra && !elytra && elytraFly.isActive() && elytraFly.flightMode.get() == ElytraFlightModes.Bounce) {
            cir.setReturnValue(Bounce.recastElytra(mc.player));
        }
        previousElytra = elytra;
    }

    @ModifyReturnValue(method = "hasStatusEffect", at = @At("RETURN"))
    private boolean hasStatusEffect(boolean original, StatusEffect effect) {
        if (Modules.get().get(PotionSpoof.class).shouldBlock(effect)) return false;

        return original;
    }
}
