package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.LivingEntityMoveEvent;
import dev.lemonclient.events.entity.player.JumpVelocityMultiplierEvent;
import dev.lemonclient.events.entity.player.PlayerMoveEvent;
import dev.lemonclient.events.entity.player.StrafeMoveEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.combat.Hitboxes;
import dev.lemonclient.systems.modules.movement.*;
import dev.lemonclient.systems.modules.movement.elytrafly.ElytraFly;
import dev.lemonclient.systems.modules.render.ESP;
import dev.lemonclient.systems.modules.render.ForceSneak;
import dev.lemonclient.systems.modules.render.NoRender;
import dev.lemonclient.systems.modules.render.Shaders;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.fakeplayer.FakePlayerEntity;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

import static dev.lemonclient.LemonClient.mc;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract boolean isInPose(EntityPose pose);

    @Shadow
    public abstract Text getName();

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract ActionResult interact(PlayerEntity player, Hand hand);

    @Shadow
    protected abstract void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);

    @Shadow
    protected abstract boolean stepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3d movement);

    @Shadow
    public abstract float getStepHeight();

    @Shadow
    public abstract boolean isOnGround();

    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @ModifyExpressionValue(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getVelocity(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d updateMovementInFluidFluidStateGetVelocity(Vec3d vec) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if ((Object) this == mc.player && velocity.isActive() && velocity.liquids.get()) {
            vec = vec.multiply(velocity.getHorizontal(velocity.liquidsHorizontal), velocity.getVertical(velocity.liquidsVertical), velocity.getHorizontal(velocity.liquidsHorizontal));
        }

        return vec;
    }

    @Inject(method = "updateVelocity", at = @At(value = "HEAD"), cancellable = true)
    private void onStrafe(float speed, Vec3d movementInput, CallbackInfo ci) {
        if ((Object) this == mc.player) {
            float yaw = this.getYaw(1f);
            StrafeMoveEvent e = LemonClient.EVENT_BUS.post(StrafeMoveEvent.get(movementInput, speed, yaw));
            yaw = e.yaw;
            speed = e.speed;
            movementInput = e.movementInput;
            Vec3d vec3d = fixedMovementInputToVelocity(movementInput, speed, yaw);
            this.setVelocity(this.getVelocity().add(vec3d));
        }

        ci.cancel();
    }

    @Unique
    private static Vec3d fixedMovementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double fac = movementInput.lengthSquared();
        if (fac < 1.0E-7) {
            return Vec3d.ZERO;
        } else {
            Vec3d vec = (fac > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);

            double strafe = vec.x;
            double forward = vec.z;

            float yawSin = MathHelper.sin(yaw * 0.017453292F);
            float yawCos = MathHelper.cos(yaw * 0.017453292F);
            return new Vec3d(
                strafe * yawCos - forward * yawSin,
                vec.y,
                forward * yawCos + strafe * yawSin);
        }
    }


    @Inject(method = "isTouchingWater", at = @At(value = "HEAD"), cancellable = true)
    private void isTouchingWater(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this == mc.player && Modules.get().isActive(Flight.class)) info.setReturnValue(false);
        if ((Object) this == mc.player && Modules.get().get(NoSlow.class).fluidDrag()) info.setReturnValue(false);
    }

    @Inject(method = "isInLava", at = @At(value = "HEAD"), cancellable = true)
    private void isInLava(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this == mc.player && Modules.get().isActive(Flight.class)) info.setReturnValue(false);
        if ((Object) this == mc.player && Modules.get().get(NoSlow.class).fluidDrag()) info.setReturnValue(false);
    }

    @ModifyExpressionValue(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSubmergedInWater()Z"))
    private boolean isSubmergedInWater(boolean submerged) {
        if ((Object) this == mc.player && Modules.get().get(NoSlow.class).fluidDrag()) return false;
        if ((Object) this == mc.player && Modules.get().isActive(Flight.class)) return false;
        return submerged;
    }

    @ModifyArgs(method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Args args, Entity entity) {
        Velocity velocity = Modules.get().get(Velocity.class);

        // Velocity
        if ((Object) this == mc.player && velocity.isActive() && velocity.entityPush.get()) {
            double multiplier = velocity.entityPushAmount.get();
            args.set(0, (double) args.get(0) * multiplier);
            args.set(2, (double) args.get(2) * multiplier);
        }
        // FakePlayerEntity
        else if (entity instanceof FakePlayerEntity player && player.doNotPush) {
            args.set(0, 0.0);
            args.set(2, 0.0);
        }
    }

    @ModifyReturnValue(method = "getJumpVelocityMultiplier", at = @At("RETURN"))
    private float onGetJumpVelocityMultiplier(float original) {
        if ((Object) this == mc.player) {
            JumpVelocityMultiplierEvent event = LemonClient.EVENT_BUS.post(JumpVelocityMultiplierEvent.get());
            return (original * event.multiplier);
        }

        return original;
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
        if ((Object) this == mc.player) {
            LemonClient.EVENT_BUS.post(PlayerMoveEvent.get(type, movement));
        } else if ((Object) this instanceof LivingEntity) {
            LemonClient.EVENT_BUS.post(LivingEntityMoveEvent.get((LivingEntity) (Object) this, movement));
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> info) {
        if (PostProcessShaders.rendering) {
            Color color = Modules.get().get(ESP.class).getColor((Entity) (Object) this);
            if (color != null) info.setReturnValue(color.getPacked());
        }
    }

    @Redirect(method = "getVelocityMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block getVelocityMultiplierGetBlockProxy(BlockState blockState) {
        if ((Object) this != mc.player) return blockState.getBlock();
        if (blockState.getBlock() == Blocks.SOUL_SAND && Modules.get().get(NoSlow.class).soulSand())
            return Blocks.STONE;
        if (blockState.getBlock() == Blocks.HONEY_BLOCK && Modules.get().get(NoSlow.class).honeyBlock())
            return Blocks.STONE;
        return blockState.getBlock();
    }

    @ModifyReturnValue(method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("RETURN"))
    private boolean isInvisibleToCanceller(boolean original) {
        if (!Utils.canUpdate()) return original;
        ESP esp = Modules.get().get(ESP.class);
        if (Modules.get().get(NoRender.class).noInvisibility() || esp.isActive() && !esp.shouldSkip((Entity) (Object) this))
            return false;
        return original;
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void isGlowing(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(NoRender.class).noGlowing()) info.setReturnValue(false);
    }

    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> info) {
        double v = Modules.get().get(Hitboxes.class).getEntityValue((Entity) (Object) this);
        if (v != 0) info.setReturnValue((float) v);
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void onIsInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
        if (player == null) info.setReturnValue(false);
    }

    @Inject(method = "getPose", at = @At("HEAD"), cancellable = true)
    private void getPoseHook(CallbackInfoReturnable<EntityPose> info) {
        if ((Object) this == mc.player && Modules.get().get(ElytraFly.class).canPacketEfly()) {
            info.setReturnValue(EntityPose.FALL_FLYING);
        }
    }

    @ModifyReturnValue(method = "bypassesLandingEffects", at = @At("RETURN"))
    private boolean cancelBounce(boolean original) {
        return Modules.get().get(NoFall.class).cancelBounce() || original;
    }

    @Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void inject(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        StepPlus step = Modules.get().get(StepPlus.class);
        if (step.shouldPauseWeb()) return;

        Entity entity = (Entity) (Object) this;
        boolean active = step.isActive() && entity == mc.player;

        if (active && step.slow.get()) {
            step.slowStep(entity, movement, cir);
            return;
        }

        active = active && System.currentTimeMillis() - step.lastStep > step.cooldown.get() * 1000;

        Box box = this.getBoundingBox();
        List<VoxelShape> list = this.getWorld().getEntityCollisions(entity, box.stretch(movement));
        Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(entity, movement, box, this.getWorld(), list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = this.isOnGround() || (!active && bl2 && movement.y < 0.0);
        if ((active ? step.height.get() : this.getStepHeight()) > 0.0F && bl4 && (bl || bl3)) {
            Vec3d vec3d2 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, active ? step.height.get() : this.getStepHeight(), movement.z), box, this.getWorld(), list);
            Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, active ? step.height.get() : this.getStepHeight(), 0.0), box.stretch(movement.x, 0.0, movement.z), this.getWorld(), list);
            if (vec3d3.y < (active ? step.height.get() : this.getStepHeight())) {
                Vec3d vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, 0.0, movement.z), box.offset(vec3d3), this.getWorld(), list).add(vec3d3);
                if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                    vec3d2 = vec3d4;
                }
            }

            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                Vec3d v = vec3d2.add(Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, -vec3d2.y + movement.y, 0.0), box.offset(vec3d2), entity.getWorld(), list));
                if (active) step.step(step.getOffsets(v.y));
                cir.setReturnValue(v);
                return;
            }
        }

        cir.setReturnValue(vec3d);
    }

    @Inject(method = "isInSneakingPose", at = @At(value = "RETURN"), cancellable = true)
    private void isSneaking(CallbackInfoReturnable<Boolean> cir) {
        if (mc.player == null || this.getName() != mc.player.getName()) {
            cir.setReturnValue(Modules.get().get(ForceSneak.class).isActive() || this.isInPose(EntityPose.CROUCHING));
        }
    }

    @Inject(method = "doesNotCollide(Lnet/minecraft/util/math/Box;)Z", at = @At("RETURN"), cancellable = true)
    private void doesNotCollide(Box box, CallbackInfoReturnable<Boolean> cir) {
        if (Modules.get().isActive(AntiCrawl.class)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
        Shaders shaders = Modules.get().get(Shaders.class);
        if (shaders.isActive()) {
            cir.setReturnValue(shaders.shouldRender((Entity) (Object) this));
        }
    }
}
