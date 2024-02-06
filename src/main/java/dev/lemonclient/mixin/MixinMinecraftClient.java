package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.player.InteractEvent;
import dev.lemonclient.events.entity.player.ItemUseCrosshairTargetEvent;
import dev.lemonclient.events.game.GameLeftEvent;
import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.events.game.ResourcePacksReloadedEvent;
import dev.lemonclient.events.game.WindowResizedEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.gui.WidgetScreen;
import dev.lemonclient.mixininterface.IMinecraftClient;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.player.FastUse;
import dev.lemonclient.systems.modules.player.MultiTask;
import dev.lemonclient.systems.modules.render.OldHitting;
import dev.lemonclient.systems.modules.render.UnfocusedCPU;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.CPSUtils;
import dev.lemonclient.utils.misc.Starscript;
import meteordevelopment.starscript.Script;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;

@Mixin(value = MinecraftClient.class, priority = 1001)
public abstract class MixinMinecraftClient implements IMinecraftClient {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Shadow
    @Final
    public GameOptions options;
    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;
    @Shadow
    @Nullable
    public HitResult crosshairTarget;
    @Shadow
    @Nullable
    public Screen currentScreen;

    @Shadow
    protected abstract void handleBlockBreaking(boolean breaking);

    @Shadow
    private int itemUseCooldown;

    @Shadow
    protected abstract void doItemPick();

    @Shadow
    protected abstract boolean doAttack();

    @Shadow
    @Final
    public Mouse mouse;
    @Unique
    private boolean doItemUseCalled;
    @Unique
    private boolean rightClick;
    @Unique
    private long lastTime;
    @Unique
    private boolean firstFrame;

    @Shadow
    public ClientWorld world;

    @Shadow
    @Final
    private Window window;

    @Shadow
    protected abstract void doItemUse();

    @Shadow
    public abstract Profiler getProfiler();

    @Shadow
    public abstract boolean isWindowFocused();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        LemonClient.INSTANCE.onInitializeClient();
        firstFrame = true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo info) {
        doItemUseCalled = false;

        getProfiler().push(LemonClient.MOD_ID + "_pre_update");
        LemonClient.EVENT_BUS.post(TickEvent.Pre.get());
        getProfiler().pop();

        if (rightClick && !doItemUseCalled && interactionManager != null) doItemUse();
        rightClick = false;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo info) {
        getProfiler().push(LemonClient.MOD_ID + "_post_update");
        LemonClient.EVENT_BUS.post(TickEvent.Post.get());
        getProfiler().pop();
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        CPSUtils.onAttack();
    }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void onDoItemUse(CallbackInfo info) {
        doItemUseCalled = true;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (world != null) {
            LemonClient.EVENT_BUS.post(GameLeftEvent.get());
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof WidgetScreen)
            screen.mouseMoved(mouse.getX() * window.getScaleFactor(), mouse.getY() * window.getScaleFactor());

        OpenScreenEvent event = OpenScreenEvent.get(screen);
        LemonClient.EVENT_BUS.post(event);

        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onDoItemUseHand(CallbackInfo ci, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack) {
        FastUse fastUse = Modules.get().get(FastUse.class);
        if (fastUse.isActive()) {
            itemUseCooldown = fastUse.getItemUseCooldown(itemStack);
        }
    }

    @ModifyExpressionValue(method = "doItemUse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 1))
    private HitResult doItemUseMinecraftClientCrosshairTargetProxy(HitResult original) {
        return LemonClient.EVENT_BUS.post(ItemUseCrosshairTargetEvent.get(original)).target;
    }

    @ModifyReturnValue(method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private CompletableFuture<Void> onReloadResourcesNewCompletableFuture(CompletableFuture<Void> original) {
        return original.thenRun(() -> LemonClient.EVENT_BUS.post(ResourcePacksReloadedEvent.INSTANCE));
    }

    @ModifyArg(method = "updateWindowTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"))
    private String setTitle(String original) {
        if (Config.get() == null || !Config.get().customWindowTitle.get()) return original;

        String customTitle = Config.get().customWindowTitleText.get();
        Script script = Starscript.compile(customTitle);

        if (script != null) {
            String title = Starscript.run(script);
            if (title != null) customTitle = title;
        }

        return customTitle;
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo info) {
        LemonClient.EVENT_BUS.post(WindowResizedEvent.INSTANCE);
    }

    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> info) {
        if (Modules.get().isActive(UnfocusedCPU.class) && !isWindowFocused()) {
            info.setReturnValue(Math.min(Modules.get().get(UnfocusedCPU.class).fps.get(), this.options.getMaxFps().getValue()));
        }
    }

    // Time delta

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo info) {
        long time = System.currentTimeMillis();

        if (firstFrame) {
            lastTime = time;
            firstFrame = false;
        }

        Utils.frameTime = (time - lastTime) / 1000.0;
        lastTime = time;
    }

    @Redirect(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    public boolean breakBlockCheck(ClientPlayerEntity clientPlayerEntity) {
        return !Modules.get().isActive(MultiTask.class) && (LemonClient.EVENT_BUS.post(InteractEvent.get(clientPlayerEntity.isUsingItem()))).usingItem;
    }

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"), require = 0)
    public boolean useItemBreakCheck(ClientPlayerInteractionManager clientPlayerInteractionManager) {
        return !Modules.get().isActive(MultiTask.class) && (LemonClient.EVENT_BUS.post(InteractEvent.get(clientPlayerInteractionManager.isBreakingBlock()))).usingItem;
    }

    @Inject(method = "handleInputEvents", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;", shift = At.Shift.BEFORE, ordinal = 11), cancellable = true)
    private void onInput(CallbackInfo ci) {
        OldHitting oldHitting = Modules.get().get(OldHitting.class);
        if (oldHitting.isActive()) {
            boolean leftClicked = false;
            if (this.player.isUsingItem()) {
                if (!this.options.useKey.isPressed()) {
                    this.interactionManager.stopUsingItem(this.player);
                }

                if (!this.player.hasVehicle() && this.crosshairTarget != null) {
                    if (this.player.getActiveItem().getItem() instanceof SwordItem || this.player.getActiveItem().getItem() instanceof BowItem || this.player.getActiveItem().getItem().isFood() || this.player.getActiveItem().getItem() instanceof PotionItem) {
                        if (this.options.attackKey.isPressed()) {
                            if (this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                                this.player.swingHand(Hand.MAIN_HAND, false);
                            }
                        }
                    }
                }

                processKeys:
                while (true) {
                    if (!this.options.attackKey.wasPressed()) {
                        while (this.options.useKey.wasPressed()) {
                        }

                        while (true) {
                            if (this.options.pickItemKey.wasPressed()) {
                                continue;
                            }
                            break processKeys;
                        }
                    }
                }
            } else {
                while (this.options.attackKey.wasPressed()) {
                    leftClicked |= this.doAttack();
                }

                while (this.options.useKey.wasPressed()) {
                    this.doItemUse();
                }

                while (this.options.pickItemKey.wasPressed()) {
                    this.doItemPick();
                }
            }

            if (this.options.useKey.isPressed() && this.itemUseCooldown == 0 && !this.player.isUsingItem()) {
                this.doItemUse();
            }

            this.handleBlockBreaking(this.currentScreen == null && !leftClicked && this.options.attackKey.isPressed() && this.mouse.isCursorLocked());

            ci.cancel();
        }
    }

    // Interface

    @Override
    public void rightClick() {
        rightClick = true;
    }
}
