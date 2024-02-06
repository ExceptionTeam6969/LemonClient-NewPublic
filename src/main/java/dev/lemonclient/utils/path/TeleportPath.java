package dev.lemonclient.utils.path;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.CopyOnWriteArrayList;

import static dev.lemonclient.LemonClient.mc;

public class TeleportPath {
    public static void teleport(Vec3d from, Vec3d to) {
        teleport(from, to, true, null);
    }

    public static void teleport(Vec3d from, Vec3d to, boolean back, Runnable task) {
        teleportTo(from, to);
        if (task != null) task.run();
        if (back) teleportTo(to, from);
    }

    public static void teleportTo(Vec3d to, boolean back, Runnable task) {
        teleport(mc.player.getPos(), to, back, task);
    }

    public static void teleportTo(Vec3d to) {
        teleport(mc.player.getPos(), to, false, null);
    }

    public static void teleportTo(Vec3d from, Vec3d to) {
        CopyOnWriteArrayList<Vec3d> path = computePath(from, to);
        for (Vec3d pathElm : path) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pathElm.getX(), pathElm.getY(), pathElm.getZ(), true));
        }
        mc.player.updatePosition(to.x, to.y, to.z);
    }

    public static CopyOnWriteArrayList<Vec3d> computePath(Vec3d topFrom, Vec3d to) {
        if (!CustomPathFinder.canPassThrow(BlockPos.ofFloored(topFrom))) {
            topFrom = CustomPathFinder.addVector(topFrom, 0, 1, 0);
        }

        CustomPathFinder pathfinder = new CustomPathFinder(topFrom, to);
        pathfinder.compute();

        int i = 0;
        Vec3d lastLoc = null;
        Vec3d lastDashLoc = null;
        CopyOnWriteArrayList<Vec3d> path = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Vec3d> pathFinderPath = pathfinder.getPath();
        for (Vec3d pathElm : pathFinderPath) {
            if (i == 0 || i == pathFinderPath.size() - 1) {
                if (lastLoc != null) {
                    path.add(CustomPathFinder.addVector(lastLoc, 0.5, 0, 0.5));
                }
                path.add(CustomPathFinder.addVector(pathElm, 0.5, 0, 0.5));
                lastDashLoc = pathElm;
            } else {
                boolean canContinue = true;
                double dashDistance = 5.0D;
                if (CustomPathFinder.squareDistanceTo(pathElm, lastDashLoc) > dashDistance * dashDistance) {
                    canContinue = false;
                } else {
                    double smallX = Math.min(lastDashLoc.getX(), pathElm.getX());
                    double smallY = Math.min(lastDashLoc.getY(), pathElm.getY());
                    double smallZ = Math.min(lastDashLoc.getZ(), pathElm.getZ());
                    double bigX = Math.max(lastDashLoc.getX(), pathElm.getX());
                    double bigY = Math.max(lastDashLoc.getY(), pathElm.getY());
                    double bigZ = Math.max(lastDashLoc.getZ(), pathElm.getZ());
                    cordsLoop:
                    for (int x = (int) smallX; x <= bigX; x++) {
                        for (int y = (int) smallY; y <= bigY; y++) {
                            for (int z = (int) smallZ; z <= bigZ; z++) {
                                if (!CustomPathFinder.checkPositionValidity(x, y, z)) {
                                    canContinue = false;
                                    break cordsLoop;
                                }
                            }
                        }
                    }
                }
                if (!canContinue) {
                    path.add(CustomPathFinder.addVector(lastLoc, 0.5, 0, 0.5));
                    lastDashLoc = lastLoc;
                }
            }
            lastLoc = pathElm;
            i++;
        }
        return path;
    }
}
