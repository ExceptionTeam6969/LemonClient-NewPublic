package dev.lemonclient.systems.modules.misc;

import dev.lemonclient.events.game.OpenScreenEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class ShulkerDupe extends Module {
    public ShulkerDupe() {
        super(Categories.Misc, "Shulker Dupe", "ShulkerDupe only works in vanilla, forge, and fabric servers version 1.19 and below.");
    }

    private final SettingGroup sgAutoTool = settings.createGroup("Auto Tool");

    //--------------------AutoTool--------------------//
    private final Setting<Boolean> autoTool = sgAutoTool.add(new BoolSetting.Builder()
        .name("Auto Tool")
        .description("Uses Pickaxe when breaking shulker.")
        .defaultValue(true)
        .build()
    );
    public static boolean shouldDupe;
    public static boolean shouldDupeAll;
    private boolean timerWASon = false;

    @Override
    public void onActivate() {
        timerWASon = false;
        shouldDupeAll = false;
        shouldDupe = false;
    }

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (event.screen instanceof ShulkerBoxScreen) {
            shouldDupeAll = false;
            shouldDupe = false;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (shouldDupe | shouldDupeAll) {
            if (Modules.get().get(Timer.class).isActive()) {
                timerWASon = true;
                Modules.get().get(Timer.class).toggle();
            }
            for (int i = 0; i < 8; i++) {
                if (autoTool.get() && (mc.player.getInventory().getStack(0).getItem() instanceof PickaxeItem || mc.player.getInventory().getStack(1).getItem() instanceof PickaxeItem || mc.player.getInventory().getStack(2).getItem() instanceof PickaxeItem || mc.player.getInventory().getStack(3).getItem() instanceof PickaxeItem || mc.player.getInventory().getStack(4).getItem() instanceof PickaxeItem || mc.player.getInventory().getStack(5).getItem() instanceof PickaxeItem || mc.player.getInventory().getStack(6).getItem() instanceof PickaxeItem || mc.player.getInventory().getStack(7).getItem() instanceof PickaxeItem || mc.player.getInventory().getStack(8).getItem() instanceof PickaxeItem) && !(mc.player.getInventory().getMainHandStack().getItem() instanceof PickaxeItem)) {
                    mc.player.getInventory().selectedSlot++;
                    if (mc.player.getInventory().selectedSlot > 8) mc.player.getInventory().selectedSlot = 0;
                }
            }
        } else {
            if (!Modules.get().get(Timer.class).isActive() && timerWASon) {
                timerWASon = false;
                Modules.get().get(Timer.class).toggle();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.currentScreen instanceof ShulkerBoxScreen) {
            BlockHitResult a = (BlockHitResult) mc.crosshairTarget;
            if (shouldDupe | shouldDupeAll) {
                mc.interactionManager.updateBlockBreakingProgress(a.getBlockPos(), Direction.DOWN);
            }
        }
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Sent event) {
        if (event.packet instanceof PlayerActionC2SPacket) {
            if (shouldDupeAll) {
                if (((PlayerActionC2SPacket) event.packet).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
                    for (int i = 0; i < 27; i++) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    }
                    shouldDupeAll = false;
                }
            } else if (shouldDupe) {
                if (((PlayerActionC2SPacket) event.packet).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                    shouldDupe = false;
                }
            }
        }
    }
}
