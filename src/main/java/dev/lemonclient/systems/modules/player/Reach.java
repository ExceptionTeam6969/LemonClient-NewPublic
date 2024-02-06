package dev.lemonclient.systems.modules.player;

import dev.lemonclient.settings.DoubleSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class Reach extends Module {
    public Reach() {
        super(Categories.Player, "Reach", "Gives you super long arms.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> blockReach = sgGeneral.add(new DoubleSetting.Builder()
        .name("block-reach")
        .description("The reach modifier for blocks.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> entityReach = sgGeneral.add(new DoubleSetting.Builder()
        .name("entity-reach")
        .description("The reach modifier for entities.")
        .defaultValue(3)
        .min(0)
        .sliderMax(6)
        .build()
    );

    public float blockReach() {
        if (!isActive()) return mc.interactionManager.getCurrentGameMode().isCreative() ? 5.0F : 4.5F;
        return blockReach.get().floatValue();
    }

    public float entityReach() {
        if (!isActive()) return 3;
        return entityReach.get().floatValue();
    }
}
