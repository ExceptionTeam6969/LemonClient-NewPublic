package dev.lemonclient.mixin;

import com.mojang.authlib.Environment;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(YggdrasilMinecraftSessionService.class)
public interface IYggdrasilMinecraftSessionService {
    @Invoker("<init>")
    static YggdrasilMinecraftSessionService createYggdrasilMinecraftSessionService(final YggdrasilAuthenticationService service, final Environment env) {
        throw new UnsupportedOperationException();
    }
}
