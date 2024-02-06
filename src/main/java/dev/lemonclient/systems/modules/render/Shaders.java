package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.managers.impl.ShaderManager;
import dev.lemonclient.settings.ColorSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Shaders extends Module {
    public Shaders() {
        super(Categories.Render, "Shaders", "Customize rendering with shaders.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSelect = settings.createGroup("Select");
    private final SettingGroup sgColors = settings.createGroup("Colors");

    //--------------------General--------------------//
    public Setting<ShaderManager.Shader> mode = enumSetting(sgGeneral, "Mode", "Select the shader mode for rendering.", ShaderManager.Shader.Default);
    private final Setting<ShaderManager.Shader> handsMode = enumSetting(sgGeneral, "Hands Mode", "Select the shader mode for rendering hands.", ShaderManager.Shader.Default);
    public final Setting<Integer> maxRange = intSetting(sgGeneral, "Max Range", "Set the maximum rendering range.", 64, 16, 256);
    public final Setting<Double> factor = doubleSetting(sgGeneral, "Gradient Factor", "Adjust the gradient factor for rendering.", 2, 0, 20);
    public final Setting<Double> gradient = doubleSetting(sgGeneral, "Gradient", "Adjust the gradient for rendering.", 2, 0, 20);
    public final Setting<Integer> alpha2 = intSetting(sgGeneral, "Gradient Alpha", "Set the alpha value for the gradient.", 170, 0, 255);
    public final Setting<Integer> lineWidth = intSetting(sgGeneral, "Line Width", "Set the width of the lines.", 2, 0, 20);
    public final Setting<Integer> quality = intSetting(sgGeneral, "Quality", "Set the rendering quality.", 3, 0, 20);
    public final Setting<Integer> octaves = intSetting(sgGeneral, "Smoke Octaves", "Adjust the number of octaves for smoke rendering.", 10, 5, 30);
    public final Setting<Integer> fillAlpha = intSetting(sgGeneral, "Fill Alpha", "Set the alpha value for filling.", 170, 0, 255);
    public final Setting<Boolean> glow = boolSetting(sgGeneral, "Smoke Glow", "Enable or disable the glow effect for smoke rendering.", true);

    //--------------------sgSelect--------------------//
    private final Setting<Boolean> hands = boolSetting(sgSelect, "Hands", "Render hands with shaders.", true);
    private final Setting<Boolean> players = boolSetting(sgSelect, "Players", "Render players with shaders.", true);
    private final Setting<Boolean> friends = boolSetting(sgSelect, "Friends", "Render friends with shaders.", true);
    private final Setting<Boolean> crystals = boolSetting(sgSelect, "Crystals", "Render end crystals with shaders.", true);
    private final Setting<Boolean> creatures = boolSetting(sgSelect, "Creatures", "Render creatures with shaders.", false);
    private final Setting<Boolean> monsters = boolSetting(sgSelect, "Monsters", "Render monsters with shaders.", false);
    private final Setting<Boolean> ambients = boolSetting(sgSelect, "Ambients", "Render ambients with shaders.", false);
    private final Setting<Boolean> others = boolSetting(sgSelect, "Others", "Render other entities with shaders.", false);

    //--------------------Colors--------------------//
    public final Setting<SettingColor> outlineColor = sgColors.add(new ColorSetting.Builder()
        .name("Outline")
        .description("Select the color for rendering outlines.")
        .defaultValue(new SettingColor(255, 255, 0, 136))
        .build()
    );
    public final Setting<SettingColor> outlineColor1 = sgColors.add(new ColorSetting.Builder()
        .name("Smoke Outline")
        .description("Select the color for rendering smoke outlines.")
        .defaultValue(new SettingColor(255, 255, 0, 136))
        .build()
    );
    public final Setting<SettingColor> outlineColor2 = sgColors.add(new ColorSetting.Builder()
        .name("Smoke Outline2")
        .description("Select the color for rendering another smoke outline.")
        .defaultValue(new SettingColor(255, 255, 0, 136))
        .build()
    );
    public final Setting<SettingColor> fillColor1 = sgColors.add(new ColorSetting.Builder()
        .name("Fill")
        .description("Select the color for rendering fill.")
        .defaultValue(new SettingColor(255, 255, 0, 136))
        .build()
    );
    public final Setting<SettingColor> fillColor2 = sgColors.add(new ColorSetting.Builder()
        .name("Smoke Fill")
        .description("Select the color for rendering smoke fill.")
        .defaultValue(new SettingColor(255, 255, 0, 136))
        .build()
    );
    public final Setting<SettingColor> fillColor3 = sgColors.add(new ColorSetting.Builder()
        .name("Smoke Fill2")
        .description("Select the color for rendering another smoke fill.")
        .defaultValue(new SettingColor(255, 255, 0, 136))
        .build()
    );

    public boolean shouldRender(Entity entity) {
        if (entity == null) return false;
        if (mc.player == null) return false;
        if (Math.sqrt(mc.player.squaredDistanceTo(entity.getPos())) > maxRange.get()) return false;

        if (entity instanceof PlayerEntity) {
            if (entity == mc.player) return false;
            if (Friends.get().isFriend((PlayerEntity) entity)) return friends.get();

            return players.get();
        }

        if (entity instanceof EndCrystalEntity) return crystals.get();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> creatures.get();
            case MONSTER -> monsters.get();
            case AMBIENT, WATER_AMBIENT -> ambients.get();
            default -> others.get();
        };
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (hands.get()) {
            Managers.SHADER.renderShader(() -> mc.gameRenderer.renderHand(event.matrices, mc.gameRenderer.getCamera(), mc.getTickDelta()), handsMode.get());
        }
    }

    @Override
    public void onDeactivate() {
        Managers.SHADER.reloadShaders();
    }
}
