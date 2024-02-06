package dev.lemonclient.bot.actions;

import dev.lemonclient.bot.BotAction;
import dev.lemonclient.utils.player.ChatUtils;

public class SayAction extends BotAction {
    public SayAction(BotAction action) {
        this(action.action);
        this.isClient = action.isClient;
    }

    public SayAction(String text) {
        super("say", text);
    }

    @Override
    public void execute() {
        if (isClient) {
            ChatUtils.sendPlayerMsg(this.action);
        }
    }
}
