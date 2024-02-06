package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.entity.PlayerModelPart;

public class SkinBlinker extends Module {
    public SkinBlinker() {
        super(Categories.Render, "Skin Blinker", "Blinks different parts of your skin.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("What mode the skin blinker should behave in.")
        .defaultValue(Mode.Sequential)
        .build()
    );

    private final Setting<SequentialMode> seqMode = sgGeneral.add(new EnumSetting.Builder<SequentialMode>()
        .name("sequential-mode")
        .description("Whether to toggle your skin parts on or off.")
        .defaultValue(SequentialMode.On)
        .visible(() -> mode.get() == Mode.Sequential)
        .build()
    );

    private final Setting<Integer> sequentialDelay = sgGeneral.add(new IntSetting.Builder()
        .name("sequential-delay")
        .description("Delay in ticks between each part of skin to toggle.")
        .defaultValue(5)
        .min(1)
        .sliderRange(1, 15)
        .visible(() -> mode.get() == Mode.Sequential)
        .build()
    );

    private final Setting<Boolean> cape = sgGeneral.add(new BoolSetting.Builder()
        .name("cape")
        .description("Blinks the cape part of your skin (only works if you have a Mojang cape).")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Individual)
        .build()
    );

    private final Setting<Integer> capeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("cape-delay")
        .description("Delay in ticks between toggling the cape part of the skin.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 15)
        .visible(() -> mode.get() == Mode.Individual && cape.get())
        .build()
    );

    private final Setting<Boolean> head = sgGeneral.add(new BoolSetting.Builder()
        .name("head")
        .description("Blinks the head part of your skin.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Individual)
        .build()
    );

    private final Setting<Integer> headDelay = sgGeneral.add(new IntSetting.Builder()
        .name("head-delay")
        .description("Delay in ticks between toggling the head part of the skin.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 15)
        .visible(() -> mode.get() == Mode.Individual && head.get())
        .build()
    );

    private final Setting<Boolean> jacket = sgGeneral.add(new BoolSetting.Builder()
        .name("jacket")
        .description("Blinks the torso part of your skin.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Individual)
        .build()
    );

    private final Setting<Integer> jacketDelay = sgGeneral.add(new IntSetting.Builder()
        .name("jacket-delay")
        .description("Delay in ticks between toggling the jacket part of the skin.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 15)
        .visible(() -> mode.get() == Mode.Individual && jacket.get())
        .build()
    );

    private final Setting<Boolean> leftArm = sgGeneral.add(new BoolSetting.Builder()
        .name("left-arm")
        .description("Blinks the left arm of your skin.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Individual)
        .build()
    );

    private final Setting<Integer> leftArmDelay = sgGeneral.add(new IntSetting.Builder()
        .name("left-arm-delay")
        .description("Delay in ticks between toggling the left arm part of the skin.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 15)
        .visible(() -> mode.get() == Mode.Individual && leftArm.get())
        .build()
    );

    private final Setting<Boolean> rightArm = sgGeneral.add(new BoolSetting.Builder()
        .name("right-arm")
        .description("Blinks the right arm of your skin.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Individual)
        .build()
    );

    private final Setting<Integer> rightArmDelay = sgGeneral.add(new IntSetting.Builder()
        .name("right-arm-delay")
        .description("Delay in ticks between toggling the right arm part of the skin.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 15)
        .visible(() -> mode.get() == Mode.Individual && rightArm.get())
        .build()
    );

    private final Setting<Boolean> leftLeg = sgGeneral.add(new BoolSetting.Builder()
        .name("left-leg")
        .description("Blinks the head left leg of your skin.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Individual)
        .build()
    );

    private final Setting<Integer> leftLegDelay = sgGeneral.add(new IntSetting.Builder()
        .name("left-leg-delay")
        .description("Delay in ticks between toggling the left leg part of the skin.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 15)
        .visible(() -> mode.get() == Mode.Individual && leftLeg.get())
        .build()
    );

    private final Setting<Boolean> rightLeg = sgGeneral.add(new BoolSetting.Builder()
        .name("right-leg")
        .description("Blinks the head right leg of your skin.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Individual)
        .build()
    );

    private final Setting<Integer> rightLegDelay = sgGeneral.add(new IntSetting.Builder()
        .name("right-leg-delay")
        .description("Delay in ticks between toggling the right leg part of the skin.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 15)
        .visible(() -> mode.get() == Mode.Individual && rightLeg.get())
        .build()
    );

    private int ticksPassed;
    private int headTimer, jacketTimer, leftArmTimer, rightArmTimer, leftLegTimer, rightLegTimer, capeTimer;

    public enum Mode {
        Sequential,
        Individual
    }

    public enum SequentialMode {
        On,
        Off
    }

    @Override
    public void onActivate() {
        // Sequential
        ticksPassed = 0;

        // Individual
        headTimer = 0;
        jacketTimer = 0;
        capeTimer = 0;
        leftArmTimer = 0;
        rightArmTimer = 0;
        leftLegTimer = 0;
        rightLegTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // Sequential mode
        if (mode.get() == Mode.Sequential) {
            if (ticksPassed < sequentialDelay.get() * 5) ticksPassed++;
            else ticksPassed = 0;

            if (ticksPassed > 0) mc.options.togglePlayerModelPart(PlayerModelPart.HAT, hat());
            if (ticksPassed > sequentialDelay.get()) {
                mc.options.togglePlayerModelPart(PlayerModelPart.LEFT_SLEEVE, arm());
                mc.options.togglePlayerModelPart(PlayerModelPart.RIGHT_SLEEVE, arm());
            }
            if (ticksPassed > sequentialDelay.get() * 2)
                mc.options.togglePlayerModelPart(PlayerModelPart.JACKET, mid());
            if (ticksPassed > sequentialDelay.get() * 3) {
                mc.options.togglePlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG, legs());
                mc.options.togglePlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG, legs());
            }
        } else {
            // Individual mode

            // cape
            if (cape.get()) {
                if (capeTimer < capeDelay.get() * 2) {
                    capeTimer++;
                    mc.options.togglePlayerModelPart(PlayerModelPart.CAPE, capeTimer <= capeDelay.get());
                } else capeTimer = 0;
            }

            // head
            if (head.get()) {
                if (headTimer < headDelay.get() * 2) {
                    headTimer++;
                    mc.options.togglePlayerModelPart(PlayerModelPart.HAT, headTimer <= headDelay.get());
                } else headTimer = 0;
            }

            // jacket
            if (jacket.get()) {
                if (jacketTimer < jacketDelay.get() * 2) {
                    jacketTimer++;
                    mc.options.togglePlayerModelPart(PlayerModelPart.JACKET, jacketTimer <= jacketDelay.get());
                } else jacketTimer = 0;
            }

            // left arm
            if (leftArm.get()) {
                if (leftArmTimer < leftArmDelay.get() * 2) {
                    leftArmTimer++;
                    mc.options.togglePlayerModelPart(PlayerModelPart.LEFT_SLEEVE, leftArmTimer <= leftArmDelay.get());
                } else leftArmTimer = 0;
            }

            // right arm
            if (rightArm.get()) {
                if (rightArmTimer < rightArmDelay.get() * 2) {
                    rightArmTimer++;
                    mc.options.togglePlayerModelPart(PlayerModelPart.RIGHT_SLEEVE, rightArmTimer <= rightArmDelay.get());
                } else rightArmTimer = 0;
            }

            // left leg
            if (leftLeg.get()) {
                if (leftLegTimer < leftLegDelay.get() * 2) {
                    leftLegTimer++;
                    mc.options.togglePlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG, leftLegTimer <= leftLegDelay.get());
                } else leftLegTimer = 0;
            }

            // right leg
            if (rightLeg.get()) {
                if (rightLegTimer < rightLegDelay.get() * 2) {
                    rightLegTimer++;
                    mc.options.togglePlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG, rightLegTimer <= rightLegDelay.get());
                } else rightLegTimer = 0;
            }


        }
    }

    private boolean hat() {
        if (seqMode.get() == SequentialMode.Off) return ticksPassed <= sequentialDelay.get();
        else return ticksPassed > sequentialDelay.get();
    }

    private boolean arm() {
        if (seqMode.get() == SequentialMode.Off) return ticksPassed <= sequentialDelay.get() * 2;
        else return ticksPassed > sequentialDelay.get() * 2;
    }

    private boolean mid() {
        if (seqMode.get() == SequentialMode.Off) return ticksPassed <= sequentialDelay.get() * 3;
        else return ticksPassed > sequentialDelay.get() * 3;
    }

    private boolean legs() {
        if (seqMode.get() == SequentialMode.Off) return ticksPassed <= sequentialDelay.get() * 4;
        else return ticksPassed > sequentialDelay.get() * 4;
    }
}
