package dev.lemonclient.systems.modules.world;

import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.RayTraceUtils;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import dev.lemonclient.LemonClient;
import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.player.Rotations;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockIterator;
import dev.lemonclient.utils.world.BlockUtils;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class LitematicaPrinter extends Module {
    public LitematicaPrinter() {
        super(Categories.World, "Litematica Printer", "Automatically prints open schematics");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgWhitelist = settings.createGroup("Whitelist");
    private final SettingGroup sgRendering = settings.createGroup("Rendering");

    private final Setting<Integer> printing_range = sgGeneral.add(new IntSetting.Builder()
        .name("printing-range")
        .description("The block place range.")
        .defaultValue(2)
        .min(1).sliderMin(1)
        .max(6).sliderMax(6)
        .build()
    );

    private final Setting<Integer> printing_delay = sgGeneral.add(new IntSetting.Builder()
        .name("printing-delay")
        .description("Delay between printing blocks in ticks.")
        .defaultValue(2)
        .min(0).sliderMin(0)
        .max(100).sliderMax(40)
        .build()
    );

    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
        .name("Blocks Per Tick")
        .description("How many blocks place per tick.")
        .defaultValue(1)
        .min(1).sliderMin(1)
        .max(100).sliderMax(100)
        .build()
    );

    private final Setting<Boolean> advanced = sgGeneral.add(new BoolSetting.Builder()
        .name("advanced")
        .description("Respect block rotation (places blocks in weird places in singleplayer, multiplayer should work fine).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("air-place")
        .description("Allow the bot to place in the air.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> placeThroughWall = sgGeneral.add(new BoolSetting.Builder()
        .name("Place Through Wall")
        .description("Allow the bot to place through walls.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("swing")
        .description("Swing hand when placing.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> returnHand = sgGeneral.add(new BoolSetting.Builder()
        .name("return-slot")
        .description("Return to old slot.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotate to the blocks being placed.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> clientSide = sgGeneral.add(new BoolSetting.Builder()
        .name("Client side Rotation")
        .description("Rotate to the blocks being placed on client side.")
        .defaultValue(false)
        .visible(rotate::get)
        .build()
    );

    private final Setting<Boolean> dirtgrass = sgGeneral.add(new BoolSetting.Builder()
        .name("dirt-as-grass")
        .description("Use dirt instead of grass.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SortAlgorithm> firstAlgorithm = sgGeneral.add(new EnumSetting.Builder<SortAlgorithm>()
        .name("first-sorting-mode")
        .description("The blocks you want to place first.")
        .defaultValue(SortAlgorithm.None)
        .build()
    );

    private final Setting<SortingSecond> secondAlgorithm = sgGeneral.add(new EnumSetting.Builder<SortingSecond>()
        .name("second-sorting-mode")
        .description("Second pass of sorting eg. place first blocks higher and closest to you.")
        .defaultValue(SortingSecond.None)
        .visible(() -> firstAlgorithm.get().applySecondSorting)
        .build()
    );

    private final Setting<Boolean> whitelistenabled = sgWhitelist.add(new BoolSetting.Builder()
        .name("whitelist-enabled")
        .description("Only place selected blocks.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Block>> whitelist = sgWhitelist.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("Blocks to place.")
        .visible(whitelistenabled::get)
        .build()
    );

    private final Setting<Boolean> renderBlocks = sgRendering.add(new BoolSetting.Builder()
        .name("render-placed-blocks")
        .description("Renders block placements.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRendering.add(new IntSetting.Builder()
        .name("fade-time")
        .description("Time for the rendering to fade, in ticks.")
        .defaultValue(3)
        .min(1).sliderMin(1)
        .max(1000).sliderMax(20)
        .visible(renderBlocks::get)
        .build()
    );

    private final Setting<SettingColor> colour = sgRendering.add(new ColorSetting.Builder()
        .name("colour")
        .description("The cubes colour.")
        .defaultValue(new SettingColor(95, 190, 255))
        .visible(renderBlocks::get)
        .build()
    );

    private int timer;
    private int usedSlot = -1;
    private final List<BlockPos> toSort = new ArrayList<>();
    private final List<Pair<Integer, BlockPos>> placed_fade = new ArrayList<>();

    @Override
    public void onActivate() {
        onDeactivate();
    }

    @Override
    public void onDeactivate() {
        placed_fade.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) {
            placed_fade.clear();
            return;
        }

        placed_fade.forEach(s -> s.setLeft(s.getLeft() - 1));
        placed_fade.removeIf(s -> s.getLeft() <= 0);

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        if (worldSchematic == null) {
            placed_fade.clear();
            toggle();
            return;
        }

        toSort.clear();


        if (timer >= printing_delay.get()) {
            BlockIterator.register(printing_range.get() + 1, printing_range.get() + 1, (pos, blockState) -> {
                BlockState required = worldSchematic.getBlockState(pos);

                if (
                    mc.player.getBlockPos().isWithinDistance(pos, printing_range.get())
                        && blockState.isReplaceable()
                        && !required.isLiquid()
                        && !required.isAir()
                        && blockState.getBlock() != required.getBlock()
                        && DataManager.getRenderLayerRange().isPositionWithinRange(pos)
                        && !mc.player.getBoundingBox().intersects(Vec3d.of(pos), Vec3d.of(pos).add(1, 1, 1))
                        && required.canPlaceAt(mc.world, pos)
                ) {
                    boolean isBlockInLineOfSight = isBlockInLineOfSight(pos, required);

                    if (
                        airPlace.get()
                            && placeThroughWall.get()
                            || !airPlace.get()
                            && !placeThroughWall.get()
                            && isBlockInLineOfSight
                            && getVisiblePlaceSide(
                            pos,
                            required,
                            printing_range.get(),
                            advanced.get() ? dir(required) : null
                        ) != null
                            || airPlace.get()
                            && !placeThroughWall.get()
                            && isBlockInLineOfSight
                            || !airPlace.get()
                            && placeThroughWall.get()
                            && BlockUtils.getPlaceSide(pos) != null
                    ) {
                        if (!whitelistenabled.get() || whitelist.get().contains(required.getBlock())) {
                            toSort.add(new BlockPos(pos));
                        }
                    }
                }
            });

            BlockIterator.after(() -> {
                //if (!tosort.isEmpty()) info(tosort.toString());

                if (firstAlgorithm.get() != SortAlgorithm.None) {
                    if (firstAlgorithm.get().applySecondSorting) {
                        if (secondAlgorithm.get() != SortingSecond.None) {
                            toSort.sort(secondAlgorithm.get().algorithm);
                        }
                    }
                    toSort.sort(firstAlgorithm.get().algorithm);
                }


                int placed = 0;
                for (BlockPos pos : toSort) {

                    BlockState state = worldSchematic.getBlockState(pos);
                    Item item = state.getBlock().asItem();

                    if (dirtgrass.get() && item == Items.GRASS_BLOCK)
                        item = Items.DIRT;
                    if (switchItem(item, state, () -> place(state, pos))) {
                        timer = 0;
                        placed++;
                        if (renderBlocks.get()) {
                            placed_fade.add(new Pair<>(fadeTime.get(), new BlockPos(pos)));
                        }
                        if (placed >= bpt.get()) {
                            return;
                        }
                    }
                }
            });


        } else timer++;
    }

    public boolean place(BlockState required, BlockPos pos) {
        if (mc.player == null || mc.world == null) return false;
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;

        Direction wantedSide = advanced.get() ? dir(required) : null;


        Direction placeSide = placeThroughWall.get() ?
            getPlaceSide(pos, wantedSide)
            : getVisiblePlaceSide(
            pos,
            required,
            printing_range.get(),
            wantedSide
        );

        return place(pos, placeSide, airPlace.get(), swing.get(), rotate.get(), clientSide.get(), printing_range.get());
    }

    private boolean switchItem(Item item, BlockState state, Supplier<Boolean> action) {
        if (mc.player == null) return false;

        int selectedSlot = mc.player.getInventory().selectedSlot;
        boolean isCreative = mc.player.getAbilities().creativeMode;
        ItemStack requiredItemStack = item.getDefaultStack();
        NbtCompound nbt = getNbtFromBlockState(requiredItemStack, state);
        requiredItemStack.setNbt(nbt);
        FindItemResult result = InvUtils.find(item);

        if (!isCreative && mc.player.getMainHandStack().getItem() == item || isCreative && mc.player.getMainHandStack().getItem() == item && ItemStack.canCombine(mc.player.getMainHandStack(), requiredItemStack)) {
            if (action.get()) {
                usedSlot = mc.player.getInventory().selectedSlot;
                return true;
            } else return false;

        } else if (!isCreative && usedSlot != -1 && mc.player.getInventory().getStack(usedSlot).getItem() == item || isCreative && usedSlot != -1 && mc.player.getInventory().getStack(usedSlot).getItem() == item && ItemStack.canCombine(mc.player.getInventory().getStack(usedSlot), requiredItemStack)) {
            InvUtils.swap(usedSlot, returnHand.get());
            if (action.get()) {
                return true;
            } else {
                InvUtils.swap(selectedSlot, returnHand.get());
                return false;
            }
        } else if (result.found() && !isCreative || result.found() && isCreative && result.found() && result.slot() != -1 && ItemStack.canCombine(requiredItemStack, mc.player.getInventory().getStack(result.slot()))) {
            if (result.isHotbar()) {
                InvUtils.swap(result.slot(), returnHand.get());

                if (action.get()) {
                    usedSlot = mc.player.getInventory().selectedSlot;
                    return true;
                } else {
                    InvUtils.swap(selectedSlot, returnHand.get());
                    return false;
                }

            } else if (result.isMain()) {
                FindItemResult empty = InvUtils.findEmpty();

                if (empty.found() && empty.isHotbar()) {
                    InvUtils.move().from(result.slot()).toHotbar(empty.slot());
                    InvUtils.swap(empty.slot(), returnHand.get());

                    if (action.get()) {
                        usedSlot = mc.player.getInventory().selectedSlot;
                        return true;
                    } else {
                        InvUtils.swap(selectedSlot, returnHand.get());
                        return false;
                    }

                } else if (usedSlot != -1) {
                    InvUtils.move().from(result.slot()).toHotbar(usedSlot);
                    InvUtils.swap(usedSlot, returnHand.get());

                    if (action.get()) {
                        return true;
                    } else {
                        InvUtils.swap(selectedSlot, returnHand.get());
                        return false;
                    }

                } else return false;
            } else return false;
        } else if (isCreative) {
            int slot = 0;
            FindItemResult fir = InvUtils.find(ItemStack::isEmpty, 0, 8);
            if (fir.found()) {
                slot = fir.slot();
            }
            mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, requiredItemStack));
            InvUtils.swap(slot, returnHand.get());
            return true;
        } else return false;
    }

    private Direction dir(BlockState state) {
        if (state.contains(Properties.FACING)) return state.get(Properties.FACING);
        else if (state.contains(Properties.AXIS))
            return Direction.from(state.get(Properties.AXIS), Direction.AxisDirection.POSITIVE);
        else if (state.contains(Properties.HORIZONTAL_AXIS))
            return Direction.from(state.get(Properties.HORIZONTAL_AXIS), Direction.AxisDirection.POSITIVE);
        else return Direction.UP;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        placed_fade.forEach(s -> {
            Color a = new Color(colour.get().r, colour.get().g, colour.get().b, (int) (((float) s.getLeft() / (float) fadeTime.get()) * colour.get().a));
            event.renderer.box(s.getRight(), a, null, ShapeMode.Sides, 0);
        });
    }

    private boolean place(BlockPos blockPos, Direction direction, boolean airPlace, boolean swingHand, boolean rotate, boolean clientSide, int range) {
        if (mc.player == null) return false;
        if (!BlockUtils.canPlace(blockPos)) return false;

        Vec3d hitPos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

        BlockPos neighbour;

        if (direction == null) {
            direction = Direction.UP;
            neighbour = blockPos;
        } else if (airPlace) {
            neighbour = blockPos;
        } else {
            neighbour = blockPos.offset(direction.getOpposite());
            hitPos.add(direction.getOffsetX() * 0.5, direction.getOffsetY() * 0.5, direction.getOffsetZ() * 0.5);
        }

        Direction s = direction;

        if (rotate) {
            BetterBlockPos placeAgainstPos = new BetterBlockPos(neighbour.getX(), neighbour.getY(), neighbour.getZ());
            VoxelShape collisionShape = mc.world.getBlockState(placeAgainstPos).getCollisionShape(mc.world, placeAgainstPos);

            if (collisionShape.isEmpty()) {
                Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), 50, clientSide, () ->
                    place(new BlockHitResult(hitPos, s, neighbour, false), swingHand)
                );
                return true;
            }

            Box aabb = collisionShape.getBoundingBox();
            for (Vec3d placementMultiplier : aabbSideMultipliers(direction.getOpposite())) {
                double placeX = placeAgainstPos.x + aabb.minX * placementMultiplier.x + aabb.maxX * (1 - placementMultiplier.x);
                double placeY = placeAgainstPos.y + aabb.minY * placementMultiplier.y + aabb.maxY * (1 - placementMultiplier.y);
                double placeZ = placeAgainstPos.z + aabb.minZ * placementMultiplier.z + aabb.maxZ * (1 - placementMultiplier.z);

                Vec3d testHitPos = new Vec3d(placeX, placeY, placeZ);
                Vec3d playerHead = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());

                Rotation rot = RotationUtils.calcRotationFromVec3d(playerHead, testHitPos, new Rotation(mc.player.getYaw(), mc.player.getPitch()));
                HitResult res = RayTraceUtils.rayTraceTowards(mc.player, rot, range, false);
                BlockHitResult blockHitRes = ((BlockHitResult) res);
                if (res == null ||
                    !blockHitRes.getBlockPos().equals(placeAgainstPos) ||
                    blockHitRes.getSide() != direction
                ) continue;

                if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(Rotations.getYaw(testHitPos), Rotations.getPitch(testHitPos), priority, RotationType.BlockPlace, Objects.hash(name + "placing")))
                    return false;

                place(new BlockHitResult(testHitPos, s, neighbour, false), swingHand);

                if (SettingUtils.shouldRotate(RotationType.BlockPlace))
                    Managers.ROTATION.end(Objects.hash(name + "placing"));

                return true;
            }
        } else {
            place(new BlockHitResult(hitPos, s, neighbour, false), swingHand);
        }

        return true;
    }

    private void place(BlockHitResult blockHitResult, boolean swing) {
        if (mc.player == null || mc.interactionManager == null || mc.getNetworkHandler() == null) return;
        boolean wasSneaking = mc.player.input.sneaking;
        mc.player.input.sneaking = false;

        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);

        if (result.shouldSwingHand()) {
            if (swing) mc.player.swingHand(Hand.MAIN_HAND);
            else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        mc.player.input.sneaking = wasSneaking;
    }

    private boolean isBlockNormalCube(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof ScaffoldingBlock
            || block instanceof ShulkerBoxBlock
            || block instanceof PointedDripstoneBlock
            || block instanceof AmethystClusterBlock) {
            return false;
        }
        try {
            return Block.isShapeFullCube(state.getCollisionShape(null, null)) || state.getBlock() instanceof StairsBlock;
        } catch (Exception ignored) {
            // if we can't get the collision shape, assume it's bad...
        }
        return false;
    }

    private boolean canPlaceAgainst(BlockState placeAtState, BlockState placeAgainstState, Direction against) {
        return isBlockNormalCube(placeAgainstState) ||
            placeAgainstState.getBlock() == Blocks.GLASS ||
            placeAgainstState.getBlock() instanceof StainedGlassBlock ||
            placeAgainstState.getBlock() instanceof SlabBlock &&
                (
                    placeAgainstState.get(SlabBlock.TYPE) != SlabType.BOTTOM &&
                        placeAtState.getBlock() == placeAgainstState.getBlock() &&
                        against != Direction.DOWN ||
                        placeAtState.getBlock() != placeAgainstState.getBlock()
                );
    }

    private boolean isBlockInLineOfSight(BlockPos placeAt, BlockState placeAtState) {
        Vec3d playerHead = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
        Vec3d placeAtVec = new Vec3d(placeAt.getX(), placeAt.getY(), placeAt.getZ());

        ShapeType type = RaycastContext.ShapeType.COLLIDER;
        FluidHandling fluid = RaycastContext.FluidHandling.NONE;

        RaycastContext context =
            new RaycastContext(playerHead, placeAtVec, type, fluid, mc.player);
        BlockHitResult bhr = mc.world.raycast(context);

        // check line of sight
        return (bhr.getType() == HitResult.Type.MISS);

    }

    private Direction getVisiblePlaceSide(BlockPos placeAt, BlockState placeAtState, int range, Direction requiredDir) {
        if (mc.world == null) return null;
        for (Direction against : Direction.values()) {
            BetterBlockPos placeAgainstPos = new BetterBlockPos(placeAt.getX(), placeAt.getY(), placeAt.getZ()).relative(against);
            // BlockState placeAgainstState = mc.world.getBlockState(placeAgainstPos);

            if (requiredDir != null && requiredDir != against && requiredDir != Direction.UP)
                continue;

            if (!canPlaceAgainst(
                placeAtState,
                mc.world.getBlockState(placeAgainstPos),
                against
            ))
                continue;
            Box aabb = mc.world.getBlockState(placeAgainstPos).getCollisionShape(mc.world, placeAgainstPos).getBoundingBox();

            for (Vec3d placementMultiplier : aabbSideMultipliers(against)) {
                double placeX = placeAgainstPos.x + aabb.minX * placementMultiplier.x + aabb.maxX * (1 - placementMultiplier.x);
                double placeY = placeAgainstPos.y + aabb.minY * placementMultiplier.y + aabb.maxY * (1 - placementMultiplier.y);
                double placeZ = placeAgainstPos.z + aabb.minZ * placementMultiplier.z + aabb.maxZ * (1 - placementMultiplier.z);

                Vec3d hitPos = new Vec3d(placeX, placeY, placeZ);
                Vec3d playerHead = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());

                Rotation rot = RotationUtils.calcRotationFromVec3d(playerHead, hitPos, new Rotation(mc.player.getYaw(), mc.player.getPitch()));
                HitResult res = RayTraceUtils.rayTraceTowards(mc.player, rot, range, false);
                BlockHitResult blockHitRes = ((BlockHitResult) res);

                if (
                    res == null
                        || res.getType() != HitResult.Type.BLOCK
                        || !blockHitRes.getBlockPos().equals(placeAgainstPos)
                        || blockHitRes.getSide() != against.getOpposite()
                ) continue;


                return against.getOpposite();

            }
        }

        return null;
    }

    private Direction getPlaceSide(BlockPos blockPos, Direction requiredDir) {
        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            Direction side2 = side.getOpposite();

            if (requiredDir != null && requiredDir != side2 && requiredDir != Direction.UP)
                continue;

            BlockState state = mc.world.getBlockState(neighbor);

            // Check if neighbour isn't empty
            if (state.isAir() || !BlockUtils.isClickable(state.getBlock())) continue;

            // Check if neighbour is a fluid
            if (!state.getFluidState().isEmpty()) continue;
            info(String.valueOf(side2));
            return side2;
        }

        return null;
    }

    public static NbtCompound getNbtFromBlockState(ItemStack itemStack, BlockState state) {
        NbtCompound nbt = itemStack.getOrCreateNbt();
        NbtCompound subNbt = new NbtCompound();
        for (Property<?> property : state.getProperties()) {
            subNbt.putString(property.getName(), state.get(property).toString());
        }
        nbt.put("BlockStateTag", subNbt);

        return nbt;
    }

    private static Vec3d[] aabbSideMultipliers(Direction side) {
        switch (side) {
            case UP -> {
                return new Vec3d[]{new Vec3d(0.5, 1, 0.5), new Vec3d(0.1, 1, 0.5), new Vec3d(0.9, 1, 0.5), new Vec3d(0.5, 1, 0.1), new Vec3d(0.5, 1, 0.9)};
            }
            case DOWN -> {
                return new Vec3d[]{new Vec3d(0.5, 0, 0.5), new Vec3d(0.1, 0, 0.5), new Vec3d(0.9, 0, 0.5), new Vec3d(0.5, 0, 0.1), new Vec3d(0.5, 0, 0.9)};
            }
            case NORTH, SOUTH, EAST, WEST -> {
                double x = side.getOffsetX() == 0 ? 0.5 : (1 + side.getOffsetX()) / 2D;
                double z = side.getOffsetZ() == 0 ? 0.5 : (1 + side.getOffsetZ()) / 2D;
                return new Vec3d[]{new Vec3d(x, 0.25, z), new Vec3d(x, 0.75, z)};
            }
            default -> // null
                throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unused")
    public enum SortAlgorithm {
        None(false, (a, b) -> 0),
        TopDown(true, Comparator.comparingInt(value -> value.getY() * -1)),
        DownTop(true, Comparator.comparingInt(Vec3i::getY)),
        Nearest(false, Comparator.comparingDouble(value -> LemonClient.mc.player != null ? Utils.squaredDistance(LemonClient.mc.player.getX(), LemonClient.mc.player.getY(), LemonClient.mc.player.getZ(), value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5) : 0)),
        Furthest(false, Comparator.comparingDouble(value -> LemonClient.mc.player != null ? (Utils.squaredDistance(LemonClient.mc.player.getX(), LemonClient.mc.player.getY(), LemonClient.mc.player.getZ(), value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5)) * -1 : 0));


        final boolean applySecondSorting;
        final Comparator<BlockPos> algorithm;

        SortAlgorithm(boolean applySecondSorting, Comparator<BlockPos> algorithm) {
            this.applySecondSorting = applySecondSorting;
            this.algorithm = algorithm;
        }
    }

    @SuppressWarnings("unused")
    public enum SortingSecond {
        None(SortAlgorithm.None.algorithm),
        Nearest(SortAlgorithm.Nearest.algorithm),
        Furthest(SortAlgorithm.Furthest.algorithm);

        final Comparator<BlockPos> algorithm;

        SortingSecond(Comparator<BlockPos> algorithm) {
            this.algorithm = algorithm;
        }
    }
}
