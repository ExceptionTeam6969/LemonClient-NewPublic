package dev.lemonclient.events.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HeldItemRendererEvent {
    private static final HeldItemRendererEvent INSTANCE = new HeldItemRendererEvent();

    public Hand hand;
    public MatrixStack matrix;
    public ItemStack item;

    public static HeldItemRendererEvent get(Hand hand, MatrixStack matrices, ItemStack item) {
        INSTANCE.hand = hand;
        INSTANCE.item = item;
        INSTANCE.matrix = matrices;
        return INSTANCE;
    }
}
