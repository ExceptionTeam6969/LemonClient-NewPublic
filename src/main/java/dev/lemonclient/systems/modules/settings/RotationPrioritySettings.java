package dev.lemonclient.systems.modules.settings;

import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class RotationPrioritySettings extends Module {
    public RotationPrioritySettings() {
        super(Categories.Settings, "Rotation Priority", "Global rotation priority settings for every lemon module.");
    }

    private final SettingGroup sgCombat = settings.createGroup("Combat");
    private final SettingGroup sgPlayer = settings.createGroup("Player");

    //  Combat
    public final Setting<Integer> autoAnchor = sgCombat.add(new IntSetting.Builder()
        .name("Auto Anchor")
        .description(".")
        .defaultValue(9)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> autoCrystal = sgCombat.add(new IntSetting.Builder()
        .name("Auto Crystal")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> autoCrystalPlus = sgCombat.add(new IntSetting.Builder()
        .name("Auto Crystal+")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> autoHoleFill = sgCombat.add(new IntSetting.Builder()
        .name("Auto Hole Fill")
        .description(".")
        .defaultValue(7)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> autoHoleFillPlus = sgCombat.add(new IntSetting.Builder()
        .name("Auto Hole Fill+")
        .description(".")
        .defaultValue(7)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> autoPearlClip = sgCombat.add(new IntSetting.Builder()
        .name("Auto Pearl Clip")
        .description(".")
        .defaultValue(6)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> autoTrap = sgCombat.add(new IntSetting.Builder()
        .name("Auto Trap")
        .description(".")
        .defaultValue(5)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> autoWeb = sgCombat.add(new IntSetting.Builder()
        .name("Auto Web")
        .description(".")
        .defaultValue(8)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> bedBombV4 = sgCombat.add(new IntSetting.Builder()
        .name("Bed Bomb V4")
        .description(".")
        .defaultValue(8)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> cevBreaker = sgCombat.add(new IntSetting.Builder()
        .name("Cev Breaker")
        .description(".")
        .defaultValue(12)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> killAura = sgCombat.add(new IntSetting.Builder()
        .name("Kill Aura")
        .description(".")
        .defaultValue(11)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> pistonCrystal = sgCombat.add(new IntSetting.Builder()
        .name("Piston Crystal")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> selfWeb = sgCombat.add(new IntSetting.Builder()
        .name("Self Web")
        .description(".")
        .defaultValue(3)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> scaffold = sgCombat.add(new IntSetting.Builder()
        .name("Scaffold")
        .description(".")
        .defaultValue(2)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> selfTrap = sgCombat.add(new IntSetting.Builder()
        .name("Self Trap")
        .description(".")
        .defaultValue(1)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> surroundPlus = sgCombat.add(new IntSetting.Builder()
        .name("Surround+")
        .description(".")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );

    //  Player
    public final Setting<Integer> antiAim = sgPlayer.add(new IntSetting.Builder()
        .name("Anti Aim")
        .description(".")
        .defaultValue(12)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> antiAFK = sgPlayer.add(new IntSetting.Builder()
        .name("Anti AFK")
        .description(".")
        .defaultValue(15)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    public final Setting<Integer> autoMine = sgPlayer.add(new IntSetting.Builder()
        .name("Auto Mine")
        .description(".")
        .defaultValue(9)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
}
