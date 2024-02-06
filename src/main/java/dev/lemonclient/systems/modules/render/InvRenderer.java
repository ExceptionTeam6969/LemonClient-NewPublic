package dev.lemonclient.systems.modules.render;

import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class InvRenderer extends Module {
    public InvRenderer() {
        super(Categories.Render, "Inv Renderer", ".");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> lineMeteor = sgGeneral.add(new BoolSetting.Builder()
        .name("Meteor")
        .description("Render some meteor on gui.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> meteorRainbow = sgGeneral.add(new BoolSetting.Builder()
        .name("Meteor Rainbow")
        .description("Enable rainbow background meteor color.")
        .defaultValue(false)
        .visible(lineMeteor::get)
        .build()
    );
    public final Setting<Boolean> snowParticles = sgGeneral.add(new BoolSetting.Builder()
        .name("Snow Particles")
        .description("Render some snow particles on gui.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Integer> spawnDelays = sgGeneral.add(new IntSetting.Builder()
        .name("Snow Spawn Delay")
        .description("If passed, renderer will spawn new snow particle.")
        .defaultValue(2100)
        .sliderRange(500, 10000)
        .visible(snowParticles::get)
        .build()
    );
    public final Setting<Boolean> particles = sgGeneral.add(new BoolSetting.Builder()
        .name("Particles")
        .description("Render some other particles on gui.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> bothParticles = sgGeneral.add(new BoolSetting.Builder()
        .name("Both Particles")
        .description("Render 2r particles on gui.")
        .defaultValue(true)
        .visible(particles::get)
        .build()
    );
}
