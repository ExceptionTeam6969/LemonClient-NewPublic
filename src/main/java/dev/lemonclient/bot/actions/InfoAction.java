package dev.lemonclient.bot.actions;

import dev.lemonclient.bot.BotAction;

public class InfoAction extends BotAction {
    public InfoAction(BotAction action) {
        this(action.action);
        this.isClient = action.isClient;
    }

    public InfoAction(String text) {
        super("info", text);
    }

    @Override
    public void execute() {
        info(action);
    }
}
