package dev.lemonclient.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.lemonclient.commands.Commands;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.NoRender;
import dev.lemonclient.utils.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Screen.class, priority = 500) // needs to be before baritone
public abstract class MixinScreen {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void onRenderBackground(CallbackInfo info) {
        if (Utils.canUpdate() && Modules.get().get(NoRender.class).noGuiBackground())
            info.cancel();
    }

    @Inject(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 1, remap = false), cancellable = true)
    private void onRunCommand(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style.getClickEvent().getValue().startsWith(Config.get().prefix.get())) {
            try {
                Commands.dispatch(style.getClickEvent().getValue().substring(Config.get().prefix.get().length()));
                cir.setReturnValue(true);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
