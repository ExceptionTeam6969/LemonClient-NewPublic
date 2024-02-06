package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.misc.ExtrapolationUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.TimerUtils;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoWeb extends Module {
    public AutoWeb() {
        super(Categories.Combat, "Auto Web", "Automatically places webs on other players.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgPredict = settings.createGroup("Predict");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseOnEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Eat")
        .description("Pause while eating.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("The mode to switch web.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Boolean> faceBrainless = sgGeneral.add(new BoolSetting.Builder()
        .name("Face Brainless")
        .description("Places cobweb on the target's face.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("Radius")
        .description("Extension radius.")
        .defaultValue(1)
        .range(0, 4)
        .visible(faceBrainless::get)
        .build()
    );
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("How many ticks between obsidian placement.")
        .defaultValue(0)
        .sliderRange(0, 100)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<Boolean> speedDetection = sgSpeed.add(new BoolSetting.Builder()
        .name("Speed Detection")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> playerMaxSpeed = sgSpeed.add(new IntSetting.Builder()
        .name("Player Max Speed")
        .defaultValue(1000)
        .sliderRange(0, 1000)
        .visible(speedDetection::get)
        .build()
    );
    private final Setting<Integer> playerMixSpeed = sgSpeed.add(new IntSetting.Builder()
        .name("Player Mix Speed")
        .defaultValue(1)
        .sliderRange(0, 100)
        .visible(speedDetection::get)
        .build()
    );

    //--------------------Predict--------------------//
    private final Setting<Integer> extrapolation = sgPredict.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("How many ticks of movement should be predicted for enemy damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgPredict.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description("How many earlier ticks should be used in average calculation for extrapolation motion.")
        .defaultValue(2)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders your swing client-side.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(swing::get)
        .build()
    );
    private final Setting<Boolean> renderExt = sgRender.add(new BoolSetting.Builder()
        .name("Render Extrapolation")
        .description("Renders boxes at players' predicted positions.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of the boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Lines) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Sides) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );

    private final TimerUtils timer = new TimerUtils();
    private final List<Render> renderBlocks = new ArrayList<>();
    private List<PlayerEntity> targets = new ArrayList<>();

    public enum SwitchMode {
        Silent,
        InvSwitch,
        PickSilent
    }

    @Override
    public void onActivate() {
        timer.reset();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        updateTargets();

        if (targets.isEmpty()) return;

        for (PlayerEntity target : targets) {
            Box extrapolated = ExtrapolationUtils.extrapolate((AbstractClientPlayerEntity) target, extrapolation.get(), extSmoothness.get());

            if (pauseOnEat.get() && mc.player.isUsingItem()) {
                return;
            }

            if (extrapolated != null && (!speedDetection.get() || Managers.SPEED.getPlayerSpeed(target) <= playerMaxSpeed.get() && Managers.SPEED.getPlayerSpeed(target) >= playerMixSpeed.get())) {
                if (timer.passedMs(delay.get())) {
                    placeWeb(BlockPos.ofFloored(extrapolated.minX, extrapolated.minY, extrapolated.minZ));
                }

                if (faceBrainless.get()) {
                    for (int xOffset = -radius.get(); xOffset <= radius.get(); xOffset++) {
                        for (int zOffset = -radius.get(); zOffset <= radius.get(); zOffset++) {
                            BlockPos blockPos = BlockPos.ofFloored(target.getEyePos()).add(xOffset, 0, zOffset);

                            if (Math.sqrt(xOffset * xOffset + zOffset * zOffset) <= radius.get() && blockPos.getY() >= target.getY() - 1 && !BlockUtils.solid(blockPos) && timer.passedMs(delay.get())) {
                                placeWeb(blockPos);
                            }
                        }
                    }
                }
            }

            if (render.get()) {
                renderBlocks.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

                renderBlocks.forEach(r -> {
                    double progress = 1 - Math.min(System.currentTimeMillis() - r.time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

                    event.renderer.box(r.pos, Render2DUtils.injectAlpha(sideColor.get(), (int) Math.round(sideColor.get().a * progress)), Render2DUtils.injectAlpha(lineColor.get(), (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
                });
            }

            if (renderExt.get() && extrapolated != null) {
                event.renderer.box(extrapolated, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }
        }
    }

    @Override
    public String getInfoString() {
        return targets.stream().map(target -> target.getGameProfile().getName()).findFirst().orElse(null);
    }

    private void updateTargets() {
        List<PlayerEntity> players = new ArrayList<>();
        double closestDist = 1000;
        PlayerEntity closest;
        double dist;
        for (int i = 3; i > 0; i--) {
            closest = null;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (players.contains(player) || Friends.get().isFriend(player) || player == mc.player) {
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
                players.add(closest);
            }
        }
        targets = players;
    }

    private void placeWeb(BlockPos blockPos) {
        if (BlockUtils.solid(blockPos)) {
            return;
        }

        if (!SettingUtils.inPlaceRange(blockPos)) {
            return;
        }

        FindItemResult web = !switchMode.get().equals(SwitchMode.Silent) ? InvUtils.find(Items.COBWEB) : InvUtils.findInHotbar(Items.COBWEB);
        if (!web.found()) {
            return;
        }

        PlaceData data = SettingUtils.getPlaceData(blockPos);
        if (!data.valid()) {
            return;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return;
        }

        boolean switched = switch (switchMode.get()) {
            case Silent -> InvUtils.swap(web.slot(), true);
            case InvSwitch -> InvUtils.invSwitch(web.slot());
            case PickSilent -> InvUtils.pickSwitch(web.slot());
        };

        if (!switched) {
            return;
        }

        placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());

        timer.reset();

        renderBlocks.add(new Render(blockPos, System.currentTimeMillis()));

        if (swing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        switch (switchMode.get()) {
            case Silent -> InvUtils.swapBack();
            case InvSwitch -> InvUtils.invSwapBack();
            case PickSilent -> InvUtils.pickSwapBack();
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }
    }

    public record Render(BlockPos pos, long time) {
    }
}
