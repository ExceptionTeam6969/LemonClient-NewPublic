package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.entity.player.MoveEvent;
import dev.lemonclient.events.entity.player.SprintEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.combat.KillAura;
import dev.lemonclient.systems.modules.movement.Scaffold;
import dev.lemonclient.systems.modules.movement.Strafe;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.ColorMode;
import dev.lemonclient.utils.render.Render3DUtils;
import dev.lemonclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import static dev.lemonclient.utils.player.PlayerUtils.isMoving;

public class TargetStrafe extends Module {
    public TargetStrafe() {
        super(Categories.Render, "Target Strafe", "TargetStrafe");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    public Setting<Boolean> jump = sgGeneral.add(new BoolSetting.Builder()
        .name("jump")
        .defaultValue(true)
        .build()
    );
    public Setting<Boolean> withSpeed = sgGeneral.add(new BoolSetting.Builder()
        .name("with-speed")
        .defaultValue(false)
        .build()
    );
    public Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .defaultValue(1.3)
        .sliderRange(0.1, 10.0)
        .min(0.1)
        .build()
    );

    private final Setting<Boost> boost = sgGeneral.add(new EnumSetting.Builder<Boost>()
        .name("boost")
        .defaultValue(Boost.None)
        .build()
    );
    public Setting<Double> setSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .defaultValue(1.3)
        .sliderRange(0.1, 10.0)
        .min(0.1)
        .build()
    );
    private final Setting<Double> velReduction = sgGeneral.add(new DoubleSetting.Builder()
        .name("reduction")
        .defaultValue(6.0)
        .sliderRange(0.1, 10.0)
        .min(0.1)
        .visible(() -> boost.get() == Boost.Damage)
        .build()
    );
    private final Setting<Double> maxVelocitySpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-velocity")
        .defaultValue(0.8)
        .sliderRange(0.1, 10.0)
        .min(0.1)
        .visible(() -> boost.get() == Boost.Damage)
        .build()
    );
    public Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .defaultValue(true)
        .build()
    );

    public final ColorMode.ColorSettings colorSettings = ColorMode.ColorSettings.create(sgRender);

    public static double oldSpeed, contextFriction, fovval;
    public static boolean needSwap, needSprintState, skip, switchDir, disabled;
    public static int noSlowTicks, jumpTicks, waterTicks;
    static long disableTime;

    public boolean serversprint;

    @Override
    public void onActivate() {
        oldSpeed = 0;
        fovval = mc.options.getFovEffectScale().getValue();
        mc.options.getFovEffectScale().setValue(0d);
        skip = true;
    }

    public boolean canStrafe() {
        if (mc.player.isSneaking()) {
            return false;
        }
        if (mc.player.isInLava()) {
            return false;
        }
        if (Modules.get().isActive(Scaffold.class)) {
            return false;
        }
        if (Modules.get().isActive(Strafe.class) && !withSpeed.get()) {
            return false;
        }
        if (mc.player.isSubmergedInWater() || waterTicks > 0) {
            return false;
        }
        return !mc.player.getAbilities().flying;
    }

    public boolean needToSwitch(double x, double z) {

        if (mc.player.horizontalCollision || ((mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed()) && jumpTicks <= 0)) {
            jumpTicks = 10;
            return true;
        }
        for (int i = (int) (mc.player.getY() + 4); i >= 0; --i) {
            BlockPos playerPos = new BlockPos((int) Math.floor(x), (int) Math.floor(i), (int) Math.floor(z));
            blockFIRE:
            {
                blockLAVA:
                {
                    if (mc.world.getBlockState(playerPos).getBlock().equals(Blocks.LAVA))
                        break blockLAVA;
                    if (!mc.world.getBlockState(playerPos).getBlock().equals(Blocks.FIRE))
                        break blockFIRE;
                }
                return true;
            }
            if (!BlockUtils.solid(playerPos))
                continue;
            return false;
        }
        return false;
    }

    @Override
    public void onDeactivate() {
        mc.options.getFovEffectScale().setValue(fovval);
    }

    private double calculateSpeed(MoveEvent move) {
        jumpTicks--;
        float speedAttributes = getAIMoveSpeed();
        final float frictionFactor = mc.world.getBlockState(new BlockPos.Mutable().set(mc.player.getX(), getBoundingBox().getMin(Direction.Axis.Y) - move.y, mc.player.getZ())).getBlock().getSlipperiness() * 0.91F;
        float n6 = mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && mc.player.isUsingItem() ? 0.88f : (float) (oldSpeed > 0.32 && mc.player.isUsingItem() ? 0.88 : 0.91F);
        if (mc.player.isOnGround()) {
            n6 = frictionFactor;
        }
        float n7 = (float) (0.1631f / Math.pow(n6, 3.0f));
        float n8;
        if (mc.player.isOnGround()) {
            n8 = speedAttributes * n7;
            if (move.y > 0) {
                n8 += boost.get() == Boost.Elytra && getElytra() != -1 && disabled ? 0.65 : 0.2f;
            }
            disabled = false;
        } else {
            n8 = 0.0255f;
        }
        boolean noslow = false;
        double max2 = oldSpeed + n8;
        double max = 0.0;
        if (mc.player.isUsingItem() && move.y <= 0) {
            double n10 = oldSpeed + n8 * 0.25;
            double motionY2 = move.y;
            if (motionY2 != 0.0 && Math.abs(motionY2) < 0.08) {
                n10 += 0.055;
            }
            if (max2 > (max = Math.max(0.043, n10))) {
                noslow = true;
                ++noSlowTicks;
            } else {
                noSlowTicks = Math.max(noSlowTicks - 1, 0);
            }
        } else {
            noSlowTicks = 0;
        }
        if (noSlowTicks > 3) {
            max2 = max - 0.019;
        } else {
            max2 = Math.max(noslow ? 0 : 0.25, max2) - (mc.player.age % 2 == 0 ? 0.001 : 0.002);
        }
        contextFriction = n6;
        if (!mc.player.isOnGround()) {
            needSprintState = !mc.player.isSprinting();
            needSwap = true;
        } else {
            needSprintState = false;
        }
        return max2;
    }

    private Box getBoundingBox() {
        return new Box(mc.player.getX() - 0.1, mc.player.getY(), mc.player.getZ() - 0.1, mc.player.getX() + 0.1, mc.player.getY() + 1, mc.player.getZ() + 0.1);
    }

    private float getAIMoveSpeed() {
        boolean prevSprinting = mc.player.isSprinting();
        mc.player.setSprinting(false);
        float speed = mc.player.getMovementSpeed() * 1.3f;
        mc.player.setSprinting(prevSprinting);
        return speed;
    }

    private void disabler(int elytra) {
        if (elytra == -1) return;
        if (System.currentTimeMillis() - disableTime > 190L) {
            if (elytra != -2) {
                mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
            }

            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

            if (elytra != -2) {
                mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
            }
            disableTime = System.currentTimeMillis();
        }
        disabled = true;
    }

    private double wrapDS(double x, double z) {
        double diffX = x - mc.player.getX();
        double diffZ = z - mc.player.getZ();
        return Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
    }

    @EventHandler
    private void onMove(MoveEvent event) {
        int elytraSlot = getElytra();

        if (boost.get() == Boost.Elytra && elytraSlot != -1) {
            if (isMoving() && !mc.player.isOnGround() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, event.y, 0.0f)).iterator().hasNext() && disabled) {
                oldSpeed = setSpeed.get();
            }
        }

        if (canStrafe()) {
            if (KillAura.curTarget != null && Modules.get().isActive(KillAura.class)) {
                double speed = calculateSpeed(event);

                double wrap = Math.atan2(mc.player.getZ() - KillAura.curTarget.getZ(), mc.player.getX() - KillAura.curTarget.getX());
                wrap += switchDir ? speed / Math.sqrt(mc.player.squaredDistanceTo(KillAura.curTarget)) : -(speed / Math.sqrt(mc.player.squaredDistanceTo(KillAura.curTarget)));

                double x = KillAura.curTarget.getX() + distance.get() * Math.cos(wrap);
                double z = KillAura.curTarget.getZ() + distance.get() * Math.sin(wrap);

                if (needToSwitch(x, z)) {
                    switchDir = !switchDir;
                    wrap += 2 * (switchDir ? speed / Math.sqrt(mc.player.squaredDistanceTo(KillAura.curTarget)) : -(speed / Math.sqrt(mc.player.squaredDistanceTo(KillAura.curTarget))));
                    x = KillAura.curTarget.getX() + distance.get() * Math.cos(wrap);
                    z = KillAura.curTarget.getZ() + distance.get() * Math.sin(wrap);
                }

                event.x = (speed * -Math.sin(Math.toRadians(wrapDS(x, z))));
                event.z = (speed * Math.cos(Math.toRadians(wrapDS(x, z))));
                event.cancel();

            }
        } else {
            oldSpeed = 0;
        }
    }

    @EventHandler
    private void updateValues(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        oldSpeed = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ) * contextFriction;

        if (mc.player.isOnGround() && jump.get() && KillAura.curTarget != null) {
            mc.player.jump();
        }

        if (mc.player.isSubmergedInWater()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
    }

    @EventHandler
    private void onUpdate(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        colorSettings.tick();

        if ((boost.get() == Boost.Elytra && getElytra() != -1 && !mc.player.isOnGround() && mc.player.fallDistance > 0 && !disabled)) {
            disabler(getElytra());
        }
    }

    @EventHandler
    private void onRender(Render3DEvent e) {
        if (render.get() && KillAura.curTarget != null) {
            Render3DUtils.drawGlowCircle(e.matrices, KillAura.curTarget.getX(), KillAura.curTarget.getY(), KillAura.curTarget.getZ(), distance.get().floatValue(), colorSettings);
        }
    }


    @EventHandler
    private void onPacketSent(PacketEvent.Sent e) {
        if (e.packet instanceof ClientCommandC2SPacket p) {
            if (p.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                serversprint = true;
            } else if (p.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
                serversprint = false;
            }
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive e) {
        if (e.packet instanceof PlayerPositionLookS2CPacket) {
            oldSpeed = 0;
        }
        EntityVelocityUpdateS2CPacket velocity;
        if (e.packet instanceof EntityVelocityUpdateS2CPacket && (velocity = (EntityVelocityUpdateS2CPacket) e.packet).getId() == mc.player.getId() && boost.get() == Boost.Damage) {
            if (mc.player.isOnGround()) return;

            int vX = velocity.getVelocityX();
            int vZ = velocity.getVelocityZ();

            if (vX < 0) vX *= -1;
            if (vZ < 0) vZ *= -1;

            oldSpeed = (vX + vZ) / (velReduction.get() * 1000f);
            oldSpeed = Math.min(oldSpeed, maxVelocitySpeed.get());

            e.cancel();
        }
    }


    @EventHandler
    public void actionEvent(SprintEvent eventAction) {
        if (canStrafe()) {
            if (serversprint != needSprintState) {
                eventAction.isSprint = (!serversprint);
            }
        }
        if (needSwap) {
            eventAction.isSprint = (!mc.player.isSprinting());
            needSwap = false;
        }
    }

    public enum Boost {
        None, Elytra, Damage
    }

    private float getFrictionFactor() {
        BlockPos.Mutable bp = new BlockPos.Mutable();
        bp.set(mc.player.prevX, mc.player.getBoundingBox().minY - 0.8D, mc.player.prevZ);
        return mc.world.getBlockState(bp).getBlock().getSlipperiness() * 0.91F;
    }

    public int getElytra() {
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.getItem() == Items.ELYTRA && stack.getDamage() < 430) {
                return -2;
            }
        }

        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.ELYTRA && s.getDamage() < 430) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }

        return slot;
    }
}
