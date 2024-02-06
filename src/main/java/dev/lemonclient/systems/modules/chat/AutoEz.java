package dev.lemonclient.systems.modules.chat;

import dev.lemonclient.events.game.GameJoinedEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.math.MathUtils;
import dev.lemonclient.utils.player.ChatUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.List;
import java.util.UUID;

import static dev.lemonclient.utils.player.DeathUtils.getTargets;

public class AutoEz extends Module {
    public AutoEz() {
        super(Categories.Chat, "Auto Ez", "Sends message in chat if you kill someone");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> killMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("Kill Messages")
        .description("Randomly takes the message from the list and sends on each kill.")
        .defaultValue(
            "with ease | {kills} ks",
            "cry more kiddo | {kills} ks",
            "{target} has been put to sleep by LemonClient | {kills} ks",
            "nice fireworks | {kills} ks",
            "packed :smoke: | {kills} ks",
            "LemonClient owning yet again | {kills} ks",
            "coping much? | {kills} ks",
            "ez | {kills} ks",
            "back to spawn you go! | {kills} ks",
            "cope cope seethe cope!1!!1!11 | {kills} ks",
            "smoking fags with LemonClient | {kills} ks",
            "debil | {kills} ks",
            "curb stomping kids with LemonClient | {kills} ks"
        )
        .build()
    );
    private final Setting<Boolean> resetKillCount = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset Killcount")
        .description("Resets killcount on death.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> messageOnPop = sgGeneral.add(new BoolSetting.Builder()
        .name("Message On Pop")
        .description("Sends message in chat when target is popping totem.")
        .defaultValue(true)
        .build()
    );
    private final Setting<List<String>> popMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("Pop Messages")
        .description("Randomly takes the message from the list and sends on target pop.")
        .defaultValue(
            "popped by the best client lemonclient!",
            "{target} popped by powerful lemonclient",
            "{target} needs a new totem",
            "owning {target}",
            "{target} you should buy lemonclient and stop popping totems.",
            "{target} popped {pops}, thanks to LemonClient!"
        )
        .visible(messageOnPop::get)
        .build()
    );
    private final Setting<Integer> skipMessage = sgGeneral.add(new IntSetting.Builder()
        .name("Skip Message")
        .description("Skips messages to prevent being kicked for spamming.")
        .defaultValue(4)
        .min(0)
        .sliderMax(20)
        .visible(messageOnPop::get)
        .build()
    );

    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap<>();
    private int kills, pops, skips;

    @Override
    public void onActivate() {
        totemPopMap.clear();

        kills = 0;
        pops = 0;
        skips = skipMessage.get();
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (killMessages.get().isEmpty()) killMessages.get().add("{target} owned by LemonClient");
        if (mc.player != null && resetKillCount.get() && mc.player.isDead()) kills = 0;
    }

    public void onKill(PlayerEntity player) {
        if (isActive()) {
            kills++;
            ChatUtils.sendPlayerMsg(Config.get().prefix.get() + "say " + getMessage(player, MessageType.Kill));
        }
    }

    @EventHandler
    private void onPop(PacketEvent.Receive event) {
        if (!messageOnPop.get()) return;
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;
        if (p.getStatus() != 35) return;
        Entity entity = p.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity)) return;
        if ((entity.equals(mc.player))) return;

        synchronized (totemPopMap) {
            pops = totemPopMap.getOrDefault(entity.getUuid(), 0);
            totemPopMap.put(entity.getUuid(), ++pops);

            if (skips >= skipMessage.get() && getTargets().contains(((PlayerEntity) entity).getGameProfile().getName())) {
                ChatUtils.sendPlayerMsg(Config.get().prefix.get() + "say " + getMessage((PlayerEntity) entity, MessageType.Pop));
                skips = 0;
            } else skips++;

        }
    }

    @EventHandler
    private void onDeath(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        synchronized (totemPopMap) {
            if (mc.world != null) {
                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (!totemPopMap.containsKey(player.getUuid())) continue;

                    if (player.deathTime > 0 || player.getHealth() <= 0) totemPopMap.removeInt(player.getUuid());
                }
            }
        }
    }

    @EventHandler
    public void onJoin(GameJoinedEvent event) {
        totemPopMap.clear();

        kills = 0;
        pops = 0;
        skips = skipMessage.get();
    }

    public String getMessage(PlayerEntity player, MessageType messageType) {
        List<String> messageList = null;
        switch (messageType) {
            case Kill -> messageList = killMessages.get();
            case Pop -> messageList = popMessages.get();
        }

        String text = messageList.get(MathUtils.randomNum(0, messageList.size() - 1));

        text = text.replace("{target}", player.getGameProfile().getName());
        text = text.replace("{kills}", String.valueOf(kills));
        if (mc.player != null) {
            text = text.replace("{me}", mc.player.getGameProfile().getName());
        }
        text = text.replace("{pops}", String.valueOf(pops));
        text = getGrammar(text);

        return messageList.isEmpty() ? "i ez'ed u with minimal effort" : text;
    }

    private String getGrammar(String text) {
        String finalText;

        if (pops == 1) finalText = text.replace("totem's", "totem");
        else finalText = text.replace("totem", "totem's");

        return finalText;
    }

    public enum MessageType {
        Kill,
        Pop
    }
}
