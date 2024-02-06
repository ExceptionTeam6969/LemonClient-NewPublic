package dev.lemonclient.bot.actions;

import dev.lemonclient.bot.BotAction;

public class DisconnectAction extends BotAction {
    public DisconnectAction(BotAction action) {
        this(action.action);
        this.isClient = action.isClient;
    }

    public DisconnectAction(String reason) {
        super("disconnect", reason);
    }

    @Override
    public void execute() {
        info("Disconnect: " + action);
    }
}
