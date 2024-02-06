package dev.lemonclient.systems.modules.render;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class EntityRenders extends Module {
    public EntityRenders() {
        super(Categories.Render, "Entity Renders", "changes the way entities are rendered");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<Boolean> flipEntities = sgGeneral.add(new BoolSetting.Builder()
        .name("Flip Entities")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> matrixScale = sgGeneral.add(new BoolSetting.Builder()
        .name("Matrix Scale")
        .defaultValue(true)
        .build()
    );
    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .defaultValue(1)
        .sliderRange(0, 6.5)
        .visible(matrixScale::get)
        .build()
    );
}
