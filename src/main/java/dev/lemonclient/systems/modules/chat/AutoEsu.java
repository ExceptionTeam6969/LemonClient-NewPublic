package dev.lemonclient.systems.modules.chat;

import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.render.animation.CaptureMark;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.settings.StringListSetting;
import dev.lemonclient.systems.friends.Friends;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.math.MathUtils;
import dev.lemonclient.utils.player.ChatUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.world.hole.HoleUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class AutoEsu extends Module {
    public AutoEsu() {
        super(Categories.Chat, "Auto Esu", "When you feel like your target is tough, this module will help you solve it.");
    }

    private final SettingGroup sgHole = settings.createGroup("Hole");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> inHole = boolSetting(sgHole, "In Hole", true);
    private final Setting<Double> seconds = doubleSetting(sgHole, "Seconds", 10.0, 0.0, 3600, inHole::get);
    private final Setting<List<String>> messages = sgHole.add(new StringListSetting.Builder()
        .name("Messages")
        .description("Messages to use for spam.")
        .defaultValue(List.of("Ez hole dog %target%."))
        .visible(inHole::get)
        .build()
    );
    private final Setting<Integer> holeSpamDelay = intSetting(sgHole, "Spam Delay", 20, 0, 200, inHole::get);

    private final Setting<Boolean> render = boolSetting(sgRender, "Render", true);
    private final Setting<ColorMode> colorMode = enumSetting(sgRender, "Color Mode", ColorMode.Sky);
    private final Setting<Integer> colorSpeed = intSetting(sgRender, "Color Speed", 18, 2, 54);
    private final Setting<SettingColor> hcolor1 = colorSetting(sgRender, "Color", new SettingColor(-6974059));
    private final Setting<SettingColor> acolor = colorSetting(sgRender, "Color 2", new SettingColor(-8365735));

    private int tick = 0;
    private int timer = 0;
    private List<PlayerEntity> targets = new ArrayList<>();

    public enum ColorMode {
        Static,
        Sky,
        LightRainbow,
        Rainbow,
        Fade,
        DoubleColor,
        Analogous
    }

    @Override
    public void onActivate() {
        tick = 0;
        timer = holeSpamDelay.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        CaptureMark.tick();

        updateTargets();

        if (targets.isEmpty()) return;

        for (PlayerEntity target : targets) {
            if (inHole.get()) {
                if (HoleUtils.inHole(target)) {
                    tick++;
                } else {
                    tick = 0;
                }

                if (tick >= seconds.get() * 20) {
                    if (timer <= 0) {
                        holeEsu(target);
                        timer = holeSpamDelay.get();
                    } else {
                        timer--;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender3D(Render3DEvent event) {
        targets.forEach(target -> {
            if (inHole.get() && render.get() && tick >= seconds.get() * 20) {
                CaptureMark.render(target, this);
            }
        });
    }

    private void holeEsu(PlayerEntity player) {
        if (messages.get().isEmpty()) return;
        ChatUtils.sendPlayerMsg(messages.get().get(MathUtils.randomNum(0, messages.get().size() - 1)).replace("%target%", player.getEntityName()));
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

    public Color getColor(int count) {
        return switch (colorMode.get()) {
            case Sky -> Render2DUtils.skyRainbow(colorSpeed.get(), count);
            case LightRainbow -> Render2DUtils.rainbow(colorSpeed.get(), count, .6f, 1, 1);
            case Rainbow -> Render2DUtils.rainbow(colorSpeed.get(), count, 1f, 1, 1);
            case Fade -> Render2DUtils.fade(colorSpeed.get(), count, hcolor1.get(), 1);
            case DoubleColor -> Render2DUtils.TwoColoreffect(hcolor1.get(), acolor.get(), colorSpeed.get(), count);
            case Analogous ->
                Render2DUtils.interpolateColorsBackAndForth(colorSpeed.get(), count, hcolor1.get(), Render2DUtils.getAnalogousColor(acolor.get()), true);
            default -> hcolor1.get();
        };
    }
}
