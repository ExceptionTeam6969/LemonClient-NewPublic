package dev.lemonclient.utils.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record PlaceData(BlockPos pos, Direction dir, boolean valid) {
}
