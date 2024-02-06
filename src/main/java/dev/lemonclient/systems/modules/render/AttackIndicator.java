package dev.lemonclient.systems.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.entity.player.AttackEntityEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.mixininterface.IPlayerInteractEntityC2SPacket;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.Render2DUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Set;

public class AttackIndicator extends Module {
    public AttackIndicator() {
        super(Categories.Render, "Attack Indicator", "attack animation.");
    }

    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgWhitelist = settings.createGroup("Render Whitelist");

    //--------------------Render--------------------//
    public Setting<Integer> lifetime = sgRender.add(new IntSetting.Builder()
        .name("Live Time")
        .description("The lifetime of indicator in seconds.")
        .defaultValue(1)
        .range(1, 10)
        .build()
    );

    //--------------------Render Whitelist--------------------//
    private final Setting<ListMode> listMode = sgWhitelist.add(new EnumSetting.Builder<ListMode>()
        .name("List Mode")
        .description("Selection mode.")
        .defaultValue(ListMode.Whitelist)
        .build()
    );
    private final Setting<Set<EntityType<?>>> whitelist = sgWhitelist.add(new EntityTypeListSetting.Builder()
        .name("Whitelist")
        .description("The entities you want to render.")
        .defaultValue(EntityType.END_CRYSTAL)
        .visible(() -> listMode.get() == ListMode.Whitelist)
        .build()
    );
    private final Setting<Set<EntityType<?>>> blacklist = sgWhitelist.add(new EntityTypeListSetting.Builder()
        .name("Blacklist")
        .description("The entities you don't want to render.")
        .visible(() -> listMode.get() == ListMode.Blacklist)
        .build()
    );

    public boolean shouldRender = false;
    private long lastAttackTime = 0;

    public enum ListMode {
        Whitelist,
        Blacklist
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        shouldRender = isRenderEntity(event.entity.getType());
        if (shouldRender) {
            lastAttackTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    private void onAttackWithPacket(PacketEvent.Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet && packet.getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            shouldRender = isRenderEntity(packet.getEntity().getType());
            if (shouldRender) {
                lastAttackTime = System.currentTimeMillis();
            }
        }
    }

    private boolean isRenderEntity(EntityType<?> type) {
        return switch (listMode.get()) {
            case Whitelist -> whitelist.get().contains(type);
            case Blacklist -> !blacklist.get().contains(type);
        };
    }

    public void render(DrawContext context, int width, int height) {
        if (!shouldRender) return;

        int alpha;
        long timeElapsed = System.currentTimeMillis() - lastAttackTime;

        if (timeElapsed < 1000) {
            alpha = (int) (255 - (timeElapsed / 1000.0) * lifetime.get() * 255);
        } else {
            alpha = 0;
        }

        alpha = MathHelper.clamp(alpha, 0, 255);

        if (alpha > 0) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha / 255f);

            Render2DUtils.drawTexture(context, new Identifier(LemonClient.MOD_ID, "textures/hitmarker.png"), (width - 15) / 2, (height - 15) / 2, 15, 15);

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }
}
