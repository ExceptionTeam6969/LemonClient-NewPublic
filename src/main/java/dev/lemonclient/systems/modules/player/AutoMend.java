package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.ItemListSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AutoMend extends Module {
    public AutoMend() {
        super(Categories.Player, "Auto Mend", "Automatically replaces items in your offhand with mending when fully repaired.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> blacklist = sgGeneral.add(new ItemListSetting.Builder()
        .name("blacklist")
        .description("Item blacklist.")
        .filter(Item::isDamageable)
        .build()
    );

    private final Setting<Boolean> force = sgGeneral.add(new BoolSetting.Builder()
        .name("force")
        .description("Replaces item in offhand even if there is some other non-repairable item.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Automatically disables when there are no more items to repair.")
        .defaultValue(true)
        .build()
    );

    private boolean didMove;

    @Override
    public void onActivate() {
        didMove = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (shouldWait()) return;

        int slot = getSlot();

        if (slot == -1) {
            if (autoDisable.get()) {
                info("Repaired all items, disabling");

                if (didMove) {
                    int emptySlot = getEmptySlot();
                    InvUtils.move().fromOffhand().to(emptySlot);
                }

                toggle();
            }
        } else {
            InvUtils.move().from(slot).toOffhand();
            didMove = true;
        }
    }

    private boolean shouldWait() {
        ItemStack itemStack = mc.player.getOffHandStack();

        if (itemStack.isEmpty()) return false;

        if (EnchantmentHelper.getLevel(Enchantments.MENDING, itemStack) > 0) {
            return itemStack.getDamage() != 0;
        }

        return !force.get();
    }

    private int getSlot() {
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (blacklist.get().contains(itemStack.getItem())) continue;

            if (EnchantmentHelper.getLevel(Enchantments.MENDING, itemStack) > 0 && itemStack.getDamage() > 0) {
                return i;
            }
        }

        return -1;
    }

    private int getEmptySlot() {
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        }

        return -1;
    }
}
