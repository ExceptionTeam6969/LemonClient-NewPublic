package dev.lemonclient.systems.hud;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.screens.HudEditorScreen;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.ISerializable;
import dev.lemonclient.utils.other.Snapper;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;

public abstract class HudElement implements Snapper.Element, ISerializable<HudElement> {
    public final HudElementInfo<?> info;
    private boolean active;

    public final Settings settings = new Settings();
    public final HudBox box = new HudBox(this);

    public boolean autoAnchors = true;
    public int x, y;

    public final String COLOR = "Color is the visual perception of different wavelengths of light as hue, saturation, and brightness.";

    public HudElement(HudElementInfo<?> info) {
        this.info = info;
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void toggle() {
        active = !active;
    }

    public void setSize(double width, double height) {
        box.setSize(width, height);
    }

    @Override
    public void setPos(int x, int y) {
        if (autoAnchors) {
            box.setPos(x, y);
            box.xAnchor = XAnchor.Left;
            box.yAnchor = YAnchor.Top;
            box.updateAnchors();
        } else {
            box.setPos(box.x + (x - this.x), box.y + (y - this.y));
        }

        updatePos();
    }

    @Override
    public void move(int deltaX, int deltaY) {
        box.move(deltaX, deltaY);
        updatePos();
    }

    public void updatePos() {
        x = box.getRenderX();
        y = box.getRenderY();
    }

    protected double alignX(double width, Alignment alignment) {
        return box.alignX(getWidth(), width, alignment);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return box.width;
    }

    @Override
    public int getHeight() {
        return box.height;
    }

    protected boolean isInEditor() {
        return !Utils.canUpdate() || HudEditorScreen.isOpen();
    }

    public void remove() {
        Hud.get().remove(this);
    }

    public void tick(HudRenderer renderer) {
    }

    public void render(HudRenderer renderer) {
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

    public void onFontChanged() {
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }

    // Serialization

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("name", info.name);
        tag.putBoolean("active", active);

        tag.put("settings", settings.toTag());
        tag.put("box", box.toTag());

        tag.putBoolean("autoAnchors", autoAnchors);

        return tag;
    }

    @Override
    public HudElement fromTag(NbtCompound tag) {
        settings.reset();

        active = tag.getBoolean("active");

        settings.fromTag(tag.getCompound("settings"));
        box.fromTag(tag.getCompound("box"));

        autoAnchors = tag.getBoolean("autoAnchors");

        x = box.getRenderX();
        y = box.getRenderY();

        return this;
    }
}
