package dev.lemonclient.mixin;

import com.mojang.authlib.GameProfile;
import dev.lemonclient.mixininterface.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ChatHudLine.class)
public class MixinChatHudLine implements IChatHudLine {
    @Shadow
    @Final
    private Text content;
    @Unique
    private int id;
    @Unique
    private GameProfile sender;

    @Override
    public String meteor$getText() {
        return content.getString();
    }

    @Override
    public int meteor$getId() {
        return id;
    }

    @Override
    public void meteor$setId(int id) {
        this.id = id;
    }

    @Override
    public GameProfile meteor$getSender() {
        return sender;
    }

    @Override
    public void meteor$setSender(GameProfile profile) {
        sender = profile;
    }
}
