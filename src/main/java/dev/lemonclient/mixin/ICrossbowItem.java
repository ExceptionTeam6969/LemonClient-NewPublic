package dev.lemonclient.mixin;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowItem.class)
public interface ICrossbowItem {
    @Invoker("getSpeed")
    static float getSpeed(ItemStack itemStack) {
        return 0;
    }
}
