package dev.lemonclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.player.FinishUsingItemEvent;
import dev.lemonclient.events.entity.player.StoppedUsingItemEvent;
import dev.lemonclient.events.game.ItemStackTooltipEvent;
import dev.lemonclient.events.game.SectionVisibleEvent;
import dev.lemonclient.utils.Utils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static dev.lemonclient.LemonClient.mc;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> onGetTooltip(List<Text> original, PlayerEntity player, TooltipContext context) {
        if (Utils.canUpdate()) {
            ItemStackTooltipEvent event = LemonClient.EVENT_BUS.post(ItemStackTooltipEvent.get((ItemStack) (Object) this, original));
            return event.list;
        }

        return original;
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if (user == mc.player) {
            LemonClient.EVENT_BUS.post(FinishUsingItemEvent.get((ItemStack) (Object) this));
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        if (user == mc.player) {
            LemonClient.EVENT_BUS.post(StoppedUsingItemEvent.get((ItemStack) (Object) this));
        }
    }

    @ModifyReturnValue(method = "isSectionVisible", at = @At("RETURN"))
    private static boolean onSectionVisible(boolean original, int flags, ItemStack.TooltipSection tooltipSection) {
        SectionVisibleEvent event = LemonClient.EVENT_BUS.post(SectionVisibleEvent.get(tooltipSection, original));
        return event.visible;
    }
}
