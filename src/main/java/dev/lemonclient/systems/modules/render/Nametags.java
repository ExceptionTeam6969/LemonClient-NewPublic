package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.NameProtect;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.render.NametagUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.RenderUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.opengl.GL11;

import java.lang.Math;
import java.util.*;

public class Nametags extends Module {
    public Nametags() {
        super(Categories.Render, "Nametags", "Displays customizable nametags above players.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlayers = settings.createGroup("Players");
    private final SettingGroup sgItems = settings.createGroup("Items");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select entities to draw nametags on.")
        .defaultValue(EntityType.PLAYER, EntityType.ITEM)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the nametag.")
        .defaultValue(1.1)
        .min(0.1)
        .build()
    );

    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-self")
        .description("Ignore yourself when in third person or freecam.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignore rendering nametags for friends.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreBots = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-bots")
        .description("Only render non-bot nametags.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> culling = sgGeneral.add(new BoolSetting.Builder()
        .name("culling")
        .description("Only render a certain number of nametags at a certain distance.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> maxCullRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("culling-range")
        .description("Only render nametags within this distance of your player.")
        .defaultValue(20)
        .min(0)
        .sliderMax(200)
        .visible(culling::get)
        .build()
    );

    private final Setting<Integer> maxCullCount = sgGeneral.add(new IntSetting.Builder()
        .name("culling-count")
        .description("Only render this many nametags.")
        .defaultValue(50)
        .min(1)
        .sliderRange(1, 100)
        .visible(culling::get)
        .build()
    );

    //Players

    private final Setting<Boolean> displayHealth = sgPlayers.add(new BoolSetting.Builder()
        .name("health")
        .description("Shows the player's health.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> displayGameMode = sgPlayers.add(new BoolSetting.Builder()
        .name("gamemode")
        .description("Shows the player's GameMode.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> displayDistance = sgPlayers.add(new BoolSetting.Builder()
        .name("distance")
        .description("Shows the distance between you and the player.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> displayPops = sgPlayers.add(new BoolSetting.Builder()
        .name("Totem Pops")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> displayPing = sgPlayers.add(new BoolSetting.Builder()
        .name("ping")
        .description("Shows the player's ping.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> displayItems = sgPlayers.add(new BoolSetting.Builder()
        .name("items")
        .description("Displays armor and hand items above the name tags.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> itemSpacing = sgPlayers.add(new DoubleSetting.Builder()
        .name("item-spacing")
        .description("The spacing between items.")
        .defaultValue(2)
        .range(0, 10)
        .visible(displayItems::get)
        .build()
    );

    private final Setting<Boolean> ignoreEmpty = sgPlayers.add(new BoolSetting.Builder()
        .name("ignore-empty-slots")
        .description("Doesn't add spacing where an empty item stack would be.")
        .defaultValue(true)
        .visible(displayItems::get)
        .build()
    );

    private final Setting<Boolean> displayEnchants = sgPlayers.add(new BoolSetting.Builder()
        .name("display-enchants")
        .description("Displays item enchantments on the items.")
        .defaultValue(false)
        .visible(displayItems::get)
        .build()
    );

    private final Setting<List<Enchantment>> shownEnchantments = sgPlayers.add(new EnchantmentListSetting.Builder()
        .name("shown-enchantments")
        .description("The enchantments that are shown on nametags.")
        .visible(() -> displayItems.get() && displayEnchants.get())
        .defaultValue(
            Enchantments.PROTECTION,
            Enchantments.BLAST_PROTECTION,
            Enchantments.FIRE_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION
        )
        .build()
    );

    private final Setting<Position> enchantPos = sgPlayers.add(new EnumSetting.Builder<Position>()
        .name("enchantment-position")
        .description("Where the enchantments are rendered.")
        .defaultValue(Position.Above)
        .visible(() -> displayItems.get() && displayEnchants.get())
        .build()
    );

    private final Setting<Integer> enchantLength = sgPlayers.add(new IntSetting.Builder()
        .name("enchant-name-length")
        .description("The length enchantment names are trimmed to.")
        .defaultValue(3)
        .range(1, 5)
        .sliderRange(1, 5)
        .visible(() -> displayItems.get() && displayEnchants.get())
        .build()
    );

    private final Setting<Double> enchantTextScale = sgPlayers.add(new DoubleSetting.Builder()
        .name("enchant-text-scale")
        .description("The scale of the enchantment text.")
        .defaultValue(1)
        .range(0.1, 2)
        .sliderRange(0.1, 2)
        .visible(() -> displayItems.get() && displayEnchants.get())
        .build()
    );
    private final Setting<Boolean> box = sgPlayers.add(new BoolSetting.Builder()
        .name("Box")
        .description(".")
        .defaultValue(true)
        .build()
    );

    // Items
    private final Setting<Double> itemScale = sgItems.add(new DoubleSetting.Builder()
        .name("Item Scale")
        .description("The scale of the item tags.")
        .defaultValue(0.6)
        .min(0.1)
        .build()
    );
    private final Setting<Boolean> itemCount = sgItems.add(new BoolSetting.Builder()
        .name("Show Count")
        .description("Displays the number of items in the stack.")
        .defaultValue(true)
        .build()
    );

    // Render

    private final Setting<SettingColor> background = sgRender.add(new ColorSetting.Builder()
        .name("Background Color")
        .description("The color of the nametag background.")
        .defaultValue(new SettingColor(0, 0, 0, 75))
        .build()
    );
    private final Setting<SettingColor> outline = sgRender.add(new ColorSetting.Builder()
        .name("Outline Color")
        .description("The color of the nametag outline.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<SettingColor> boxHealthColor = sgRender.add(new ColorSetting.Builder()
        .name("Box Health Color")
        .description("The color of the box health.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> nameColor = sgRender.add(new ColorSetting.Builder()
        .name("name-color")
        .description("The color of the nametag names.")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<SettingColor> pingColor = sgRender.add(new ColorSetting.Builder()
        .name("ping-color")
        .description("The color of the nametag ping.")
        .defaultValue(new SettingColor(20, 170, 170))
        .visible(displayPing::get)
        .build()
    );

    private final Setting<SettingColor> gamemodeColor = sgRender.add(new ColorSetting.Builder()
        .name("gamemode-color")
        .description("The color of the nametag gamemode.")
        .defaultValue(new SettingColor(232, 185, 35))
        .visible(displayGameMode::get)
        .build()
    );

    private final Setting<DistanceColorMode> distanceColorMode = sgRender.add(new EnumSetting.Builder<DistanceColorMode>()
        .name("distance-color-mode")
        .description("The mode to color the nametag distance with.")
        .defaultValue(DistanceColorMode.Gradient)
        .visible(displayDistance::get)
        .build()
    );

    private final Setting<SettingColor> distanceColor = sgRender.add(new ColorSetting.Builder()
        .name("distance-color")
        .description("The color of the nametag distance.")
        .defaultValue(new SettingColor(150, 150, 150))
        .visible(() -> displayDistance.get() && distanceColorMode.get() == DistanceColorMode.Flat)
        .build()
    );
    private final Setting<SettingColor> popsColor = sgRender.add(new ColorSetting.Builder()
        .name("Pops color")
        .description("The color of the nametag pops.")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(displayPops::get)
        .build()
    );

    private final Color WHITE = new Color(255, 255, 255);
    private final Color RED = new Color(255, 25, 25);
    private final Color AMBER = new Color(255, 105, 25);
    private final Color GREEN = new Color(25, 252, 25);
    private final Color GOLD = new Color(232, 185, 35);

    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    private final Vector3d pos = new Vector3d();
    private final double[] itemWidths = new double[6];

    private final List<Entity> entityList = new ArrayList<>();

    private static String ticksToTime(int ticks) {
        if (ticks > 20 * 3600) {
            int h = ticks / 20 / 3600;
            return h + " h";
        } else if (ticks > 20 * 60) {
            int m = ticks / 20 / 60;
            return m + " m";
        } else {
            int s = ticks / 20;
            int ms = (ticks % 20) / 2;
            return s + "." + ms + " s";
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        entityList.clear();

        boolean freecamNotActive = !Modules.get().isActive(Freecam.class);
        boolean notThirdPerson = mc.options.getPerspective().isFirstPerson();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        for (Entity entity : mc.world.getEntities()) {
            EntityType<?> type = entity.getType();
            if (!entities.get().contains(type)) continue;

            if (type == EntityType.PLAYER) {
                if ((ignoreSelf.get() || (freecamNotActive && notThirdPerson)) && entity == mc.player) continue;
                if (EntityUtils.getGameMode((PlayerEntity) entity) == null && ignoreBots.get()) continue;
                if (Friends.get().isFriend((PlayerEntity) entity) && ignoreFriends.get()) continue;
            }

            if (!culling.get() || PlayerUtils.isWithinCamera(entity, maxCullRange.get())) {
                entityList.add(entity);
            }
        }

        entityList.sort(Comparator.comparing(e -> e.squaredDistanceTo(cameraPos)));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        int count = getRenderCount();
        boolean shadow = Config.get().customFont.get();

        for (int i = count - 1; i > -1; i--) {
            Entity entity = entityList.get(i);

            Utils.set(pos, entity, event.tickDelta);
            pos.add(0, getHeight(entity), 0);

            EntityType<?> type = entity.getType();

            if (NametagUtils.to2D(pos, scale.get())) {
                if (type == EntityType.PLAYER) {
                    renderNametagPlayer(event, (PlayerEntity) entity, shadow);
                } else if (type == EntityType.TNT) {
                    renderTntNametag((TntEntity) entity, shadow);
                } else if (entity instanceof LivingEntity) {
                    renderGenericNametag((LivingEntity) entity, shadow);
                }
            }

            if (NametagUtils.to2D(pos, itemScale.get())) {
                if (type == EntityType.ITEM) {
                    renderNametagItem(((ItemEntity) entity).getStack(), shadow);
                } else if (type == EntityType.ITEM_FRAME) {
                    renderNametagItem(((ItemFrameEntity) entity).getHeldItemStack(), shadow);
                }
            }
        }
    }

    @Override
    public void onRender2D(DrawContext context) {
        for (int i = MathHelper.clamp(culling.get() ? maxCullCount.get() : mc.world.getPlayers().size(), 0, mc.world.getPlayers().size()) - 1; i > -1; i--) {
            PlayerEntity player = mc.world.getPlayers().get(i);
            if ((ignoreSelf.get() || (!Modules.get().isActive(Freecam.class) && mc.options.getPerspective().isFirstPerson())) && player == mc.player)
                continue;
            if (ignoreBots.get() && EntityUtils.getGameMode(player) == null) continue;

            if (box.get()) drawBox(player, context);
        }
    }

    private @NotNull Vec3d worldSpaceToScreenSpace(@NotNull Vec3d pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
    }

    public void drawBox(PlayerEntity ent, DrawContext context) {
        double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
        double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
        double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
        Box axisAlignedBB2 = ent.getBoundingBox();
        Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
        Vec3d[] vectors = new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};

        Vector4d position = null;
        for (Vec3d vector : vectors) {
            vector = worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector != null && vector.z > 0 && vector.z < 1) {
                if (position == null) position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);
                position.w = Math.max(vector.y, position.w);
            }
        }

        if (position != null) {
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;

            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (posX - 1F), (float) posY, (float) (posX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (posX - 1F), (float) (posY - 0.5), (float) (endPosX + 0.5), (float) (posY + 0.5 + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (endPosX - 0.5 - 0.5), (float) posY, (float) (endPosX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (posX - 1), (float) (endPosY - 0.5 - 0.5), (float) (endPosX + 0.5), (float) (endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);

            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (posX - 0.5f), (float) posY, (float) (posX + 0.5 - 0.5), (float) endPosY, new Color(-6974059), new Color(-6974059), new Color(-6974059), new Color(-6974059));
            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) posX, (float) (endPosY - 0.5f), (float) endPosX, (float) endPosY, new Color(-6974059), new Color(-6974059), new Color(-6974059), new Color(-6974059));
            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (posX - 0.5), (float) posY, (float) endPosX, (float) (posY + 0.5), new Color(-6974059), new Color(-6974059), new Color(-6974059), new Color(-6974059));
            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (endPosX - 0.5), (float) posY, (float) endPosX, (float) endPosY, new Color(-6974059), new Color(-6974059), new Color(-6974059), new Color(-6974059));

            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (posX - 5), (float) posY, (float) posX - 3, (float) endPosY, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DUtils.drawRectDumbWay(context.getMatrices(), (float) (posX - 5), (float) (endPosY + (posY - endPosY) * ent.getHealth() / 20f), (float) posX - 3, (float) endPosY, boxHealthColor.get(), boxHealthColor.get(), boxHealthColor.get(), boxHealthColor.get());
        }
    }

    private int getRenderCount() {
        int count = culling.get() ? maxCullCount.get() : entityList.size();
        count = MathHelper.clamp(count, 0, entityList.size());

        return count;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(getRenderCount());
    }

    private double getHeight(Entity entity) {
        double height = entity.getEyeHeight(entity.getPose());

        if (entity.getType() == EntityType.ITEM || entity.getType() == EntityType.ITEM_FRAME) height += 0.2;
        else height += 0.5;

        return height;
    }

    private void renderNametagPlayer(Render2DEvent event, PlayerEntity player, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos, event.drawContext);

        // Gamemode
        GameMode gm = EntityUtils.getGameMode(player);
        String gmText = "BOT";
        if (gm != null) {
            gmText = switch (gm) {
                case SPECTATOR -> "Sp";
                case SURVIVAL -> "S";
                case CREATIVE -> "C";
                case ADVENTURE -> "A";
            };
        }

        gmText = "[" + gmText + "] ";

        // Name
        String name;
        Color nameColor = PlayerUtils.getPlayerColor(player, this.nameColor.get());

        if (player == mc.player) name = Modules.get().get(NameProtect.class).getName(player.getEntityName());
        else name = player.getEntityName();

        // Health
        float absorption = player.getAbsorptionAmount();
        int health = Math.round(player.getHealth() + absorption);
        double healthPercentage = health / (player.getMaxHealth() + absorption);

        String healthText = " " + health;
        Color healthColor;

        if (healthPercentage <= 0.333) healthColor = RED;
        else if (healthPercentage <= 0.666) healthColor = AMBER;
        else healthColor = GREEN;

        // Ping
        int ping = EntityUtils.getPing(player);
        String pingText = " [" + ping + "ms]";

        // Distance
        double dist = Math.round(PlayerUtils.distanceToCamera(player) * 10.0) / 10.0;
        String distText = " " + dist + "m";

        // Pops
        int pops = Managers.PLAYER.getPops(player);
        String popsText = " " + pops + " Pops";

        // Calc widths
        double gmWidth = text.getWidth(gmText, shadow);
        double nameWidth = text.getWidth(name, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double pingWidth = text.getWidth(pingText, shadow);
        double distWidth = text.getWidth(distText, shadow);
        double popsWidth = text.getWidth(popsText, shadow);

        double width = nameWidth;

        boolean renderPlayerDistance = player != mc.cameraEntity || Modules.get().isActive(Freecam.class);

        boolean renderPops = Managers.PLAYER.getPops(player) != 0;

        if (displayHealth.get()) width += healthWidth;
        if (displayGameMode.get()) width += gmWidth;
        if (displayPing.get()) width += pingWidth;
        if (displayDistance.get() && renderPlayerDistance) width += distWidth;
        if (displayPops.get() && renderPops) width += popsWidth;

        double widthHalf = width / 2;
        double heightDown = text.getHeight(shadow);

        drawBackground(-widthHalf, -heightDown, width, heightDown);
        drawOutline(-widthHalf, -heightDown, width, heightDown);

        // Render texts
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        if (displayGameMode.get()) hX = text.render(gmText, hX, hY, gamemodeColor.get(), shadow);
        hX = text.render(name, hX, hY, nameColor, shadow);

        if (displayHealth.get()) hX = text.render(healthText, hX, hY, healthColor, shadow);
        if (displayPing.get()) hX = text.render(pingText, hX, hY, pingColor.get(), shadow);
        if (displayDistance.get() && renderPlayerDistance) {
            hX = switch (distanceColorMode.get()) {
                case Flat -> text.render(distText, hX, hY, distanceColor.get(), shadow);
                case Gradient -> text.render(distText, hX, hY, EntityUtils.getColorFromDistance(player), shadow);
            };
        }
        if (displayPops.get() && renderPops) text.render(popsText, hX, hY, popsColor.get(), shadow);

        text.end();

        if (displayItems.get()) {
            // Item calc
            Arrays.fill(itemWidths, 0);
            boolean hasItems = false;
            int maxEnchantCount = 0;

            for (int i = 0; i < 6; i++) {
                ItemStack itemStack = getItem(player, i);

                // Setting up widths
                if (itemWidths[i] == 0 && (!ignoreEmpty.get() || !itemStack.isEmpty()))
                    itemWidths[i] = 32 + itemSpacing.get();

                if (!itemStack.isEmpty()) hasItems = true;

                if (displayEnchants.get()) {
                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack);

                    int size = 0;
                    for (var enchantment : enchantments.keySet()) {
                        if (!shownEnchantments.get().contains(enchantment)) continue;
                        String enchantName = Utils.getEnchantSimpleName(enchantment, enchantLength.get()) + " " + enchantments.get(enchantment);
                        itemWidths[i] = Math.max(itemWidths[i], (text.getWidth(enchantName, shadow) / 2));
                        size++;
                    }

                    maxEnchantCount = Math.max(maxEnchantCount, size);
                }
            }

            double itemsHeight = (hasItems ? 32 : 0);
            double itemWidthTotal = 0;
            for (double w : itemWidths) itemWidthTotal += w;
            double itemWidthHalf = itemWidthTotal / 2;

            double y = -heightDown - 7 - itemsHeight;
            double x = -itemWidthHalf;

            // Rendering items and enchants
            for (int i = 0; i < 6; i++) {
                ItemStack stack = getItem(player, i);

                RenderUtils.drawItem(event.drawContext, stack, (int) x, (int) y, 2, true);

                if (maxEnchantCount > 0 && displayEnchants.get()) {
                    text.begin(0.5 * enchantTextScale.get(), false, true);

                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
                    Map<Enchantment, Integer> enchantmentsToShow = new HashMap<>();

                    for (Enchantment enchantment : enchantments.keySet()) {
                        if (shownEnchantments.get().contains(enchantment)) {
                            enchantmentsToShow.put(enchantment, enchantments.get(enchantment));
                        }
                    }

                    double aW = itemWidths[i];
                    double enchantY = 0;

                    double addY = switch (enchantPos.get()) {
                        case Above -> -((enchantmentsToShow.size() + 1) * text.getHeight(shadow));
                        case OnTop -> (itemsHeight - enchantmentsToShow.size() * text.getHeight(shadow)) / 2;
                    };

                    double enchantX;

                    for (Enchantment enchantment : enchantmentsToShow.keySet()) {
                        String enchantName = Utils.getEnchantSimpleName(enchantment, enchantLength.get()) + " " + enchantmentsToShow.get(enchantment);

                        Color enchantColor = WHITE;
                        if (enchantment.isCursed()) enchantColor = RED;

                        enchantX = switch (enchantPos.get()) {
                            case Above -> x + (aW / 2) - (text.getWidth(enchantName, shadow) / 2);
                            case OnTop -> x + (aW - text.getWidth(enchantName, shadow)) / 2;
                        };

                        text.render(enchantName, enchantX, y + addY + enchantY, enchantColor, shadow);

                        enchantY += text.getHeight(shadow);
                    }

                    text.end();
                }

                x += itemWidths[i];
            }
        } else if (displayEnchants.get()) displayEnchants.set(false);

        NametagUtils.end(event.drawContext);
    }

    private void renderNametagItem(ItemStack stack, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);

        String name = stack.getName().getString();
        String count = " x" + stack.getCount();

        double nameWidth = text.getWidth(name, shadow);
        double countWidth = text.getWidth(count, shadow);
        double heightDown = text.getHeight(shadow);

        double width = nameWidth;
        if (itemCount.get()) width += countWidth;
        double widthHalf = width / 2;

        drawBackground(-widthHalf, -heightDown, width, heightDown);
        drawOutline(-widthHalf, -heightDown, width, heightDown);

        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        hX = text.render(name, hX, hY, nameColor.get(), shadow);
        if (itemCount.get()) text.render(count, hX, hY, GOLD, shadow);
        text.end();

        NametagUtils.end();
    }

    private void renderGenericNametag(LivingEntity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);

        //Name
        String nameText = entity.getType().getName().getString();
        nameText += " ";

        //Health
        float absorption = entity.getAbsorptionAmount();
        int health = Math.round(entity.getHealth() + absorption);
        double healthPercentage = health / (entity.getMaxHealth() + absorption);

        String healthText = String.valueOf(health);
        Color healthColor;

        if (healthPercentage <= 0.333) healthColor = RED;
        else if (healthPercentage <= 0.666) healthColor = AMBER;
        else healthColor = GREEN;

        double nameWidth = text.getWidth(nameText, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double heightDown = text.getHeight(shadow);

        double width = nameWidth + healthWidth;
        double widthHalf = width / 2;

        drawBackground(-widthHalf, -heightDown, width, heightDown);
        drawOutline(-widthHalf, -heightDown, width, heightDown);

        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        hX = text.render(nameText, hX, hY, nameColor.get(), shadow);
        text.render(healthText, hX, hY, healthColor, shadow);
        text.end();

        NametagUtils.end();
    }

    private void renderTntNametag(TntEntity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);

        String fuseText = ticksToTime(entity.getFuse());

        double width = text.getWidth(fuseText, shadow);
        double heightDown = text.getHeight(shadow);

        double widthHalf = width / 2;

        drawBackground(-widthHalf, -heightDown, width, heightDown);
        drawOutline(-widthHalf, -heightDown, width, heightDown);

        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;

        text.render(fuseText, hX, hY, nameColor.get(), shadow);
        text.end();

        NametagUtils.end();
    }

    private ItemStack getItem(PlayerEntity entity, int index) {
        return switch (index) {
            case 0 -> entity.getMainHandStack();
            case 1 -> entity.getInventory().armor.get(3);
            case 2 -> entity.getInventory().armor.get(2);
            case 3 -> entity.getInventory().armor.get(1);
            case 4 -> entity.getInventory().armor.get(0);
            case 5 -> entity.getOffHandStack();
            default -> ItemStack.EMPTY;
        };
    }

    private void drawBackground(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1, y - 1, width + 2, height + 2, background.get());
        Renderer2D.COLOR.render(null);
    }

    private void drawOutline(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.boxLines(x - 1.5, y - 1.5, width + 2.5, height + 2.5, outline.get());
        Renderer2D.COLOR.render(null, 2.0f);
    }

    public enum Position {
        Above,
        OnTop
    }

    public enum DistanceColorMode {
        Gradient,
        Flat
    }

    public boolean excludeBots() {
        return ignoreBots.get();
    }

    public boolean playerNametags() {
        return isActive() && entities.get().contains(EntityType.PLAYER);
    }
}
