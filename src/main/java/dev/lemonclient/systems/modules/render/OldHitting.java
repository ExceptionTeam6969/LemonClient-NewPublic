package dev.lemonclient.systems.modules.render;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class OldHitting extends Module {
    public OldHitting() {
        super(Categories.Render, "Old Hitting", "1.8 combat!");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<RenderMode> animationMode = sgGeneral.add(new EnumSetting.Builder<RenderMode>()
        .name("Animation Mode")
        .defaultValue(RenderMode.Vanilla)
        .build()
    );
    public final Setting<Boolean> visibleOffHand = sgGeneral.add(new BoolSetting.Builder()
        .name("Visible Offhand")
        .defaultValue(false)
        .build()
    );

    @Override
    public void onActivate() {
        super.onActivate();
    }

    public enum RenderMode {
        Vanilla
    }
}
