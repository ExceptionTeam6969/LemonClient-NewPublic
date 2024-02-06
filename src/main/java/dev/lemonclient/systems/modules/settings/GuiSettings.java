package dev.lemonclient.systems.modules.settings;
/*
import dev.lemonclient.addon.LemonClient;
import dev.lemonclient.addon.LemonModule;
import dev.lemonclient.settings.*;
import dev.lemonclient.utils.misc.Keybind;

public class GuiSettings extends LemonModule {
    public GuiSettings() {
        super(LemonClient.Settings, "Gui", "Global gui settings for every lemon custom gui screen.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBg = settings.createGroup("Background");

    //--------------------General--------------------//
    public final Setting<Keybind> keyBind = sgGeneral.add(new KeybindSetting.Builder()
        .name("Key Bind")
        .description("The keybind to open clickgui.")
        .defaultValue(Keybind.none())
        .build()
    );

    //--------------------Bg-Settings--------------------//
    public final Setting<Boolean> lineMeteor = sgBg.add(new BoolSetting.Builder()
        .name("Line Meteor")
        .description("Render some meteor on gui.")
        .defaultValue(true)
        .visible(() -> !keyBind.get().equals(Keybind.none()))
        .build()
    );
    public final Setting<Boolean> meteorRainbow = sgBg.add(new BoolSetting.Builder()
        .name("Meteor Rainbow")
        .description("Enable rainbow background meteor color.")
        .defaultValue(false)
        .visible(() -> !keyBind.get().equals(Keybind.none()))
        .build()
    );
    public final Setting<Boolean> snowParticles = sgBg.add(new BoolSetting.Builder()
        .name("Snow Particles")
        .description("Render some snow particles on gui.")
        .defaultValue(false)
        .visible(() -> !keyBind.get().equals(Keybind.none()))
        .build()
    );
    public final Setting<Integer> spawnDelays = sgBg.add(new IntSetting.Builder()
        .name("Snow Spawn Delay")
        .description("If passed, renderer will spawn new snow particle.")
        .defaultValue(2100)
        .sliderRange(500, 10000)
        .visible(() -> !keyBind.get().equals(Keybind.none()))
        .build()
    );
    public final Setting<Boolean> particles = sgBg.add(new BoolSetting.Builder()
        .name("Particles")
        .description("Render some other particles on gui.")
        .defaultValue(false)
        .visible(() -> !keyBind.get().equals(Keybind.none()))
        .build()
    );
    public final Setting<ParticlesMode> particlesMode = sgBg.add(new EnumSetting.Builder<ParticlesMode>()
        .name("Particles Mode")
        .description("Render 2r particles on gui.")
        .defaultValue(ParticlesMode.Default)
        .visible(() -> particles.get() && !keyBind.get().equals(Keybind.none()))
        .build()
    );

    public enum ParticlesMode {
        Default,
        New,
        Both
    }
}
*/
