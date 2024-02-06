package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.render.GetFovEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class CustomFOV extends Module {
    public CustomFOV() {
        super(Categories.Render, "Custom FOV", "Allows more customisation to the FOV.");
    }

    private final SettingGroup sgGame = settings.createGroup("Game Fov");
    private final SettingGroup sgItem = settings.createGroup("Item Fov");

    //--------------------Game--------------------//
    public final Setting<Integer> FOV = sgGame.add(new IntSetting.Builder()
        .name("FOV")
        .description("What the FOV should be.")
        .defaultValue(120)
        .range(0, 358)
        .sliderRange(0, 358)
        .build()
    );

    //--------------------Item--------------------//
    public final Setting<Boolean> modifyItemFov = sgItem.add(new BoolSetting.Builder()
        .name("Modify Item Fov")
        .description("Change item fov for hand rendering.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Integer> itemFov = sgItem.add(new IntSetting.Builder()
        .name("Item FOV")
        .description("Item fov value for hand rendering.")
        .defaultValue(120)
        .range(0, 358)
        .sliderRange(0, 358)
        .visible(modifyItemFov::get)
        .build()
    );

    @EventHandler
    private void onFov(GetFovEvent event) {
        event.fov = FOV.get();
    }
}
