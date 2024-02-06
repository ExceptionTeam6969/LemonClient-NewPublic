package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.TimeBomber;
import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.player.AutoMine;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.EntityUtils;
import dev.lemonclient.utils.misc.ExtrapolationUtils;
import dev.lemonclient.utils.player.DamageInfo;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.NametagUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.Render3DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.TimerUtils;
import dev.lemonclient.utils.world.BlockInfo;
import dev.lemonclient.utils.world.BlockUtils;
import dev.lemonclient.utils.world.PlaceData;
import dev.lemonclient.utils.world.hole.HoleUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import org.joml.Vector3d;

import java.text.NumberFormat;
import java.util.*;

public class AutoAnchor extends Module {
    public AutoAnchor() {
        super(Categories.Combat, "Auto Anchor", "Automatically destroys people using anchors.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgExtrapolation = settings.createGroup("Extrapolation");
    private final SettingGroup sgCompatibility = settings.createGroup("Compatibility");
    private final SettingGroup sgText = settings.createGroup("Text");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgDebug = settings.createGroup("Debug");

    //--------------------General--------------------//
    private final Setting<Boolean> toggleModules = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle Modules")
        .description("Turn off other modules when Cev Breaker is activated.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> toggleBack = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle Back On")
        .description("Turn the modules back on when Cev Breaker is deactivated.")
        .defaultValue(false)
        .visible(toggleModules::get)
        .build()
    );
    private final Setting<List<Module>> modules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("Modules")
        .description("Which modules to toggle.")
        .visible(toggleModules::get)
        .build()
    );
    private final Setting<Boolean> checkDimension = sgGeneral.add(new BoolSetting.Builder()
        .name("Dimension Check")
        .description("Check player dimensions when module active.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Eat")
        .description("Pauses when you are eating.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Switching method. Silent is the most reliable but doesn't work everywhere.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Boolean> helpOnSurround = sgGeneral.add(new BoolSetting.Builder()
        .name("Help On Surround")
        .description("Automatically places obsidian to assist with placement of respawn anchors.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0)
        .min(0)
        .sliderMax(100)
        .visible(helpOnSurround::get)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<Double> placeSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Place Speed")
        .description("How many anchors should be blown every second.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> explodeSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Explode Speed")
        .description("How many anchors should be explode.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> dynamicSpeed = sgSpeed.add(new BoolSetting.Builder()
        .name("Dynamic Speed")
        .description("Dynamic place speed based on target movement.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> dynamicHealth = sgSpeed.add(new BoolSetting.Builder()
        .name("Dynamic Health")
        .description("Dynamic place speed based on target health.")
        .defaultValue(false)
        .visible(dynamicSpeed::get)
        .build()
    );
    private final Setting<Double> dynamicHealthValue = sgSpeed.add(new DoubleSetting.Builder()
        .name("Dynamic Max Health")
        .description("Maximum health value of dynamic speed.")
        .defaultValue(12)
        .min(0)
        .sliderRange(0, 36)
        .visible(dynamicHealth::get)
        .build()
    );
    private final Setting<Integer> dynamicPlayerMixSpeed = sgSpeed.add(new IntSetting.Builder()
        .name("Dynamic Player Mix Speed")
        .defaultValue(5)
        .sliderRange(0, 1000)
        .visible(dynamicSpeed::get)
        .build()
    );
    private final Setting<Double> dynamicPlaceSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Dynamic Place Speed")
        .description("How many anchors should be blown every second on dynamic.")
        .defaultValue(9.0)
        .min(0)
        .sliderRange(0, 20)
        .visible(dynamicSpeed::get)
        .build()
    );
    private final Setting<Boolean> dynamicDamage = sgSpeed.add(new BoolSetting.Builder()
        .name("Dynamic Damage")
        .description("Dynamic speed when target receives higher damage.")
        .defaultValue(false)
        .visible(dynamicSpeed::get)
        .build()
    );
    private final Setting<Double> damagePlaceSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Damage Place Speed Factor")
        .description("Sets speed to damage multiplied by factor.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 20)
        .visible(dynamicDamage::get)
        .build()
    );
    private final Setting<Double> damagePlaceMaxSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Damage Place Max Speed")
        .description("Maximum speed for damage mode.")
        .defaultValue(12)
        .min(0)
        .sliderRange(0, 20)
        .visible(dynamicDamage::get)
        .build()
    );
    private final Setting<Double> damageExplodeSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Damage Explode Speed Factor")
        .description("Sets speed to damage multiplied by factor.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 20)
        .visible(dynamicDamage::get)
        .build()
    );
    private final Setting<Double> damageExplodeMaxSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Damage Explode Max Speed")
        .description("Maximum speed for damage mode.")
        .defaultValue(12)
        .min(0)
        .sliderRange(0, 20)
        .visible(dynamicDamage::get)
        .build()
    );
    private final Setting<Double> dynamicExplodeSpeed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Dynamic Explode Speed")
        .description("How many anchors should be explode on dynamic.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Damage--------------------//
    private final Setting<Double> minTargetDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Min Target Damage")
        .description("Minimum damage required to place.")
        .defaultValue(5.0)
        .min(0.0)
        .sliderRange(0.0, 20.0)
        .build()
    );
    private final Setting<Double> maxSelfDmg = sgDamage.add(new DoubleSetting.Builder()
        .name("Max Self Damage")
        .description("Maximum damage to self.")
        .defaultValue(6.0)
        .min(0.0)
        .sliderRange(0.0, 20.0)
        .build()
    );

    //--------------------Extrapolation--------------------//
    private final Setting<Integer> selfExt = sgExtrapolation.add(new IntSetting.Builder()
        .name("Self Extrapolation")
        .description("How many ticks of movement should be predicted for self damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("How many ticks of movement should be predicted for enemy damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description("How many earlier ticks should be used in average calculation for extrapolation motion.")
        .defaultValue(2)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    //--------------------Compatibility--------------------//
    private final Setting<Double> autoMineDamage = sgCompatibility.add(new DoubleSetting.Builder()
        .name("Auto Mine Damage")
        .description("Prioritizes placing on automine target block.")
        .defaultValue(1.1)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );
    private final Setting<Boolean> amPlace = sgCompatibility.add(new BoolSetting.Builder()
        .name("Auto Mine Place")
        .description("Ignores automine block before if actually breaks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> amProgress = sgCompatibility.add(new DoubleSetting.Builder()
        .name("Auto Mine Progress")
        .description("Ignores the block after it has reached this progress.")
        .defaultValue(0.95)
        .range(0, 1)
        .sliderRange(0, 1)
        .visible(amPlace::get)
        .build()
    );
    private final Setting<AutoMineBrokenMode> amBroken = sgCompatibility.add(new EnumSetting.Builder<AutoMineBrokenMode>()
        .name("Auto Mine Broken")
        .description("Doesn't place on automine block.")
        .defaultValue(AutoMineBrokenMode.Near)
        .build()
    );

    //--------------------Text--------------------//
    private final Setting<Boolean> renderDmg = sgText.add(new BoolSetting.Builder()
        .name("Render Text Damage")
        .description("2D rendering of player and enemy damage.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> scale = sgText.add(new DoubleSetting.Builder()
        .name("Scale")
        .defaultValue(1.0)
        .sliderRange(0.1, 2.0)
        .build()
    );
    private final Setting<Integer> decimal = sgText.add(new IntSetting.Builder()
        .name("Decimal")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<SettingColor> damageColor = sgText.add(new ColorSetting.Builder()
        .name("Damage Text Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(renderDmg::get)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Place Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> interactSwing = sgRender.add(new BoolSetting.Builder()
        .name("Interact Swing")
        .description("Renders swing animation when interacting with a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> interactHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Interact Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(interactSwing::get)
        .build()
    );
    private final Setting<Boolean> renderTargetEsp = sgRender.add(new BoolSetting.Builder()
        .name("Render Target")
        .description("Render on target.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Color")
        .description(COLOR)
        .defaultValue(new SettingColor(149, 149, 149, 170))
        .visible(renderTargetEsp::get)
        .build()
    );
    private final Setting<Boolean> renderHelper = sgRender.add(new BoolSetting.Builder()
        .name("Render Helper")
        .description("Renders the block where it is placed.")
        .defaultValue(true)
        .visible(helpOnSurround::get)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Helper Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Helper Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> helperShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Helper Shape Mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Sides)
        .visible(() -> helpOnSurround.get() && renderHelper.get())
        .build()
    );
    private final Setting<SettingColor> helperLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Helper Line Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> helpOnSurround.get() && renderHelper.get() && helperShapeMode.get().lines())
        .build()
    );
    private final Setting<SettingColor> helperSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Helper Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> helpOnSurround.get() && renderHelper.get() && helperShapeMode.get().sides())
        .build()
    );
    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description("What should the render look like.")
        .defaultValue(RenderMode.Normal)
        .build()
    );
    private final Setting<FadeMode> fadeMode = sgRender.add(new EnumSetting.Builder<FadeMode>()
        .name("Fade Mode")
        .description("How long the fading should take.")
        .defaultValue(FadeMode.Normal)
        .visible(() -> renderMode.get().equals(RenderMode.Smooth))
        .build()
    );
    private final Setting<Boolean> expansion = sgRender.add(new BoolSetting.Builder()
        .name("Expansion")
        .description("Slowly expand the box at the beginning of render.")
        .defaultValue(true)
        .visible(() -> renderMode.get().equals(RenderMode.Smooth))
        .build()
    );
    private final Setting<Double> animationSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Move Speed")
        .description("How fast should box move.")
        .defaultValue(1.0)
        .min(0.0)
        .sliderRange(0.0, 10.0)
        .visible(() -> renderMode.get().equals(RenderMode.Smooth))
        .build()
    );
    private final Setting<Double> animationMoveExponent = sgRender.add(new DoubleSetting.Builder()
        .name("Longer Animation Move Speed")
        .description("Moves faster when longer away from the target.")
        .defaultValue(2.0)
        .min(0.0)
        .sliderRange(0.0, 10.0)
        .visible(() -> renderMode.get().equals(RenderMode.Smooth))
        .build()
    );
    private final Setting<Double> animationExponent = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Speed")
        .description("How fast should box grow.")
        .defaultValue(3.0)
        .min(0.0)
        .sliderRange(0.0, 10.0)
        .visible(() -> renderMode.get().equals(RenderMode.Smooth))
        .build()
    );
    public final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts should be renderer.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of rendered stuff")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered stuff")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );

    //--------------------Debug--------------------//
    private final Setting<Boolean> renderExt = sgDebug.add(new BoolSetting.Builder()
        .name("Render Extrapolation")
        .description("Renders boxes at players' predicted positions.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> renderSelfExt = sgDebug.add(new BoolSetting.Builder()
        .name("Render Self Extrapolation")
        .description("Renders box at your predicted position.")
        .defaultValue(false)
        .build()
    );

    private long tickTime = -1L;
    private long lastTime = 0;

    private double bestDmg = -1.0;
    private double selfDmg = -1.0;
    private double delta = 0.0;
    private double renderProgress = 0.0;

    private BlockPos calcPos = null;
    private BlockPos renderPos = null;
    public static BlockPos placePos = null;

    private Vec3d renderTarget = null;
    public Vec3d renderPosV = null;

    private PlaceData placeData = null;
    private PlaceData calcData = null;

    private double placeTimer = 0;
    private double explodeTimer = 0;
    private final TimerUtils timer = new TimerUtils();

    private final Map<BlockPos, Anchor> anchors = new HashMap<>();
    private final Map<AbstractClientPlayerEntity, Box> extPos = new HashMap<>();
    private List<PlayerEntity> targets = new ArrayList<>();
    private final List<Render> renderPlacing = new ArrayList<>();
    private final ArrayList<Module> toActivate = new ArrayList<>();

    private AutoMine autoMine = null;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPre(TickEvent.Post event) {
        renderPos = calcPos;
        placePos = calcPos;
        placeData = calcData;
        tickTime = System.currentTimeMillis();
        bestDmg = -1.0;
        selfDmg = -1.0;
        calcPos = null;
        calcData = null;
        timer.reset();
        autoMine = Modules.get().get(AutoMine.class);

        updateTargets();

        ExtrapolationUtils.extrapolateMap(extPos, player -> player == mc.player ? selfExt.get() : extrapolation.get(), player -> extSmoothness.get());
    }

    @Override
    public void onActivate() {
        if (toggleModules.get() && !modules.get().isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : modules.get()) {
                if (module.isActive()) {
                    module.toggle();
                    toActivate.add(module);
                }
            }
        }

        anchors.clear();
        extPos.clear();
        placePos = null;
        placeData = null;
        calcPos = null;
        calcData = null;
        renderPos = null;
        renderPosV = null;
        renderProgress = 0.0;
    }

    @Override
    public void onDeactivate() {
        if (toggleBack.get() && !toActivate.isEmpty() && mc.world != null && mc.player != null) {
            for (Module module : toActivate) {
                if (!module.isActive()) {
                    module.toggle();
                }
            }
        }
    }

    @Override
    public String getInfoString() {
        return targets.stream().map(target -> target.getGameProfile().getName()).findFirst().orElse(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (checkDimension.get() && mc.world.getDimension().respawnAnchorWorks()) {
            toggle();
            sendDisableMsg("you are in the nether");
            return;
        }

        delta = (float) (System.currentTimeMillis() - lastTime) / 1000.0f;
        placeTimer += delta;
        explodeTimer += delta;
        lastTime = System.currentTimeMillis();

        if (tickTime < 0L) {
            return;
        }

        if (!TimeBomber.shouldBomb()) {
            updatePos();
        }

        if (shouldPause()) {
            update();
        }

        List<BlockPos> toRemove = new ArrayList<>();
        anchors.forEach((pos, anchor) -> {
            if (System.currentTimeMillis() - anchor.time > 500L) {
                toRemove.add(pos);
            }
        });
        toRemove.forEach(anchors::remove);

        targets.stream().filter(target -> !targets.isEmpty() && renderTargetEsp.get() && placePos != null && shouldPause()).forEach(target -> Render3DUtils.drawJello(event.matrices, target, color.get()));

        if (renderHelper.get()) {
            renderPlacing.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

            renderPlacing.forEach(r -> {
                double progress = 1 - Math.min(System.currentTimeMillis() - r.time + renderTime.get() * 1000, fadeTime.get() * 1000) / (fadeTime.get() * 1000d);

                event.renderer.box(r.pos, Render2DUtils.injectAlpha(helperSideColor.get(), (int) Math.round(helperSideColor.get().a * progress)), Render2DUtils.injectAlpha(helperLineColor.get(), (int) Math.round(helperLineColor.get().a * progress)), helperShapeMode.get(), 0);
            });
        }

        switch (renderMode.get()) {
            case Normal -> {
                if (renderPos != null && shouldPause()) {
                    event.renderer.box(renderPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                }
            }
            case Smooth -> {
                if (renderPos != null && shouldPause()) {
                    renderProgress = Math.min(1.0, renderProgress + delta);
                    renderTarget = new Vec3d(placePos.getX(), placePos.up().getY(), placePos.getZ());
                } else {
                    renderProgress = Math.max(0.0, renderProgress - delta);
                }

                if (renderTarget != null) {
                    renderPosV = smoothMove(renderPosV, renderTarget, delta * animationSpeed.get() * 5.0);
                }

                double r;
                if (renderPosV != null && (r = 0.5 - Math.pow(1.0 - renderProgress, animationExponent.get()) / 2.0) >= 0.001) {
                    double down = -0.5;
                    double up = -0.5;
                    double width = 0.5;
                    switch (fadeMode.get()) {
                        case Normal -> {
                            up = -0.5 + r;
                            down = -0.5 - r;
                            width = r;
                        }
                        case Up -> {
                            up = 0.0;
                            down = -(r * 2.0);
                        }
                        case Down -> {
                            up = -1.0 + r * 2.0;
                            down = -1.0;
                        }
                    }

                    Box box = expansion.get() ?
                        new Box(renderPosV.getX() + 0.5 - width, renderPosV.getY() + down, renderPosV.getZ() + 0.5 - width, renderPosV.getX() + 0.5 + width, renderPosV.getY() + up, renderPosV.getZ() + 0.5 + width)
                        : new Box(renderPosV, new Vec3d(renderPosV.getX() + 1, renderPosV.getY() + 1, renderPosV.getZ() + 1));

                    event.renderer.box(box, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                }
            }
        }

        // Render extrapolation
        if (renderExt.get()) {
            extPos.forEach((name, bb) -> {
                if (renderSelfExt.get() || !name.equals(mc.player)) {
                    event.renderer.box(bb, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                }
            });
        }
    }

    private Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
        if (current == null) {
            return target;
        }

        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);
        double x = (absX + Math.pow(absX, animationMoveExponent.get() - 1.0)) * delta;
        double y = (absX + Math.pow(absY, animationMoveExponent.get() - 1.0)) * delta;
        double z = (absX + Math.pow(absZ, animationMoveExponent.get() - 1.0)) * delta;

        return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x), current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y), current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!renderDmg.get() || bestDmg == -1.0 || !InvUtils.findInHotbar(Items.GLOWSTONE).found() || !InvUtils.findInHotbar(Items.RESPAWN_ANCHOR).found() || mc.player.isSneaking())
            return;

        if (targets != null && renderPos != null && shouldPause()) {
            Vector3d vec3d = renderMode.get().equals(RenderMode.Smooth) ? new Vector3d(renderPosV.x + 0.5, renderPosV.add(0.0, -1.0, 0.0).y + 0.5, renderPosV.z + 0.5) : new Vector3d(renderPos.getX() + 0.5, renderPos.getY() + 0.5, renderPos.getZ() + 0.5);
            if (NametagUtils.to2D(vec3d, scale.get(), true)) {
                TextRenderer font = TextRenderer.get();

                NametagUtils.begin(vec3d);
                font.begin(scale.get());

                NumberFormat why = NumberFormat.getNumberInstance();
                why.setMaximumFractionDigits(decimal.get());

                String text3 = why.format(bestDmg) + " / " + why.format(selfDmg);

                font.render(text3, -(font.getWidth(text3) / 2.0), -font.getHeight(), damageColor.get(), false);

                font.end();
                NametagUtils.end();
            }
        }
    }

    private boolean shouldPause() {
        return !pauseEat.get() || !mc.player.isUsingItem();
    }

    private void updateTargets() {
        List<PlayerEntity> players = new ArrayList<>();
        double closestDist = 1000;
        PlayerEntity closest;
        double dist;
        for (int i = 3; i > 0; i--) {
            closest = null;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (players.contains(player) || Friends.get().isFriend(player) || player == mc.player) {
                    continue;
                }

                dist = player.distanceTo(mc.player);

                if (dist > 15) {
                    continue;
                }

                if (closest == null || dist < closestDist) {
                    closestDist = dist;
                    closest = player;
                }
            }
            if (closest != null) {
                players.add(closest);
            }
        }
        targets = players;
    }

    private boolean blockBroken(BlockPos blockPos) {
        if (!amPlace.get()) return false;
        if (autoMine == null || !autoMine.isActive()) return false;
        if (autoMine.targetPos() == null) return false;
        if (!autoMine.targetPos().equals(blockPos)) return false;

        double progress = autoMine.getMineProgress();

        if (progress >= 1 && !amBroken.get().broken) return true;
        if (progress >= amProgress.get() && !amBroken.get().near) return true;
        return progress < amProgress.get() && !amBroken.get().normal;
    }

    private void updatePos() {
        if (targets.isEmpty()) return;

        new Thread(() -> {
            for (PlayerEntity target : targets) {
                for (BlockPos blockPos : BlockInfo.getSphere(target.getBlockPos(), SettingUtils.range.radius.get(), SettingUtils.range.radius.get())) {
                    PlaceData data;
                    double selfDmg;
                    double bestDmg;

                    if (BlockUtils.solid(blockPos) && !(mc.world.getBlockState(blockPos).getBlock() instanceof RespawnAnchorBlock) || !SettingUtils.inAnchorPlaceRange(blockPos) || EntityUtils.intersectsWithEntity(new Box(blockPos), entity -> !(entity instanceof ItemEntity)) || !dmgCheck(bestDmg = getDmg(blockPos), selfDmg = DamageInfo.anchorDamage(mc.player, extPos.containsKey(mc.player) ? extPos.get(mc.player) : mc.player.getBoundingBox(), blockPos)) || !(data = SettingUtils.getPlaceData(blockPos)).valid() || blockBroken(blockPos)) {
                        continue;
                    }

                    this.calcData = data;
                    this.calcPos = blockPos;
                    this.bestDmg = bestDmg;
                    this.selfDmg = selfDmg;
                }
            }
        }).start();
    }

    private void update() {
        if (placePos == null || placeData == null || !placeData.valid()) return;

        for (PlayerEntity target : targets) {
            if (!helpOnSurround.get() || updateHelper(target)) {
                Anchor anchor = getAnchor(placePos);

                switch (anchor.state) {
                    case Anchor -> {
                        if (chargeUpdate()) {
                            Anchor a = new Anchor(AnchorState.Loaded, anchor.charges + 1, System.currentTimeMillis());

                            anchors.remove(placePos);
                            anchors.put(placePos, a);
                        }
                    }
                    case Loaded -> {
                        if (explodeTimer <= 1 / getExplodeSpeed(target)) return;

                        if (explodeUpdate()) {
                            anchors.remove(placePos);
                            anchors.put(placePos, new Anchor(AnchorState.Air, 0, System.currentTimeMillis()));
                            explodeTimer = 0;
                        }
                    }
                    case Air -> {
                        if (placeTimer <= 1 / getPlaceSpeed(target)) return;

                        if (placeUpdate()) {
                            anchors.remove(placePos);
                            anchors.put(placePos, new Anchor(AnchorState.Anchor, 0, System.currentTimeMillis()));
                            placeTimer = 0.0;
                        }
                    }
                }
            }
        }
    }

    private double getPlaceSpeed(PlayerEntity target) {
        if (dynamicSpeed.get() && ((dynamicHealth.get() && target.getHealth() <= dynamicHealthValue.get()) || (Managers.SPEED.getPlayerSpeed(target) != 0 && Managers.SPEED.getPlayerSpeed(target) >= dynamicPlayerMixSpeed.get()))) {
            return dynamicPlaceSpeed.get();
        }
        return getDamageSpeed(placeSpeed.get(), damagePlaceSpeed.get(), damagePlaceMaxSpeed.get());
    }

    private double getExplodeSpeed(PlayerEntity target) {
        if (dynamicSpeed.get() && ((dynamicHealth.get() && target.getHealth() <= dynamicHealthValue.get()) || (Managers.SPEED.getPlayerSpeed(target) != 0 && Managers.SPEED.getPlayerSpeed(target) >= dynamicPlayerMixSpeed.get()))) {
            return dynamicExplodeSpeed.get();
        }
        return getDamageSpeed(explodeSpeed.get(), damageExplodeSpeed.get(), damageExplodeMaxSpeed.get());
    }

    private double getDamageSpeed(double defaultSpeed, double damageSpeed, double damageMaxSpeed) {
        return dynamicDamage.get() ? Math.min(getDmg(placePos) * damageSpeed, damageMaxSpeed) : defaultSpeed;
    }

    private boolean updateHelper(PlayerEntity target) {
        if (HoleUtils.inHole(target) && timer.passedMs(placeDelay.get().longValue())) {
            for (BlockPos blockPos : getHelper(target.getBlockPos().up(2))) {
                FindItemResult result = switchMode.get().equals(SwitchMode.Normal) || switchMode.get().equals(SwitchMode.Silent) ? InvUtils.findInHotbar(Items.OBSIDIAN) : InvUtils.find(Items.OBSIDIAN);

                if (!result.found()) {
                    return true;
                }

                PlaceData data = SettingUtils.getPlaceData(blockPos);

                if (data.valid() && !BlockUtils.solid(blockPos)) {
                    if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
                        return false;
                    }

                    boolean switched = switch (switchMode.get()) {
                        case Normal, Silent -> InvUtils.swap(result.slot(), true);
                        case InvSwitch -> InvUtils.invSwitch(result.slot());
                        case PickSilent -> InvUtils.pickSwitch(result.slot());
                    };

                    if (!switched) {
                        return false;
                    }

                    renderPlacing.add(new Render(blockPos, System.currentTimeMillis()));
                    placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
                    if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

                    timer.reset();

                    switch (switchMode.get()) {
                        case Silent -> InvUtils.swapBack();
                        case InvSwitch -> InvUtils.invSwapBack();
                        case PickSilent -> InvUtils.pickSwapBack();
                    }

                    if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        Managers.ROTATION.end(Objects.hash(name + "placing"));
                    }
                }
            }
        }
        return true;
    }

    private List<BlockPos> getHelper(BlockPos block) {
        List<BlockPos> list = new ArrayList<>();
        if (!BlockUtils.replaceable(block)) return list;
        if (SettingUtils.getPlaceData(block).valid()) return list;

        // 1 block support
        Direction support1 = getSupport(block);

        if (support1 != null) {
            if (block.offset(support1) != block && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(support1))), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                list.add(block.offset(support1));
            }
            return list;
        }

        // 2 block support
        for (Direction dir : Direction.values()) {
            if (!BlockUtils.replaceable(block.offset(dir)) || !SettingUtils.inPlaceRange(block.offset(dir))) {
                continue;
            }

            Direction support2 = getSupport(block.offset(dir));

            if (support2 != null) {
                if (block.offset(dir).offset(support2) != block && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(dir).offset(support2))), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                    list.add(block.offset(dir).offset(support2));
                }
                return list;
            }
        }

        return list;
    }

    private Direction getSupport(BlockPos position) {
        Direction cDir = null;
        double cDist = 1000;
        int value = -1;

        for (Direction dir : Direction.values()) {
            PlaceData data = SettingUtils.getPlaceData(position.offset(dir));

            if (!data.valid() || !SettingUtils.inPlaceRange(data.pos())) {
                continue;
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                double dist = mc.player.getEyePos().distanceTo(position.offset(dir).toCenterPos());

                if (dist < cDist || value < 2) {
                    value = 2;
                    cDir = dir;
                    cDist = dist;
                }
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM && entity.getType() != EntityType.END_CRYSTAL)) {
                double dist = mc.player.getEyePos().distanceTo(position.offset(dir).toCenterPos());

                if (dist < cDist || value < 1) {
                    value = 1;
                    cDir = dir;
                    cDist = dist;
                }
            }

        }
        return cDir;
    }

    public boolean isHeadExploding() {
        return isActive() && targets.stream().allMatch(target -> target != null && calcPos != null && calcPos.equals(target.getBlockPos().up(2)) && (placeTimer != 0 || explodeTimer != 0));
    }

    private void place(Hand hand) {
        placeBlock(hand, placeData.pos().toCenterPos(), placeData.dir(), placeData.pos());
        if (placeSwing.get()) clientSwing(placeHand.get(), hand);
    }

    private Anchor getAnchor(BlockPos pos) {
        if (anchors.containsKey(pos)) {
            return anchors.get(pos);
        }
        BlockState state = mc.world.getBlockState(pos);
        return new Anchor(state.getBlock() == Blocks.RESPAWN_ANCHOR ? (state.get(Properties.CHARGES) < 1 ? AnchorState.Anchor : AnchorState.Loaded) : AnchorState.Air, state.getBlock() == Blocks.RESPAWN_ANCHOR ? state.get(Properties.CHARGES) : 0, System.currentTimeMillis());
    }

    private boolean placeUpdate() {
        Hand hand = Managers.HOLDING.isHolding(Items.RESPAWN_ANCHOR) ? Hand.MAIN_HAND : (mc.player.getOffHandStack().getItem() == Items.RESPAWN_ANCHOR ? Hand.OFF_HAND : null);

        FindItemResult result;
        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    result = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = result.found();
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(placeData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return false;
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    result = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
                    InvUtils.swap(result.slot(), true);
                }
                case PickSilent -> {
                    result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = InvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = InvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }

        place(hand == null ? Hand.MAIN_HAND : hand);

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> InvUtils.pickSwapBack();
                case InvSwitch -> InvUtils.invSwapBack();
            }
        }

        return true;
    }

    private boolean chargeUpdate() {
        if (mc.player.isSneaking()) {
            return false;
        }

        Hand hand = Managers.HOLDING.isHolding(Items.GLOWSTONE) ? Hand.MAIN_HAND : (mc.player.getOffHandStack().getItem() == Items.GLOWSTONE ? Hand.OFF_HAND : null);
        Direction dir = SettingUtils.getPlaceOnDirection(placePos);

        if (dir == null) {
            return false;
        }

        FindItemResult result;
        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    result = InvUtils.findInHotbar(Items.GLOWSTONE);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    result = InvUtils.find(Items.GLOWSTONE);
                    switched = result.found();
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(placePos, priority, RotationType.Interact, Objects.hash(name + "interact"))) {
            return false;
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    result = InvUtils.findInHotbar(Items.GLOWSTONE);
                    InvUtils.swap(result.slot(), true);
                }
                case PickSilent -> {
                    result = InvUtils.find(Items.GLOWSTONE);
                    switched = InvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    result = InvUtils.find(Items.GLOWSTONE);
                    switched = InvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {
            return false;
        }

        interact(placePos, dir, hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end(Objects.hash(name + "interact"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> InvUtils.pickSwapBack();
                case InvSwitch -> InvUtils.invSwapBack();
            }
        }
        return true;
    }

    private boolean explodeUpdate() {
        if (mc.player.isSneaking()) {
            return false;
        }

        Hand hand = !Managers.HOLDING.isHolding(Items.GLOWSTONE) ? Hand.MAIN_HAND : (mc.player.getOffHandStack().getItem() != Items.GLOWSTONE ? Hand.OFF_HAND : null);
        Direction dir = SettingUtils.getPlaceOnDirection(placePos);

        if (dir == null) {
            return false;
        }

        FindItemResult result;
        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    result = InvUtils.findInHotbar(stack -> stack.getItem() != Items.GLOWSTONE);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    result = InvUtils.find(stack -> stack.getItem() != Items.GLOWSTONE);
                    switched = result.found();
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(placePos, priority, RotationType.Interact, Objects.hash(name + "explode"))) {
            return false;
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    result = InvUtils.findInHotbar(item -> item.getItem() != Items.GLOWSTONE);
                    InvUtils.swap(result.slot(), true);
                }
                case PickSilent -> {
                    result = InvUtils.find(item -> item.getItem() != Items.GLOWSTONE);
                    switched = InvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    result = InvUtils.find(item -> item.getItem() != Items.GLOWSTONE);
                    switched = InvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {
            return false;
        }

        interact(placePos, dir, hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end(Objects.hash(name + "explode"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> InvUtils.pickSwapBack();
                case InvSwitch -> InvUtils.invSwapBack();
            }
        }
        return true;
    }

    private void interact(BlockPos pos, Direction dir, Hand hand) {
        interactBlock(hand, pos.toCenterPos(), dir, pos);
        if (interactSwing.get()) clientSwing(interactHand.get(), hand);
    }

    private boolean dmgCheck(double dmg, double self) {
        if (dmg <= bestDmg) {
            return false;
        }
        if (dmg <= minTargetDmg.get()) {
            return false;
        }
        return !(self >= maxSelfDmg.get());
    }

    private double getDmg(BlockPos blockPos) {
        double highest = -1.0;
        for (Map.Entry<AbstractClientPlayerEntity, Box> entry : extPos.entrySet()) {
            AbstractClientPlayerEntity player = entry.getKey();
            if (player.getHealth() <= 0 || player == mc.player) continue;

            Box box = entry.getValue();
            double dmg = DamageInfo.anchorDamage(player, box, blockPos);

            if (blockPos.equals(autoMine.targetPos())) {
                dmg *= autoMineDamage.get();
            }

            if (dmg > highest) {
                highest = dmg;
            }
        }

        return highest;
    }

    public enum SwitchMode {
        Silent,
        Normal,
        PickSilent,
        InvSwitch
    }

    public enum AutoMineBrokenMode {
        Near(true, false, false),
        Broken(true, true, false),
        Never(false, false, false),
        Always(true, true, true);

        public final boolean normal;
        public final boolean near;
        public final boolean broken;

        AutoMineBrokenMode(boolean normal, boolean near, boolean broken) {
            this.normal = normal;
            this.near = near;
            this.broken = broken;
        }
    }

    public enum RenderMode {
        Normal,
        Smooth
    }

    public enum FadeMode {
        Normal,
        Up,
        Down
    }

    public enum AnchorState {
        Air,
        Anchor,
        Loaded
    }

    private record Anchor(AnchorState state, int charges, long time) {
    }

    public record Render(BlockPos pos, long time) {
    }
}
