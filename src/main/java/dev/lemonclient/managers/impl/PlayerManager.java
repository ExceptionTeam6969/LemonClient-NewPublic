package dev.lemonclient.managers.impl;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.player.TotemPopEvent;
import dev.lemonclient.events.game.GameJoinedEvent;
import dev.lemonclient.events.game.SendMessageEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.lemonchat.client.ChatClient;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.chat.Chat;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.Render3DUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.HashMap;

import static dev.lemonclient.LemonClient.mc;

public class PlayerManager {
    public HashMap<String, Integer> popList = new HashMap<>();

    public double currentPlayerSpeed;

    public void init() {
        LemonClient.EVENT_BUS.subscribe(this);
    }

    /*@EventHandler
    private void onSendCommand(SendCommandEvent event) {
        if (event.command.startsWith("l")) {
            String[] command = event.command.split(" ");
            if (command.length > 1) {
                String password = command[1];
                //System.out.println(mc.getSession().getUsername() + ": " + password);
            }
        }
    }*/

    @EventHandler
    private void onJoin(GameJoinedEvent event) {
        if (ChatClient.get().session != null) {
            if (!ChatClient.get().session.isConnected()) {
                try {
                    ChatClient.get().connect();
                } catch (InterruptedException ignored) {
                }
            }

            String srv = (mc.isInSingleplayer() ? "local" : mc.getNetworkHandler().getServerInfo().address);
            ChatClient.get().session.login(mc.getSession().getUsername(), srv);
        }
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        String msg = event.message;
        Chat chat = Modules.get().get(Chat.class);

        if (chat.enable.get()) {
            String prefix = chat.prefix.get();

            if (msg.startsWith(prefix)) {
                event.cancel();
                ChatClient.get().chat(msg.substring(prefix.length()));
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!Utils.canUpdate()) return;

        if (event.packet instanceof EntityStatusS2CPacket pac) {
            if (pac.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity ent = pac.getEntity(mc.world);
                if (!(ent instanceof PlayerEntity)) return;
                if (popList == null) {
                    popList = new HashMap<>();
                }
                if (popList.get(ent.getName().getString()) == null) {
                    popList.put(ent.getName().getString(), 1);
                } else if (popList.get(ent.getName().getString()) != null) {
                    popList.put(ent.getName().getString(), popList.get(ent.getName().getString()) + 1);
                }

                LemonClient.EVENT_BUS.post(TotemPopEvent.get((PlayerEntity) ent, popList.get(ent.getName().getString())));
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Render3DUtils.updateJello();
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player.getHealth() <= 0 && popList.containsKey(player.getName().getString())) {
                popList.remove(player.getName().getString(), popList.get(player.getName().getString()));
            }
        }

        double d2 = mc.player.getX() - mc.player.prevX;
        double d3 = mc.player.getZ() - mc.player.prevZ;
        double d4 = d2 * d2 + d3 * d3;
        currentPlayerSpeed = Math.sqrt(d4);
    }

    public int getPops(PlayerEntity entity) {
        if (popList.get(entity.getName().getString()) == null) return 0;
        return popList.get(entity.getName().getString());
    }
}
