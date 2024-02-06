package dev.lemonclient.mixin;

import dev.lemonclient.mixininterface.IAbstractFurnaceScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class MixinAbstractFurnaceScreenHandler implements IAbstractFurnaceScreenHandler {
    @Shadow
    protected abstract boolean isSmeltable(ItemStack itemStack);

    @Override
    public boolean isItemSmeltable(ItemStack itemStack) {
        return isSmeltable(itemStack);
    }
}
