package dev.lemonclient.systems.modules;

import com.google.common.collect.Ordering;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.client.ActiveModulesChangedEvent;
import dev.lemonclient.events.client.KeyEvent;
import dev.lemonclient.events.client.ModuleBindChangedEvent;
import dev.lemonclient.events.client.MouseButtonEvent;
import dev.lemonclient.events.game.GameJoinedEvent;
import dev.lemonclient.events.game.GameLeftEvent;
import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.pathing.BaritoneUtils;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.System;
import dev.lemonclient.systems.Systems;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.hud.elements.ToastNotificationsHud;
import dev.lemonclient.systems.modules.chat.*;
import dev.lemonclient.systems.modules.client.*;
import dev.lemonclient.systems.modules.combat.*;
import dev.lemonclient.systems.modules.misc.*;
import dev.lemonclient.systems.modules.movement.*;
import dev.lemonclient.systems.modules.movement.elytrafly.ElytraFly;
import dev.lemonclient.systems.modules.movement.movementtimer.MovementTimer;
import dev.lemonclient.systems.modules.player.*;
import dev.lemonclient.systems.modules.render.*;
import dev.lemonclient.systems.modules.render.blockesp.BlockESP;
import dev.lemonclient.systems.modules.render.marker.Marker;
import dev.lemonclient.systems.modules.settings.*;
import dev.lemonclient.systems.modules.world.Timer;
import dev.lemonclient.systems.modules.world.*;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.Keybind;
import dev.lemonclient.utils.misc.ValueComparableMap;
import dev.lemonclient.utils.misc.input.Input;
import dev.lemonclient.utils.misc.input.KeyAction;
import dev.lemonclient.utils.render.ToastSystem;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static dev.lemonclient.LemonClient.mc;

public class Modules extends System<Modules> {
    public static final ModuleRegistry REGISTRY = new ModuleRegistry();

    private static final List<Category> CATEGORIES = new ArrayList<>();

    public final List<Module> modules = new ArrayList<>();
    private final Map<Class<? extends Module>, Module> moduleInstances = new Reference2ReferenceOpenHashMap<>();
    private final Map<Category, List<Module>> groups = new Reference2ReferenceOpenHashMap<>();

    private final List<Module> active = new ArrayList<>();
    private Module moduleToBind;

    public Modules() {
        super("modules");
    }

    @UltraLock
    public static Modules get() {
        return Systems.get(Modules.class);
    }

    @UltraLock
    @Override
    public void init() {
        initCombat();
        initPlayer();
        initMovement();
        initRender();
        initWorld();
        initMisc();
        initChat();
        initSettings();
        initClient();
    }

    @Override
    public void load(File folder) {
        for (Module module : modules) {
            for (SettingGroup group : module.settings) {
                for (Setting<?> setting : group) setting.reset();
            }
        }

        super.load(folder);
    }

    @UltraLock
    public void sortModules() {
        for (List<Module> modules : groups.values()) {
            modules.sort(Comparator.comparing(o -> o.title));
        }
        modules.sort(Comparator.comparing(o -> o.title));
    }

    @UltraLock
    public static void registerCategory(Category category) {
        if (!Categories.REGISTERING) {
            throw new RuntimeException("Modules.registerCategory - Cannot register category outside of onRegisterCategories callback.");
        }

        CATEGORIES.add(category);
    }

    public static Iterable<Category> loopCategories() {
        return CATEGORIES;
    }

    public static int getCategoryIndex(Iterable<Category> categories, Category category) {
        int index = 0;
        for (Category loopCategory : categories) {
            if (category.equals(loopCategory)) break;
            index++;
        }
        return index;
    }

    public static int getCategoryIndex(Category category) {
        return getCategoryIndex(loopCategories(), category);
    }

    public static Category getCategoryByHash(int hash) {
        for (Category category : CATEGORIES) {
            if (category.hashCode() == hash) return category;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> klass) {
        return (T) moduleInstances.get(klass);
    }

    public Module get(String name) {
        for (Module module : moduleInstances.values()) {
            if (module.name.equalsIgnoreCase(name)) {
                return module;
            } else if (module.name.equalsIgnoreCase(Utils.nameToTitle(name))) {
                return module;
            }
        }

        return null;
    }

    public boolean isActive(Class<? extends Module> klass) {
        Module module = get(klass);
        return module != null && module.isActive();
    }

    @UltraLock
    public List<Module> getModulesByCategory(Category category) {
        return groups.computeIfAbsent(category, category1 -> new ArrayList<>());
    }

    public Collection<Module> getAll() {
        return moduleInstances.values();
    }

    public List<Module> getList() {
        return modules;
    }

    public int getCount() {
        return moduleInstances.values().size();
    }

    public List<Module> getActive() {
        synchronized (active) {
            return active;
        }
    }

    public Set<Module> searchTitles(String text) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Ordering.natural());

        for (Module module : this.moduleInstances.values()) {
            int score = Utils.searchLevenshteinDefault(module.title, text, false);
            modules.put(module, modules.getOrDefault(module, 0) + score);
        }

        return modules.keySet();
    }

    public Set<Module> searchSettingTitles(String text) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Ordering.natural());

        for (Module module : this.moduleInstances.values()) {
            int lowest = Integer.MAX_VALUE;
            for (SettingGroup sg : module.settings) {
                for (Setting<?> setting : sg) {
                    int score = Utils.searchLevenshteinDefault(setting.title, text, false);
                    if (score < lowest) lowest = score;
                }
            }
            modules.put(module, modules.getOrDefault(module, 0) + lowest);
        }

        return modules.keySet();
    }

    @UltraLock
    void addActive(Module module) {
        synchronized (active) {
            if (!active.contains(module)) {
                active.add(module);
                LemonClient.EVENT_BUS.post(ActiveModulesChangedEvent.INSTANCE);

                ToggleSettings toggleSettings = Modules.get().get(ToggleSettings.class);

                if (toggleSettings.active.get())
                    Managers.SOUND.enableSound.play(toggleSettings.volume.get().floatValue());

                if (ToastNotificationsHud.INSTANCE != null && ToastNotificationsHud.INSTANCE.toggleMessage.get() && ToastNotificationsHud.INSTANCE.toggleList.get().contains(module)) {
                    ToastNotificationsHud.addToggled(module, " ON!");
                }

                Managers.NOTIFICATION.info(module.title, module.title + " ON!");
            }
        }
    }

    @UltraLock
    void removeActive(Module module) {
        synchronized (active) {
            if (active.remove(module)) {
                LemonClient.EVENT_BUS.post(ActiveModulesChangedEvent.INSTANCE);

                ToggleSettings toggleSettings = Modules.get().get(ToggleSettings.class);

                if (toggleSettings.disable.get())
                    Managers.SOUND.disableSound.play(toggleSettings.volume.get().floatValue());

                if (ToastNotificationsHud.INSTANCE != null && ToastNotificationsHud.INSTANCE.toggleMessage.get() && ToastNotificationsHud.INSTANCE.toggleList.get().contains(module)) {
                    ToastNotificationsHud.addToggled(module, " OFF!");
                }

                Managers.NOTIFICATION.info(module.title, module.title + " OFF!");
            }
        }
    }

    // Binding

    @UltraLock
    public void setModuleToBind(Module moduleToBind) {
        this.moduleToBind = moduleToBind;
    }

    @UltraLock
    public boolean isBinding() {
        return moduleToBind != null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onKeyBinding(KeyEvent event) {
        if (event.action == KeyAction.Press && onBinding(true, event.key)) event.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onButtonBinding(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && onBinding(false, event.button)) event.cancel();
    }

    private boolean onBinding(boolean isKey, int value) {
        if (!isBinding()) return false;

        if (moduleToBind.keybind.canBindTo(isKey, value)) {
            moduleToBind.keybind.set(isKey, value);
            moduleToBind.info("Bound to (highlight)%s(default).", moduleToBind.keybind);
            mc.getToastManager().add(new ToastSystem(moduleToBind.category.icon, moduleToBind.category.color, moduleToBind.title, null, Formatting.GRAY + "Bound to " + Formatting.WHITE + moduleToBind.keybind + Formatting.GRAY + ".", Config.get().toastDuration.get()));
        } else if (value == GLFW.GLFW_KEY_ESCAPE) {
            moduleToBind.keybind.set(Keybind.none());
            moduleToBind.info("Removed bind.");
        } else return false;

        LemonClient.EVENT_BUS.post(ModuleBindChangedEvent.get(moduleToBind));
        moduleToBind = null;

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Repeat) return;
        onAction(true, event.key, event.action == KeyAction.Press);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Repeat) return;
        onAction(false, event.button, event.action == KeyAction.Press);
    }

    private void onAction(boolean isKey, int value, boolean isPress) {
        if (mc.currentScreen == null && !Input.isKeyPressed(GLFW.GLFW_KEY_F3)) {
            for (Module module : moduleInstances.values()) {
                if (module.keybind.matches(isKey, value) && (isPress || module.toggleOnBindRelease)) {
                    module.toggle();
                    module.sendToggledMsg(module);
                    module.sendToggledToast(module);
                }
            }
        }
    }

    // End of binding

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) return;

        for (Module module : moduleInstances.values()) {
            if (module.toggleOnBindRelease && module.isActive()) {
                module.toggle();
                module.sendToggledMsg(module);
                module.sendToggledToast(module);
            }
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive() && !module.runInMainMenu) {
                    LemonClient.EVENT_BUS.subscribe(module);
                    module.onActivate();
                }
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive() && !module.runInMainMenu) {
                    LemonClient.EVENT_BUS.unsubscribe(module);
                    module.onDeactivate();
                }
            }
        }
    }

    public void disableAll() {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive()) module.toggle();
            }
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        NbtList modulesTag = new NbtList();
        for (Module module : getAll()) {
            NbtCompound moduleTag = module.toTag();
            if (moduleTag != null) modulesTag.add(moduleTag);
        }
        tag.put("modules", modulesTag);

        return tag;
    }

    @Override
    public Modules fromTag(NbtCompound tag) {
        disableAll();

        NbtList modulesTag = tag.getList("modules", 10);
        for (NbtElement moduleTagI : modulesTag) {
            NbtCompound moduleTag = (NbtCompound) moduleTagI;
            Module module = get(moduleTag.getString("name"));
            if (module != null) module.fromTag(moduleTag);
        }

        return this;
    }

    // INIT MODULES

    public void add(Module module) {
        // Check if the module's category is registered
        if (!CATEGORIES.contains(module.category)) {
            throw new RuntimeException("Modules.addModule - Module's category was not registered.");
        }

        // Remove the previous module with the same name
        AtomicReference<Module> removedModule = new AtomicReference<>();
        if (moduleInstances.values().removeIf(module1 -> {
            if (module1.name.equals(module.name)) {
                removedModule.set(module1);
                module1.settings.unregisterColorSettings();

                return true;
            }

            return false;
        })) {
            getModulesByCategory(removedModule.get().category).remove(removedModule.get());
        }

        // Add the module
        moduleInstances.put(module.getClass(), module);
        modules.add(module);
        getModulesByCategory(module.category).add(module);

        // Register color settings for the module
        module.settings.registerColorSettings(module);
    }

    @UltraLock
    private void initCombat() {
        add(new AimAssist());
        add(new AntiAim());
        add(new ArrowDodge());
        add(new AutoAnchor());
        add(new AutoAnvil());
        add(new AutoArmor());
        add(new AutoCity());
        add(new AutoCrystal());
        add(new AutoCrystalPlus());
        add(new AutoHoleFill());
        add(new AutoHoleFillPlus());
        add(new AutoPearlClip());
        add(new AutoTotem());
        add(new AutoTrap());
        add(new AutoWeapon());
        add(new AutoWeb());
        add(new BedBombV2());
        add(new BedBombV4());
        add(new BowAimbot());
        add(new BowBomb());
        add(new BowSpam());
        add(new Burrow());
        add(new CevBreaker());
        add(new CityMiner());
        add(new Criticals());
        add(new Hitboxes());
        add(new HolePush());
        add(new InfiniteAura());
        add(new KillAura());
        add(new Offhand());
        add(new PistonCrystal());
        add(new Quiver());
        add(new SelfAnvil());
        add(new SelfProtect());
        add(new SelfTrap());
        add(new SelfWeb());
        add(new Surround());
        add(new SurroundPlus());
        add(new TNTAura());
    }

    @UltraLock
    private void initPlayer() {
        add(new AntiHunger());
        add(new AutoClicker());
        add(new AutoCraft());
        add(new AutoDrop());
        add(new AutoEat());
        add(new AutoFish());
        add(new AutoGap());
        add(new AutoMend());
        add(new AutoMine());
        add(new AutoReplenish());
        add(new AutoTool());
        add(new BedCrafter());
        add(new BreakDelay());
        add(new ChestSwap());
        add(new FastUse());
        add(new GhostHand());
        add(new LiquidInteract());
        add(new MultiTask());
        add(new NoInteract());
        add(new NoMiningTrace());
        add(new NoRotate());
        add(new PacketEat());
        add(new Portals());
        add(new PotionSaver());
        add(new PotionSpoof());
        add(new Reach());
        add(new Rotation());
        add(new ShieldBypass());
        add(new ShulkerPlacer());
        add(new ShulkerReplenish());
        add(new SpeedMine());
        add(new Twerk());
        add(new XPThrower());
        add(new HandSync());
    }

    @UltraLock
    private void initMovement() {
        add(new AirJump());
        add(new Anchor());
        add(new AntiAFK());
        add(new AntiCrawl());
        add(new AntiVoid());
        add(new AutoJump());
        add(new AutoWalk());
        add(new Blink());
        add(new BoatFly());
        add(new BurrowMovement());
        add(new ClickTP());
        add(new EFlyBypass());
        add(new ElytraBoost());
        add(new ElytraFlyPlus());
        add(new ElytraFly());
        add(new EntityControl());
        add(new EntitySpeed());
        add(new FastClimb());
        add(new Flight());
        add(new FlightPlus());
        add(new Freeze());
        add(new GuiMove());
        add(new HighJump());
        add(new HoleSnap());
        add(new Jesus());
        add(new LongJump());
        add(new MovementTimer());
        add(new NoFall());
        add(new NoJumpDelay());
        add(new NoSlow());
        add(new OldAnvil());
        add(new PacketFly());
        add(new Parkour());
        add(new ReverseStep());
        add(new SafeWalk());
        add(new Scaffold());
        add(new Slippy());
        add(new Sneak());
        add(new Spider());
        add(new Sprint());
        add(new Step());
        add(new StepPlus());
        add(new Strafe());
        add(new StrafePlus());
        add(new TickShift());
        add(new TridentBoost());
        add(new Velocity());
    }

    @UltraLock
    private void initRender() {
        add(new Ambience());
        add(new Animations());
        add(new AspectRatio());
        add(new AttackIndicator());
        add(new BetterBeacons());
        add(new BetterTab());
        add(new BetterTooltips());
        add(new BlockESP());
        add(new BlockSelection());
        add(new Blur());
        add(new BossStack());
        add(new Breadcrumbs());
        add(new BreakESP());
        add(new BreakIndicators());
        add(new BurrowESP());
        add(new CameraTweaks());
        add(new Chams());
        add(new CityESP());
        add(new CustomWeather());
        add(new CrystalAnimation());
        add(new CrystalChams());
        add(new CustomFOV());
        add(new EntityOwner());
        add(new EntityRenders());
        add(new ESP());
        add(new FogRenderer());
        add(new ForceSneak());
        add(new Freecam());
        add(new FreeLook());
        add(new Fullbright());
        add(new HandTweaks());
        add(new HandView());
        add(new HitParticles());
        add(new HoleESP());
        add(new InvRenderer());
        add(new ItemHighlight());
        add(new ItemPhysics());
        add(new JumpAnimation());
        add(new KillEffects());
        add(new LightOverlay());
        add(new LogoutSpots());
        add(new Lootbeams());
        add(new ModelRender());
        add(new MotionBlur());
        add(new Marker());
        add(new Nametags());
        add(new NoBlockEntities());
        add(new NoRender());
        add(new OldHitting());
        add(new Particles());
        add(new PopChams());
        add(new Shaders());
        add(new SkinBlinker());
        add(new StorageESP());
        add(new SwingAnimation());
        add(new TargetStrafe());
        add(new TimeChanger());
        add(new Tracers());
        add(new Trail());
        add(new Trajectories());
        add(new TunnelESP());
        add(new UnfocusedCPU());
        add(new ViewModel());
        add(new VoidESP());
        add(new WallHack());
        add(new WaypointsModule());
        add(new Xray());
        add(new Zoom());
    }

    @UltraLock
    private void initWorld() {
        add(new AirPlace());
        add(new Ambience());
        add(new AutoBreed());
        add(new AutoBrewer());
        add(new AutoMount());
        add(new AutoNametag());
        add(new AutoShearer());
        add(new AutoSign());
        add(new AutoSmelter());
        add(new AutoWither());
        add(new BuildHeight());
        add(new Collisions());
        add(new EChestFarmer());
        add(new EndermanLook());
        add(new Flamethrower());
        add(new LiquidFiller());
        add(new MountBypass());
        add(new NoGhostBlocks());
        add(new Nuker());
        add(new OreSim());
        add(new SpawnProofer());
        add(new StashFinder());
        add(new Timer());
        add(new VeinMiner());

        if (BaritoneUtils.IS_AVAILABLE) {
            add(new Excavator());
            add(new InfinityMiner());
            add(new LitematicaPrinter());
        }
    }

    @UltraLock
    private void initMisc() {
        add(new AntiPacketKick());
        add(new AutoLog());
        add(new AutoReconnect());
        add(new AutoRespawn());
        add(new BookBot());
        add(new InvTweaks());
        add(new MidClickExtra());
        add(new MidClickFriend());
        add(new NameProtect());
        add(new Notebot());
        add(new Notifier());
        add(new PacketCanceller());
        add(new ShulkerDupe());
        add(new SoundBlocker());
        add(new SoundModifier());
        add(new Suicide());
        add(new VillagerRoller());
    }

    @UltraLock
    private void initChat() {
        add(new AutoEsu());
        add(new AutoEz());
        add(new AutoLoadKit());
        add(new BetterChat());
        add(new Chat());
        add(new ChatCalc());
        add(new GroupChat());
        add(new MessageAura());
        add(new Spammer());
        add(new SuperSpammer());
    }

    @UltraLock
    private void initSettings() {
        add(new FacingSettings());
        add(new RangeSettings());
        add(new RaytraceSettings());
        add(new RotationPrioritySettings());
        add(new RotationSettings());
        add(new ServerSettings());
        add(new SwingSettings());
        add(new ToggleSettings());
    }

    @UltraLock
    private void initClient() {
        add(new BotNetModule());
        add(new Capes());
        add(new FakePlayer());
        add(new Notifications());
        add(new PingSpoof());
        add(new ServerSpoof());
    }

    public static class ModuleRegistry extends SimpleRegistry<Module> {
        public ModuleRegistry() {
            super(RegistryKey.ofRegistry(new Identifier(LemonClient.MOD_ID, "modules")), Lifecycle.stable());
        }

        @Override
        public int size() {
            return Modules.get().getAll().size();
        }

        @Override
        public Identifier getId(Module entry) {
            return null;
        }

        @Override
        public Optional<RegistryKey<Module>> getKey(Module entry) {
            return Optional.empty();
        }

        @Override
        public int getRawId(Module entry) {
            return 0;
        }

        @Override
        public Module get(RegistryKey<Module> key) {
            return null;
        }

        @Override
        public Module get(Identifier id) {
            return null;
        }

        @Override
        public Lifecycle getEntryLifecycle(Module object) {
            return null;
        }

        @Override
        public Lifecycle getLifecycle() {
            return null;
        }

        @Override
        public Set<Identifier> getIds() {
            return null;
        }

        @Override
        public boolean containsId(Identifier id) {
            return false;
        }

        @Nullable
        @Override
        public Module get(int index) {
            return null;
        }

        @Override
        public Iterator<Module> iterator() {
            return new ModuleIterator();
        }

        @Override
        public boolean contains(RegistryKey<Module> key) {
            return false;
        }

        @Override
        public Set<Map.Entry<RegistryKey<Module>, Module>> getEntrySet() {
            return null;
        }

        @Override
        public Set<RegistryKey<Module>> getKeys() {
            return null;
        }

        @Override
        public Optional<RegistryEntry.Reference<Module>> getRandom(Random random) {
            return Optional.empty();
        }

        @Override
        public Registry<Module> freeze() {
            return null;
        }

        @Override
        public RegistryEntry.Reference<Module> createEntry(Module value) {
            return null;
        }

        @Override
        public Optional<RegistryEntry.Reference<Module>> getEntry(int rawId) {
            return Optional.empty();
        }

        @Override
        public Optional<RegistryEntry.Reference<Module>> getEntry(RegistryKey<Module> key) {
            return Optional.empty();
        }

        @Override
        public Stream<RegistryEntry.Reference<Module>> streamEntries() {
            return null;
        }

        @Override
        public Optional<RegistryEntryList.Named<Module>> getEntryList(TagKey<Module> tag) {
            return Optional.empty();
        }

        @Override
        public RegistryEntryList.Named<Module> getOrCreateEntryList(TagKey<Module> tag) {
            return null;
        }

        @Override
        public Stream<Pair<TagKey<Module>, RegistryEntryList.Named<Module>>> streamTagsAndEntries() {
            return null;
        }

        @Override
        public Stream<TagKey<Module>> streamTags() {
            return null;
        }

        @Override
        public void clearTags() {
        }

        @Override
        public void populateTags(Map<TagKey<Module>, List<RegistryEntry<Module>>> tagEntries) {
        }

        private static class ModuleIterator implements Iterator<Module> {
            private final Iterator<Module> iterator = Modules.get().getAll().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Module next() {
                return iterator.next();
            }
        }
    }
}
