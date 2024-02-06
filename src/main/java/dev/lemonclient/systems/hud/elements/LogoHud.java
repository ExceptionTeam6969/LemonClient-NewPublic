package dev.lemonclient.systems.hud.elements;

import dev.lemonclient.renderer.GL;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.HudElementInfo;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.utils.render.color.RainbowColor;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;

public class LogoHud extends HudElement {
    public static final HudElementInfo<LogoHud> INFO = new HudElementInfo<>(Hud.GROUP, "Logo", "You should use fabric api to see it!", LogoHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<LogoEnum> logo = sgGeneral.add(new EnumSetting.Builder<LogoEnum>()
        .name("Logo")
        .defaultValue(LogoEnum.Text)
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("The scale of the logo.")
        .defaultValue(3.5)
        .min(0.1)
        .sliderRange(0.1, 5)
        .build()
    );
    public final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("Chroma")
        .description("Chroma logo animation.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Factor")
        .defaultValue(0.10)
        .min(0.01)
        .sliderMax(5)
        .decimalPlaces(4)
        .visible(chroma::get)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(() -> !chroma.get())
        .build()
    );

    private Identifier image = new Identifier("lemon-client", "text.png");

    private static final RainbowColor RAINBOW = new RainbowColor();

    public LogoHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        box.setSize(72 * scale.get(), 15 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        switch (logo.get()) {
            case Text -> image = new Identifier("lemon-client", "textures/text.png");
            case NewText -> image = new Identifier("lemon-client", "textures/newtext.png");
            case Logo -> image = new Identifier("lemon-client", "icons/icon.png");
        }

        GL.bindTexture(image);
        Renderer2D.TEXTURE.begin();
        if (chroma.get()) {
            RAINBOW.setSpeed(chromaSpeed.get() / 100);
            Renderer2D.TEXTURE.texQuad(this.x, this.y - 29 * scale.get(), 70 * scale.get(), 70 * scale.get(), RAINBOW.getNext(renderer.delta));
        } else {
            Renderer2D.TEXTURE.texQuad(this.x, this.y - 29 * scale.get(), 70 * scale.get(), 70 * scale.get(), color.get());
        }
        Renderer2D.TEXTURE.render(null);
    }

    public enum LogoEnum {
        Text,
        NewText,
        Logo
    }
}
