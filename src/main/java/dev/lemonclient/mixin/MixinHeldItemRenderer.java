package dev.lemonclient.mixin;

import com.google.common.base.MoreObjects;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.ArmRenderEvent;
import dev.lemonclient.events.render.HeldItemRendererEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.lemonclient.LemonClient.mc;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {
    @Shadow
    protected abstract void applyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack);

    @Shadow
    protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);

    @Shadow
    protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

    @Shadow
    protected abstract void renderMapInBothHands(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float pitch, float equipProgress, float swingProgress);

    @Shadow
    protected abstract void renderMapInOneHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, Arm arm, float swingProgress, ItemStack stack);

    @Shadow
    protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);

    @Shadow
    protected abstract void applyBrushTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, float equipProgress);

    @Shadow
    public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ItemStack mainHand;

    @Shadow
    private ItemStack offHand;

    @Shadow
    private float equipProgressMainHand;

    @Shadow
    private float equipProgressOffHand;

    @Shadow
    private float prevEquipProgressMainHand;

    @Shadow
    private float prevEquipProgressOffHand;

    @Inject(method = "updateHeldItems", at = @At("HEAD"), cancellable = true)
    public void updateHeldItems(CallbackInfo ci) {
        HandTweaks handTweaks = Modules.get().get(HandTweaks.class);

        if (handTweaks.isActive()) {
            ci.cancel();
            this.prevEquipProgressMainHand = this.equipProgressMainHand;
            this.prevEquipProgressOffHand = this.equipProgressOffHand;

            ItemStack itemStack = client.player.getMainHandStack();
            ItemStack itemStack2 = client.player.getOffHandStack();

            if (ItemStack.areEqual(this.mainHand, itemStack)) {
                this.mainHand = itemStack;
            }

            if (ItemStack.areEqual(this.offHand, itemStack2)) {
                this.offHand = itemStack2;
            }

            if (client.player.isRiding()) {
                this.equipProgressMainHand = MathHelper.clamp(this.equipProgressMainHand - 0.4F, 0.0F, 1.0F);
                this.equipProgressOffHand = MathHelper.clamp(this.equipProgressOffHand - 0.4F, 0.0F, 1.0F);
            } else {
                this.equipProgressMainHand += MathHelper.clamp((this.mainHand == itemStack ? 1.0F : 0.0F) - this.equipProgressMainHand, -0.4F, 0.4F);
                this.equipProgressOffHand += MathHelper.clamp((float) (this.offHand == itemStack2 ? 1 : 0) - this.equipProgressOffHand, -0.4F, 0.4F);
            }

            if (this.equipProgressMainHand < 0.1F) {
                this.mainHand = itemStack;
            }

            if (this.equipProgressOffHand < 0.1F) {
                this.offHand = itemStack2;
            }
        }
    }

    @ModifyVariable(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "STORE", ordinal = 0), index = 6)
    private float modifySwing(float swingProgress) {
        HandView module = Modules.get().get(HandView.class);
        Hand hand = MoreObjects.firstNonNull(mc.player.preferredHand, Hand.MAIN_HAND);

        if (module.isActive()) {
            if (hand == Hand.OFF_HAND && !mc.player.getOffHandStack().isEmpty()) {
                return swingProgress + module.offSwing.get().floatValue();
            }
            if (hand == Hand.MAIN_HAND && !mc.player.getMainHandStack().isEmpty()) {
                return swingProgress + module.mainSwing.get().floatValue();
            }
        }

        return swingProgress;
    }

    @Redirect(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean redirectSwapping(ItemStack left, ItemStack right) {
        return showSwapping(left, right);
    }

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 2), index = 0)
    private float modifyEquipProgressMainhand(float value) {
        float f = mc.player.getAttackCooldownProgress(1f);
        float modified = Modules.get().get(HandView.class).oldAnimations() ? 1 : f * f * f;

        return (showSwapping(mainHand, mc.player.getMainHandStack()) ? modified : 0) - equipProgressMainHand;
    }

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 3), index = 0)
    private float modifyEquipProgressOffhand(float value) {
        return (showSwapping(offHand, mc.player.getOffHandStack()) ? 1 : 0) - equipProgressOffHand;
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        LemonClient.EVENT_BUS.post(HeldItemRendererEvent.get(hand, matrices, item));
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V"))
    private void onRenderArmHoldingItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        LemonClient.EVENT_BUS.post(ArmRenderEvent.get(hand, matrices));
    }

    @Inject(method = "applyEatOrDrinkTransformation", at = @At(value = "INVOKE", target = "Ljava/lang/Math;pow(DD)D", shift = At.Shift.BEFORE), cancellable = true)
    private void cancelTransformations(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, CallbackInfo ci) {
        if (Modules.get().get(HandView.class).disableFoodAnimation()) ci.cancel();
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderArm(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        OldHitting oldHitting = Modules.get().get(OldHitting.class);
        if (oldHitting.isActive() && !oldHitting.visibleOffHand.get()) {
            if (hand == Hand.OFF_HAND) {
                return;
            }
        }

        if (!player.isUsingSpyglass()) {
            boolean bl = hand == Hand.MAIN_HAND;
            Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
            matrices.push();
            if (item.isEmpty()) {
                if (bl && !player.isInvisible()) {
                    LemonClient.EVENT_BUS.post(ArmRenderEvent.get(hand, matrices));
                    this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
                }
            } else if (item.isOf(Items.FILLED_MAP)) {
                if (bl && this.offHand.isEmpty()) {
                    this.renderMapInBothHands(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
                } else {
                    this.renderMapInOneHand(matrices, vertexConsumers, light, equipProgress, arm, swingProgress, item);
                }
            } else {
                boolean bl2;
                float f;
                float g;
                float h;
                float j;
                if (item.isOf(Items.CROSSBOW)) {
                    bl2 = CrossbowItem.isCharged(item);
                    boolean bl3 = arm == Arm.RIGHT;
                    int i = bl3 ? 1 : -1;
                    if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        matrices.translate((float) i * -0.4785682F, -0.094387F, 0.05731531F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * 65.3F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * -9.785F));
                        f = (float) item.getMaxUseTime() - ((float) this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                        g = f / (float) CrossbowItem.getPullTime(item);
                        if (g > 1.0F) {
                            g = 1.0F;
                        }

                        if (g > 0.1F) {
                            h = MathHelper.sin((f - 0.1F) * 1.3F);
                            j = g - 0.1F;
                            float k = h * j;
                            matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
                        }

                        matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
                        matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) i * 45.0F));
                    } else {
                        f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                        g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                        h = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                        matrices.translate((float) i * f, g, h);
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        this.applySwingOffset(matrices, arm, swingProgress);
                        if (bl2 && swingProgress < 0.001F && bl) {
                            matrices.translate((float) i * -0.641864F, 0.0F, 0.0F);
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * 10.0F));
                        }
                    }

                    LemonClient.EVENT_BUS.post(HeldItemRendererEvent.get(hand, matrices, item));
                    this.renderItem(player, item, bl3 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl3, matrices, vertexConsumers, light);
                } else {
                    bl2 = arm == Arm.RIGHT;
                    int l;
                    float m;
                    if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                        l = bl2 ? 1 : -1;
                        switch (item.getUseAction()) {
                            case NONE -> this.applyEquipOffset(matrices, arm, equipProgress);
                            case EAT, DRINK -> {
                                if (oldHitting.isActive()) {
                                    if (swingProgress != 0.0f) {
                                        float side = hand == Hand.MAIN_HAND ? 1.0f : -1.0F;
                                        matrices.translate(side * 0.56, -0.52 + equipProgress * -0.6, -0.72);
                                        float f2 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
                                        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(f2 * 20.0F));
                                        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(f2 * 20.0F));
                                        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(f2 * 80.0F));
                                        matrices.translate(-0.8f, 0.2f, 0f);
                                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30F));
                                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80F));
                                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60F));
                                        matrices.scale(1.4f, 1.4f, 1.4f);
                                        break;
                                    }
                                    this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item);
                                    this.applyEquipOffset(matrices, arm, equipProgress);
                                    doSwingAnimation(matrices, swingProgress);
                                    break;
                                }
                                this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item);
                                this.applyEquipOffset(matrices, arm, equipProgress);
                            }
                            case BLOCK -> {
                                if (oldHitting.isActive()) {
                                    transformFirstPersonItem(matrices, hand == Hand.MAIN_HAND ? 1.0f : -1.0F, equipProgress, swingProgress);
                                    break;
                                }
                                this.applyEquipOffset(matrices, arm, equipProgress);
                            }
                            case BOW -> {
                                this.applyEquipOffset(matrices, arm, equipProgress);
                                matrices.translate((float) l * -0.2785682F, 0.18344387F, 0.15731531F);
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 35.3F));
                                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -9.785F));
                                m = (float) item.getMaxUseTime() - ((float) this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                                f = m / 20.0F;
                                f = (f * f + f * 2.0F) / 3.0F;
                                if (f > 1.0F) {
                                    f = 1.0F;
                                }
                                if (f > 0.1F) {
                                    g = MathHelper.sin((m - 0.1F) * 1.3F);
                                    h = f - 0.1F;
                                    j = g * h;
                                    matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                                }
                                matrices.translate(f * 0.0F, f * 0.0F, f * 0.04F);
                                matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) l * 45.0F));
                            }
                            case SPEAR -> {
                                this.applyEquipOffset(matrices, arm, equipProgress);
                                matrices.translate((float) l * -0.5F, 0.7F, 0.1F);
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 35.3F));
                                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -9.785F));
                                m = (float) item.getMaxUseTime() - ((float) this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                                f = m / 10.0F;
                                if (f > 1.0F) {
                                    f = 1.0F;
                                }
                                if (f > 0.1F) {
                                    g = MathHelper.sin((m - 0.1F) * 1.3F);
                                    h = f - 0.1F;
                                    j = g * h;
                                    matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                                }
                                matrices.translate(0.0F, 0.0F, f * 0.2F);
                                matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) l * 45.0F));
                            }
                            case BRUSH -> this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
                        }
                    } else if (player.isUsingRiptide()) {
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        l = bl2 ? 1 : -1;
                        matrices.translate((float) l * -0.4F, 0.8F, 0.3F);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 65.0F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -85.0F));
                    } else {
                        float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                        m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                        f = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                        int o = bl2 ? 1 : -1;
                        matrices.translate((float) o * n, m, f);
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        this.applySwingOffset(matrices, arm, swingProgress);
                    }

                    LemonClient.EVENT_BUS.post(HeldItemRendererEvent.get(hand, matrices, item));
                    this.renderItem(player, item, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
                }
            }

            matrices.pop();
        }

        ci.cancel();
    }


    private void doSwingAnimation(MatrixStack m, float swingProgress) {
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -20.0F));
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f1 * -20.0F));
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -80.0F));
    }

    private void transformFirstPersonItem(MatrixStack m, float side, float equipProgress, float swingProgress) {
        OldHitting oldHitting = Modules.get().get(OldHitting.class);
        OldHitting.RenderMode mode = oldHitting.animationMode.get();

        switch (mode) {
            case Vanilla -> {
                m.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
                float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
                float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * (45.0F + f * -20.0F)));
                m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * f1 * -20.0F));
                m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -80.0F));
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45.0F));
                m.scale(0.9F, 0.9F, 0.9F);
                m.translate(-0.2F, 0.126F, 0.2F);
                m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 15.0F));
                m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * 80.0F));
            }
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true, require = 0)
    private void onRenderItemHook(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Modules.get().isActive(Animations.class) && !(item.isEmpty()) && !(item.getItem() instanceof FilledMapItem)) {
            ci.cancel();
            renderFirstPersonItemCustom(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
        }
    }

    @Inject(method = "applyEatOrDrinkTransformation", at = @At(value = "HEAD"), cancellable = true, require = 0)
    private void applyEatOrDrinkTransformationHook(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, CallbackInfo ci) {
        if (Modules.get().isActive(Animations.class)) {
            applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, stack);
            ci.cancel();
        }
    }

    private void renderFirstPersonItemCustom(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!player.isUsingSpyglass()) {
            boolean bl = hand == Hand.MAIN_HAND;
            Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
            matrices.push();

            boolean bl2;
            float f;
            float g;
            float h;
            float j;
            if (item.isOf(Items.CROSSBOW)) {
                bl2 = CrossbowItem.isCharged(item);
                boolean bl3 = arm == Arm.RIGHT;
                int i = bl3 ? 1 : -1;
                if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    matrices.translate((float) i * -0.4785682F, -0.094387F, 0.05731531F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * 65.3F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * -9.785F));
                    f = (float) item.getMaxUseTime() - ((float) mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                    g = f / (float) CrossbowItem.getPullTime(item);
                    if (g > 1.0F) {
                        g = 1.0F;
                    }

                    if (g > 0.1F) {
                        h = MathHelper.sin((f - 0.1F) * 1.3F);
                        j = g - 0.1F;
                        float k = h * j;
                        matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
                    }

                    matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
                    matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
                    matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) i * 45.0F));
                } else {
                    f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                    g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                    h = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                    matrices.translate((float) i * f, g, h);
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    this.applySwingOffset(matrices, arm, swingProgress);
                    if (bl2 && swingProgress < 0.001F && bl) {
                        matrices.translate((float) i * -0.641864F, 0.0F, 0.0F);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * 10.0F));
                    }
                }

                this.renderItem(player, item, bl3 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl3, matrices, vertexConsumers, light);
            } else {
                bl2 = arm == Arm.RIGHT;
                int l;
                float m;
                if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                    l = bl2 ? 1 : -1;
                    switch (item.getUseAction()) {
                        case NONE, BLOCK -> this.applyEquipOffset(matrices, arm, equipProgress);
                        case EAT, DRINK -> {
                            applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, item);
                            this.applyEquipOffset(matrices, arm, equipProgress);
                        }
                        case BOW -> {
                            this.applyEquipOffset(matrices, arm, equipProgress);
                            matrices.translate((float) l * -0.2785682F, 0.18344387F, 0.15731531F);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 35.3F));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -9.785F));
                            m = (float) item.getMaxUseTime() - ((float) mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                            f = m / 20.0F;
                            f = (f * f + f * 2.0F) / 3.0F;
                            if (f > 1.0F) {
                                f = 1.0F;
                            }
                            if (f > 0.1F) {
                                g = MathHelper.sin((m - 0.1F) * 1.3F);
                                h = f - 0.1F;
                                j = g * h;
                                matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                            }
                            matrices.translate(f * 0.0F, f * 0.0F, f * 0.04F);
                            matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) l * 45.0F));
                        }
                        case SPEAR -> {
                            this.applyEquipOffset(matrices, arm, equipProgress);
                            matrices.translate((float) l * -0.5F, 0.7F, 0.1F);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 35.3F));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -9.785F));
                            m = (float) item.getMaxUseTime() - ((float) mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                            f = m / 10.0F;
                            if (f > 1.0F) {
                                f = 1.0F;
                            }
                            if (f > 0.1F) {
                                g = MathHelper.sin((m - 0.1F) * 1.3F);
                                h = f - 0.1F;
                                j = g * h;
                                matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                            }
                            matrices.translate(0.0F, 0.0F, f * 0.2F);
                            matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) l * 45.0F));
                        }
                        case BRUSH -> this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
                    }
                } else if (player.isUsingRiptide()) {
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    l = bl2 ? 1 : -1;
                    matrices.translate((float) l * -0.4F, 0.8F, 0.3F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) l * 65.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) l * -85.0F));
                } else {
                    float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                    m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
                    f = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
                    int o = bl2 ? 1 : -1;
                    matrices.translate(0, 0, 0);
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    this.applySwingOffset(matrices, arm, swingProgress);
                }

                this.renderItem(player, item, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
            }
            matrices.pop();
        }
    }

    private void applyEatOrDrinkTransformationCustom(MatrixStack matrices, float tickDelta, Arm arm, @NotNull ItemStack stack) {
        float f = (float) mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float g = f / (float) stack.getMaxUseTime();
        float h;
        if (g < 0.8F) {
            h = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.005F);
            matrices.translate(0.0F, h, 0.0F);
        }
        h = 1.0F - (float) Math.pow(g, 27.0);
        int i = arm == Arm.RIGHT ? 1 : -1;

        ViewModel viewModel = Modules.get().get(ViewModel.class);

        matrices.translate(h * 0.6F * (float) i * viewModel.eatX.get(), h * -0.5F * viewModel.eatY.get(), h * 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * h * 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * h * 30.0F));
    }

    @Unique
    private boolean showSwapping(ItemStack stack1, ItemStack stack2) {
        return !Modules.get().get(HandView.class).showSwapping() || ItemStack.areEqual(stack1, stack2);
    }
}
