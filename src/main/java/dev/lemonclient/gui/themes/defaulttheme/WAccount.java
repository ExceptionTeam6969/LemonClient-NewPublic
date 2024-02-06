package dev.lemonclient.gui.themes.defaulttheme;

import dev.lemonclient.gui.WidgetScreen;
import dev.lemonclient.systems.accounts.Account;
import dev.lemonclient.utils.render.color.Color;

public class WAccount extends dev.lemonclient.gui.widgets.WAccount implements LCGuiWidget {
    public WAccount(WidgetScreen screen, Account<?> account) {
        super(screen, account);
    }

    @Override
    protected Color loggedInColor() {
        return theme().loggedInColor.get();
    }

    @Override
    protected Color accountTypeColor() {
        return theme().textSecondaryColor.get();
    }
}
