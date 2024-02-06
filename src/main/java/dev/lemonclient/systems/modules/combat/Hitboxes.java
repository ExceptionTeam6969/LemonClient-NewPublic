package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.InvUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;

import java.util.Set;

public class Hitboxes extends Module {
    public Hitboxes() {
        super(Categories.Combat, "Hitboxes", "Expands an entity's hitboxes.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Which entities to target.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Double> value = sgGeneral.add(new DoubleSetting.Builder()
        .name("expand")
        .description("How much to expand the hitbox of the entity.")
        .defaultValue(0.5)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Doesn't expand the hitboxes of friends.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyOnWeapon = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-weapon")
        .description("Only modifies hitbox when holding a weapon in hand.")
        .defaultValue(false)
        .build()
    );

    public double getEntityValue(Entity entity) {
        if (!(isActive() && testWeapon()) || (ignoreFriends.get() && entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity) entity)))
            return 0;
        if (entities.get().contains(entity.getType())) return value.get();
        return 0;
    }

    private boolean testWeapon() {
        if (!onlyOnWeapon.get()) return true;
        return InvUtils.testInHands(itemStack -> itemStack.getItem() instanceof SwordItem || itemStack.getItem() instanceof AxeItem);
    }
}
