package dev.lemonclient.events.game;

import dev.lemonclient.events.Cancellable;

public class SendCommandEvent extends Cancellable {
    private static final SendCommandEvent INSTANCE = new SendCommandEvent();

    public String command;

    public static SendCommandEvent get(String command) {
        INSTANCE.setCancelled(false);
        INSTANCE.command = command;
        return INSTANCE;
    }
}
