package dev.lemonclient.settings;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.widgets.containers.WContainer;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.misc.ISerializable;
import dev.lemonclient.utils.misc.NbtUtils;
import dev.lemonclient.utils.render.color.RainbowColors;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import top.fl0wowp4rty.phantomshield.annotations.license.VirtualizationLock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Settings implements ISerializable<Settings>, Iterable<SettingGroup> {
    private SettingGroup defaultGroup;
    public final List<SettingGroup> groups = new ArrayList<>(1);

    @VirtualizationLock
    public void onActivated() {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) {
                setting.onActivated();
            }
        }
    }

    @VirtualizationLock
    public Setting<?> get(String name) {
        for (SettingGroup sg : this) {
            for (Setting<?> setting : sg) {
                if (name.equalsIgnoreCase(setting.name)) return setting;
            }
        }

        return null;
    }

    @VirtualizationLock
    public void reset() {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) {
                setting.reset();
            }
        }
    }

    @VirtualizationLock
    public SettingGroup getGroup(String name) {
        for (SettingGroup sg : this) {
            if (sg.name.equals(name)) return sg;
        }

        return null;
    }

    @VirtualizationLock
    public int sizeGroups() {
        return groups.size();
    }

    @VirtualizationLock
    public SettingGroup getDefaultGroup() {
        if (defaultGroup == null) defaultGroup = createGroup("General");
        return defaultGroup;
    }

    @VirtualizationLock
    public SettingGroup createGroup(String name, boolean expanded) {
        SettingGroup group = new SettingGroup(name, expanded);
        groups.add(group);
        return group;
    }

    @VirtualizationLock
    public SettingGroup createGroup(String name) {
        return createGroup(name, true);
    }

    @VirtualizationLock
    public void registerColorSettings(Module module) {
        for (SettingGroup group : this) {
            for (Setting<?> setting : group) {
                setting.module = module;

                if (setting instanceof ColorSetting) {
                    RainbowColors.addSetting((Setting<SettingColor>) setting);
                } else if (setting instanceof ColorListSetting) {
                    RainbowColors.addSettingList((Setting<List<SettingColor>>) setting);
                }
            }
        }
    }

    @VirtualizationLock
    public void unregisterColorSettings() {
        for (SettingGroup group : this) {
            for (Setting<?> setting : group) {
                if (setting instanceof ColorSetting) {
                    RainbowColors.removeSetting((Setting<SettingColor>) setting);
                } else if (setting instanceof ColorListSetting) {
                    RainbowColors.removeSettingList((Setting<List<SettingColor>>) setting);
                }
            }
        }
    }

    @VirtualizationLock
    public void tick(WContainer settings, GuiTheme theme) {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) {
                boolean visible = setting.isVisible();

                if (visible != setting.lastWasVisible) {
                    settings.clear();
                    settings.add(theme.settings(this)).expandX();
                }

                setting.lastWasVisible = visible;
            }
        }
    }

    @VirtualizationLock
    @Override
    public Iterator<SettingGroup> iterator() {
        return groups.iterator();
    }

    @VirtualizationLock
    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("groups", NbtUtils.listToTag(groups));

        return tag;
    }

    @VirtualizationLock
    @Override
    public Settings fromTag(NbtCompound tag) {
        NbtList groupsTag = tag.getList("groups", 10);

        for (NbtElement t : groupsTag) {
            NbtCompound groupTag = (NbtCompound) t;

            SettingGroup sg = getGroup(groupTag.getString("name"));
            if (sg != null) sg.fromTag(groupTag);
        }

        return this;
    }
}
