package dev.lemonclient.utils.render;

import dev.lemonclient.settings.*;
import dev.lemonclient.utils.render.color.SettingColor;

public enum ColorMode {
    Custom,
    Rainbow,
    TwoColor,
    Astolfo;

    public static class ColorSettings {
        public static ColorSettings create(SettingGroup group) {
            return create(group, null);
        }

        public static ColorSettings create(SettingGroup group, IVisible visible) {
            return new ColorSettings(group, visible);
        }

        public final Setting<ColorMode> mode;
        public final Setting<SettingColor> color;
        public final Setting<SettingColor> color2;
        public final Setting<Integer> colorOffset;
        public final Setting<Double> glowRadius;
        public final Setting<Double> inGlowRadius;

        public final AstolfoAnimation astolfo = new AstolfoAnimation();

        private ColorSettings(SettingGroup sg, IVisible visible) {
            this.mode = sg.add(new EnumSetting.Builder<ColorMode>()
                .name("Color Mode")
                .description("Select the color mode for the animation.")
                .defaultValue(ColorMode.Astolfo)
                .visible(visible)
                .build()
            );
            this.color = sg.add(new ColorSetting.Builder()
                .name("Color")
                .description("The primary color for the animation.")
                .defaultValue(new SettingColor(3649978))
                .visible(visible)
                .build()
            );
            this.color2 = sg.add(new ColorSetting.Builder()
                .name("Color2")
                .description("The secondary color for the Two-Color mode.")
                .defaultValue(new SettingColor(3646789))
                .visible(visible)
                .build()
            );
            this.colorOffset = sg.add(new IntSetting.Builder()
                .name("Color Offset")
                .description("Offset for the Two-Color mode.")
                .defaultValue(10)
                .visible(visible)
                .sliderRange(1, 20)
                .build()
            );
            this.glowRadius = sg.add(new DoubleSetting.Builder()
                .name("Out Glow Radius")
                .defaultValue(0.3)
                .visible(visible)
                .sliderRange(0.0, 10.0)
                .build()
            );
            this.inGlowRadius = sg.add(new DoubleSetting.Builder()
                .name("In Glow Radius")
                .defaultValue(0.3)
                .visible(visible)
                .sliderRange(0.0, 10.0)
                .build()
            );
        }

        public void tick() {
            astolfo.update();
        }
    }
}
