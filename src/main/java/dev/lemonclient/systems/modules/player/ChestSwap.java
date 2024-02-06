package dev.lemonclient.systems.modules.player;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.ToastSystem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.util.Formatting;

public class ChestSwap extends Module {
    public ChestSwap() {
        super(Categories.Player, "Chest Swap", "Automatically swaps between a chestplate and an elytra.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> closeInventory = sgGeneral.add(new BoolSetting.Builder()
        .name("close-inventory")
        .description("Sends inventory close after swap.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Chestplate> chestplate = sgGeneral.add(new EnumSetting.Builder<Chestplate>()
        .name("chestplate")
        .description("Which type of chestplate to swap to.")
        .defaultValue(Chestplate.PreferNetherite)
        .build()
    );
    private final Setting<Boolean> stayOn = sgGeneral.add(new BoolSetting.Builder()
        .name("stay-on")
        .description("Stays on and activates when you turn it off.")
        .defaultValue(false)
        .build()
    );

    @Override
    public void onActivate() {
        swap();
        if (!stayOn.get()) toggle();
    }

    @Override
    public void onDeactivate() {
        if (stayOn.get()) swap();
    }

    public void swap() {
        Item currentItem = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem();

        if (currentItem == Items.ELYTRA) {
            equipChestplate();
        } else if (currentItem instanceof ArmorItem && ((ArmorItem) currentItem).getSlotType() == EquipmentSlot.CHEST) {
            equipElytra();
        } else {
            if (!equipChestplate()) equipElytra();
        }
    }

    private boolean equipChestplate() {
        int bestSlot = -1;
        boolean breakLoop = false;

        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            Item item = mc.player.getInventory().main.get(i).getItem();

            switch (chestplate.get()) {
                case Diamond -> {
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                }
                case Netherite -> {
                    if (item == Items.NETHERITE_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                }
                case PreferDiamond -> {
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    } else if (item == Items.NETHERITE_CHESTPLATE) {
                        bestSlot = i;
                    }
                }
                case PreferNetherite -> {
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                    } else if (item == Items.NETHERITE_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                }
            }

            if (breakLoop) break;
        }

        if (bestSlot != -1) equip(bestSlot);
        return bestSlot != -1;
    }

    private void equipElytra() {
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            Item item = mc.player.getInventory().main.get(i).getItem();

            if (item == Items.ELYTRA) {
                equip(i);
                break;
            }
        }
    }

    private void equip(int slot) {
        InvUtils.move().from(slot).toArmor(2);
        if (closeInventory.get()) {
            // Notchian clients send a Close Window packet with Window ID 0 to close their inventory even though there is never an Open Screen packet for the inventory.
            sendPacket(new CloseHandledScreenC2SPacket(0));
        }
    }

    @Override
    public void sendToggledMsg(Module module) {
        if (stayOn.get()) super.sendToggledMsg(module);
        else if (Config.get().chatFeedback.get()) info("Triggered (highlight)%s(default).", title);
    }

    @Override
    public void sendToggledToast(Module module) {
        if (stayOn.get()) super.sendToggledToast(module);
        else if (Config.get().toastFeedback.get())
            mc.getToastManager().add(new ToastSystem(module.category.icon, module.category.color, title, null, Formatting.GRAY + "Has been triggered.", Config.get().toastDuration.get()));
    }

    public enum Chestplate {
        Diamond,
        Netherite,
        PreferDiamond,
        PreferNetherite
    }
}
