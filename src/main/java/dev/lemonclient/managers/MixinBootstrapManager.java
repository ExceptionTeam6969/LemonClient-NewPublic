package dev.lemonclient.managers;

import dev.lemonclient.lemonchat.client.ChatClient;
import top.fl0wowp4rty.phantomshield.annotations.Native;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;

@Native
public class MixinBootstrapManager {
    @UltraLock
    public static void init() {
        // URL Class Loader
        //ServiceImpl.init();

        // LemonChat Connect
        ChatClient.main(null);
    }
}
