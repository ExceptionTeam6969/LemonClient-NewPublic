package dev.lemonclient.systems.friends;

import com.mojang.util.UUIDTypeAdapter;
import dev.lemonclient.systems.System;
import dev.lemonclient.systems.Systems;
import dev.lemonclient.utils.misc.NbtUtils;
import dev.lemonclient.utils.network.Executor;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Friends extends System<Friends> implements Iterable<Friend> {
    private final List<Friend> friends = new ArrayList<>();
    public final SettingColor color = new SettingColor(0, 255, 0);

    public Friends() {
        super("friends");
    }

    public static Friends get() {
        return Systems.get(Friends.class);
    }

    public boolean add(Friend friend) {
        if (friend.name.isEmpty() || friend.name.contains(" ")) return false;

        if (!friends.contains(friend)) {
            friends.add(friend);
            save();

            return true;
        }

        return false;
    }

    public boolean remove(Friend friend) {
        if (friends.remove(friend)) {
            save();
            return true;
        }

        return false;
    }

    public Friend get(String name) {
        for (Friend friend : friends) {
            if (friend.name.equals(name)) {
                return friend;
            }
        }

        return null;
    }

    public Friend get(PlayerEntity player) {
        return get(player.getEntityName());
    }

    public Friend get(PlayerListEntry player) {
        return get(player.getProfile().getName());
    }

    public boolean isFriend(PlayerEntity player) {
        return player != null && get(player) != null;
    }

    public boolean isFriend(PlayerListEntry player) {
        return get(player) != null;
    }

    public boolean shouldAttack(PlayerEntity player) {
        return !isFriend(player);
    }

    public int count() {
        return friends.size();
    }

    public boolean isEmpty() {
        return friends.isEmpty();
    }

    @Override
    public @NotNull Iterator<Friend> iterator() {
        return friends.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("friends", NbtUtils.listToTag(friends));

        return tag;
    }

    @Override
    public Friends fromTag(NbtCompound tag) {
        friends.clear();

        for (NbtElement itemTag : tag.getList("friends", 10)) {
            NbtCompound friendTag = (NbtCompound) itemTag;
            if (!friendTag.contains("name")) continue;

            String name = friendTag.getString("name");
            if (get(name) != null) continue;

            String uuid = friendTag.getString("id");
            Friend friend = !uuid.isBlank()
                ? new Friend(name, UUIDTypeAdapter.fromString(uuid))
                : new Friend(name);

            friends.add(friend);
        }

        Collections.sort(friends);

        Executor.execute(() -> friends.forEach(Friend::updateInfo));

        return this;
    }
}
