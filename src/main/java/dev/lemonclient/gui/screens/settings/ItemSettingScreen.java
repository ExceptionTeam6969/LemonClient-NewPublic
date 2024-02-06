package dev.lemonclient.gui.screens.settings;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.WindowScreen;
import dev.lemonclient.gui.widgets.WItemWithLabel;
import dev.lemonclient.gui.widgets.containers.WTable;
import dev.lemonclient.gui.widgets.input.WTextBox;
import dev.lemonclient.gui.widgets.pressable.WButton;
import dev.lemonclient.settings.ItemSetting;
import dev.lemonclient.utils.misc.Names;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.StringUtils;

public class ItemSettingScreen extends WindowScreen {
    private final ItemSetting setting;

    private WTable table;

    private WTextBox filter;
    private String filterText = "";

    public ItemSettingScreen(GuiTheme theme, ItemSetting setting) {
        super(theme, "Select item");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();
        initTable();
    }

    public void initTable() {
        for (Item item : Registries.ITEM) {
            if (setting.filter != null && !setting.filter.test(item)) continue;
            if (item == Items.AIR) continue;

            WItemWithLabel itemLabel = theme.itemWithLabel(item.getDefaultStack(), Names.get(item));
            if (!filterText.isEmpty() && !StringUtils.containsIgnoreCase(itemLabel.getLabelText(), filterText))
                continue;
            table.add(itemLabel);

            WButton select = table.add(theme.button("Select")).expandCellX().right().widget();
            select.action = () -> {
                setting.set(item);
                close();
            };

            table.row();
        }
    }
}
