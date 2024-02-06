package dev.lemonclient.systems.config;

import dev.lemonclient.LemonClient;
import dev.lemonclient.enums.MainMenuShaders;
import dev.lemonclient.renderer.Fonts;
import dev.lemonclient.renderer.text.FontFace;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.System;
import dev.lemonclient.systems.Systems;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

import static dev.lemonclient.LemonClient.mc;

public class Config extends System<Config> {
    public final Settings settings = new Settings();

    private final SettingGroup sgVisual = settings.createGroup("Visual");
    private final SettingGroup sgChat = settings.createGroup("Chat");
    private final SettingGroup sgToasts = settings.createGroup("Toasts");
    private final SettingGroup sgMisc = settings.createGroup("Misc");

    // Visual

    public final Setting<Boolean> customFont = sgVisual.add(new BoolSetting.Builder()
        .name("custom-font")
        .description("Use a custom font.")
        .defaultValue(true)
        .build()
    );
    public final Setting<FontFace> font = sgVisual.add(new FontFaceSetting.Builder()
        .name("font")
        .description("Custom font to use.")
        .visible(customFont::get)
        .onChanged(Fonts::load)
        .build()
    );
    public final Setting<Double> rainbowSpeed = sgVisual.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("The global rainbow speed.")
        .defaultValue(0.5)
        .range(0, 10)
        .sliderMax(5)
        .build()
    );
    public final Setting<Boolean> customSplashOverlay = sgVisual.add(new BoolSetting.Builder()
        .name("Custom Splash Overlay")
        .description("Show custom splash overlay.")
        .defaultValue(true)
        .build()
    );
    public final Setting<MainMenuShaders> titleScreenShaderType = sgVisual.add(new EnumSetting.Builder<MainMenuShaders>()
        .name("Title Screen Shader Type")
        .defaultValue(MainMenuShaders.None)
        .build()
    );
    public final Setting<Boolean> titleScreenSplashes = sgVisual.add(new BoolSetting.Builder()
        .name("Custom Title Screen Splashes")
        .description("Show custom splash texts on title screen.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> customWindowTitle = sgVisual.add(new BoolSetting.Builder()
        .name("Custom Window Title")
        .description("Show custom text in the window title.")
        .defaultValue(true)
        .onModuleActivated(setting -> mc.updateWindowTitle())
        .onChanged(value -> mc.updateWindowTitle())
        .build()
    );
    public final Setting<String> customWindowTitleText = sgVisual.add(new StringSetting.Builder()
        .name("Window Title Text")
        .description("The text it displays in the window title.")
        .visible(customWindowTitle::get)
        .defaultValue("Minecraft " + SharedConstants.getGameVersion().getName() + " - " + LemonClient.NAME + " " + LemonClient.VERSION)
        .onChanged(value -> mc.updateWindowTitle())
        .build()
    );
    public final Setting<SettingColor> friendColor = sgVisual.add(new ColorSetting.Builder()
        .name("friend-color")
        .description("The color used to show friends.")
        .defaultValue(new SettingColor(0, 255, 180))
        .build()
    );

    // Chat

    public final Setting<String> prefix = sgChat.add(new StringSetting.Builder()
        .name("prefix")
        .description("Prefix.")
        .defaultValue(".")
        .build()
    );
    public final Setting<Boolean> chatFeedback = sgChat.add(new BoolSetting.Builder()
        .name("chat-feedback")
        .description("Sends chat feedback when client performs certain actions.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> deleteChatFeedback = sgChat.add(new BoolSetting.Builder()
        .name("delete-chat-feedback")
        .description("Delete previous matching chat feedback to keep chat clear.")
        .visible(chatFeedback::get)
        .defaultValue(true)
        .build()
    );

    // Toasts
    public final Setting<Boolean> toastFeedback = sgToasts.add(new BoolSetting.Builder()
        .name("Toast Feedback")
        .description("Sends a toast feedback when LemonClient performs certain actions.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Integer> toastDuration = sgToasts.add(new IntSetting.Builder()
        .name("Duration")
        .description("Determines how long the toast will stay visible in milliseconds")
        .defaultValue(3000)
        .min(1)
        .sliderRange(1, 6000)
        .build()
    );
    public final Setting<Boolean> toastSound = sgToasts.add(new BoolSetting.Builder()
        .name("Sound")
        .description("Plays a sound when a toast appears.")
        .defaultValue(true)
        .build()
    );

    // Misc

    public final Setting<Integer> rotationHoldTicks = sgMisc.add(new IntSetting.Builder()
        .name("rotation-hold")
        .description("Hold long to hold server side rotation when not sending any packets.")
        .defaultValue(4)
        .build()
    );

    public final Setting<Boolean> useTeamColor = sgMisc.add(new BoolSetting.Builder()
        .name("use-team-color")
        .description("Uses player's team color for rendering things like esp and tracers.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> moduleSearchCount = sgMisc.add(new IntSetting.Builder()
        .name("module-search-count")
        .description("Amount of modules and settings to be shown in the module search bar.")
        .defaultValue(8)
        .min(1).sliderMax(12)
        .build()
    );

    public List<String> dontShowAgainPrompts = new ArrayList<>();

    public Config() {
        super("config");
    }

    public static Config get() {
        return Systems.get(Config.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("version", LemonClient.VERSION.toString());
        tag.put("settings", settings.toTag());
        tag.put("dontShowAgainPrompts", listToTag(dontShowAgainPrompts));

        return tag;
    }

    @Override
    public Config fromTag(NbtCompound tag) {
        if (tag.contains("settings")) settings.fromTag(tag.getCompound("settings"));
        if (tag.contains("dontShowAgainPrompts")) dontShowAgainPrompts = listFromTag(tag, "dontShowAgainPrompts");

        return this;
    }

    private NbtList listToTag(List<String> list) {
        NbtList nbt = new NbtList();
        for (String item : list) nbt.add(NbtString.of(item));
        return nbt;
    }

    private List<String> listFromTag(NbtCompound tag, String key) {
        List<String> list = new ArrayList<>();
        for (NbtElement item : tag.getList(key, 8)) list.add(item.asString());
        return list;
    }
}
