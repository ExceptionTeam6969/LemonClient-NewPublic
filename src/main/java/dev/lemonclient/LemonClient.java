package dev.lemonclient;

import dev.lemonclient.events.client.KeyEvent;
import dev.lemonclient.events.client.MouseButtonEvent;
import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.gui.GuiThemes;
import dev.lemonclient.gui.WidgetScreen;
import dev.lemonclient.gui.tabs.Tabs;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.render.CanvasManager;
import dev.lemonclient.systems.Systems;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.chat.Chat;
import dev.lemonclient.systems.modules.client.Capes;
import dev.lemonclient.systems.modules.render.ItemPhysics;
import dev.lemonclient.systems.modules.render.Zoom;
import dev.lemonclient.utils.PostInit;
import dev.lemonclient.utils.PreInit;
import dev.lemonclient.utils.ReflectInit;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.Version;
import dev.lemonclient.utils.misc.input.KeyAction;
import dev.lemonclient.utils.misc.input.KeyBinds;
import dev.lemonclient.utils.render.color.Color;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.fl0wowp4rty.phantomshield.annotations.Native;
import top.fl0wowp4rty.phantomshield.annotations.license.MemoryCheck;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;
import top.fl0wowp4rty.phantomshield.annotations.license.VirtualizationLock;

import java.io.File;
import java.lang.invoke.MethodHandles;

@Native
public class LemonClient implements ClientModInitializer {
    public static final String MOD_ID = "lemon-client";
    public static final ModMetadata MOD_META;
    public static final String NAME;
    public static final Version VERSION;
    public static final String DEV_BUILD;

    public static LemonClient INSTANCE;

    public static MinecraftClient mc;
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final File FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();
    public static final File SOUNDS_FOLDER = new File(FOLDER, "sounds");
    public static final Logger LOG;

    public final Color MAIN_COLOR = new Color(255, 158, 0, 255);

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();

        NAME = "Lemon Client";
        LOG = LoggerFactory.getLogger(NAME);

        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.contains("-")) versionString = versionString.split("-")[0];

        // When building and running through IntelliJ and not Gradle it doesn't replace the version so just use a dummy
        if (versionString.equals("${version}")) versionString = "0.0.0";

        VERSION = new Version(versionString);
        DEV_BUILD = MOD_META.getCustomValue(LemonClient.MOD_ID + ":devbuild").getAsString();
    }

    @UltraLock
    @MemoryCheck
    @Override
    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }

        LOG.info("[LemonClient] Initializing...");

        //--------------------Global Minecraft Client Accessor--------------------//
        mc = MinecraftClient.getInstance();

        //--------------------Pre Load--------------------//
        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
            Systems.addPreLoadTask(() -> {
                Modules.get().get(Chat.class).toggle(); // CHAT
                Modules.get().get(Capes.class).toggle(); // CAPES
                Modules.get().get(ItemPhysics.class).toggle(); // ITEM PHYSICS

                // MESSAGES
                Modules.get().get(Zoom.class).setToggleMessage(false); // ZOOM
            });
        }

        //--------------------Register Event Handlers--------------------//
        EVENT_BUS.registerLambdaFactory("dev.lemonclient", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        //--------------------Register Init Classes--------------------//
        ReflectInit.add("dev.lemonclient");

        //--------------------Pre Init--------------------//
        ReflectInit.init(PreInit.class);

        //--------------------Register Module Categories--------------------//
        Categories.init();

        //--------------------Load Systems--------------------//
        Systems.init();

        //--------------------Subscribe After Systems Are Loaded--------------------//
        EVENT_BUS.subscribe(this);

        //--------------------Sort Modules--------------------//
        Modules.get().sortModules();

        //--------------------Load Configs--------------------//
        Systems.load();

        //--------------------Post Init--------------------//
        ReflectInit.init(PostInit.class);

        //--------------------Save On Shutdown--------------------//
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Systems.save();
            GuiThemes.save();
        }));

        //--------------------Managers--------------------//
        CanvasManager.init();
        Managers.PLAYER.init();
        Managers.MUSIC.init();

        LOG.info("[LemonClient] Loaded Successfully!");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.currentScreen == null && mc.getOverlay() == null && KeyBinds.OPEN_COMMANDS.wasPressed()) {
            mc.setScreen(new ChatScreen(Config.get().prefix.get()));
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesKey(event.key, 0)) {
            toggleGui();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesMouse(event.button)) {
            toggleGui();
        }
    }

    @VirtualizationLock
    private void toggleGui() {
        if (Utils.canCloseGui()) mc.currentScreen.close();
        else if (Utils.canOpenGui()) Tabs.get().get(0).openScreen(GuiThemes.get());
    }

    // Hide HUD

    private boolean wasWidgetScreen, wasHudHiddenRoot;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onOpenScreen(OpenScreenEvent event) {
        boolean hideHud = GuiThemes.get().hideHUD();

        if (hideHud) {
            if (!wasWidgetScreen) wasHudHiddenRoot = mc.options.hudHidden;

            if (event.screen instanceof WidgetScreen) mc.options.hudHidden = true;
            else if (!wasHudHiddenRoot) mc.options.hudHidden = false;
        }

        wasWidgetScreen = event.screen instanceof WidgetScreen;
    }
}
