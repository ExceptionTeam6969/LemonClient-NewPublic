package dev.lemonclient.bot.actions;

import dev.lemonclient.bot.BotAction;

public class BotCmdAction extends BotAction {
    public BotCmdAction(BotAction action) {
        this(action.action);
        this.isClient = action.isClient;
    }

    public BotCmdAction(String cmd) {
        super("botCmd", cmd);
    }

    @Override
    public void execute() {
        if (isClient) return;
        if (action.isEmpty()) return;

        String[] args = action.split(" ");
        if (args.length > 0) {
            switch (args[0]) {
                case "ping" -> {

                }
            }
        }
    }
}
