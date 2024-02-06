package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.Chams;
import dev.lemonclient.systems.modules.render.EntityRenders;
import dev.lemonclient.systems.modules.render.Freecam;
import dev.lemonclient.systems.modules.render.NoRender;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.player.Rotations;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static dev.lemonclient.LemonClient.mc;
import static org.lwjgl.opengl.GL11.*;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow
    @Nullable
    protected abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

    @Unique
    private boolean itsami = false;
    @Unique
    private LivingEntity lastEntity;

    // Freecam

    @ModifyExpressionValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
    private Entity hasLabelGetCameraEntityProxy(Entity cameraEntity) {
        return Modules.get().isActive(Freecam.class) ? null : cameraEntity;
    }

    //3rd Person Rotation

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 2, at = @At(value = "STORE", ordinal = 0))
    public float changeYaw(float oldValue, LivingEntity entity) {
        if (entity.equals(mc.player) && Rotations.rotationTimer < 10) return Rotations.serverYaw;
        return oldValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 3, at = @At(value = "STORE", ordinal = 0))
    public float changeHeadYaw(float oldValue, LivingEntity entity) {
        if (entity.equals(mc.player) && Rotations.rotationTimer < 10) return Rotations.serverYaw;
        return oldValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 5, at = @At(value = "STORE", ordinal = 3))
    public float changePitch(float oldValue, LivingEntity entity) {
        if (entity.equals(mc.player) && Rotations.rotationTimer < 10) return Rotations.serverPitch;
        return oldValue;
    }

    // Player model rendering in main menu

    @ModifyExpressionValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getScoreboardTeam()Lnet/minecraft/scoreboard/AbstractTeam;"))
    private AbstractTeam hasLabelClientPlayerEntityGetScoreboardTeamProxy(AbstractTeam team) {
        return (mc.player == null) ? null : team;
    }

    // Through walls chams

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void renderHead(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noDeadEntities() && livingEntity.isDead()) ci.cancel();

        Chams chams = Modules.get().get(Chams.class);

        if (chams.isActive() && chams.shouldRender(livingEntity)) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1.0f, -1100000.0f);
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void renderTail(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams chams = Modules.get().get(Chams.class);

        if (chams.isActive() && chams.shouldRender(livingEntity)) {
            glPolygonOffset(1.0f, 1100000.0f);
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
    }

    // Player chams

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    private void modifyScale(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.players.get() || !(livingEntity instanceof PlayerEntity)) return;
        if (module.ignoreSelf.get() && livingEntity == mc.player) return;

        args.set(0, -module.playersScale.get().floatValue());
        args.set(1, -module.playersScale.get().floatValue());
        args.set(2, module.playersScale.get().floatValue());
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void modifyColor(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.players.get() || !(livingEntity instanceof PlayerEntity)) return;
        if (module.ignoreSelf.get() && livingEntity == mc.player) return;

        Color color = PlayerUtils.getPlayerColor(((PlayerEntity) livingEntity), module.playersColor.get());
        args.set(4, color.r / 255f);
        args.set(5, color.g / 255f);
        args.set(6, color.b / 255f);
        args.set(7, module.playersColor.get().a / 255f);
    }

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer getRenderLayer(LivingEntityRenderer<T, M> livingEntityRenderer, T livingEntity, boolean showBody, boolean translucent, boolean showOutline) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.players.get() || !(livingEntity instanceof PlayerEntity) || module.playersTexture.get())
            return getRenderLayer(livingEntity, showBody, translucent, showOutline);
        if (module.ignoreSelf.get() && livingEntity == mc.player)
            return getRenderLayer(livingEntity, showBody, translucent, showOutline);

        return RenderLayer.getItemEntityTranslucentCull(Chams.BLANK);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        lastEntity = livingEntity;
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void inject(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        itsami = livingEntity == mc.player;
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F", ordinal = 0))
    public void changeBodyYaw(Args args) {
        if (itsami) {
            Managers.ROTATION.setBodyYaw(args);
        }
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F", ordinal = 1))
    public void changeHeadYaw(Args args) {
        if (itsami) {
            Managers.ROTATION.setHeadYaw(args);
        }
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    public void changePitch(Args args) {
        if (itsami) {
            Managers.ROTATION.setPitch(args);
        }
    }

    /*@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void onRenderModel(EntityModel entityModel, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        Color newColor = new Color(red, green, blue, alpha);
        if (Modules.get().get(NoRender.class).antiPlayerCollision() && lastEntity != mc.player) {
            float overrideAlpha = (float) (mc.player.squaredDistanceTo(lastEntity.getPos()) / 3f) + 0.2f;
            newColor = Render2DUtils.injectAlpha(newColor, (int) (255f * MathUtils.clamp(overrideAlpha, 0f, 1f)));
        }
        entityModel.render(matrices, vertices, light, overlay, newColor.r / 255F, newColor.g / 255F, newColor.b / 255F, newColor.a / 255F);
    }*/

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void renderHook(Args args) {
        if (Modules.get().get(NoRender.class).antiPlayerCollision() && lastEntity != mc.player) {
            args.set(7, MathHelper.clamp((float) (mc.player.squaredDistanceTo(lastEntity.getPos()) / 3f) + 0.2f, 0f, 1f));
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    public void onRender(LivingEntity livingEntity, float f, float g, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        EntityRenders entityRenders = Modules.get().get(EntityRenders.class);
        if (entityRenders.isActive() && entityRenders.matrixScale.get())
            matrices.scale(entityRenders.scale.get().floatValue(), entityRenders.scale.get().floatValue(), entityRenders.scale.get().floatValue());
    }

    @Inject(method = "shouldFlipUpsideDown", at = @At("HEAD"), cancellable = true)
    private static void shouldFlip(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        EntityRenders entityRenders = Modules.get().get(EntityRenders.class);
        if (entityRenders.isActive() && entityRenders.flipEntities.get()) {
            cir.setReturnValue(true);
        }
    }
}
