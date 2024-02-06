package dev.lemonclient.mixin;

import dev.lemonclient.LemonClient;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.chat.Chat;
import dev.lemonclient.systems.modules.render.BetterTab;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {
    @ModifyConstant(constant = @Constant(longValue = 80L), method = "collectPlayerEntries")
    private long modifyCount(long count) {
        BetterTab module = Modules.get().get(BetterTab.class);

        return module.isActive() ? module.tabSize.get() : count;
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(PlayerListEntry playerListEntry, CallbackInfoReturnable<Text> info) {
        BetterTab betterTab = Modules.get().get(BetterTab.class);
        Chat chat = Modules.get().get(Chat.class);

        if (chat.isActive() && chat.enableTab.get()) info.setReturnValue(chat.getPlayerName(playerListEntry));
        if (betterTab.isActive()) info.setReturnValue(betterTab.getPlayerName(playerListEntry));
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), index = 0)
    private int modifyWidth(int width) {
        BetterTab module = Modules.get().get(BetterTab.class);

        return module.isActive() && module.accurateLatency.get() ? width + 30 : width;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 20), require = 0)
    private int modifyHeight(int height) {
        BetterTab module = Modules.get().get(BetterTab.class);
        return module.isActive() ? module.tabHeight.get() : height;
    }

    @Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
    private void onRenderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
        BetterTab betterTab = Modules.get().get(BetterTab.class);

        if (betterTab.isActive() && betterTab.accurateLatency.get()) {
            TextRenderer textRenderer = LemonClient.mc.textRenderer;

            int latency = MathHelper.clamp(entry.getLatency(), 0, 9999);
            int color = latency < 150 ? 0x00E970 : latency < 300 ? 0xE7D020 : 0xD74238;
            String text = latency + "ms";
            context.drawTextWithShadow(textRenderer, text, x + width - textRenderer.getWidth(text), y, color);
            ci.cancel();
        }
    }
}
