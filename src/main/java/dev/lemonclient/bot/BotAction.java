package dev.lemonclient.bot;

import com.google.gson.annotations.SerializedName;
import dev.lemonclient.bot.client.BotSession;
import dev.lemonclient.lemonchat.utils.ChatFormatting;
import dev.lemonclient.lemonchat.utils.StringUtils;
import dev.lemonclient.utils.Utils;
import net.minecraft.text.Text;

import static dev.lemonclient.LemonClient.mc;

public class BotAction {
    @SerializedName("name")
    public String name;
    @SerializedName("action")
    public String action;
    @SerializedName("client")
    public boolean isClient;

    public BotAction(String name, String action) {
        this.name = name;
        this.action = action;
    }

    public void send(BotSession session) {
        session.send(this);
    }

    public void info(String msg, Object... args) {
        msg = StringUtils.getReplaced(msg, args);
        if (Utils.canUpdate()) {
            msg = "{}[{}{}Bot{}Net{}] {}" + msg;
            msg = StringUtils.getReplaced(msg, ChatFormatting.GRAY, ChatFormatting.GREEN, isClient ? "C" : "S", ChatFormatting.BLUE, ChatFormatting.GRAY, ChatFormatting.RESET);
            mc.inGameHud.getChatHud().addMessage(Text.literal(msg));
        }
    }

    public void init() {
    }

    public void execute() {
    }
}
