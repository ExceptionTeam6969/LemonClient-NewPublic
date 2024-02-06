package dev.lemonclient.mixin;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.TooltipDataEvent;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.OldHitting;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public class MixinItem {
    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void onTooltipData(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir) {
        TooltipDataEvent event = LemonClient.EVENT_BUS.post(TooltipDataEvent.get(stack));
        if (event.tooltipData != null) {
            cir.setReturnValue(Optional.of(event.tooltipData));
        }
    }

    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    private void onAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
        OldHitting oldHitting = Modules.get().get(OldHitting.class);
        if (((Item) (Object) this) instanceof SwordItem) {
            if (oldHitting.isActive()) {
                cir.setReturnValue(UseAction.BLOCK);
            }
        }
    }

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    private void onTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        OldHitting oldHitting = Modules.get().get(OldHitting.class);
        if (((Item) (Object) this) instanceof SwordItem) {
            if (oldHitting.isActive()) {
                cir.setReturnValue(72000);
            }
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        OldHitting oldHitting = Modules.get().get(OldHitting.class);
        if (((Item) (Object) this) instanceof SwordItem) {
            if (oldHitting.isActive()) {
                ItemStack itemStack = user.getStackInHand(hand);
                user.setCurrentHand(hand);
                cir.setReturnValue(TypedActionResult.consume(itemStack));
            }
        }
    }
}
