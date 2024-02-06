package dev.lemonclient.systems.modules.client;

import dev.lemonclient.events.game.GameJoinedEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.others.Task;
import dev.lemonclient.utils.player.ChatUtils;
import dev.lemonclient.utils.world.CardinalDirection;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import static dev.lemonclient.systems.hud.elements.ToastNotificationsHud.addToast;
import static dev.lemonclient.utils.world.BlockInfo.isCombatBlock;

public class Notifications extends Module {
    public Notifications() {
        super(Categories.Client, "Notifications", "Sends messages in hud about different events.");
    }

    private final SettingGroup sgArmour = settings.createGroup("Breaks");
    private final SettingGroup sgPlayers = settings.createGroup("Players");
    private final SettingGroup sgSurround = settings.createGroup("Surround");
    private final SettingGroup sgNone = settings.createGroup("");


    //--------------------Armor--------------------//
    private final Setting<Boolean> armor = sgArmour.add(new BoolSetting.Builder()
        .name("Armor")
        .description("Sends notifications while armor is low.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> percentage = sgArmour.add(new IntSetting.Builder()
        .name("Percentage")
        .description("Precentage of armor to trigger notifier.")
        .defaultValue(40)
        .sliderRange(1, 100)
        .visible(armor::get)
        .build()
    );
    private final Setting<String> message = sgArmour.add(new StringSetting.Builder()
        .name("Message")
        .description("Messages for armor notify.")
        .defaultValue("Your {armor}({value}%) is lower than {%}%!")
        .build()
    );

    //--------------------Players--------------------//
    private final Setting<Boolean> totemNotif = sgPlayers.add(new BoolSetting.Builder()
        .name("Totem Pops")
        .description("Sends notification for totem pops.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> deaths = sgPlayers.add(new BoolSetting.Builder()
        .name("Deaths")
        .description("Sends notification for deaths.")
        .defaultValue(false)
        .build()
    );

    //--------------------Surround--------------------//
    private final Setting<Boolean> surroundBreak = sgSurround.add(new BoolSetting.Builder()
        .name("Surround Break")
        .description("Notifies you while someone breaking your surround.")
        .defaultValue(false)
        .build()
    );

    //--------------------Notification--------------------//
    private final Setting<Mode> notifications = sgNone.add(new EnumSetting.Builder<Mode>()
        .name("Notifications")
        .defaultValue(Mode.Toast)
        .build()
    );

    private BlockPos prevBreakPos;
    private final Task bootsTask = new Task();
    private final Task leggingsTask = new Task();
    private final Task chestplateTask = new Task();
    private final Task helmetTask = new Task();

    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap<>();
    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap<>();

    @Override
    public void onActivate() {
        totemPopMap.clear();
        chatIdMap.clear();

        bootsTask.reset();
        leggingsTask.reset();
        chestplateTask.reset();
        helmetTask.reset();
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        totemPopMap.clear();
        chatIdMap.clear();
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;
        if (p.getStatus() != 35) return;
        Entity entity = p.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity)) return;
        if ((entity.equals(mc.player))) return;

        synchronized (totemPopMap) {
            int pops = totemPopMap.getOrDefault(entity.getUuid(), 0);
            totemPopMap.put(entity.getUuid(), ++pops);
            if (totemNotif.get()) send(title, entity.getEntityName() + " popped " + pops + " time`s!", notifications);

            if (notifications.get().equals(Mode.Toast)) {
                Managers.NOTIFICATION.info(title, entity.getEntityName() + " popped " + pops + " time`s!");
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        synchronized (totemPopMap) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!totemPopMap.containsKey(player.getUuid())) continue;

                if (player.deathTime > 0 || player.getHealth() <= 0) {
                    int pops = totemPopMap.getOrDefault(player.getUuid(), 0);
                    String xx = player.getEntityName() + " just died after " + pops + " pops!";
                    if (deaths.get()) send(title, xx, notifications);
                    if (notifications.get().equals(Mode.Toast)) {
                        Managers.NOTIFICATION.info(title, xx);
                    }
                    totemPopMap.removeInt(player.getUuid());
                    chatIdMap.removeInt(player.getUuid());
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        if (!armor.get()) return;
        ItemStack boots = mc.player.getInventory().getArmorStack(0);
        ItemStack leggings = mc.player.getInventory().getArmorStack(1);
        ItemStack chestplate = mc.player.getInventory().getArmorStack(2);
        ItemStack helmet = mc.player.getInventory().getArmorStack(3);

        if (boots.isEmpty() && leggings.isEmpty() && chestplate.isEmpty() && helmet.isEmpty()) return;

        if (getPercentage(boots) < percentage.get()) {
            bootsTask.run(() -> notifyArmor("Boots", getPercentage(boots)));
        } else bootsTask.reset();

        if (getPercentage(leggings) < percentage.get()) {
            leggingsTask.run(() -> notifyArmor("Leggings", getPercentage(leggings)));
        } else leggingsTask.reset();

        if (getPercentage(chestplate) < percentage.get()) {
            chestplateTask.run(() -> notifyArmor("Chestplate", getPercentage(chestplate)));
        } else chestplateTask.reset();

        if (getPercentage(helmet) < percentage.get()) {
            helmetTask.run(() -> notifyArmor("Helmet", getPercentage(helmet)));
        } else helmetTask.reset();
    }

    @EventHandler
    public void onBreakPacket(PacketEvent.Receive event) {
        if (surroundBreak.get()) {
            if (event.packet instanceof BlockBreakingProgressS2CPacket bbpp) {
                BlockPos bbp = bbpp.getPos();

                if (bbp.equals(prevBreakPos) && bbpp.getProgress() > 0) return;

                PlayerEntity breakingPlayer = (PlayerEntity) mc.world.getEntityById(bbpp.getEntityId());
                BlockPos playerBlockPos = mc.player.getBlockPos();
                boolean validBlock = isCombatBlock(bbp);

                assert breakingPlayer != null;
                if (breakingPlayer.equals(mc.player)) return;

                for (CardinalDirection direction : CardinalDirection.values()) {
                    if (validBlock && bbp.equals(playerBlockPos.offset(direction.toDirection())))
                        notifySurroundBreak(breakingPlayer);
                }

                prevBreakPos = bbp;
            }
        }
    }

    public static void send(String notificationTitle, String msg, Setting<Mode> notify) {
        switch (notify.get()) {
            case Chat -> ChatUtils.info(msg);
            case Toast -> addToast(msg);
            case Notification -> Managers.NOTIFICATION.info(notificationTitle, msg);
        }
    }

    private void notifyArmor(String armor, int percentage) {
        String msg = message.get();

        msg = msg.replace("{armor}", armor);
        msg = msg.replace("{value}", String.valueOf(percentage));
        msg = msg.replace("{%}", String.valueOf(this.percentage.get()));

        if (percentage != 0) send(title, msg, notifications);

        switch (notifications.get()) {
            case Notification -> Managers.NOTIFICATION.warn(title, msg);
            case Chat -> warning(msg);
        }
    }

    private int getPercentage(ItemStack itemStack) {
        return Math.round(((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage());
    }

    private void notifySurroundBreak(PlayerEntity player) {
        String xx = "Your surround is being broken by " + player.getEntityName();

        switch (notifications.get()) {
            case Chat -> sendDisableMsg(xx);
            case Notification -> Managers.NOTIFICATION.warn(title, xx);
            case Toast -> send(title, xx, notifications);
        }
    }

    public enum Mode {
        Toast,
        Notification,
        Chat
    }
}
