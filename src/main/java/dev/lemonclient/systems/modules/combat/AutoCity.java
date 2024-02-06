package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BlockListSetting;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.player.AutoMine;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.timers.TimerUtils;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.CityUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.List;

public class AutoCity extends Module {
    public AutoCity() {
        super(Categories.Combat, "Auto City", "Automatically mine the target's protected blocks (with AutoMine).");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMineList = settings.createGroup("Mine List");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = boolSetting(sgGeneral, "Pause On Eat", "Pause while eating.", true);
    private final Setting<Integer> delay = intSetting(sgGeneral, "Delay", 35, 0, 2000);
    private final Setting<Boolean> mineHead = boolSetting(sgGeneral, "Mine Head", false);
    private final Setting<Boolean> mineBurrow = boolSetting(sgGeneral, "Mine Burrow", true);
    private final Setting<Boolean> mineSurround = boolSetting(sgGeneral, "Mine Surround", true);

    //--------------------Mine List--------------------//
    private final Setting<ListMode> listMode = sgMineList.add(new EnumSetting.Builder<ListMode>()
        .name("List Mode")
        .description("Selection mode.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );
    private final Setting<List<Block>> blacklist = sgMineList.add(new BlockListSetting.Builder()
        .name("Blacklist")
        .description("The blocks you don't want to mine.")
        .defaultValue(
            Blocks.RESPAWN_ANCHOR,
            Blocks.COBWEB
        )
        .visible(() -> listMode.get() == ListMode.Blacklist)
        .build()
    );
    private final Setting<List<Block>> whitelist = sgMineList.add(new BlockListSetting.Builder()
        .name("Whitelist")
        .description("The blocks you want to mine.")
        .visible(() -> listMode.get() == ListMode.Whitelist)
        .build()
    );

    private PlayerEntity target;
    private AutoMine autoMine = null;
    private final TimerUtils timer = new TimerUtils();

    public enum ListMode {
        Whitelist,
        Blacklist
    }

    @Override
    public void onActivate() {
        autoMine = Modules.get().get(AutoMine.class);
        timer.reset();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.Post event) {
        if (pauseEat.get() && mc.player.isUsingItem()) return;

        if (timer.passedMs(delay.get())) {
            updateTargets();
            if (target == null || !InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof PickaxeItem).found())
                return;

            updateAutoCity();
        }
    }

    @Override
    public String getInfoString() {
        return target != null ? target.getGameProfile().getName() : null;
    }

    private void updateTargets() {
        double closestDist = 1000;
        PlayerEntity closest;
        double dist;
        for (int i = 3; i > 0; i--) {
            closest = null;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (closest == player || Friends.get().isFriend(player) || player == mc.player) {
                    continue;
                }

                dist = player.distanceTo(mc.player);

                if (dist > 15) {
                    continue;
                }

                if (closest == null || dist < closestDist) {
                    closestDist = dist;
                    closest = player;
                }
            }
            if (closest != null) {
                target = closest;
            }
        }
    }

    private void updateAutoCity() {
        BlockPos feet = target.getBlockPos();

        if (mineBurrow.get()) {
            double[][] offsets = {{0.3, 0}, {-0.3, 0}, {0, 0.3}, {0, -0.3}, {0.3, 0.3}, {-0.3, -0.3}};
            for (double[] offset : offsets) {
                BlockPos burBlock = BlockPos.ofFloored(target.getPos().add(offset[0], 0, offset[1]));
                if (canMine(burBlock)) {
                    mine(burBlock);
                    return;
                }
            }
        }

        if (mineHead.get()) {
            for (int i = 2; i <= 3; i++) {
                BlockPos upPos = feet.up(i);
                if (canMine(upPos)) {
                    mine(upPos);
                    return;
                }
            }
        }

        if (mineSurround.get()) {
            if (CityUtils.getCityList(target) == null) return;

            for (BlockPos cityBlock : CityUtils.getCityList(target)) {
                if (canMine(cityBlock)) {
                    mine(cityBlock);
                    return;
                }
            }
        }
    }

    private void mine(BlockPos blockPos) {
        Direction dir = SettingUtils.getPlaceOnDirection(blockPos);
        if (dir == null) return;

        autoMine.onStart(blockPos);

        timer.reset();
    }

    private boolean canMine(BlockPos blockPos) {
        // Valid Player Check
        if (notValidPlayer(blockPos)) {
            return false;
        }

        // Place On Direction Check
        Direction dir = SettingUtils.getPlaceOnDirection(blockPos);
        if (dir == null) {
            return false;
        }

        // Valid MinePos Check
        if (autoMine.isActive() && (autoMine.breakPos != null || (autoMine.targetPos() != null && blockPos.equals(autoMine.targetPos())))) {
            return false;
        }

        // Mine Range Check
        if (!SettingUtils.inMineRange(blockPos)) {
            return false;
        }

        // Mineable Check
        if (!BlockUtils.canBreak(blockPos) || !isMineable(getBlock(blockPos))) {
            return false;
        }

        return true;
    }

    private boolean notValidPlayer(BlockPos blockPos) {
        for (PlayerEntity entity : mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(blockPos))) {
            if (entity != mc.player || !Friends.get().isFriend(entity)) continue;
            return true;
        }
        return false;
    }

    private boolean isMineable(Block block) {
        return listMode.get() == ListMode.Whitelist ? whitelist.get().contains(block) : !blacklist.get().contains(block);
    }

    private Block getBlock(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos).getBlock();
    }
}
