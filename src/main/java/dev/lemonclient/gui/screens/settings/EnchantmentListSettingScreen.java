package dev.lemonclient.gui.screens.settings;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.utils.misc.Names;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;

import java.util.List;

public class EnchantmentListSettingScreen extends LeftRightListSettingScreen<Enchantment> {
    public EnchantmentListSettingScreen(GuiTheme theme, Setting<List<Enchantment>> setting) {
        super(theme, "Select Enchantments", setting, setting.get(), Registries.ENCHANTMENT);
    }

    @Override
    protected WWidget getValueWidget(Enchantment value) {
        return theme.label(getValueName(value));
    }

    @Override
    protected String getValueName(Enchantment value) {
        return Names.get(value);
    }
}
