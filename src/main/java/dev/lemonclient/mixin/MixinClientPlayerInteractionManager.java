package dev.lemonclient.mixin;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.DropItemsEvent;
import dev.lemonclient.events.entity.player.*;
import dev.lemonclient.events.game.ClickWindowEvent;
import dev.lemonclient.mixininterface.IClientPlayerInteractionManager;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.InvTweaks;
import dev.lemonclient.systems.modules.player.AutoMine;
import dev.lemonclient.systems.modules.player.BreakDelay;
import dev.lemonclient.systems.modules.player.Reach;
import dev.lemonclient.systems.modules.player.SpeedMine;
import dev.lemonclient.utils.player.Rotations;
import dev.lemonclient.utils.world.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static dev.lemonclient.LemonClient.mc;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager implements IClientPlayerInteractionManager {
    @Shadow
    protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    @Shadow
    public abstract boolean breakBlock(BlockPos pos);

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private float blockBreakingSoundCooldown;
    @Shadow
    private float currentBreakingProgress;
    @Shadow
    private ItemStack selectedStack;
    @Shadow
    private BlockPos currentBreakingPos;
    @Shadow
    private boolean breakingBlock;

    @Shadow
    public abstract int getBlockBreakingProgress();

    @Shadow
    private int blockBreakingCooldown;

    @Shadow
    protected abstract void syncSelectedSlot();

    @Shadow
    public abstract void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player);

    @Shadow
    @Final
    private ClientPlayNetworkHandler networkHandler;

    private BlockPos position = null;

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo info) {
        if (actionType == SlotActionType.THROW && slotId >= 0 && slotId < player.currentScreenHandler.slots.size()) {
            if (LemonClient.EVENT_BUS.post(DropItemsEvent.get(player.currentScreenHandler.slots.get(slotId).getStack())).isCancelled())
                info.cancel();
        } else if (slotId == -999) {
            // Clicking outside of inventory
            if (LemonClient.EVENT_BUS.post(DropItemsEvent.get(player.currentScreenHandler.getCursorStack())).isCancelled())
                info.cancel();
        }
    }

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    public void onClickArmorSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!Modules.get().get(InvTweaks.class).armorStorage()) return;

        ScreenHandler screenHandler = player.currentScreenHandler;

        if (screenHandler instanceof PlayerScreenHandler) {
            if (slotId >= 5 && slotId <= 8) {
                int armorSlot = (8 - slotId) + 36;
                if (actionType == SlotActionType.PICKUP && !screenHandler.getCursorStack().isEmpty()) {
                    clickSlot(syncId, 17, armorSlot, SlotActionType.SWAP, player); //armor slot <-> inv slot
                    clickSlot(syncId, 17, button, SlotActionType.PICKUP, player); //inv slot <-> cursor slot
                    clickSlot(syncId, 17, armorSlot, SlotActionType.SWAP, player); //armor slot <-> inv slot
                    ci.cancel();
                } else if (actionType == SlotActionType.SWAP) {
                    if (button >= 10) {
                        clickSlot(syncId, 45, armorSlot, SlotActionType.SWAP, player);
                        ci.cancel();
                    } else {
                        clickSlot(syncId, 36 + button, armorSlot, SlotActionType.SWAP, player); //invert swap
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (LemonClient.EVENT_BUS.post(StartBreakingBlockEvent.get(blockPos, direction)).isCancelled()) info.cancel();
        else {
            SpeedMine sm = Modules.get().get(SpeedMine.class);
            BlockState state = mc.world.getBlockState(blockPos);

            if (!sm.instamine() || !sm.filter(state.getBlock())) return;

            if (state.calcBlockBreakingDelta(mc.player, mc.world, blockPos) > 0.5f) {
                mc.world.breakBlock(blockPos, true, mc.player);
                networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
                networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
                info.setReturnValue(true);
            }
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (LemonClient.EVENT_BUS.post(InteractBlockEvent.get(player.getMainHandStack().isEmpty() ? Hand.OFF_HAND : hand, hitResult)).isCancelled())
            cir.setReturnValue(ActionResult.FAIL);
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo info) {
        if (LemonClient.EVENT_BUS.post(AttackEntityEvent.get(target)).isCancelled()) info.cancel();
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        if (LemonClient.EVENT_BUS.post(InteractEntityEvent.get(entity, hand)).isCancelled())
            info.setReturnValue(ActionResult.FAIL);
    }

    @Inject(method = "dropCreativeStack", at = @At("HEAD"), cancellable = true)
    private void onDropCreativeStack(ItemStack stack, CallbackInfo info) {
        if (LemonClient.EVENT_BUS.post(DropItemsEvent.get(stack)).isCancelled()) info.cancel();
    }

    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> info) {
        info.setReturnValue(Modules.get().get(Reach.class).blockReach());
    }

    @Inject(method = "hasExtendedReach", at = @At("HEAD"), cancellable = true)
    private void onHasExtendedReach(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().isActive(Reach.class)) info.setReturnValue(false);
    }

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.PUTFIELD, ordinal = 1))
    private void creativeBreakDelayChange(ClientPlayerInteractionManager interactionManager, int value) {
        BlockBreakingCooldownEvent event = LemonClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        blockBreakingCooldown = event.cooldown;
    }

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void survivalBreakDelayChange(ClientPlayerInteractionManager interactionManager, int value) {
        BlockBreakingCooldownEvent event = LemonClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        blockBreakingCooldown = event.cooldown;
    }

    @Redirect(method = "attackBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.PUTFIELD))
    private void creativeBreakDelayChange2(ClientPlayerInteractionManager interactionManager, int value) {
        BlockBreakingCooldownEvent event = LemonClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        blockBreakingCooldown = event.cooldown;
    }

    @Redirect(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
    private float deltaChange(BlockState blockState, PlayerEntity player, BlockView world, BlockPos pos) {
        float delta = blockState.calcBlockBreakingDelta(player, world, pos);
        if (Modules.get().get(BreakDelay.class).noInstaBreak.get() && delta >= 1) {
            BlockBreakingCooldownEvent event = LemonClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(blockBreakingCooldown));
            blockBreakingCooldown = event.cooldown;
            return 0;
        }
        return delta;
    }

    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private void onBreakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
        final BreakBlockEvent event = BreakBlockEvent.get(blockPos);
        LemonClient.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            info.setReturnValue(false);
            info.cancel();
        }
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void onInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        InteractItemEvent event = LemonClient.EVENT_BUS.post(InteractItemEvent.get(hand));
        if (event.toReturn != null) info.setReturnValue(event.toReturn);
    }

    @Inject(method = "cancelBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void onCancelBlockBreaking(CallbackInfo info) {
        if (BlockUtils.breaking) info.cancel();
    }

    @ModifyArgs(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$Full;<init>(DDDFFZ)V"))
    private void onInteractItem(Args args) {
        if (Rotations.rotating) {
            args.set(3, Rotations.serverYaw);
            args.set(4, Rotations.serverPitch);
        }
    }

    @Override
    public void syncSelected() {
        syncSelectedSlot();
    }

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void windowClick(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo callbackInfo) {
        ClickWindowEvent event = ClickWindowEvent.get(syncId, slotId, button, actionType);
        LemonClient.EVENT_BUS.post(event);

        if (event.isCancelled()) callbackInfo.cancel();
    }

    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void onAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        position = pos;
    }

    @Redirect(method = "attackBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V", ordinal = 1))
    private void onStart(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) {
            sendSequencedPacket(world, packetCreator);
            return;
        }

        BlockState blockState = world.getBlockState(position);
        boolean bl = !blockState.isAir();
        if (bl && this.currentBreakingProgress == 0.0F) {
            blockState.onBlockBreakStart(this.client.world, position, this.client.player);
        }

        if (bl && blockState.calcBlockBreakingDelta(this.client.player, this.client.player.getWorld(), position) >= 1.0F) {
            this.breakBlock(position);
        } else {
            breakingBlock = true;
            currentBreakingPos = position;
            selectedStack = this.client.player.getMainHandStack();
            currentBreakingProgress = 0.0F;
            blockBreakingSoundCooldown = 0.0F;
            client.world.setBlockBreakingInfo(this.client.player.getId(), this.currentBreakingPos, getBlockBreakingProgress());
        }

        autoMine.onStart(position);
    }

    @Redirect(method = "attackBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
    private void onAbort(ClientPlayNetworkHandler instance, Packet<?> packet) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) {
            instance.sendPacket(packet);
            return;
        }

        autoMine.onAbort(position);
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void onUpdateProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        position = pos;
    }

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V", ordinal = 1))
    private void onStop(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) {
            sendSequencedPacket(world, packetCreator);
            return;
        }

        autoMine.onStop();
    }

    @Redirect(method = "cancelBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void cancel(ClientPlayNetworkHandler instance, Packet<?> packet) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) {
            instance.sendPacket(packet);
            return;
        }

        autoMine.onAbort(currentBreakingPos);
    }
}
