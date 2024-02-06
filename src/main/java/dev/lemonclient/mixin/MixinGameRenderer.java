package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.render.RenderAfterWorldEvent;
import dev.lemonclient.events.render.ShaderEffectRenderEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.mixininterface.IVec3d;
import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.player.LiquidInteract;
import dev.lemonclient.systems.modules.player.NoMiningTrace;
import dev.lemonclient.systems.modules.player.Reach;
import dev.lemonclient.systems.modules.render.*;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.NametagUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.RenderUtils;
import dev.lemonclient.utils.render.shaders.GlProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Consumer;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(value = GameRenderer.class, priority = 999)
public abstract class MixinGameRenderer {
    @Shadow
    @Final
    MinecraftClient client;

    @Shadow
    public abstract void updateTargetedEntity(float tickDelta);

    @Shadow
    public abstract void reset();

    @Shadow
    @Final
    private Camera camera;

    @Shadow
    private float zoom;

    @Shadow
    private float zoomX;

    @Shadow
    private float zoomY;

    @Shadow
    private float viewDistance;

    @Unique
    private Renderer3D renderer;

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", shift = At.Shift.AFTER))
    public void postRender3dHook(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        Managers.SHADER.renderShaders();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", shift = AFTER), method = "render")
    private void hookShaderRender(float tickDelta, long nanoTime, boolean renderLevel, CallbackInfo info) {
        LemonClient.EVENT_BUS.post(ShaderEffectRenderEvent.get(tickDelta));
    }

    @Inject(method = "loadPrograms", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    void loadAllTheShaders(ResourceFactory factory, CallbackInfo ci, List<ShaderStage> stages, List<Pair<ShaderProgram, Consumer<ShaderProgram>>> shadersToLoad) {
        Render2DUtils.initShaders();
        GlProgram.forEachProgram(loader -> shadersToLoad.add(new Pair<>(loader.getLeft().apply(factory), loader.getRight())));
    }

    @Inject(method = "loadPrograms", at = @At(value = "RETURN"))
    private void loadPostPrograms(ResourceFactory factory, CallbackInfo ci) {
    }

    @Inject(at = @At("TAIL"), method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D", cancellable = true)
    public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        CustomFOV customFov = Modules.get().get(CustomFOV.class);

        if (customFov.isActive()) {
            if (customFov.modifyItemFov.get() && cir.getReturnValue() == 70) {
                cir.setReturnValue(customFov.itemFov.get().doubleValue());
            }
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    private void onRender3DHook(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        Nametags.lastProjMat.set(RenderSystem.getProjectionMatrix());
        Nametags.lastModMat.set(RenderSystem.getModelViewMatrix());
        Nametags.lastWorldSpaceMatrix.set(matrix.peek().getPositionMatrix());

        LemonClient.EVENT_BUS.post(Render3DEvent.Unlimited.get(matrix, tickDelta));
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        if (!Utils.canUpdate()) return;

        client.getProfiler().push(LemonClient.MOD_ID + "_render");

        if (renderer == null) renderer = new Renderer3D();

        Render3DEvent event = Render3DEvent.get(matrices, renderer, tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z);

        RenderUtils.updateScreenCenter();
        NametagUtils.onRender(matrices, RenderSystem.getProjectionMatrix());

        renderer.begin();
        LemonClient.EVENT_BUS.post(event);
        renderer.render(matrices, 1.0f);

        RenderSystem.applyModelViewMatrix();
        client.getProfiler().pop();
    }

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void onRenderWorldTail(CallbackInfo info) {
        LemonClient.EVENT_BUS.post(RenderAfterWorldEvent.INSTANCE);
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        AspectRatio aspectRatio = Modules.get().get(AspectRatio.class);

        if (aspectRatio.isActive()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (fov * 0.01745329238474369), aspectRatio.getSettingRatio(), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Inject(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), cancellable = true)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
        if (Modules.get().get(NoMiningTrace.class).canWork() && Modules.get().get(Freecam.class).canWork() && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            client.getProfiler().pop();
            info.cancel();
        }
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"))
    private HitResult updateTargetedEntityEntityRayTraceProxy(Entity entity, double maxDistance, float tickDelta, boolean includeFluids) {
        if (Modules.get().isActive(LiquidInteract.class)) {
            HitResult result = entity.raycast(maxDistance, tickDelta, includeFluids);
            if (result.getType() != HitResult.Type.MISS) return result;

            return entity.raycast(maxDistance, tickDelta, true);
        }
        return entity.raycast(maxDistance, tickDelta, includeFluids);
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING && Modules.get().get(NoRender.class).noTotemAnimation()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float applyCameraTransformationsMathHelperLerpProxy(float original) {
        return Modules.get().get(NoRender.class).noNausea() ? 0 : original;
    }

    // Freecam

    @Unique
    private boolean freecamSet = false;

    @Inject(method = "updateTargetedEntity", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        Freecam freecam = Modules.get().get(Freecam.class);

        if (freecam.isActive() && client.getCameraEntity() != null && !freecamSet) {
            info.cancel();
            Entity cameraE = client.getCameraEntity();

            double x = cameraE.getX();
            double y = cameraE.getY();
            double z = cameraE.getZ();
            double prevX = cameraE.prevX;
            double prevY = cameraE.prevY;
            double prevZ = cameraE.prevZ;
            float yaw = cameraE.getYaw();
            float pitch = cameraE.getPitch();
            float prevYaw = cameraE.prevYaw;
            float prevPitch = cameraE.prevPitch;

            ((IVec3d) cameraE.getPos()).set(freecam.pos.x, freecam.pos.y - cameraE.getEyeHeight(cameraE.getPose()), freecam.pos.z);
            cameraE.prevX = freecam.prevPos.x;
            cameraE.prevY = freecam.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.prevZ = freecam.prevPos.z;
            cameraE.setYaw(freecam.yaw);
            cameraE.setPitch(freecam.pitch);
            cameraE.prevYaw = freecam.prevYaw;
            cameraE.prevPitch = freecam.prevPitch;

            freecamSet = true;
            updateTargetedEntity(tickDelta);
            freecamSet = false;

            ((IVec3d) cameraE.getPos()).set(x, y, z);
            cameraE.prevX = prevX;
            cameraE.prevY = prevY;
            cameraE.prevZ = prevZ;
            cameraE.setYaw(yaw);
            cameraE.setPitch(pitch);
            cameraE.prevYaw = prevYaw;
            cameraE.prevPitch = prevPitch;
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo info) {
        if (!Modules.get().get(Freecam.class).renderHands() ||
            !Modules.get().get(Zoom.class).renderHands())
            info.cancel();
    }

    @ModifyConstant(method = "updateTargetedEntity", constant = @Constant(doubleValue = 3), require = 0)
    private double updateTargetedEntityModifySurvivalReach(double d) {
        return Modules.get().get(Reach.class).entityReach();
    }

    @ModifyConstant(method = "updateTargetedEntity", constant = @Constant(doubleValue = 9), require = 0)
    private double updateTargetedEntityModifySquaredMaxReach(double d) {
        Reach reach = Modules.get().get(Reach.class);
        return reach.entityReach() * reach.entityReach();
    }
}
