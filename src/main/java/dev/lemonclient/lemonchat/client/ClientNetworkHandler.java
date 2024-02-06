package dev.lemonclient.lemonchat.client;

import dev.lemonclient.lemonchat.network.NetHandler;
import dev.lemonclient.lemonchat.network.c2s.ChatMessageC2S;
import dev.lemonclient.lemonchat.network.c2s.HandShakeC2S;
import dev.lemonclient.lemonchat.network.c2s.LoginC2S;
import dev.lemonclient.lemonchat.network.s2c.ChatMessageS2C;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.chat.Chat;
import dev.lemonclient.utils.Utils;
import net.minecraft.text.Text;

import java.io.IOException;

import static dev.lemonclient.LemonClient.mc;

public class ClientNetworkHandler implements NetHandler {
    public final ClientSession session;

    public ClientNetworkHandler(ClientSession session) {
        this.session = session;
    }

    @Override
    public void onHandShakeC2S(HandShakeC2S packet) throws IOException {

    }

    @Override
    public void onLoginC2S(LoginC2S packet) throws IOException {

    }

    @Override
    public void onMessageC2S(ChatMessageC2S packet) throws IOException {

    }

    @Override
    public void onMessageS2C(ChatMessageS2C packet) throws IOException {
        if (Modules.get().get(Chat.class).enable.get()) {
            sendMessage(packet.message);
        }
    }

    public void sendMessage(String text, Object... args) {
        for (Object arg : args) {
            text = text.replaceFirst("\\{}", arg.toString());
        }

        if (Utils.canUpdate()) {
            mc.inGameHud.getChatHud().addMessage(Text.literal(text));
        } else ChatClient.LOGGER.info(text);
    }
}
