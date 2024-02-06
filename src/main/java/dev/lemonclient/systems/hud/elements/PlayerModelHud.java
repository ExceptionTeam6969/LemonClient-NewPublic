package dev.lemonclient.systems.hud.elements;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.HudElementInfo;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

import static dev.lemonclient.LemonClient.mc;

public class PlayerModelHud extends HudElement {
    public static final HudElementInfo<PlayerModelHud> INFO = new HudElementInfo<>(Hud.GROUP, "player-model", "Displays a model of your player.", PlayerModelHud::new);
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBackground = settings.createGroup("Background");

    // General

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .onChanged(aDouble -> calculateSize())
        .build()
    );

    private final Setting<Boolean> copyYaw = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-yaw")
        .description("Makes the player model's yaw equal to yours.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> customYaw = sgGeneral.add(new IntSetting.Builder()
        .name("custom-yaw")
        .description("Custom yaw for when copy yaw is off.")
        .defaultValue(0)
        .range(-180, 180)
        .sliderRange(-180, 180)
        .visible(() -> !copyYaw.get())
        .build()
    );
    private final Setting<Boolean> copyPitch = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-pitch")
        .description("Makes the player model's pitch equal to yours.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> customPitch = sgGeneral.add(new IntSetting.Builder()
        .name("custom-pitch")
        .description("Custom pitch for when copy pitch is off.")
        .defaultValue(0)
        .range(-90, 90)
        .sliderRange(-90, 90)
        .visible(() -> !copyPitch.get())
        .build()
    );
    private final Setting<CenterOrientation> centerOrientation = sgGeneral.add(new EnumSetting.Builder<CenterOrientation>()
        .name("center-orientation")
        .description("Which direction the player faces when the HUD model faces directly forward.")
        .defaultValue(CenterOrientation.South)
        .build()
    );

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    public PlayerModelHud() {
        super(INFO);

        calculateSize();
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            PlayerEntity player = mc.player;
            if (player == null) return;

            float offset = centerOrientation.get() == CenterOrientation.North ? 180 : 0;

            float yaw = copyYaw.get() ? MathHelper.wrapDegrees(player.prevYaw + (player.getYaw() - player.prevYaw) * mc.getTickDelta() + offset) : (float) customYaw.get();
            float pitch = copyPitch.get() ? player.getPitch() : (float) customPitch.get();

            drawEntity(renderer.drawContext, x, y, (int) (30 * scale.get()), -yaw, -pitch, player);
        });

        if (background.get()) {
            renderer.quad(x, y, getWidth(), getHeight(), backgroundColor.get());
        } else if (mc.player == null) {
            renderer.quad(x, y, getWidth(), getHeight(), backgroundColor.get());
            renderer.line(x, y, x + getWidth(), y + getHeight(), Color.GRAY);
            renderer.line(x + getWidth(), y, x, y + getHeight(), Color.GRAY);
        }
    }

    private void calculateSize() {
        setSize(50 * scale.get(), 75 * scale.get());
    }

    private void drawEntity(DrawContext context, int x, int y, int size, float yaw, float pitch, LivingEntity entity) {
        float tanYaw = (float) Math.atan((yaw) / 40.0f);
        float tanPitch = (float) Math.atan((pitch) / 40.0f);

        Quaternionf quaternion = new Quaternionf().rotateZ((float) Math.PI);

        float previousBodyYaw = entity.bodyYaw;
        float previousYaw = entity.getYaw();
        float previousPitch = entity.getPitch();
        float previousPrevHeadYaw = entity.prevHeadYaw;
        float prevHeadYaw = entity.headYaw;

        entity.bodyYaw = 180.0f + tanYaw * 20.0f;
        entity.setYaw(180.0f + tanYaw * 40.0f);
        entity.setPitch(-tanPitch * 20.0f);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();

        InventoryScreen.drawEntity(context, x + getWidth() / 2, (int) (y + getHeight() * 0.9f), size, quaternion, null, entity);

        entity.bodyYaw = previousBodyYaw;
        entity.setYaw(previousYaw);
        entity.setPitch(previousPitch);
        entity.prevHeadYaw = previousPrevHeadYaw;
        entity.headYaw = prevHeadYaw;
    }

    private enum CenterOrientation {
        North,
        South
    }
}
