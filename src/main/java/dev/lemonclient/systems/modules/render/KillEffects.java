package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.renderer.Renderer3D;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.sound.SoundPack;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KillEffects extends Module {
    public KillEffects() {
        super(Categories.Render, "Kill Effects", "Render some things where enemy died.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description(".")
        .defaultValue(Mode.Orthodox)
        .build()
    );
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
        .name("Y Speed")
        .description(".")
        .defaultValue(0)
        .sliderRange(-10, 10)
        .visible(() -> mode.get().equals(Mode.Orthodox))
        .build()
    );
    private final Setting<Boolean> playSound = sgGeneral.add(new BoolSetting.Builder()
        .name("Play Sound")
        .description(".")
        .defaultValue(true)
        .visible(() -> !mode.get().equals(Mode.FallingLava))
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description(".")
        .defaultValue(new SettingColor(255, 255, 0, 150))
        .visible(() -> mode.get().equals(Mode.Orthodox))
        .build()
    );

    private final Map<Entity, Long> renderEntities = new ConcurrentHashMap<>();
    private final Map<Entity, Long> lightingEntities = new ConcurrentHashMap<>();

    public enum Mode {
        Orthodox,
        FallingLava,
        LightningBolt
    }

    @EventHandler
    public void onRender(Render3DEvent.Unlimited event) {
        switch (mode.get()) {
            case Orthodox -> {
                Renderer3D renderer = new Renderer3D();
                renderer.begin();

                renderEntities.forEach((entity, time) -> {
                    if (System.currentTimeMillis() - time > 3000) {
                        renderEntities.remove(entity);
                    } else {
                        renderer.line(entity.getPos().add(0, calculateSpeed(), 0), entity.getPos().add(0, 3 + calculateSpeed(), 0), color.get());
                        renderer.line(entity.getPos().add(1, 2.3 + calculateSpeed(), 0), entity.getPos().add(-1, 2.3 + calculateSpeed(), 0), color.get());
                        renderer.line(entity.getPos().add(0.5, 1.2 + calculateSpeed(), 0), entity.getPos().add(-0.5, 0.8 + calculateSpeed(), 0), color.get());
                    }
                });

                renderer.render(event.matrices, 5.0f);
            }
            case FallingLava -> renderEntities.keySet().forEach(entity -> {
                for (int i = 0; i < entity.getHeight() * 10; i++) {
                    for (int j = 0; j < entity.getWidth() * 10; j++) {
                        for (int k = 0; k < entity.getWidth() * 10; k++) {
                            mc.world.addParticle(ParticleTypes.FALLING_LAVA, entity.getX() + j * 0.1, entity.getY() + i * 0.1, entity.getZ() + k * 0.1, 0, 0, 0);
                        }
                    }
                }

                renderEntities.remove(entity);
            });
            case LightningBolt -> renderEntities.forEach((entity, time) -> {
                LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
                lightningEntity.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
                EntitySpawnS2CPacket pac = new EntitySpawnS2CPacket(lightningEntity);
                pac.apply(mc.getNetworkHandler());

                if (playSound.get()) {
                    mc.world.playSound(mc.player, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F * 0.2F);
                    mc.world.playSound(mc.player, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F * 0.2F);
                }

                renderEntities.remove(entity);
                lightingEntities.put(entity, System.currentTimeMillis());
            });
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        mc.world.getEntities().forEach(entity -> {
            if (!(entity instanceof PlayerEntity)) return;
            if (entity == mc.player || renderEntities.containsKey(entity) || lightingEntities.containsKey(entity))
                return;
            if (entity.isAlive() || ((PlayerEntity) entity).getHealth() != 0) return;

            if (playSound.get() && mode.get() == Mode.Orthodox)
                mc.world.playSound(mc.player, entity.getBlockPos(), SoundPack.ORTHODOX_SOUNDEVENT, SoundCategory.BLOCKS, 10f, 1f);
            renderEntities.put(entity, System.currentTimeMillis());
        });

        if (!lightingEntities.isEmpty()) {
            lightingEntities.forEach((entity, time) -> {
                if (System.currentTimeMillis() - time > 5000) {
                    lightingEntities.remove(entity);
                }
            });
        }
    }

    private double calculateSpeed() {
        return speed.get() / 100D;
    }
}
