package dev.lemonclient.gui.screens;

import dev.lemonclient.events.client.ActiveModulesChangedEvent;
import dev.lemonclient.events.client.ModuleBindChangedEvent;
import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.WindowScreen;
import dev.lemonclient.gui.renderer.GuiRenderer;
import dev.lemonclient.gui.utils.Cell;
import dev.lemonclient.gui.widgets.WKeybind;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WContainer;
import dev.lemonclient.gui.widgets.containers.WHorizontalList;
import dev.lemonclient.gui.widgets.containers.WSection;
import dev.lemonclient.gui.widgets.pressable.WButton;
import dev.lemonclient.gui.widgets.pressable.WCheckbox;
import dev.lemonclient.gui.widgets.pressable.WFavorite;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.NbtUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;

public class ModuleScreen extends WindowScreen {
    private final Module module;

    private WContainer settingsContainer;
    private WKeybind keybind;
    private WCheckbox active;

    public ModuleScreen(GuiTheme theme, Module module) {
        super(theme, theme.favorite(module.favorite), module.title);
        ((WFavorite) window.icon).action = () -> module.favorite = ((WFavorite) window.icon).checked;

        this.module = module;
    }

    @Override
    public void initWidgets() {
        // Description
        add(theme.label(module.description, Utils.getWindowWidth() / 2.0));

        // Settings
        if (module.settings.groups.size() > 0) {
            settingsContainer = add(theme.verticalList()).expandX().widget();
            settingsContainer.add(theme.settings(module.settings)).expandX();
        }

        // Custom widget
        WWidget widget = module.getWidget(theme);

        if (widget != null) {
            add(theme.horizontalSeparator()).expandX();
            Cell<WWidget> cell = add(widget);
            if (widget instanceof WContainer) cell.expandX();
        }

        // Bind
        WSection section = add(theme.section("Bind", true)).expandX().widget();

        // Keybind
        WHorizontalList bind = section.add(theme.horizontalList()).expandX().widget();

        bind.add(theme.label("Bind: "));
        keybind = bind.add(theme.keybind(module.keybind)).expandX().widget();
        keybind.actionOnSet = () -> Modules.get().setModuleToBind(module);

        WButton reset = bind.add(theme.button(GuiRenderer.RESET)).expandCellX().right().widget();
        reset.action = keybind::resetBind;

        // Toggle on bind release
        WHorizontalList tobr = section.add(theme.horizontalList()).widget();

        tobr.add(theme.label("Toggle on bind release: "));
        WCheckbox tobrC = tobr.add(theme.checkbox(module.toggleOnBindRelease)).widget();
        tobrC.action = () -> module.toggleOnBindRelease = tobrC.checked;

        // Chat feedback
        WHorizontalList cf = section.add(theme.horizontalList()).widget();

        cf.add(theme.label("Toggle message: "));
        WCheckbox cfC = cf.add(theme.checkbox(module.isMessageEnabled())).widget();
        cfC.action = () -> {
            if (module.isMessageEnabled() != cfC.checked) module.setToggleMessage(cfC.checked);
        };

        // Toasts
        WHorizontalList tm = section.add(theme.horizontalList()).widget();

        tm.add(theme.label("Toggle toast: "));
        WCheckbox toastToggle = tm.add(theme.checkbox(module.isToastEnabled())).widget();
        toastToggle.action = () -> {
            if (module.isToastEnabled() != toastToggle.checked) module.setToggleToast(toastToggle.checked);
        };

        add(theme.horizontalSeparator()).expandX();

        // Bottom
        WHorizontalList bottom = add(theme.horizontalList()).expandX().widget();

        //   Active
        bottom.add(theme.label("Active: "));
        active = bottom.add(theme.checkbox(module.isActive())).expandCellX().widget();
        active.action = () -> {
            if (module.isActive() != active.checked) module.toggle();
        };
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !Modules.get().isBinding();
    }

    @Override
    public void tick() {
        super.tick();

        module.settings.tick(settingsContainer, theme);
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        keybind.reset();
    }

    @EventHandler
    private void onActiveModulesChanged(ActiveModulesChangedEvent event) {
        this.active.checked = module.isActive();
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(module.title, module.toTag());
    }

    @Override
    public boolean fromClipboard() {
        NbtCompound clipboard = NbtUtils.fromClipboard(module.toTag());

        if (clipboard != null) {
            module.fromTag(clipboard);
            return true;
        }

        return false;
    }
}
