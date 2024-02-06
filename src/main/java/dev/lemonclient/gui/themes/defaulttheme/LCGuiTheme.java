package dev.lemonclient.gui.themes.defaulttheme;

import dev.lemonclient.gui.DefaultSettingsWidgetFactory;
import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.WidgetScreen;
import dev.lemonclient.gui.renderer.packer.GuiTexture;
import dev.lemonclient.gui.themes.defaulttheme.input.WDropdown;
import dev.lemonclient.gui.themes.defaulttheme.input.WMeteorTextBox;
import dev.lemonclient.gui.themes.defaulttheme.input.WSlider;
import dev.lemonclient.gui.themes.defaulttheme.pressable.*;
import dev.lemonclient.gui.utils.AlignmentX;
import dev.lemonclient.gui.utils.CharFilter;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.input.WTextBox;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.accounts.Account;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;

import static dev.lemonclient.LemonClient.mc;

public class LCGuiTheme extends GuiTheme {
    public final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgTextColors = settings.createGroup("Text");
    private final SettingGroup sgBackgroundColors = settings.createGroup("Background");
    private final SettingGroup sgOutline = settings.createGroup("Outline");
    private final SettingGroup sgSeparator = settings.createGroup("Separator");
    private final SettingGroup sgScrollbar = settings.createGroup("Scrollbar");
    private final SettingGroup sgSlider = settings.createGroup("Slider");
    private final SettingGroup sgStarscript = settings.createGroup("Starscript");

    // General
    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Scale of the GUI.")
        .defaultValue(1)
        .min(0.75)
        .sliderMin(0.75)
        .sliderMax(4)
        .onSliderRelease()
        .onChanged(aDouble -> {
            if (mc.currentScreen instanceof WidgetScreen) ((WidgetScreen) mc.currentScreen).invalidate();
        })
        .build()
    );
    public Setting<AlignmentX> moduleAlignment = addModuleAlignmentSetting();

    public Setting<AlignmentX> addModuleAlignmentSetting() {
        return sgGeneral.add(new EnumSetting.Builder<AlignmentX>()
            .name("Module Alignment")
            .description("How module titles are aligned.")
            .defaultValue(AlignmentX.Center)
            .build()
        );
    }

    public final Setting<Boolean> categoryIcons = sgGeneral.add(new BoolSetting.Builder()
        .name("Category Icons")
        .description("Adds item icons to module categories.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> lineMeteor = sgGeneral.add(new BoolSetting.Builder()
        .name("Meteor")
        .description("Render some client on gui.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> meteorRainbow = sgGeneral.add(new BoolSetting.Builder()
        .name("Meteor Rainbow")
        .description("Enable rainbow background client color.")
        .defaultValue(false)
        .visible(lineMeteor::get)
        .build()
    );
    public final Setting<Boolean> snowParticles = sgGeneral.add(new BoolSetting.Builder()
        .name("Snow Particles")
        .description("Render some snow particles on gui.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Integer> spawnDelays = sgGeneral.add(new IntSetting.Builder()
        .name("Snow Spawn Delay")
        .description("If passed, renderer will spawn new snow particle.")
        .defaultValue(2100)
        .sliderRange(500, 10000)
        .visible(snowParticles::get)
        .build()
    );
    public final Setting<Boolean> particles = sgGeneral.add(new BoolSetting.Builder()
        .name("Particles")
        .description("Render some other particles on gui.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> bothParticles = sgGeneral.add(new BoolSetting.Builder()
        .name("Both Particles")
        .description("Render 2r particles on gui.")
        .defaultValue(false)
        .visible(particles::get)
        .build()
    );
    public final Setting<Boolean> hideHUD = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-HUD")
        .description("Hide HUD when in GUI.")
        .defaultValue(false)
        .onChanged(v -> {
            if (mc.currentScreen instanceof WidgetScreen) mc.options.hudHidden = v;
        })
        .build()
    );
    public final Setting<Integer> round = sgGeneral.add(new IntSetting.Builder()
        .name("round")
        .description("How much windows should be rounded")
        .defaultValue(5)
        .min(0)
        .max(20)
        .sliderMin(0)
        .sliderMax(15)
        .build()
    );

    // Colors
    public final Setting<SettingColor> accentColor = color("accent", "Main color of the GUI.", getAccentColor());

    public SettingColor getAccentColor() {
        return new SettingColor(130, 110, 255);
    }

    public final Setting<SettingColor> checkboxColor = color("checkbox", "Color of checkbox.", new SettingColor(130, 110, 255));
    public final Setting<SettingColor> plusColor = color("plus", "Color of plus button.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> minusColor = color("minus", "Color of minus button.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> favoriteColor = color("favorite", "Color of checked favorite button.", new SettingColor(255, 255, 0));

    // Text
    public final Setting<SettingColor> textColor = color(sgTextColors, "text", "Color of text.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> textSecondaryColor = color(sgTextColors, "text-secondary-text", "Color of secondary text.", new SettingColor(150, 150, 150));
    public final Setting<SettingColor> textHighlightColor = color(sgTextColors, "text-highlight", "Color of text highlighting.", new SettingColor(45, 125, 245, 100));
    public final Setting<SettingColor> titleTextColor = color(sgTextColors, "title-text", "Color of title text.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> loggedInColor = color(sgTextColors, "logged-in-text", "Color of logged in account name.", new SettingColor(45, 225, 45));
    public final Setting<SettingColor> placeholderColor = color(sgTextColors, "placeholder", "Color of placeholder text.", getPlaceHolderColor());

    public SettingColor getPlaceHolderColor() {
        return new SettingColor(255, 255, 255, 20);
    }

    // Background
    public final ThreeStateColorSetting backgroundColor = new ThreeStateColorSetting(
        sgBackgroundColors,
        "background",
        new SettingColor(20, 20, 20, 200),
        new SettingColor(30, 30, 30, 200),
        new SettingColor(40, 40, 40, 200)
    );

    public final Setting<SettingColor> moduleBackground = color(sgBackgroundColors, "module-background", "Color of module background when active.", getModuleBackgroundColor());

    public SettingColor getModuleBackgroundColor() {
        return new SettingColor(50, 50, 50);
    }

    // Outline
    public final ThreeStateColorSetting outlineColor = new ThreeStateColorSetting(
        sgOutline,
        "outline",
        new SettingColor(0, 0, 0),
        new SettingColor(10, 10, 10),
        new SettingColor(20, 20, 20)
    );

    // Separator
    public final Setting<SettingColor> separatorText = color(sgSeparator, "separator-text", "Color of separator text", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> separatorCenter = color(sgSeparator, "separator-center", "Center color of separators.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> separatorEdges = color(sgSeparator, "separator-edges", "Color of separator edges.", new SettingColor(225, 225, 225, 150));

    // Scrollbar
    public final ThreeStateColorSetting scrollbarColor = new ThreeStateColorSetting(
        sgScrollbar,
        "Scrollbar",
        new SettingColor(30, 30, 30, 200),
        new SettingColor(40, 40, 40, 200),
        new SettingColor(50, 50, 50, 200)
    );

    // Slider
    public final ThreeStateColorSetting sliderHandle = new ThreeStateColorSetting(
        sgSlider,
        "slider-handle",
        new SettingColor(0, 255, 180),
        new SettingColor(0, 240, 165),
        new SettingColor(0, 225, 150)
    );

    public final Setting<SettingColor> sliderLeft = color(sgSlider, "slider-left", "Color of slider left part.", new SettingColor(0, 150, 80));
    public final Setting<SettingColor> sliderRight = color(sgSlider, "slider-right", "Color of slider right part.", new SettingColor(50, 50, 50));

    // Starscript
    private final Setting<SettingColor> starscriptText = color(sgStarscript, "starscript-text", "Color of text in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptBraces = color(sgStarscript, "starscript-braces", "Color of braces in Starscript code.", new SettingColor(150, 150, 150));
    private final Setting<SettingColor> starscriptParenthesis = color(sgStarscript, "starscript-parenthesis", "Color of parenthesis in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptDots = color(sgStarscript, "starscript-dots", "Color of dots in starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptCommas = color(sgStarscript, "starscript-commas", "Color of commas in starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptOperators = color(sgStarscript, "starscript-operators", "Color of operators in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptStrings = color(sgStarscript, "starscript-strings", "Color of strings in Starscript code.", new SettingColor(106, 135, 89));
    private final Setting<SettingColor> starscriptNumbers = color(sgStarscript, "starscript-numbers", "Color of numbers in Starscript code.", new SettingColor(104, 141, 187));
    private final Setting<SettingColor> starscriptKeywords = color(sgStarscript, "starscript-keywords", "Color of keywords in Starscript code.", new SettingColor(204, 120, 50));
    private final Setting<SettingColor> starscriptAccessedObjects = color(sgStarscript, "starscript-accessed-objects", "Color of accessed objects (before a dot) in Starscript code.", new SettingColor(152, 118, 170));

    public LCGuiTheme() {
        super("Default");

        settingsFactory = new DefaultSettingsWidgetFactory(this);
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return group.add(new ColorSetting.Builder()
            .name(name + "-color")
            .description(description)
            .defaultValue(color)
            .build());
    }

    private Setting<SettingColor> color(String name, String description, SettingColor color) {
        return color(sgColors, name, description, color);
    }

    // Widgets

    @Override
    public dev.lemonclient.gui.widgets.containers.WWindow window(WWidget icon, String title) {
        return w(new WWindow(icon, title));
    }

    @Override
    public dev.lemonclient.gui.widgets.WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0) return w(new WLabel(text, title));
        return w(new WMultiLabel(text, title, maxWidth));
    }

    @Override
    public dev.lemonclient.gui.widgets.WHorizontalSeparator horizontalSeparator(String text) {
        return w(new WHorizontalSeparator(text));
    }

    @Override
    public dev.lemonclient.gui.widgets.WVerticalSeparator verticalSeparator() {
        return w(new WVerticalSeparator());
    }

    @Override
    protected dev.lemonclient.gui.widgets.pressable.WButton button(String text, GuiTexture texture) {
        return w(new WButton(text, texture));
    }

    @Override
    public dev.lemonclient.gui.widgets.pressable.WMinus minus() {
        return w(new WMinus());
    }

    @Override
    public dev.lemonclient.gui.widgets.pressable.WPlus plus() {
        return w(new WPlus());
    }

    @Override
    public dev.lemonclient.gui.widgets.pressable.WCheckbox checkbox(boolean checked) {
        return w(new WCheckbox(checked));
    }

    @Override
    public dev.lemonclient.gui.widgets.input.WSlider slider(double value, double min, double max) {
        return w(new WSlider(value, min, max));
    }

    @Override
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return w(new WMeteorTextBox(text, placeholder, filter, renderer));
    }

    @Override
    public <T> dev.lemonclient.gui.widgets.input.WDropdown<T> dropdown(T[] values, T value) {
        return w(new WDropdown<>(values, value));
    }

    @Override
    public dev.lemonclient.gui.widgets.pressable.WTriangle triangle() {
        return w(new WTriangle());
    }

    @Override
    public dev.lemonclient.gui.widgets.WTooltip tooltip(String text) {
        return w(new WTooltip(text));
    }

    @Override
    public dev.lemonclient.gui.widgets.containers.WView view() {
        return w(new WView());
    }

    @Override
    public dev.lemonclient.gui.widgets.containers.WSection section(String title, boolean expanded, WWidget headerWidget) {
        return w(new WSection(title, expanded, headerWidget));
    }

    @Override
    public dev.lemonclient.gui.widgets.WAccount account(WidgetScreen screen, Account<?> account) {
        return w(new WAccount(screen, account));
    }

    @Override
    public WWidget module(Module module) {
        return w(new WModule(module));
    }

    @Override
    public dev.lemonclient.gui.widgets.WQuad quad(Color color) {
        return w(new WQuad(color));
    }

    @Override
    public dev.lemonclient.gui.widgets.WTopBar topBar() {
        return w(new WTopBar());
    }

    @Override
    public dev.lemonclient.gui.widgets.pressable.WFavorite favorite(boolean checked) {
        return w(new WFavorite(checked));
    }

    // Colors

    @Override
    public Color textColor() {
        return textColor.get();
    }

    @Override
    public Color textSecondaryColor() {
        return textSecondaryColor.get();
    }

    //     Starscript

    @Override
    public Color starscriptTextColor() {
        return starscriptText.get();
    }

    @Override
    public Color starscriptBraceColor() {
        return starscriptBraces.get();
    }

    @Override
    public Color starscriptParenthesisColor() {
        return starscriptParenthesis.get();
    }

    @Override
    public Color starscriptDotColor() {
        return starscriptDots.get();
    }

    @Override
    public Color starscriptCommaColor() {
        return starscriptCommas.get();
    }

    @Override
    public Color starscriptOperatorColor() {
        return starscriptOperators.get();
    }

    @Override
    public Color starscriptStringColor() {
        return starscriptStrings.get();
    }

    @Override
    public Color starscriptNumberColor() {
        return starscriptNumbers.get();
    }

    @Override
    public Color starscriptKeywordColor() {
        return starscriptKeywords.get();
    }

    @Override
    public Color starscriptAccessedObjectColor() {
        return starscriptAccessedObjects.get();
    }

    // Other

    @Override
    public TextRenderer textRenderer() {
        return TextRenderer.get();
    }

    @Override
    public double scale(double value) {
        return value * scale.get();
    }

    @Override
    public boolean categoryIcons() {
        return categoryIcons.get();
    }

    @Override
    public boolean hideHUD() {
        return hideHUD.get();
    }

    public int roundAmount() {
        return round.get();
    }

    public class ThreeStateColorSetting {
        private final Setting<SettingColor> normal, hovered, pressed;

        public ThreeStateColorSetting(SettingGroup group, String name, SettingColor c1, SettingColor c2, SettingColor c3) {
            normal = color(group, name, "Color of " + name + ".", c1);
            hovered = color(group, "hovered-" + name, "Color of " + name + " when hovered.", c2);
            pressed = color(group, "pressed-" + name, "Color of " + name + " when pressed.", c3);
        }

        public SettingColor get() {
            return normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
            if (pressed) return this.pressed.get();
            return (hovered && (bypassDisableHoverColor || !disableHoverColor)) ? this.hovered.get() : this.normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered) {
            return get(pressed, hovered, false);
        }
    }
}
