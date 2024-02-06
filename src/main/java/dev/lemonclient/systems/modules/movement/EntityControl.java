package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IClientPlayerEntity;
import dev.lemonclient.mixininterface.IHorseBaseEntity;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;

public class EntityControl extends Module {
    public EntityControl() {
        super(Categories.Movement, "Entity Control", "Lets you control rideable entities without a saddle.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> maxJump = sgGeneral.add(new BoolSetting.Builder()
        .name("Max Jump")
        .description("Sets jump power to maximum.")
        .defaultValue(true)
        .build()
    );

    @Override
    public void onDeactivate() {
        if (!Utils.canUpdate() || mc.world.getEntities() == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof AbstractHorseEntity) ((IHorseBaseEntity) entity).setSaddled(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof AbstractHorseEntity) ((IHorseBaseEntity) entity).setSaddled(true);
        }

        if (maxJump.get()) ((IClientPlayerEntity) mc.player).setMountJumpStrength(1);
    }
}
