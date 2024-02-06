package dev.lemonclient.systems.hud.screens;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.WindowScreen;
import dev.lemonclient.gui.utils.Cell;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WContainer;
import dev.lemonclient.gui.widgets.containers.WHorizontalList;
import dev.lemonclient.gui.widgets.pressable.WCheckbox;
import dev.lemonclient.gui.widgets.pressable.WMinus;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.settings.Settings;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.XAnchor;
import dev.lemonclient.systems.hud.YAnchor;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.nbt.NbtCompound;

public class HudElementScreen extends WindowScreen {
    private final HudElement element;

    private WContainer settingsC1, settingsC2;
    private final Settings settings;

    public HudElementScreen(GuiTheme theme, HudElement element) {
        super(theme, element.info.title);

        this.element = element;

        settings = new Settings();
        SettingGroup sg = settings.createGroup("Anchors");
        sg.add(new BoolSetting.Builder()
            .name("auto-anchors")
            .description("Automatically assigns anchors based on the position.")
            .defaultValue(true)
            .onModuleActivated(booleanSetting -> booleanSetting.set(element.autoAnchors))
            .onChanged(aBoolean -> {
                if (aBoolean) element.box.updateAnchors();
                element.autoAnchors = aBoolean;
            })
            .build()
        );
        sg.add(new EnumSetting.Builder<XAnchor>()
            .name("x-anchor")
            .description("Horizontal anchor.")
            .defaultValue(XAnchor.Left)
            .visible(() -> !element.autoAnchors)
            .onModuleActivated(xAnchorSetting -> xAnchorSetting.set(element.box.xAnchor))
            .onChanged(element.box::setXAnchor)
            .build()
        );
        sg.add(new EnumSetting.Builder<YAnchor>()
            .name("y-anchor")
            .description("Vertical anchor.")
            .defaultValue(YAnchor.Top)
            .visible(() -> !element.autoAnchors)
            .onModuleActivated(yAnchorSetting -> yAnchorSetting.set(element.box.yAnchor))
            .onChanged(element.box::setYAnchor)
            .build()
        );
    }

    @Override
    public void initWidgets() {
        // Description
        add(theme.label(element.info.description, Utils.getWindowWidth() / 2.0));

        // Settings
        if (element.settings.sizeGroups() > 0) {
            element.settings.onActivated();

            settingsC1 = add(theme.verticalList()).expandX().widget();
            settingsC1.add(theme.settings(element.settings)).expandX();
        }

        // Anchors
        settings.onActivated();

        settingsC2 = add(theme.verticalList()).expandX().widget();
        settingsC2.add(theme.settings(settings)).expandX();

        add(theme.horizontalSeparator()).expandX();

        // Custom widget
        WWidget widget = element.getWidget(theme);

        if (widget != null) {
            Cell<WWidget> cell = add(widget);
            if (widget instanceof WContainer) cell.expandX();
            add(theme.horizontalSeparator()).expandX();
        }

        // Bottom
        WHorizontalList bottomList = add(theme.horizontalList()).expandX().widget();

        //   Active
        bottomList.add(theme.label("Active:"));
        WCheckbox active = bottomList.add(theme.checkbox(element.isActive())).widget();
        active.action = () -> {
            if (element.isActive() != active.checked) element.toggle();
        };

        //   Remove
        WMinus remove = bottomList.add(theme.minus()).expandCellX().right().widget();
        remove.action = () -> {
            element.remove();
            close();
        };
    }

    @Override
    public void tick() {
        super.tick();

        if (settingsC1 != null) {
            element.settings.tick(settingsC1, theme);
        }

        settings.tick(settingsC2, theme);
    }

    @Override
    protected void onRenderBefore(DrawContext drawContext, float delta) {
        HudEditorScreen.renderElements(drawContext);
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(element.info.title, element.toTag());
    }

    @Override
    public boolean fromClipboard() {
        NbtCompound clipboard = NbtUtils.fromClipboard(element.toTag());

        if (clipboard != null) {
            element.fromTag(clipboard);
            return true;
        }

        return false;
    }

}
