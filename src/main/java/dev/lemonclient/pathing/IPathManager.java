package dev.lemonclient.pathing;

import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.Settings;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public interface IPathManager {
    String getName();

    boolean isPathing();

    void pause();

    void resume();

    void stop();

    default void moveTo(BlockPos pos) {
        moveTo(pos, false);
    }

    void moveTo(BlockPos pos, boolean ignoreY);

    void moveInDirection(float yaw);

    void mine(Block... blocks);

    void follow(Predicate<Entity> entity);

    float getTargetYaw();

    float getTargetPitch();

    ISettings getSettings();

    interface ISettings {
        Settings get();

        Setting<Boolean> getWalkOnWater();

        Setting<Boolean> getWalkOnLava();

        Setting<Boolean> getStep();

        Setting<Boolean> getNoFall();

        void save();
    }
}
