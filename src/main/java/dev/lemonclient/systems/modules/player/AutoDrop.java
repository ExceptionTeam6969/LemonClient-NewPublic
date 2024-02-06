package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class AutoDrop extends Module {
    public AutoDrop() {
        super(Categories.Player, "Auto Drop", "Auto drop items in inventory.");
    }

    private final SettingGroup defaultGroup = settings.getDefaultGroup();

    private final Setting<List<Item>> items = defaultGroup.add(new ItemListSetting.Builder()
        .name("Drop Items")
        .description("Items to dropping.")
        .build()
    );
    private final Setting<Integer> delay = defaultGroup.add(new IntSetting.Builder()
        .name("Delay")
        .description("Drop delay.")
        .defaultValue(5)
        .min(0)
        .build()
    );
    private final Setting<Boolean> workInstant = defaultGroup.add(new BoolSetting.Builder()
        .name("Instant Work")
        .description("Drop or remove items instant.")
        .defaultValue(true)
        .visible(() -> delay.get() == 0)
        .build()
    );
    private final Setting<Boolean> removeContainersItems = defaultGroup.add(new BoolSetting.Builder()
        .name("Work In Containers")
        .description("Remove items in chests?.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> autoDropExcludeHotbar = defaultGroup.add(new BoolSetting.Builder()
        .name("Work In Hotbar")
        .description("Allow hotbar?.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> removeItems = defaultGroup.add(new BoolSetting.Builder()
        .name("Remover")
        .description("Remove items?.")
        .defaultValue(true)
        .build()
    );

    private int tick = 0;

    @EventHandler
    public void onTickPost(TickEvent.Pre event) {
        int sync = mc.player.currentScreenHandler.syncId;

        for (int i = autoDropExcludeHotbar.get() ? 0 : 9; i < mc.player.getInventory().size(); i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);

            if (items.get().contains(itemStack.getItem().asItem())) {
                if (tick == 0) {
                    if (removeItems.get() && sync != -1) {
                        mc.interactionManager.clickSlot(sync, invIndexToSlotId(i), 300, SlotActionType.SWAP, mc.player);
                    } else if (!removeItems.get()) {
                        InvUtils.drop().slot(i);
                    }
                    if (!workInstant.get()) {
                        tick = delay.get();
                    }
                } else {
                    tick--;
                }
                if (!workInstant.get()) {
                    break;
                }
            }
        }
        if (removeContainersItems.get()) {
            for (int i = 0; i < SlotUtils.indexToId(SlotUtils.MAIN_START); i++) {
                ScreenHandler handler = mc.player.currentScreenHandler;
                if (!handler.getSlot(i).hasStack()) continue;

                Item item = handler.getSlot(i).getStack().getItem();
                if (items.get().contains(item.asItem())) {
                    if (tick == 0) {
                        if (removeItems.get()) {
                            mc.interactionManager.clickSlot(handler.syncId, getIndexToSlotId(handler, i), 300, SlotActionType.SWAP, mc.player);
                        } else {
                            InvUtils.drop().slot(i);
                        }
                        if (!workInstant.get()) {
                            tick = delay.get();
                        }
                    } else {
                        tick--;
                    }
                    if (!workInstant.get()) {
                        break;
                    }
                }
            }
        }
    }

    public static int invIndexToSlotId(int invIndex) {
        return invIndex < 9 && invIndex != -1 ? 44 - (8 - invIndex) : invIndex;
    }

    public static int getIndexToSlotId(ScreenHandler handler, int invIndex) {
        if (handler instanceof GenericContainerScreenHandler genericContainerScreenHandler) {
            int count = genericContainerScreenHandler.slots.size();
            return invIndex < 0 && invIndex != -1 ? count - (-1 - invIndex) : invIndex;
        } else if (handler instanceof ShulkerBoxScreenHandler genericContainerScreenHandler) {
            int count = genericContainerScreenHandler.slots.size();
            return invIndex < 0 && invIndex != -1 ? count - (-1 - invIndex) : invIndex;
        }
        return invIndex < 9 && invIndex != -1 ? 44 - (8 - invIndex) : invIndex;
    }
}
