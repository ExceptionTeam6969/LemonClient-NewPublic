package dev.lemonclient.systems.modules.client;

import com.mojang.authlib.GameProfile;
import dev.lemonclient.events.entity.player.AttackEntityEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.ILivingEntity;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.combat.Criticals;
import dev.lemonclient.utils.player.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FakePlayer extends Module {
    public FakePlayer() {
        super(Categories.Client, "Fake Player", "Spawns a client-side fake player for testing usages. No need to be active.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<String> name = stringSetting(sgGeneral, "Name", "The name of the fake player.", "Fake Player");
    public final Setting<Boolean> copyInventory = boolSetting(sgGeneral, "Copy Inventory", false);
    private final Setting<Boolean> record = boolSetting(sgGeneral, "Record", false);
    private final Setting<Boolean> play = boolSetting(sgGeneral, "Play", false);
    private final Setting<Boolean> autoTotem = boolSetting(sgGeneral, "Auto Totem", true);

    private OtherClientPlayerEntity fakePlayer;
    private final List<PlayerState> positions = new ArrayList<>();
    private int movementTick, deathTime;


    @Override
    public void onActivate() {
        fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), name.get()));
        fakePlayer.copyPositionAndRotation(mc.player);

        if (copyInventory.get()) {
            fakePlayer.setStackInHand(Hand.MAIN_HAND, mc.player.getMainHandStack().copy());
            fakePlayer.setStackInHand(Hand.OFF_HAND, mc.player.getOffHandStack().copy());

            fakePlayer.getInventory().setStack(36, mc.player.getInventory().getStack(36).copy());
            fakePlayer.getInventory().setStack(37, mc.player.getInventory().getStack(37).copy());
            fakePlayer.getInventory().setStack(38, mc.player.getInventory().getStack(38).copy());
            fakePlayer.getInventory().setStack(39, mc.player.getInventory().getStack(39).copy());
        }

        mc.world.addEntity(fakePlayer.getId(), fakePlayer);
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ExplosionS2CPacket explosion && fakePlayer != null && fakePlayer.hurtTime == 0) {
            try {
                fakePlayer.onDamaged(mc.world.getDamageSources().generic());
                fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - DamageUtils.getExplosionDamage(new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()), fakePlayer));
                if (fakePlayer.isDead()) {
                    if (fakePlayer.tryUseTotem(mc.world.getDamageSources().generic())) {
                        fakePlayer.setHealth(10f);
                        new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(mc.player.networkHandler);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (record.get()) {
            positions.add(new PlayerState(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch()));
            return;
        }
        if (fakePlayer != null) {
            if (play.get() && !positions.isEmpty()) {
                movementTick++;

                if (movementTick >= positions.size()) {
                    movementTick = 0;
                    return;
                }
                PlayerState p = positions.get(movementTick);
                fakePlayer.setYaw(p.yaw);
                fakePlayer.setPitch(p.pitch);
                fakePlayer.setHeadYaw(p.yaw);

                fakePlayer.updateTrackedPosition(p.x, p.y, p.z);
                fakePlayer.updateTrackedPositionAndAngles(p.x, p.y, p.z, p.yaw, p.pitch, 3, true);
            } else movementTick = 0;

            if (autoTotem.get() && fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING)
                fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));

            if (fakePlayer.isDead()) {
                deathTime++;
                if (deathTime > 10) toggle();
            }
        }
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (fakePlayer != null && event.entity == fakePlayer && fakePlayer.hurtTime == 0) {
            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1f, 1f);

            if (mc.player.fallDistance > 0 || Modules.get().isActive(Criticals.class)) {
                mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1f, 1f);
            }

            fakePlayer.onDamaged(mc.world.getDamageSources().generic());
            if (getAttackCooldown() >= 0.85) {
                fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - getHitDamage(mc.player.getMainHandStack(), fakePlayer));
            } else {
                fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - 1f);
            }

            if (fakePlayer.isDead()) {
                if (fakePlayer.tryUseTotem(mc.world.getDamageSources().generic())) {
                    fakePlayer.setHealth(10f);
                    new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(mc.player.networkHandler);
                }
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (fakePlayer == null) return;
        fakePlayer.kill();
        fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
        fakePlayer.onRemoved();
        fakePlayer = null;
        positions.clear();
        deathTime = 0;
    }

    private float getHitDamage(ItemStack weapon, PlayerEntity ent) {
        float baseDamage = 1f;

        if (weapon.getItem() instanceof SwordItem swordItem)
            baseDamage = swordItem.getAttackDamage();

        if (weapon.getItem() instanceof AxeItem axeItem)
            baseDamage = axeItem.getAttackDamage();

        if (mc.player.fallDistance > 0 || Modules.get().isActive(Criticals.class)) {
            baseDamage += baseDamage / 2f;
        }

        baseDamage += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, weapon);

        if (mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
            int strength = Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
            baseDamage += 3 * strength;
        }

        // Reduce by armour
        baseDamage = DamageUtil.getDamageLeft(baseDamage, ent.getArmor(), (float) ent.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

        return baseDamage;
    }

    private float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * (20.0 * mc.getTickDelta()));
    }

    private float getAttackCooldown() {
        return MathHelper.clamp(((float) ((ILivingEntity) mc.player).getLastAttackedTicks() + 0.5f) / getAttackCooldownProgressPerTick(), 0.0F, 1.0F);
    }

    private record PlayerState(double x, double y, double z, float yaw, float pitch) {
    }
}
