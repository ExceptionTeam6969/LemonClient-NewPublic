package dev.lemonclient.mixininterface;

import net.minecraft.item.ItemStack;

// Using accessor causes a stackoverflow for some fucking reason
public interface IAbstractFurnaceScreenHandler {
    boolean isItemSmeltable(ItemStack itemStack);
}
