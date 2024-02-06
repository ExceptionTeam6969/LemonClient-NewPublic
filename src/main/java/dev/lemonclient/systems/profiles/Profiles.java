package dev.lemonclient.systems.profiles;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.game.GameJoinedEvent;
import dev.lemonclient.systems.System;
import dev.lemonclient.systems.Systems;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.NbtUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Profiles extends System<Profiles> implements Iterable<Profile> {
    public static final File FOLDER = new File(LemonClient.FOLDER, "profiles");

    private List<Profile> profiles = new ArrayList<>();

    public Profiles() {
        super("profiles");
    }

    public static Profiles get() {
        return Systems.get(Profiles.class);
    }

    public void add(Profile profile) {
        if (!profiles.contains(profile)) profiles.add(profile);
        profile.save();
        save();
    }

    public void remove(Profile profile) {
        if (profiles.remove(profile)) profile.delete();
        save();
    }

    public Profile get(String name) {
        for (Profile profile : this) {
            if (profile.name.get().equalsIgnoreCase(name)) {
                return profile;
            }
        }

        return null;
    }

    public List<Profile> getAll() {
        return profiles;
    }

    @Override
    public File getFile() {
        return new File(FOLDER, "profiles.nbt");
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        for (Profile profile : this) {
            if (profile.loadOnJoin.get().contains(Utils.getWorldName())) {
                profile.load();
            }
        }
    }

    public boolean isEmpty() {
        return profiles.isEmpty();
    }

    @Override
    public Iterator<Profile> iterator() {
        return profiles.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("profiles", NbtUtils.listToTag(profiles));
        return tag;
    }

    @Override
    public Profiles fromTag(NbtCompound tag) {
        profiles = NbtUtils.listFromTag(tag.getList("profiles", 10), Profile::new);
        return this;
    }
}
