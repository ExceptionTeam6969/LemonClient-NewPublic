package dev.lemonclient.gui.screens.settings;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.WindowScreen;
import dev.lemonclient.gui.utils.Cell;
import dev.lemonclient.gui.widgets.WLabel;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WTable;
import dev.lemonclient.gui.widgets.containers.WView;
import dev.lemonclient.gui.widgets.input.WDropdown;
import dev.lemonclient.gui.widgets.input.WTextBox;
import dev.lemonclient.gui.widgets.pressable.WButton;
import dev.lemonclient.renderer.Fonts;
import dev.lemonclient.renderer.text.FontFamily;
import dev.lemonclient.renderer.text.FontInfo;
import dev.lemonclient.settings.FontFaceSetting;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class FontFaceSettingScreen extends WindowScreen {
    private final FontFaceSetting setting;

    private WTable table;

    private WTextBox filter;
    private String filterText = "";

    public FontFaceSettingScreen(GuiTheme theme, FontFaceSetting setting) {
        super(theme, "Select Font");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        filter = add(theme.textBox("")).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        window.view.hasScrollBar = false;

        enterAction = () -> {
            List<Cell<?>> row = table.getRow(0);
            if (row == null) return;

            WWidget widget = row.get(2).widget();
            if (widget instanceof WButton button) {
                button.action.run();
            }
        };

        WView view = add(theme.view()).expandX().widget();
        view.scrollOnlyWhenMouseOver = false;
        table = view.add(theme.table()).expandX().widget();

        initTable();
    }

    private void initTable() {
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            String name = fontFamily.getName();

            WLabel item = theme.label(name);
            if (!filterText.isEmpty() && !StringUtils.containsIgnoreCase(name, filterText)) continue;
            table.add(item);

            WDropdown<FontInfo.Type> dropdown = table.add(theme.dropdown(FontInfo.Type.Regular)).right().widget();

            WButton select = table.add(theme.button("Select")).expandCellX().right().widget();
            select.action = () -> {
                setting.set(fontFamily.get(dropdown.get()));
                close();
            };

            table.row();
        }
    }
}
