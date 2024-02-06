package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.*;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.player.AutoMine;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.EntityInfo;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.entity.TargetUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class CityMiner extends Module {
    public CityMiner() {
        super(Categories.Combat, "City Miner", "Automatically mine blocks next to someone's feet.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<MineMode> mineMode = sgGeneral.add(new EnumSetting.Builder<MineMode>()
        .name("Mine Mode")
        .description(".")
        .defaultValue(MineMode.Click)
        .build()
    );
    private final Setting<Boolean> mineBurrow = sgGeneral.add(new BoolSetting.Builder()
        .name("Mine Burrow")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("How to switch to a pickaxe.")
        .defaultValue(SwitchMode.Normal)
        .visible(() -> mineMode.get().equals(MineMode.Normal))
        .build()
    );
    private final Setting<Boolean> chatInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("Chat Info")
        .description("Whether the module should send messages in chat.")
        .defaultValue(true)
        .visible(() -> mineMode.get().equals(MineMode.Normal))
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders your swing client-side.")
        .defaultValue(true)
        .visible(() -> mineMode.get().equals(MineMode.Normal))
        .build()
    );
    private final Setting<SwingHand> swingHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(() -> swing.get() && mineMode.get().equals(MineMode.Normal))
        .build()
    );
    private final Setting<Boolean> renderBlock = sgRender.add(new BoolSetting.Builder()
        .name("render-block")
        .description("Whether to render the block being broken.")
        .defaultValue(true)
        .visible(() -> mineMode.get().equals(MineMode.Normal))
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(() -> renderBlock.get() && mineMode.get().equals(MineMode.Normal))
        .build()
    );
    private final Setting<SettingColor> startSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Start Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(225, 0, 0, 75))
        .visible(() -> renderBlock.get() && shapeMode.get().sides() && mineMode.get().equals(MineMode.Normal))
        .build()
    );
    private final Setting<SettingColor> startLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Start Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(225, 0, 0, 255))
        .visible(() -> renderBlock.get() && shapeMode.get().lines() && mineMode.get().equals(MineMode.Normal))
        .build()
    );
    private final Setting<SettingColor> endSideColor = sgRender.add(new ColorSetting.Builder()
        .name("End Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(0, 255, 0, 75))
        .visible(() -> renderBlock.get() && shapeMode.get().sides() && mineMode.get().equals(MineMode.Normal))
        .build()
    );
    private final Setting<SettingColor> endLineColor = sgRender.add(new ColorSetting.Builder()
        .name("End Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .visible(() -> renderBlock.get() && shapeMode.get().lines() && mineMode.get().equals(MineMode.Normal))
        .build()
    );

    private PlayerEntity target;
    private BlockPos burrowPos, cityPos;
    private FindItemResult pick;
    private float progress;

    @Override
    public void onActivate() {
        target = TargetUtils.getPlayerTarget();
        burrowPos = EntityInfo.getFillBlock(target);
        cityPos = EntityUtils.getCityBlock(target);

        if ((burrowPos == null && cityPos == null) || !SettingUtils.inMineRange(cityPos)) {
            if (chatInfo.get()) error("Couldn't find a good block, disabling.");
            toggle();
            return;
        }

        switch (mineMode.get()) {
            case Normal -> {
                pick = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);
                if (!pick.isHotbar()) {
                    error("No pickaxe found... disabling.");
                    toggle();
                    return;
                }

                progress = 0.0f;
                mine(false);
            }
            case Click -> {
                AutoMine autoMine = Modules.get().get(AutoMine.class);
                if (mineBurrow.get() && (autoMine.isActive() && autoMine.doubleBreak.get() && autoMine.silentDouble.get())) {
                    mc.interactionManager.attackBlock(burrowPos, Direction.UP);
                    mc.interactionManager.attackBlock(cityPos, Direction.UP);
                } else if (cityPos != null) {
                    mc.interactionManager.attackBlock(cityPos, Direction.UP);
                } else if (mineBurrow.get() && cityPos == null) {
                    mc.interactionManager.attackBlock(burrowPos, Direction.UP);
                }

                toggle();
            }
        }
    }

    @Override
    public void onDeactivate() {
        target = null;
        cityPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mineMode.get().equals(MineMode.Normal)) {
            if (!SettingUtils.inMineRange(cityPos)) {
                if (chatInfo.get()) error("Couldn't find a target, disabling.");
                toggle();
                return;
            }

            if (progress < 1.0f) {
                pick = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);
                if (!pick.isHotbar()) {
                    error("No pickaxe found... disabling.");
                    toggle();
                    return;
                }
                progress += BlockUtils.getBreakDelta(pick.slot(), mc.world.getBlockState(cityPos));
                if (progress < 1.0f) return;
            }

            mine(true);

            toggle();
        }
    }

    public void mine(boolean done) {
        if (SettingUtils.shouldRotate(RotationType.Mining))
            Managers.ROTATION.start(cityPos, priority, RotationType.BlockPlace, Objects.hash(name + "mining"));

        InvUtils.swap(pick.slot(), switchMode.get() == SwitchMode.Silent);

        Direction direction = BlockUtils.getDirection(cityPos);

        if (!done) {
            SettingUtils.swing(SwingState.Pre, SwingType.Mining, Hand.MAIN_HAND);
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, cityPos, direction));
        }
        sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, cityPos, direction));
        SettingUtils.swing(SwingState.Post, SwingType.Mining, Hand.MAIN_HAND);

        if (swing.get()) clientSwing(swingHand.get(), Hand.MAIN_HAND);

        if (switchMode.get() == SwitchMode.Silent) InvUtils.swapBack();

        if (SettingUtils.shouldRotate(RotationType.Mining))
            Managers.ROTATION.end(Objects.hash(name + "mining"));
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mineMode.get().equals(MineMode.Normal)) {
            if (cityPos == null || !renderBlock.get()) return;

            Color color1 = progress >= 0.95 ? endSideColor.get() : startSideColor.get();
            Color color2 = progress >= 0.95 ? endLineColor.get() : startLineColor.get();

            double min = progress / 2;
            Vec3d vec3d = cityPos.toCenterPos();
            Box box = new Box(vec3d.x - min, vec3d.y - min, vec3d.z - min, vec3d.x + min, vec3d.y + min, vec3d.z + min);

            event.renderer.box(box, color1, color2, shapeMode.get(), 0);
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }

    public enum MineMode {
        Normal,
        Click
    }

    public enum SwitchMode {
        Normal,
        Silent
    }
}
