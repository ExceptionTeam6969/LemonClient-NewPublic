package dev.lemonclient.utils.player;

import com.mojang.brigadier.StringReader;
import dev.lemonclient.mixininterface.IChatHud;
import dev.lemonclient.pathing.BaritoneUtils;
import dev.lemonclient.systems.config.Config;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import static dev.lemonclient.LemonClient.mc;

public class ChatUtils {
    private static String forcedPrefixClassName;

    public static final String PREFIX = Formatting.GRAY + "[" + Formatting.RED + "LemonClient" + Formatting.GRAY + "]";

    public static void forceNextPrefixClass(Class<?> klass) {
        forcedPrefixClassName = klass.getName();
    }

    // Player

    /**
     * Sends the message as if the user typed it into chat.
     */
    public static void sendPlayerMsg(String message) {
        mc.inGameHud.getChatHud().addToMessageHistory(message);

        if (message.startsWith("/")) mc.player.networkHandler.sendChatCommand(message.substring(1));
        else mc.player.networkHandler.sendChatMessage(message);
    }

    // Default

    public static void info(String message, Object... args) {
        sendMsg(Formatting.GRAY, message, args);
    }

    public static void infoPrefix(String prefix, String message, Object... args) {
        sendMsg(0, prefix, Formatting.LIGHT_PURPLE, Formatting.GRAY, message, args);
    }

    // Warning

    public static void warning(String message, Object... args) {
        sendMsg(Formatting.YELLOW, message, args);
    }

    public static void warningPrefix(String prefix, String message, Object... args) {
        sendMsg(0, prefix, Formatting.LIGHT_PURPLE, Formatting.YELLOW, message, args);
    }

    // Error

    public static void error(String message, Object... args) {
        sendMsg(Formatting.RED, message, args);
    }

    public static void errorPrefix(String prefix, String message, Object... args) {
        sendMsg(0, prefix, Formatting.LIGHT_PURPLE, Formatting.RED, message, args);
    }

    // Misc

    public static void sendMsg(Text message) {
        sendMsg(null, message);
    }

    public static void sendMsg(String prefix, Text message) {
        sendMsg(0, prefix, Formatting.LIGHT_PURPLE, message);
    }

    public static void sendMsg(Formatting color, String message, Object... args) {
        sendMsg(0, null, null, color, message, args);
    }

    public static void sendMsg(int id, Formatting color, String message, Object... args) {
        sendMsg(id, null, null, color, message, args);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable Formatting prefixColor, Formatting messageColor, String messageContent, Object... args) {
        MutableText message = formatMsg(String.format(messageContent, args), messageColor);
        sendMsg(id, prefixTitle, prefixColor, message);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable Formatting prefixColor, String messageContent, Formatting messageColor) {
        MutableText message = formatMsg(messageContent, messageColor);
        sendMsg(id, prefixTitle, prefixColor, message);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable Formatting prefixColor, Text msg) {
        if (mc.world == null) return;

        MutableText message = Text.empty();
        message.append(PREFIX + " ");
        if (prefixTitle != null) message.append(getCustomPrefix(prefixTitle, prefixColor));
        message.append(msg);

        if (!Config.get().deleteChatFeedback.get()) id = 0;

        ((IChatHud) mc.inGameHud.getChatHud()).lemonclient$add(message, id);
    }

    private static MutableText getCustomPrefix(String prefixTitle, Formatting prefixColor) {
        MutableText prefix = Text.empty();
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));

        prefix.append("[");

        MutableText moduleTitle = Text.literal(prefixTitle);
        moduleTitle.setStyle(moduleTitle.getStyle().withFormatting(prefixColor));
        prefix.append(moduleTitle);

        prefix.append("] ");

        return prefix;
    }

    private static MutableText formatMsg(String message, Formatting defaultColor) {
        StringReader reader = new StringReader(message);
        MutableText text = Text.empty();
        Style style = Style.EMPTY.withFormatting(defaultColor);
        StringBuilder result = new StringBuilder();
        boolean formatting = false;
        while (reader.canRead()) {
            char c = reader.read();
            if (c == '(') {
                text.append(Text.literal(result.toString()).setStyle(style));
                result.setLength(0);
                result.append(c);
                formatting = true;
            } else {
                result.append(c);

                if (formatting && c == ')') {
                    switch (result.toString()) {
                        case "(default)" -> {
                            style = style.withFormatting(defaultColor);
                            result.setLength(0);
                        }
                        case "(highlight)" -> {
                            style = style.withFormatting(Formatting.WHITE);
                            result.setLength(0);
                        }
                        case "(underline)" -> {
                            style = style.withFormatting(Formatting.UNDERLINE);
                            result.setLength(0);
                        }
                    }
                    formatting = false;
                }
            }
        }

        if (!result.isEmpty()) text.append(Text.literal(result.toString()).setStyle(style));

        return text;
    }

    public static MutableText formatCoords(Vec3d pos) {
        String coordsString = String.format("(highlight)(underline)%.0f, %.0f, %.0f(default)", pos.x, pos.y, pos.z);
        MutableText coordsText = formatMsg(coordsString, Formatting.GRAY);

        Style style = coordsText.getStyle().withFormatting(Formatting.BOLD).withHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            Text.literal("Set as Baritone goal")
        ));

        if (BaritoneUtils.IS_AVAILABLE) {
            style = style.withClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                String.format("%sgoto %d %d %d", BaritoneUtils.getPrefix(), (int) pos.x, (int) pos.y, (int) pos.z)
            ));
        }

        coordsText.setStyle(style);
        return coordsText;
    }
}
