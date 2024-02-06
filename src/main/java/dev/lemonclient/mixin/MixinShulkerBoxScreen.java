package dev.lemonclient.mixin;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.ShulkerDupe;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShulkerBoxScreen.class)
public class MixinShulkerBoxScreen extends Screen {
    public MixinShulkerBoxScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        if (Modules.get().isActive(ShulkerDupe.class)) {
            addDrawableChild(new ButtonWidget.Builder(Text.literal("Dupe"), button -> ShulkerDupe.shouldDupe = true)
                .position(240, height / 2 + 35 - 140)
                .size(50, 15)
                .build()
            );
            addDrawableChild(new ButtonWidget.Builder(Text.literal("Dupe All"), button -> ShulkerDupe.shouldDupeAll = true)
                .position(295, height / 2 + 35 - 140)
                .size(50, 15)
                .build()
            );
        }
    }
}
