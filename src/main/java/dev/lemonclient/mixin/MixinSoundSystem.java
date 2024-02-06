package dev.lemonclient.mixin;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.world.PlaySoundEvent;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class MixinSoundSystem {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void onPlay(SoundInstance soundInstance, CallbackInfo info) {
        PlaySoundEvent event = LemonClient.EVENT_BUS.post(PlaySoundEvent.get(soundInstance));

        if (event.isCancelled()) info.cancel();
    }
}
