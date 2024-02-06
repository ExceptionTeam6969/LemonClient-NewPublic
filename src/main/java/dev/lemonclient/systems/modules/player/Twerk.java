package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.IntSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class Twerk extends Module {
    public Twerk() {
        super(Categories.Player, "Twerk", "Automatically sex with other players. =w=");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //--------------------General--------------------//
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Which method to sneak.")
        .defaultValue(Mode.Vanilla)
        .build()
    );
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
        .name("Speed")
        .description("The speed of twerking.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 100)
        .build()
    );

    private boolean hasTwerked = false;
    private int timer;

    @Override
    public void onActivate() {
        timer = 0;
    }

    @Override
    public void onDeactivate() {
        hasTwerked = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        timer++;
        if (timer < 10 - speed.get()) return;
        hasTwerked = !hasTwerked;
        timer = -1;
    }

    public boolean doPacket() {
        return isActive() && hasTwerked && !mc.player.getAbilities().flying && mode.get() == Mode.Packet;
    }

    public boolean doVanilla() {
        return isActive() && hasTwerked && !mc.player.getAbilities().flying && mode.get() == Mode.Vanilla;
    }

    public enum Mode {
        Packet("Packet"),
        Vanilla("Vanilla");

        private final String title;

        Mode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
