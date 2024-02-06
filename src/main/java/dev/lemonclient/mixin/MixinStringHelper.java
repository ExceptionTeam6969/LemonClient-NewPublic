package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.chat.BetterChat;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(StringHelper.class)
public class MixinStringHelper {
    @ModifyArg(method = "truncateChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;truncate(Ljava/lang/String;IZ)Ljava/lang/String;"), index = 1)
    private static int injected(int maxLength) { // this method is only used in one place, to truncate chat messages, so it's fine to do this
        return (Modules.get().get(BetterChat.class).isInfiniteChatBox() ? Integer.MAX_VALUE : maxLength);
    }
}
