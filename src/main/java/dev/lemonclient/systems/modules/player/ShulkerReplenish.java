package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IShulkerBoxScreenHandler;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.PistonBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class ShulkerReplenish extends Module {
    public ShulkerReplenish() {
        super(Categories.Player, "Shulker Replenish", "Auto replenish items from shulker box.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoClose = boolSetting(sgGeneral, "Auto Close", true);
    private final Setting<Boolean> smart = boolSetting(sgGeneral, "Smart", true);
    private final Setting<Integer> crystal = intSetting(sgGeneral, "Crystal", 6, 0, 20, smart::get);
    private final Setting<Integer> respawnAnchor = intSetting(sgGeneral, "Anchor", 6, 0, 20, smart::get);
    private final Setting<Integer> glowStone = intSetting(sgGeneral, "Glow Stone", 6, 0, 20, smart::get);
    private final Setting<Integer> exp = intSetting(sgGeneral, "Exp", 6, 0, 20, smart::get);
    private final Setting<Integer> totem = intSetting(sgGeneral, "Totem", 6, 0, 30, smart::get);
    private final Setting<Integer> gapple = intSetting(sgGeneral, "Gapple", 3, 0, 10, smart::get);
    private final Setting<Integer> endChest = intSetting(sgGeneral, "Ender Chest", 1, 0, 5, smart::get);
    private final Setting<Integer> cobWeb = intSetting(sgGeneral, "CobWeb", 1, 0, 5, smart::get);
    private final Setting<Integer> redStoneBlock = intSetting(sgGeneral, "RedStone Block", 1, 0, 5, smart::get);
    private final Setting<Integer> piston = intSetting(sgGeneral, "Piston", 1, 0, 5, smart::get);

    private final int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        this.stealCountList[0] = crystal.get() - this.getItemCount(Items.END_CRYSTAL);
        this.stealCountList[1] = respawnAnchor.get() - this.getItemCount(Items.RESPAWN_ANCHOR);
        this.stealCountList[2] = glowStone.get() - this.getItemCount(Items.GLOWSTONE);
        this.stealCountList[3] = exp.get() - this.getItemCount(Items.EXPERIENCE_BOTTLE);
        this.stealCountList[4] = totem.get() - this.getItemCount(Items.TOTEM_OF_UNDYING);
        this.stealCountList[5] = gapple.get() - this.getItemCount(Items.GOLDEN_APPLE);
        this.stealCountList[6] = endChest.get() - this.getItemCount(Items.ENDER_CHEST);
        this.stealCountList[7] = cobWeb.get() - this.getItemCount(Items.COBWEB);
        this.stealCountList[8] = redStoneBlock.get() - this.getItemCount(Items.REDSTONE_BLOCK);
        this.stealCountList[9] = piston.get() - InvUtils.find(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof PistonBlock).count();

        if (!(mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler handler)) return;
        Inventory inventory = ((IShulkerBoxScreenHandler) handler).getInventory();

        for (int i = 0; i < inventory.size(); ++i) {
            if (inventory.getStack(i).isEmpty()) continue;

            if (!smart.get()) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            }

            if (!needSteal(inventory.getStack(i).getItem())) continue;
            mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
        }

        if (autoClose.get() && Modules.get().get(ShulkerPlacer.class).canUpdate) {
            mc.currentScreen.close();
        }
    }

    private boolean needSteal(Item item) {
        if (item.equals(Items.END_CRYSTAL) && this.stealCountList[0] > 0) {
            int[] stealCountList = this.stealCountList;
            stealCountList[0] = stealCountList[0] - 1;
            return true;
        }
        if (item.equals(Items.RESPAWN_ANCHOR) && this.stealCountList[1] > 0) {
            int[] stealCountList = this.stealCountList;
            stealCountList[1] = stealCountList[1] - 1;
            return true;
        }
        if (item.equals(Items.GLOWSTONE) && this.stealCountList[2] > 0) {
            int[] stealCountList = this.stealCountList;
            stealCountList[2] = stealCountList[2] - 1;
            return true;
        }
        if (item.equals(Items.EXPERIENCE_BOTTLE) && this.stealCountList[3] > 0) {
            int[] stealCountList2 = this.stealCountList;
            stealCountList2[3] = stealCountList2[3] - 1;
            return true;
        }
        if (item.equals(Items.TOTEM_OF_UNDYING) && this.stealCountList[4] > 0) {
            int[] stealCountList3 = this.stealCountList;
            stealCountList3[4] = stealCountList3[4] - 1;
            return true;
        }
        if (item.equals(Items.GOLDEN_APPLE) && this.stealCountList[5] > 0) {
            int[] stealCountList4 = this.stealCountList;
            stealCountList4[5] = stealCountList4[5] - 1;
            return true;
        }
        if (item.equals(Items.ENDER_CHEST) && this.stealCountList[6] > 0) {
            int[] stealCountList5 = this.stealCountList;
            stealCountList5[6] = stealCountList5[6] - 1;
            return true;
        }
        if (item.equals(Items.COBWEB) && this.stealCountList[7] > 0) {
            int[] stealCountList6 = this.stealCountList;
            stealCountList6[7] = stealCountList6[7] - 1;
            return true;
        }
        if (item.equals(Items.REDSTONE_BLOCK) && this.stealCountList[8] > 0) {
            int[] stealCountList7 = this.stealCountList;
            stealCountList7[8] = stealCountList7[8] - 1;
            return true;
        }
        if ((item == Items.PISTON || item == Items.STICKY_PISTON) && this.stealCountList[9] > 0) {
            int[] stealCountList8 = this.stealCountList;
            stealCountList8[9] = stealCountList8[9] - 1;
            return true;
        }
        return false;
    }

    private int getItemCount(Item item) {
        return InvUtils.find(item).count();
    }
}
