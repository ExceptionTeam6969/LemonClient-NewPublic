package dev.lemonclient.systems.profiles;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.macros.Macros;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.waypoints.Waypoints;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Profile implements ISerializable<Profile> {
    public final Settings settings = new Settings();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSave = settings.createGroup("Save");

    public Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("name")
        .description("The name of the profile.")
        .defaultValue("")
        .filter(Utils::nameFilter)
        .build()
    );

    public Setting<List<String>> loadOnJoin = sgGeneral.add(new StringListSetting.Builder()
        .name("load-on-join")
        .description("Which servers to set this profile as active when joining.")
        .filter(Utils::ipFilter)
        .build()
    );

/*    public Setting<Boolean> gui = sgSave.add(new BoolSetting.Builder()
        .name("Gui")
        .description("Whether the profile should save gui.")
        .defaultValue(false)
        .build()
    );*/

    public Setting<Boolean> hud = sgSave.add(new BoolSetting.Builder()
        .name("hud")
        .description("Whether the profile should save hud.")
        .defaultValue(false)
        .build()
    );

    public Setting<Boolean> macros = sgSave.add(new BoolSetting.Builder()
        .name("macros")
        .description("Whether the profile should save macros.")
        .defaultValue(false)
        .build()
    );

    public Setting<Boolean> modules = sgSave.add(new BoolSetting.Builder()
        .name("modules")
        .description("Whether the profile should save modules.")
        .defaultValue(false)
        .build()
    );

    public Setting<Boolean> waypoints = sgSave.add(new BoolSetting.Builder()
        .name("waypoints")
        .description("Whether the profile should save waypoints.")
        .defaultValue(false)
        .build()
    );

    public Setting<Boolean> friends = sgSave.add(new BoolSetting.Builder()
        .name("friends")
        .description("Whether the profile should save friends.")
        .defaultValue(false)
        .build()
    );

    public Profile() {
    }

    public Profile(NbtElement tag) {
        fromTag((NbtCompound) tag);
    }

    public void load() {
        File folder = getFile();

        /*if (gui.get()) ;*/
        if (hud.get()) Hud.get().load(folder);
        if (macros.get()) Macros.get().load(folder);
        if (modules.get()) Modules.get().load(folder);
        if (waypoints.get()) Waypoints.get().load(folder);
        if (friends.get()) Friends.get().load(folder);
    }

    public void save() {
        File folder = getFile();

        if (hud.get()) Hud.get().save(folder);
        if (macros.get()) Macros.get().save(folder);
        if (modules.get()) Modules.get().save(folder);
        if (waypoints.get()) Waypoints.get().save(folder);
        if (friends.get()) Friends.get().save(folder);
    }

    public void delete() {
        try {
            FileUtils.deleteDirectory(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile() {
        return new File(Profiles.FOLDER, name.get());
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("settings", settings.toTag());

        return tag;
    }

    @Override
    public Profile fromTag(NbtCompound tag) {
        if (tag.contains("settings")) {
            settings.fromTag(tag.getCompound("settings"));
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(profile.name.get(), this.name.get());
    }
}
