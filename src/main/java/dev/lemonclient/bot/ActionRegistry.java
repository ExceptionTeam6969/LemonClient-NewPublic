package dev.lemonclient.bot;

import dev.lemonclient.bot.actions.BotCmdAction;
import dev.lemonclient.bot.actions.DisconnectAction;
import dev.lemonclient.bot.actions.InfoAction;
import dev.lemonclient.bot.actions.SayAction;

import java.util.HashMap;
import java.util.Map;

public class ActionRegistry {
    private final Map<String, Class<? extends BotAction>> actionMap = new HashMap<>();
    public static ActionRegistry INSTANCE;

    public ActionRegistry() {
        INSTANCE = this;

        actionMap.put("say", SayAction.class);
        actionMap.put("disconnect", DisconnectAction.class);
        actionMap.put("info", InfoAction.class);
        actionMap.put("botCmd", BotCmdAction.class);
    }

    public static Class<? extends BotAction> get(String name) {
        if (INSTANCE == null) {
            new ActionRegistry();
        }
        return INSTANCE.actionMap.getOrDefault(name, null);
    }
}
