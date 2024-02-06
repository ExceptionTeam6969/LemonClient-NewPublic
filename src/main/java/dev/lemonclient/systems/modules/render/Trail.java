package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.ParticleTypeListSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

import java.util.List;

public class Trail extends Module {
    public Trail() {
        super(Categories.Render, "Trail", "Renders a customizable trail behind your player.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<ParticleType<?>>> particles = sgGeneral.add(new ParticleTypeListSetting.Builder()
        .name("particles")
        .description("Particles to draw.")
        .defaultValue(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, ParticleTypes.CAMPFIRE_COSY_SMOKE)
        .build()
    );

    private final Setting<Boolean> pause = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-when-stationary")
        .description("Whether or not to add particles when you are not moving.")
        .defaultValue(true)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (pause.get()
            && mc.player.getX() == mc.player.prevX
            && mc.player.getY() == mc.player.prevY
            && mc.player.getZ() == mc.player.prevZ) return;

        for (ParticleType<?> particleType : particles.get()) {
            mc.world.addParticle((ParticleEffect) particleType, mc.player.getX(), mc.player.getY(), mc.player.getZ(), 0, 0, 0);
        }
    }
}
