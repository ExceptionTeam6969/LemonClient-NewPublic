package dev.lemonclient.mixin;

import com.mojang.authlib.GameProfile;
import dev.lemonclient.LemonClient;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.NameProtect;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerListEntry {
    @Shadow
    public abstract GameProfile getProfile();

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(CallbackInfoReturnable<Identifier> info) {
        if (getProfile().getName().equals(LemonClient.mc.getSession().getUsername())) {
            if (Modules.get().get(NameProtect.class).skinProtect()) {
                info.setReturnValue(DefaultSkinHelper.getTexture());
            }
        }
    }
}
