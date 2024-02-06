package dev.lemonclient.utils.path;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.lemonclient.LemonClient.mc;
import static dev.lemonclient.utils.world.BlockInfo.getBlock;
import static dev.lemonclient.utils.world.BlockInfo.getBlockState;

public class CustomPathFinder {
    private final Vec3d startVec3;
    private final Vec3d endVec3;
    private CopyOnWriteArrayList<Vec3d> path = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Hub> hubs = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Hub> hubsToWork = new CopyOnWriteArrayList<>();

    private static final Vec3d[] flatCardinalDirections = new Vec3d[]{
        new Vec3d(1.0, 0.0, 0.0),
        new Vec3d(-1.0, 0.0, 0.0),
        new Vec3d(0.0, 0.0, 1.0),
        new Vec3d(0.0, 0.0, -1.0)
    };

    public Vec3d floor(Vec3d v) {
        return new Vec3d(Math.floor(v.x), Math.floor(v.y), Math.floor(v.z));
    }

    public CustomPathFinder(Vec3d startVec3, Vec3d endVec3) {
        this.startVec3 = floor0(addVector(startVec3, 0.0, 0.0, 0.0));
        this.endVec3 = floor0(addVector(endVec3, 0.0, 0.0, 0.0));
    }

    public CopyOnWriteArrayList<Vec3d> getPath() {
        return this.path;
    }

    public void compute() {
        this.compute(1000, 4);
    }

    public void compute(int loops, int depth) {
        this.path.clear();
        this.hubsToWork.clear();
        CopyOnWriteArrayList<Vec3d> initPath = new CopyOnWriteArrayList<>();
        initPath.add(this.startVec3);
        this.hubsToWork.add(new Hub(this.startVec3, null, initPath, squareDistanceTo(this.startVec3, this.endVec3), 0.0, 0.0));
        int i = 0;
        loop:
        while (i < loops) {
            this.hubsToWork.sort(new CompareHub());
            int j = 0;
            if (this.hubsToWork.size() == 0) break;
            for (Hub hub : new CopyOnWriteArrayList<>(this.hubsToWork)) {
                Vec3d loc2;
                if (++j > depth) break;
                this.hubsToWork.remove(hub);
                this.hubs.add(hub);
                int n = flatCardinalDirections.length;
                int n2 = 0;
                while (n2 < n) {
                    Vec3d direction = flatCardinalDirections[n2];
                    Vec3d loc = floor0(hub.getLoc().add(direction));
                    if (checkPositionValidity(loc) && this.addHub(hub, loc, 0.0)) break loop;
                    ++n2;
                }
                Vec3d loc1 = floor0(addVector(hub.getLoc(), 0.0, 1.0, 0.0));
                if (checkPositionValidity(loc1) && this.addHub(hub, loc1, 0.0) || checkPositionValidity(loc2 = floor0(addVector(hub.getLoc(), 0.0, -1.0, 0.0))) && this.addHub(hub, loc2, 0.0))
                    break loop;
            }
            ++i;
        }

        this.hubs.sort(new CompareHub());
        this.path = this.hubs.get(0).getPath();
    }

    public static boolean checkPositionValidity(Vec3d loc) {
        return checkPositionValidity((int) loc.getX(), (int) loc.getY(), (int) loc.getZ());
    }

    public static boolean checkPositionValidity(BlockPos loc) {
        return checkPositionValidity(loc.getX(), loc.getY(), loc.getZ());
    }

    public static boolean checkPositionValidity(int x, int y, int z) {
        BlockPos block1 = new BlockPos(x, y, z);
        BlockPos block2 = new BlockPos(x, y + 1, z);
        BlockPos block3 = new BlockPos(x, y - 1, z);
        return
            !CustomPathFinder.isBlockSolid(block1) &&
                !CustomPathFinder.isBlockSolid(block2) &&
                CustomPathFinder.isSafeToWalkOn(block3);
    }

    public static boolean canPassThrow(BlockPos pos) {
        Block block = getBlock(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
        return block instanceof AirBlock || block instanceof PlantBlock || block instanceof VineBlock || block instanceof LadderBlock || block instanceof FluidBlock || block instanceof SignBlock;
    }

    public static boolean isBlockSolid(BlockPos block) {
        BlockState blockState = getBlockState(block);
        Block b = getBlock(block);

        return (
            ((blockState.isSolidBlock(mc.world, block) && blockState.isFullCube(mc.world, block))) || b instanceof SlabBlock || b instanceof StairsBlock || b instanceof CactusBlock || b instanceof ChestBlock || b instanceof EnderChestBlock || b instanceof SkullBlock || b instanceof PaneBlock ||
                b instanceof FenceBlock || b instanceof WallBlock || b instanceof GlassBlock || b instanceof PistonBlock || b instanceof PistonExtensionBlock || b instanceof PistonHeadBlock || b instanceof StainedGlassBlock ||
                b instanceof TrapdoorBlock ||
                // 1.14+
                b instanceof BambooBlock || b instanceof BellBlock ||
                b instanceof CakeBlock || b instanceof RedstoneBlock ||
                b instanceof LeavesBlock
        );
    }

    private static boolean isSafeToWalkOn(BlockPos block) {
        Block b = getBlock(block);
        return (
            !(b instanceof FenceBlock) && !(b instanceof WallBlock));
    }

    public Hub isHubExisting(Vec3d loc) {
        for (Hub hub : this.hubs) {
            if (hub.getLoc().getX() != loc.getX() || hub.getLoc().getY() != loc.getY() || hub.getLoc().getZ() != loc.getZ())
                continue;
            return hub;
        }
        for (Hub hub : this.hubsToWork) {
            if (hub.getLoc().getX() != loc.getX() || hub.getLoc().getY() != loc.getY() || hub.getLoc().getZ() != loc.getZ())
                continue;
            return hub;
        }
        return null;
    }

    public boolean addHub(Hub parent, Vec3d loc, double cost) {
        Hub existingHub = this.isHubExisting(loc);
        double totalCost = cost;
        if (parent != null) {
            totalCost += parent.getTotalCost();
        }
        if (existingHub == null) {
            double minDistanceSquared = 9.0;
            if (loc.getX() == this.endVec3.getX() && loc.getY() == this.endVec3.getY() && loc.getZ() == this.endVec3.getZ() || squareDistanceTo(loc, this.endVec3) <= minDistanceSquared) {
                this.path.clear();
                assert parent != null;
                this.path = parent.getPath();
                this.path.add(loc);
                return true;
            }
            assert parent != null;
            CopyOnWriteArrayList<Vec3d> path = new CopyOnWriteArrayList<>(parent.getPath());
            path.add(loc);
            this.hubsToWork.add(new Hub(loc, parent, path, squareDistanceTo(loc, this.endVec3), cost, totalCost));
        } else if (existingHub.getCost() > cost) {
            assert parent != null;
            CopyOnWriteArrayList<Vec3d> path = new CopyOnWriteArrayList<>(parent.getPath());
            path.add(loc);
            existingHub.setLoc(loc);
            existingHub.setParent(parent);
            existingHub.setPath(path);
            existingHub.setSquareDistanceToFromTarget(squareDistanceTo(loc, this.endVec3));
            existingHub.setCost(cost);
            existingHub.setTotalCost(totalCost);
        }
        return false;
    }

    public static Vec3d addVector(Vec3d target, double x, double y, double z) {
        return new Vec3d(target.x + x, target.y + y, target.z + z);
    }

    public static Vec3d floor0(Vec3d vec) {
        return new Vec3d(MathHelper.floor(vec.x), MathHelper.floor(vec.y), MathHelper.floor(vec.z));
    }

    public static double squareDistanceTo(Vec3d target, Vec3d vec) {
        return Math.pow(target.x - vec.x, 2.0) + Math.pow(target.y - vec.y, 2.0) + Math.pow(target.z - vec.z, 2.0);
    }

    public static Vec3d add(Vec3d target, Vec3d v) {
        return addVector(target, v.getX(), v.getY(), v.getZ());
    }

    public static class CompareHub implements Comparator<Hub> {
        @Override
        public int compare(Hub o1, Hub o2) {
            return (int) (o1.getSquareDistanceToFromTarget() + o1.getTotalCost() - (o2.getSquareDistanceToFromTarget() + o2.getTotalCost()));
        }
    }

    private static class Hub {
        private Vec3d loc;
        private Hub parent;
        private CopyOnWriteArrayList<Vec3d> path;
        private double squareDistanceToFromTarget;
        private double cost;
        private double totalCost;

        public Hub(Vec3d loc, Hub parent, CopyOnWriteArrayList<Vec3d> path, double squareDistanceToFromTarget, double cost, double totalCost) {
            this.loc = loc;
            this.parent = parent;
            this.path = path;
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
            this.cost = cost;
            this.totalCost = totalCost;
        }

        public Vec3d getLoc() {
            return this.loc;
        }

        public Hub getParent() {
            return this.parent;
        }

        public CopyOnWriteArrayList<Vec3d> getPath() {
            return this.path;
        }

        public double getSquareDistanceToFromTarget() {
            return this.squareDistanceToFromTarget;
        }

        public double getCost() {
            return this.cost;
        }

        public void setLoc(Vec3d loc) {
            this.loc = loc;
        }

        public void setParent(Hub parent) {
            this.parent = parent;
        }

        public void setPath(CopyOnWriteArrayList<Vec3d> path) {
            this.path = path;
        }

        public void setSquareDistanceToFromTarget(double squareDistanceToFromTarget) {
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public double getTotalCost() {
            return this.totalCost;
        }

        public void setTotalCost(double totalCost) {
            this.totalCost = totalCost;
        }
    }
}
