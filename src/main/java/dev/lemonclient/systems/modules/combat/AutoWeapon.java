package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.events.entity.player.AttackEntityEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoWeapon extends Module {
    public AutoWeapon() {
        super(Categories.Combat, "Auto Weapon", "Finds the best weapon to use in your hotbar.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Weapon> weapon = sgGeneral.add(new EnumSetting.Builder<Weapon>()
        .name("weapon")
        .description("What type of weapon to use.")
        .defaultValue(Weapon.Sword)
        .build()
    );

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("threshold")
        .description("If the non-preferred weapon produces this much damage this will favor it over your preferred weapon.")
        .defaultValue(4)
        .build()
    );

    private final Setting<Boolean> antiBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-break")
        .description("Prevents you from breaking your weapon.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        InvUtils.swap(getBestWeapon(EntityUtils.getGroup(event.entity)), false);
    }

    private int getBestWeapon(EntityGroup group) {
        int slotS = mc.player.getInventory().selectedSlot;
        int slotA = mc.player.getInventory().selectedSlot;
        double damageS = 0;
        double damageA = 0;
        double currentDamageS;
        double currentDamageA;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem swordItem
                && (!antiBreak.get() || (stack.getMaxDamage() - stack.getDamage()) > 10)) {
                currentDamageS = swordItem.getMaterial().getAttackDamage() + EnchantmentHelper.getAttackDamage(stack, group) + 2;
                if (currentDamageS > damageS) {
                    damageS = currentDamageS;
                    slotS = i;
                }
            } else if (stack.getItem() instanceof AxeItem axeItem
                && (!antiBreak.get() || (stack.getMaxDamage() - stack.getDamage()) > 10)) {
                currentDamageA = axeItem.getMaterial().getAttackDamage() + EnchantmentHelper.getAttackDamage(stack, group) + 2;
                if (currentDamageA > damageA) {
                    damageA = currentDamageA;
                    slotA = i;
                }
            }
        }
        if (weapon.get() == Weapon.Sword && threshold.get() > damageA - damageS) return slotS;
        else if (weapon.get() == Weapon.Axe && threshold.get() > damageS - damageA) return slotA;
        else if (weapon.get() == Weapon.Sword && threshold.get() < damageA - damageS) return slotA;
        else if (weapon.get() == Weapon.Axe && threshold.get() < damageS - damageA) return slotS;
        else return mc.player.getInventory().selectedSlot;
    }

    public enum Weapon {
        Sword,
        Axe
    }
}
