package dev.lemonclient.systems.modules.render;

import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.entity.EntityAddedEvent;
import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.render.NametagUtils;
import dev.lemonclient.utils.render.WireframeEntityRenderer;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.TimerUtils;
import dev.lemonclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LogoutSpots extends Module {
    public LogoutSpots() {
        super(Categories.Render, "Logout Spots", "Displays a box where another player has logged out at.");

        lineColor.onChanged();
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General
    private final Setting<Boolean> nameRender = sgGeneral.add(new BoolSetting.Builder().name("name").defaultValue(true).build());
    private final Setting<Boolean> healthRender = sgGeneral.add(new BoolSetting.Builder().name("health").defaultValue(true).build());
    private final Setting<Boolean> coordRender = sgGeneral.add(new BoolSetting.Builder().name("coordinates").defaultValue(false).build());
    private final Setting<Boolean> timePassed = sgGeneral.add(new BoolSetting.Builder().name("time-passed").defaultValue(true).build());
    private final Setting<Boolean> armorCheck = sgGeneral.add(new BoolSetting.Builder().name("armor-check").defaultValue(false).build());

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder().name("scale").defaultValue(1).min(0.2).sliderRange(0.2, 2).build());
    private final Setting<Boolean> notification = sgGeneral.add(new BoolSetting.Builder().name("chat-info").defaultValue(true).build());

    // Render
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").defaultValue(ShapeMode.Both).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color.").defaultValue(new SettingColor(79, 90, 112, 10)).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color.").defaultValue(new SettingColor(79, 90, 112)).build());
    private final Setting<SettingColor> nameColor = sgRender.add(new ColorSetting.Builder().name("name-color").description("The name color.").defaultValue(new SettingColor(255, 255, 255)).build());
    private final Setting<SettingColor> nameBackgroundColor = sgRender.add(new ColorSetting.Builder().name("name-background-color").description("The name background color.").defaultValue(new SettingColor(0, 0, 0, 75)).build());

    private final List<Entry> players = new ArrayList<>();

    private final List<PlayerListEntry> lastPlayerList = new ArrayList<>();
    private final List<PlayerEntity> lastPlayers = new ArrayList<>();

    private int timer;
    private Dimension lastDimension;

    @Override
    public void onActivate() {
        lastPlayerList.addAll(mc.getNetworkHandler().getPlayerList());
        updateLastPlayers();

        timer = 10;
        lastDimension = PlayerUtils.getDimension();
    }

    @Override
    public void onDeactivate() {
        players.clear();
        lastPlayerList.clear();
    }

    private void updateLastPlayers() {
        lastPlayers.clear();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity) lastPlayers.add((PlayerEntity) entity);
        }
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (event.entity instanceof PlayerEntity) {
            int toRemove = -1;

            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).uuid.equals(event.entity.getUuid())) {
                    if (notification.get()) info(players.get(i).name + " Just logged back!", new Color(100, 100, 255));
                    toRemove = i;
                    break;
                }
            }

            if (toRemove != -1) {
                players.remove(toRemove);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        if (mc.getNetworkHandler().getPlayerList().size() != lastPlayerList.size()) {
            for (PlayerListEntry entry : lastPlayerList) {
                if (mc.getNetworkHandler().getPlayerList().stream().anyMatch(playerListEntry -> playerListEntry.getProfile().equals(entry.getProfile())))
                    continue;

                for (PlayerEntity player : lastPlayers) {
                    if (player.getUuid().equals(entry.getProfile().getId())) {
                        if (armorCheck.get()) {
                            for (int position = 3; position >= 0; position--) {
                                ItemStack itemStack = getItem(position, player);

                                if (itemStack.isEmpty()) return;
                            }
                        }
                        add(new Entry(player));
                    }
                }
            }

            lastPlayerList.clear();
            lastPlayerList.addAll(mc.getNetworkHandler().getPlayerList());
            updateLastPlayers();
        }

        if (timer <= 0) {
            updateLastPlayers();
            timer = 10;
        } else {
            timer--;
        }

        Dimension dimension = PlayerUtils.getDimension();
        if (dimension != lastDimension) players.clear();
        lastDimension = dimension;
    }

    private void add(Entry entry) {
        players.removeIf(player -> player.uuid.equals(entry.uuid));
        players.add(entry);
    }

    private ItemStack getItem(int i, PlayerEntity playerEntity) {
        if (playerEntity == null) return ItemStack.EMPTY;

        return switch (i) {
            case 4 -> playerEntity.getOffHandStack();
            case 5 -> playerEntity.getMainHandStack();
            default -> playerEntity.getInventory().getArmorStack(i);
        };
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for (Entry player : players) player.render3D(event);
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        for (Entry player : players) player.render2D();
    }

    private static final Vector3d pos = new Vector3d();

    private class Entry {
        public final double x, y, z;
        public final double xWidth, zWidth, halfWidth, height;

        public final TimerUtils passed = new TimerUtils();

        public final UUID uuid;
        public final String name;
        public final int health, maxHealth;
        public final String healthText;
        PlayerEntity entity;

        public Entry(PlayerEntity entity) {
            if (notification.get()) info(entity.getEntityName() + " Just logged out!");

            passed.reset();
            halfWidth = entity.getWidth() / 2;
            x = entity.getX() - halfWidth;
            y = entity.getY();
            z = entity.getZ() - halfWidth;

            xWidth = entity.getBoundingBox().getXLength();
            zWidth = entity.getBoundingBox().getZLength();
            height = entity.getBoundingBox().getYLength();

            this.entity = entity;

            uuid = entity.getUuid();
            name = entity.getEntityName();
            health = Math.round(entity.getHealth() + entity.getAbsorptionAmount());
            maxHealth = Math.round(entity.getMaxHealth() + entity.getAbsorptionAmount());

            healthText = " " + health;
        }

        public void render3D(Render3DEvent event) {
            WireframeEntityRenderer.render(event, entity, scale.get(), sideColor.get(), lineColor.get(), shapeMode.get());
        }

        public void render2D() {
            if (PlayerUtils.distanceToCamera(x, y, z) > mc.options.getViewDistance().getValue() * 16) return;

            TextRenderer text = TextRenderer.get();
            double s = scale.get();
            pos.set(x + halfWidth, y + height + 0.5, z + halfWidth);

            if (!NametagUtils.to2D(pos, s)) return;

            NametagUtils.begin(pos);

            String content = "";
            if (nameRender.get()) content = content + name;
            if (healthRender.get()) content = content + " " + healthText + "HP";
            if (coordRender.get())
                content = content + " (" + Math.round(entity.getX()) + " " + Math.round(entity.getY()) + " " + Math.round(entity.getZ()) + ")";
            if (timePassed.get()) content = content + " " + passed.getPassedTimeMs() / 1000 + "s";

            // Render background
            double i = text.getWidth(content) / 2;
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(-i, 0, i * 2, text.getHeight(), nameBackgroundColor.get());
            Renderer2D.COLOR.render(null);

            // Render name and health texts
            text.beginBig();
            if (nameRender.get()) text.render(content, -i, 0, nameColor.get());
            text.end();

            NametagUtils.end();
        }
    }
}
