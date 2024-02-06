package dev.lemonclient.mixin;

import dev.lemonclient.LemonClient;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.elements.TextHud;
import dev.lemonclient.systems.modules.Category;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CrashReport.class)
public class MixinCrashReport {
    @Inject(method = "addStackTrace", at = @At("TAIL"))
    private void onAddStackTrace(StringBuilder sb, CallbackInfo info) {
        sb.append("\n\n-- Lemon Client --\n\n");
        sb.append("Version: ").append(LemonClient.VERSION).append("\n");
        if (!LemonClient.DEV_BUILD.isEmpty()) {
            sb.append("Dev Build: ").append(LemonClient.DEV_BUILD).append("\n");
        }

        if (Modules.get() != null) {
            boolean modulesActive = false;
            for (Category category : Modules.loopCategories()) {
                List<Module> modules = Modules.get().getModulesByCategory(category);
                boolean categoryActive = false;

                for (Module module : modules) {
                    if (module == null || !module.isActive()) continue;

                    if (!modulesActive) {
                        modulesActive = true;
                        sb.append("\n[[ Active Modules ]]\n");
                    }

                    if (!categoryActive) {
                        categoryActive = true;
                        sb.append("\n[")
                            .append(category)
                            .append("]:\n");
                    }

                    sb.append(module.name).append("\n");
                }
            }
        }

        if (Hud.get() != null && Hud.get().active) {
            boolean hudActive = false;
            for (HudElement element : Hud.get()) {
                if (element == null || !element.isActive()) continue;

                if (!hudActive) {
                    hudActive = true;
                    sb.append("\n[[ Active Hud Elements ]]\n");
                }

                if (!(element instanceof TextHud textHud)) sb.append(element.info.name).append("\n");
                else {
                    sb.append("Text\n{")
                        .append(textHud.text.get())
                        .append("}\n");
                    if (textHud.shown.get() != TextHud.Shown.Always) {
                        sb.append("(")
                            .append(textHud.shown.get())
                            .append(textHud.condition.get())
                            .append(")\n");
                    }
                }
            }
        }
    }
}
