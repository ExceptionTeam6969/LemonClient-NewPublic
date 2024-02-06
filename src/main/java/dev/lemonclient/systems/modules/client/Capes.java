package dev.lemonclient.systems.modules.client;

import dev.lemonclient.lemonchat.client.ClientSession;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

public class Capes extends Module {
    public Capes() {
        super(Categories.Client, "Capes", "Shows very cool capes on users which have them.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<CapeMode> capeMode = sgGeneral.add(new EnumSetting.Builder<CapeMode>()
        .name("Cape Mode")
        .description("The cape you chose.")
        .defaultValue(CapeMode.Light)
        .visible(() -> !ClientSession.get().isBeta())
        .build()
    );

    public final Setting<CapeModeBeta> capeModeBeta = sgGeneral.add(new EnumSetting.Builder<CapeModeBeta>()
        .name("Cape Mode")
        .description("The cape you chose.")
        .defaultValue(CapeModeBeta.Light)
        .visible(() -> ClientSession.get().isBeta())
        .build()
    );

    public enum CapeMode {
        Default,
        Light,
        Nichijou
    }

    public enum CapeModeBeta {
        Default,
        Light,
        Nichijou,
        Spade,
        MineCon2011,
        MineCon2012,
        MineCon2013,
        MineCon2016
    }

    public String getName() {
        return ClientSession.get().isBeta() ?
            switch (capeModeBeta.get()) {
                case Default -> "cape01";
                case Light -> "cape02";
                case Nichijou -> "cape03";
                case Spade -> "spade";
                case MineCon2011 -> "minecon2011";
                case MineCon2012 -> "minecon2012";
                case MineCon2013 -> "minecon2013";
                case MineCon2016 -> "minecon2016";
            }
            :
            switch (capeMode.get()) {
                case Default -> "cape01";
                case Light -> "cape02";
                case Nichijou -> "cape03";
            };
    }

    public Identifier getCape(AbstractClientPlayerEntity player, boolean elytra) {
        try {
            if (isActive() && player.equals(mc.player)) {
                return getTexture(getName());
            }

            if (ClientSession.get() != null && ClientSession.get().hasCape(player)) {
                return getTexture(ClientSession.get().getCapeName(player));
            }

            return elytra ? mc.getNetworkHandler().getPlayerListEntry(player.getUuid()).getElytraTexture() : mc.getNetworkHandler().getPlayerListEntry(player.getUuid()).getCapeTexture();
        } catch (Exception e) {
            return null;
        }
    }

    private Identifier getTexture(String capeName) {
        return new Identifier("lemon-client", "textures/capes/" + capeName + ".png");
    }

}
