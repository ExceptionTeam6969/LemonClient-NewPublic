package dev.lemonclient.systems.modules.world;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AutoBreed extends Module {
    public AutoBreed() {
        super(Categories.World, "Auto Breed", "Automatically breeds specified animals.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to breed.")
        .defaultValue(EntityType.HORSE, EntityType.DONKEY, EntityType.COW,
            EntityType.MOOSHROOM, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.WOLF,
            EntityType.CAT, EntityType.OCELOT, EntityType.RABBIT, EntityType.LLAMA, EntityType.TURTLE,
            EntityType.PANDA, EntityType.FOX, EntityType.BEE, EntityType.STRIDER, EntityType.HOGLIN)
        .onlyAttackable()
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("How far away the animals can be to be bred.")
        .min(0)
        .defaultValue(4.5)
        .build()
    );

    private final Setting<Hand> hand = sgGeneral.add(new EnumSetting.Builder<Hand>()
        .name("hand-for-breeding")
        .description("The hand to use for breeding.")
        .defaultValue(Hand.MAIN_HAND)
        .build()
    );

    private final Setting<Boolean> ignoreBabies = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-babies")
        .description("Whether or not to ignore the baby variants of the specified entity.")
        .defaultValue(true)
        .build()
    );

    private final List<Entity> animalsFed = new ArrayList<>();

    @Override
    public void onActivate() {
        animalsFed.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Entity entity : mc.world.getEntities()) {
            AnimalEntity animal;

            if (!(entity instanceof AnimalEntity)) continue;
            else animal = (AnimalEntity) entity;

            if (!entities.get().contains(animal.getType())
                || (animal.isBaby() && !ignoreBabies.get())
                || animalsFed.contains(animal)
                || !PlayerUtils.isWithin(animal, range.get())
                || !animal.isBreedingItem(hand.get() == Hand.MAIN_HAND ? mc.player.getMainHandStack() : mc.player.getOffHandStack()))
                continue;

            Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, () -> {
                mc.interactionManager.interactEntity(mc.player, animal, hand.get());
                mc.player.swingHand(hand.get());
                animalsFed.add(animal);
            });

            return;
        }
    }
}
