package dev.lemonclient.systems.modules;

import dev.lemonclient.LemonClient;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.enums.SwingState;
import dev.lemonclient.enums.SwingType;
import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.mixininterface.IChatHud;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.ISerializable;
import dev.lemonclient.utils.misc.Keybind;
import dev.lemonclient.utils.player.ChatUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.RotationPriorities;
import dev.lemonclient.utils.render.ToastSystem;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.BlockUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4d;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;

import java.util.Objects;

public abstract class Module implements ISerializable<Module>, Comparable<Module> {
    protected final MinecraftClient mc;

    public final Category category;
    public final String name;
    public final String title;
    public final String description;
    public final Color color;
    public final int priority;

    public final Settings settings = new Settings();

    private boolean active;
    private boolean toggleMessage = true;
    private boolean toggleToast = false;
    public boolean serialize = true;
    public boolean runInMainMenu = false;
    public boolean autoSubscribe = true;

    public final Keybind keybind = Keybind.none();
    public boolean toggleOnBindRelease = false;
    public boolean favorite = false;
    public final String COLOR = "Color is the visual perception of different wavelengths of light as hue, saturation, and brightness.";

    public Vector4d guiVector = new Vector4d(-1, 0, 0, 0);

    public Module(Category category, String name, String description) {
        this.mc = MinecraftClient.getInstance();
        this.category = category;
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.color = Color.fromHsv(Utils.random(0.0, 360.0), 0.35, 1);
        this.priority = RotationPriorities.get(this);
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }

    @UltraLock
    public void onActivate() {

    }

    @UltraLock
    public void onDeactivate() {

    }

    @UltraLock
    public void onRender2D(DrawContext event) {
    }

    @UltraLock
    public void toggle() {
        if (!active) {
            active = true;
            Modules.get().addActive(this);

            settings.onActivated();

            if (runInMainMenu || Utils.canUpdate()) {
                if (autoSubscribe) LemonClient.EVENT_BUS.subscribe(this);
                onActivate();
            }
        } else {
            if (runInMainMenu || Utils.canUpdate()) {
                if (autoSubscribe) LemonClient.EVENT_BUS.unsubscribe(this);
                onDeactivate();
            }

            active = false;
            Modules.get().removeActive(this);
        }
    }

    public void forceToggle(boolean toggle) {
        active = toggle;

        if (toggle) {
            Modules.get().addActive(this);

            settings.onActivated();

            if (runInMainMenu || Utils.canUpdate()) {
                if (autoSubscribe) LemonClient.EVENT_BUS.subscribe(this);
                onActivate();
            }
        } else {
            if (runInMainMenu || Utils.canUpdate()) {
                if (autoSubscribe) LemonClient.EVENT_BUS.unsubscribe(this);
                onDeactivate();
            }

            Modules.get().removeActive(this);
        }
    }

    public void sendToggledMsg(Module module) {
        if (Config.get().chatFeedback.get() && isMessageEnabled() && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = ChatUtils.PREFIX + " " + Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + module.title + Formatting.GRAY + "]" + Formatting.WHITE + " toggled" + (isActive() ? Formatting.GREEN + " ON" : Formatting.RED + " OFF");
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendToggledMsg(String message) {
        if (Config.get().chatFeedback.get() && isMessageEnabled() && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = ChatUtils.PREFIX + " " + Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + name + Formatting.GRAY + "]" + Formatting.WHITE + " toggled" + (isActive() ? Formatting.GREEN + " ON " : Formatting.RED + " OFF ") + Formatting.GRAY + message;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendToggledToast(Module module) {
        if (!module.isToastEnabled()) return;
        mc.getToastManager().add(new ToastSystem(module.category.icon, module.category.color, title, null, Formatting.GRAY + "Toggled " + (module.active ? Formatting.GREEN + "ON" : Formatting.RED + "OFF") + Formatting.GRAY + ".", Config.get().toastDuration.get()));
    }

    public void sendDisableMsg(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = ChatUtils.PREFIX + " " + Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + title + Formatting.GRAY + "]" + Formatting.WHITE + " toggled" + Formatting.RED + " OFF " + Formatting.GRAY + text;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void debug(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = ChatUtils.PREFIX + " " + Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + title + Formatting.GRAY + "]" + " " + Formatting.AQUA + text;
            sendMessage(Text.of(msg), 0);
        }
    }

    public void sendMessage(Text text, int id) {
        ((IChatHud) mc.inGameHud.getChatHud()).lemonclient$add(text, id);
    }

    //  Packets
    public void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(packet);
    }

    public void sendSequenced(SequencedPacketCreator packetCreator) {
        if (mc.interactionManager == null || mc.world == null || mc.getNetworkHandler() == null) return;

        PendingUpdateManager sequence = mc.world.getPendingUpdateManager().incrementSequence();
        Packet<?> packet = packetCreator.predict(sequence.getSequence());

        mc.getNetworkHandler().sendPacket(packet);

        sequence.close();
    }

    public boolean placeBlock(BlockPos blockPos, FindItemResult findItemResult, boolean checkEntities) {
        if (findItemResult.isOffhand()) {
            return place(blockPos, Hand.OFF_HAND, mc.player.getInventory().selectedSlot, checkEntities);
        }
        return place(blockPos, Hand.MAIN_HAND, findItemResult.slot(), checkEntities);
    }

    private boolean place(BlockPos blockPos, Hand hand, int slot, boolean checkEntities) {
        if (slot < 0 || slot > 8) return false;
        if (!BlockUtils.canPlace(blockPos, checkEntities)) return false;

        Vec3d hitPos = blockPos.toCenterPos();

        BlockPos neighbour;
        Direction side = BlockUtils.getPlaceSide(blockPos);

        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        placeBlock(hand, hitPos, side.getOpposite(), neighbour);

        return true;
    }

    public void placeBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        boolean inside = eyes.x > pos.getX() && eyes.x < pos.getX() + 1 && eyes.y > pos.getY() && eyes.y < pos.getY() + 1 && eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
        sendSequenced(s -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
    }

    public void interactBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        boolean inside = eyes.x > pos.getX() && eyes.x < pos.getX() + 1 && eyes.y > pos.getY() && eyes.y < pos.getY() + 1 && eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        SettingUtils.swing(SwingState.Pre, SwingType.Interact, hand);
        sendSequenced(s -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Interact, hand);
    }

    public void useItem(Hand hand) {
        SettingUtils.swing(SwingState.Pre, SwingType.Using, hand);
        sendSequenced(s -> new PlayerInteractItemC2SPacket(hand, s));
        SettingUtils.swing(SwingState.Post, SwingType.Using, hand);
    }

    public void clientSwing(SwingHand swingHand, Hand realHand) {
        Hand hand = switch (swingHand) {
            case MainHand -> Hand.MAIN_HAND;
            case OffHand -> Hand.OFF_HAND;
            case RealHand -> realHand;
        };

        mc.player.swingHand(hand, true);
    }

    public void serverSwing(SwingHand swingHand, Hand realHand) {
        Hand hand = switch (swingHand) {
            case MainHand -> Hand.MAIN_HAND;
            case OffHand -> Hand.OFF_HAND;
            case RealHand -> realHand;
        };

        mc.player.swingHand(hand);
    }

    protected <T extends Enum<?>> Setting<T> enumSetting(SettingGroup group, String name, String desc, T defVal, IVisible visible) {
        return group.add(new EnumSetting.Builder<T>().name(name).description(desc).defaultValue(defVal).visible(visible).build());
    }

    protected Setting<Double> doubleSetting(SettingGroup group, String name, String desc, double defVal, double min, double max, IVisible visible) {
        return group.add(new DoubleSetting.Builder().name(name).defaultValue(defVal).description(desc).visible(visible).sliderRange(min, max).build());
    }

    protected Setting<Boolean> boolSetting(SettingGroup group, String name, String desc, boolean defVal, IVisible visible) {
        return group.add(new BoolSetting.Builder().name(name).description(desc).visible(visible).defaultValue(defVal).build());
    }

    protected Setting<Integer> intSetting(SettingGroup group, String name, String description, int defVal, int min, int max, IVisible visible) {
        return group.add(new IntSetting.Builder().name(name).defaultValue(defVal).description(description).visible(visible).sliderRange(min, max).build());
    }

    protected <T extends Enum<?>> Setting<T> enumSetting(SettingGroup group, String name, String desc, T defVal) {
        return this.enumSetting(group, name, desc, defVal, null);
    }

    protected Setting<Double> doubleSetting(SettingGroup group, String name, String desc, double defVal, double min, double max) {
        return this.doubleSetting(group, name, desc, defVal, min, max, null);
    }

    protected Setting<Boolean> boolSetting(SettingGroup group, String name, String desc, boolean defVal) {
        return this.boolSetting(group, name, desc, defVal, null);
    }

    protected Setting<Integer> intSetting(SettingGroup group, String name, String desc, int defVal, int min, int max) {
        return this.intSetting(group, name, desc, defVal, min, max, null);
    }

    protected <T extends Enum<?>> Setting<T> enumSetting(SettingGroup group, String name, T defVal, IVisible visible) {
        return this.enumSetting(group, name, ".", defVal, visible);
    }

    protected Setting<Double> doubleSetting(SettingGroup group, String name, double defVal, double min, double max, IVisible visible) {
        return this.doubleSetting(group, name, ".", defVal, min, max, visible);
    }

    protected Setting<Boolean> boolSetting(SettingGroup group, String name, boolean defVal, IVisible visible) {
        return this.boolSetting(group, name, ".", defVal, visible);
    }

    protected Setting<Integer> intSetting(SettingGroup group, String name, int defVal, int min, int max, IVisible visible) {
        return this.intSetting(group, name, ".", defVal, min, max, visible);
    }

    protected <T extends Enum<?>> Setting<T> enumSetting(SettingGroup group, String name, T defVal) {
        return this.enumSetting(group, name, ".", defVal);
    }

    protected Setting<Double> doubleSetting(SettingGroup group, String name, double defVal, double min, double max) {
        return this.doubleSetting(group, name, ".", defVal, min, max);
    }

    protected Setting<Boolean> boolSetting(SettingGroup group, String name, boolean defVal) {
        return this.boolSetting(group, name, ".", defVal);
    }

    protected Setting<Integer> intSetting(SettingGroup group, String name, int defVal, int min, int max) {
        return this.intSetting(group, name, ".", defVal, min, max);
    }

    protected Setting<String> stringSetting(SettingGroup group, String name, String description, String defaultValue, IVisible visible) {
        return group.add(new StringSetting.Builder().name(name).description(description).defaultValue(defaultValue).visible(visible).build());
    }

    protected Setting<String> stringSetting(SettingGroup group, String name, String description, String defaultValue) {
        return stringSetting(group, name, defaultValue, defaultValue, null);
    }

    protected Setting<String> stringSetting(SettingGroup group, String name, String defaultValue, IVisible visible) {
        return stringSetting(group, name, ".", defaultValue, visible);
    }

    protected Setting<String> stringSetting(SettingGroup group, String name, String defaultValue) {
        return stringSetting(group, name, ".", defaultValue, null);
    }

    protected Setting<SettingColor> colorSetting(SettingGroup group, String name, String description, Color defVal, IVisible visible) {
        return group.add(new ColorSetting.Builder().name(name).description(description).defaultValue(defVal).visible(visible).build());
    }

    protected Setting<SettingColor> colorSetting(SettingGroup group, String name, Color defVal, IVisible visible) {
        return colorSetting(group, name, ".", defVal, visible);
    }

    protected Setting<SettingColor> colorSetting(SettingGroup group, String name, String description, Color defVal) {
        return colorSetting(group, name, description, defVal, null);
    }

    protected Setting<SettingColor> colorSetting(SettingGroup group, String name, Color defVal) {
        return colorSetting(group, name, ".", defVal);
    }

    public void info(Text message) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.sendMsg(title, message);
    }

    public void info(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.infoPrefix(title, message, args);
    }

    public void warning(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.warningPrefix(title, message, args);
    }

    public void error(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.errorPrefix(title, message, args);
    }

    public void setToggleMessage(boolean toggleMessage) {
        this.toggleMessage = toggleMessage;
    }

    public boolean isMessageEnabled() {
        return toggleMessage;
    }

    public void setToggleToast(boolean toggleToast) {
        this.toggleToast = toggleToast;
    }

    public boolean isToastEnabled() {
        return toggleToast;
    }

    public boolean isActive() {
        return active;
    }

    @UltraLock
    public String getInfoString() {
        return null;
    }

    @Override
    public NbtCompound toTag() {
        if (!serialize) return null;
        NbtCompound tag = new NbtCompound();

        tag.putString("name", name);
        tag.put("keybind", keybind.toTag());
        tag.putBoolean("toggleOnKeyRelease", toggleOnBindRelease);
        tag.put("settings", settings.toTag());

        tag.putBoolean("toggleMessage", toggleMessage);
        tag.putBoolean("toggleToast", toggleToast);
        tag.putBoolean("favorite", favorite);
        tag.putBoolean("active", active);

        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        // General
        if (tag.contains("key")) keybind.set(true, tag.getInt("key"));
        else keybind.fromTag(tag.getCompound("keybind"));

        toggleOnBindRelease = tag.getBoolean("toggleOnKeyRelease");

        // Settings
        NbtElement settingsTag = tag.get("settings");
        if (settingsTag instanceof NbtCompound) settings.fromTag((NbtCompound) settingsTag);

        toggleMessage = tag.getBoolean("toggleMessage");
        toggleToast = tag.getBoolean("toggleToast");
        favorite = tag.getBoolean("favorite");
        boolean active = tag.getBoolean("active");
        if (active != isActive()) toggle();

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(name, module.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(@NotNull Module o) {
        return name.compareTo(o.name);
    }
}
