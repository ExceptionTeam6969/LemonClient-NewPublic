package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.VillagerRoller;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class MixinVillagerEntity {
    @Inject(at = @At("HEAD"), method = "interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", cancellable = true)
    public void interactMob(CallbackInfoReturnable<ActionResult> cir) {
        VillagerRoller roller = Modules.get().get(VillagerRoller.class);
        if (roller.currentState == VillagerRoller.State.WaitingForTargetVillager) {
            roller.currentState = VillagerRoller.State.RollingBreakingBlock;
            roller.rollingVillager = (VillagerEntity) (Object) this;
            roller.info("We got your villager");
            cir.setReturnValue(ActionResult.CONSUME);
            cir.cancel();
        }
//        if(cir.isCancelled()) {
//            roller.info("Canceled on interact mixin");
//        }
    }
}
