package dev.lemonclient.systems.modules;

import dev.lemonclient.LemonClient;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.item.Items;
import top.fl0wowp4rty.phantomshield.annotations.Native;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;

@Native
public class Categories {
    public static final Category Combat = new Category("Combat", Items.DIAMOND_SWORD, Color.fromRGBA(225, 0, 0, 255));
    public static final Category Movement = new Category("Movement", Items.DIAMOND_BOOTS, Color.fromRGBA(0, 125, 255, 255));
    public static final Category Render = new Category("Render", Items.TINTED_GLASS, Color.fromRGBA(125, 255, 255, 255));
    public static final Category Player = new Category("Player", Items.ARMOR_STAND, Color.fromRGBA(245, 255, 100, 255));
    public static final Category World = new Category("World", Items.GRASS_BLOCK, Color.fromRGBA(0, 150, 0, 255));
    public static final Category Misc = new Category("Misc", Items.NETHER_STAR, Color.fromRGBA(0, 50, 175, 255));
    public static final Category Chat = new Category("Chat", Items.PLAYER_HEAD, Color.fromRGBA(255, 255, 255, 255));
    public static final Category Settings = new Category("Settings", Items.COMMAND_BLOCK, LemonClient.INSTANCE.MAIN_COLOR.getPacked());
    public static final Category Client = new Category("Client", Items.BEACON, LemonClient.INSTANCE.MAIN_COLOR.getPacked());

    public static boolean REGISTERING;

    @UltraLock
    public static void init() {
        REGISTERING = true;

        Modules.registerCategory(Combat);
        Modules.registerCategory(Movement);
        Modules.registerCategory(Render);
        Modules.registerCategory(Player);
        Modules.registerCategory(World);
        Modules.registerCategory(Misc);
        Modules.registerCategory(Chat);
        Modules.registerCategory(Settings);
        Modules.registerCategory(Client);

        REGISTERING = false;
    }
}
