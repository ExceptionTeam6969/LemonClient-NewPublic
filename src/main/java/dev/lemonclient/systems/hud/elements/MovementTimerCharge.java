package dev.lemonclient.systems.hud.elements;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.HudElementInfo;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.systems.modules.movement.movementtimer.MovementTimer;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;

public class MovementTimerCharge extends HudElement {
    public MovementTimerCharge() {
        super(INFO);
    }

    public static final HudElementInfo<MovementTimerCharge> INFO = new HudElementInfo<>(Hud.GROUP, "Movement Timer Charge", "Displays timer plus charge.", MovementTimerCharge::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    // General
    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Text shadow.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the element.")
        .defaultValue(0)
        .build()
    );

    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies custom text scale rather than the global one.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

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


    @Override
    public void setSize(double width, double height) {
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("A.")
        .defaultValue(new SettingColor())
        .build()
    );

    public static double find_percent(double start, double end, double val) {

        end = end - start;
        val = val - start;
        start = 0;

        return ((1 - (val / end)) * 100);
    }


    @Override
    public void render(HudRenderer renderer) {
        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
        }

        if (isInEditor()) {
            render(renderer, "4.3", textColor.get());
            return;
        }
        double percentage = find_percent(0, MovementTimer.rechargeDelay, MovementTimer.rechargeTimer);

        render(renderer, String.format("%.1f", percentage), textColor.get());
    }

    private void render(HudRenderer renderer, String right, Color rightColor) {
        double x = this.x + border.get();
        double y = this.y + border.get();

        double x2 = renderer.text("Timer Plus: ", x, y, textColor.get(), shadow.get(), getScale());
        x2 = renderer.text(right, x2, y, rightColor, shadow.get(), getScale());
        x2 = renderer.text("%", x2, y, rightColor, shadow.get(), getScale());

        setSize(x2 - x, renderer.textHeight(shadow.get(), getScale()));
    }

    private double getScale() {
        return customScale.get() ? scale.get() : -1;
    }
}
