package dev.lemonclient.systems.modules.render;

import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;

public class ModelRender extends Module {
    public ModelRender() {
        super(Categories.Render, "Model Render", "Change the rendering model of entities.");
    }

    private final SettingGroup sgCrystal = settings.createGroup("Crystal");

    public final Setting<Boolean> rubicsCubeCrystal = boolSetting(sgCrystal, "Rubicscube Crystal", true);

    public boolean rubicsCubeCrystal() {
        return Modules.get().isActive(ModelRender.class) && Modules.get().get(ModelRender.class).rubicsCubeCrystal.get();
    }
}
