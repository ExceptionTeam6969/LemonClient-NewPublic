package dev.lemonclient.systems;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.game.GameLeftEvent;
import dev.lemonclient.systems.accounts.Accounts;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.macros.Macros;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.profiles.Profiles;
import dev.lemonclient.systems.proxies.Proxies;
import dev.lemonclient.systems.waypoints.Waypoints;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import meteordevelopment.orbit.EventHandler;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Systems {
    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends System>, System<?>> systems = new Reference2ReferenceOpenHashMap<>();
    private static final List<Runnable> preLoadTasks = new ArrayList<>(1);

    public static void addPreLoadTask(Runnable task) {
        preLoadTasks.add(task);
    }

    @UltraLock
    public static void init() {
        Config config = new Config();
        System<?> configSystem = add(config);
        configSystem.init();
        configSystem.load();

        // Registers the colors from config tab. This allows rainbow colours to work for friends.
        config.settings.registerColorSettings(null);

        add(new Modules());
        add(new Macros());
        add(new Friends());
        add(new Accounts());
        add(new Waypoints());
        add(new Profiles());
        add(new Proxies());
        add(new Hud());

        LemonClient.EVENT_BUS.subscribe(Systems.class);
    }

    @UltraLock
    private static System<?> add(System<?> system) {
        systems.put(system.getClass(), system);
        LemonClient.EVENT_BUS.subscribe(system);
        system.init();

        return system;
    }

    // save/load

    @EventHandler
    private static void onGameLeft(GameLeftEvent event) {
        save();
    }

    @UltraLock
    public static void save(File folder) {
        long start = java.lang.System.currentTimeMillis();
        LemonClient.LOG.info("Saving");

        for (System<?> system : systems.values()) system.save(folder);

        LemonClient.LOG.info("Saved in {} milliseconds.", java.lang.System.currentTimeMillis() - start);
    }

    @UltraLock
    public static void save() {
        save(null);
    }

    @UltraLock
    public static void load(File folder) {
        long start = java.lang.System.currentTimeMillis();
        LemonClient.LOG.info("Loading");

        for (Runnable task : preLoadTasks) task.run();
        for (System<?> system : systems.values()) system.load(folder);

        LemonClient.LOG.info("Loaded in {} milliseconds", java.lang.System.currentTimeMillis() - start);
    }

    @UltraLock
    public static void load() {
        load(null);
    }

    @UltraLock
    @SuppressWarnings("unchecked")
    public static <T extends System<?>> T get(Class<T> klass) {
        return (T) systems.get(klass);
    }
}
