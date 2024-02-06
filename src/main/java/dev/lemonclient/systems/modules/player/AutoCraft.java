package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.ItemListSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class AutoCraft extends Module {
    public AutoCraft() {
        super(Categories.Player, "Auto Craft", "Automatically crafts items.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //--------------------General--------------------//
    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("Items")
        .description("Items you want to get crafted.")
        .defaultValue(List.of())
        .build()
    );

    private final Setting<Boolean> antiDesync = sgGeneral.add(new BoolSetting.Builder()
        .name("Anti Desync")
        .description("Try to prevent inventory desync.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> craftAll = sgGeneral.add(new BoolSetting.Builder()
        .name("Craft All")
        .description("Crafts maximum possible amount amount per craft (shift-clicking)")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> drop = sgGeneral.add(new BoolSetting.Builder()
        .name("Drop")
        .description("Automatically drops crafted items (useful for when not enough inventory space)")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.interactionManager == null) return;
        if (items.get().isEmpty()) return;

        if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler currentScreenHandler)) return;


        if (antiDesync.get()) mc.player.getInventory().updateItems();

        List<Item> itemList = items.get();
        List<RecipeResultCollection> recipeResultCollectionList = mc.player.getRecipeBook().getOrderedResults();
        for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
            for (Recipe<?> recipe : recipeResultCollection.getRecipes(true)) {
                if (!itemList.contains(recipe.getOutput(mc.world.getRegistryManager()).getItem())) continue;
                mc.interactionManager.clickRecipe(currentScreenHandler.syncId, recipe, craftAll.get());
                mc.interactionManager.clickSlot(currentScreenHandler.syncId, 0, 1,
                    drop.get() ? SlotActionType.THROW : SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }
}
