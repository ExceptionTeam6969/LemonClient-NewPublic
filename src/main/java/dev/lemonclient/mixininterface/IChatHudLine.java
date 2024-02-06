package dev.lemonclient.mixininterface;

import com.mojang.authlib.GameProfile;

public interface IChatHudLine {
    String meteor$getText();

    int meteor$getId();

    void meteor$setId(int id);

    GameProfile meteor$getSender();

    void meteor$setSender(GameProfile profile);
}
