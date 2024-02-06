package dev.lemonclient.utils.misc;

import dev.lemonclient.LemonClient;
import dev.lemonclient.utils.Utils;
import net.minecraft.SharedConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.lemonclient.LemonClient.mc;

public class Placeholders {
    private static final Pattern pattern = Pattern.compile("(%version%|%mc_version%|%player%|%username%|%server%)");

    public static String apply(String string) {
        Matcher matcher = pattern.matcher(string);
        StringBuffer sb = new StringBuffer(string.length());

        while (matcher.find()) {
            matcher.appendReplacement(sb, getReplacement(matcher.group(1)));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String getReplacement(String placeholder) {
        return switch (placeholder) {
            case "%version%" -> "v" + LemonClient.VERSION;
            case "%mc_version%" -> SharedConstants.getGameVersion().getName();
            case "%player%", "%username%" -> mc.getSession().getUsername();
            case "%server%" -> Utils.getWorldName();
            case "%health%" -> String.valueOf(Utils.getPlayerHealth());
            default -> "";
        };
    }
}
