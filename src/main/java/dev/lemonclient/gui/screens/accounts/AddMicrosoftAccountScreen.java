package dev.lemonclient.gui.screens.accounts;

import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.widgets.pressable.WButton;
import dev.lemonclient.systems.accounts.MicrosoftLogin;
import dev.lemonclient.systems.accounts.types.MicrosoftAccount;

public class AddMicrosoftAccountScreen extends AddAccountScreen {
    public AddMicrosoftAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Microsoft Account", parent);
    }

    @Override
    public void initWidgets() {
        MicrosoftLogin.getRefreshToken(refreshToken -> {

            if (refreshToken != null) {
                MicrosoftAccount account = new MicrosoftAccount(refreshToken);
                AccountsScreen.addAccount(null, parent, account);
            }

            close();
        });

        add(theme.label("Please select the account to log into in your browser."));

        WButton cancel = add(theme.button("Cancel")).expandX().widget();
        cancel.action = () -> {
            MicrosoftLogin.stopServer();
            close();
        };
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
