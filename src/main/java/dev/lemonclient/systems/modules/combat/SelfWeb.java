package dev.lemonclient.systems.modules.combat;

import dev.lemonclient.enums.RotationType;
import dev.lemonclient.enums.SwingHand;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.SettingUtils;
import dev.lemonclient.utils.entity.SortPriority;
import dev.lemonclient.utils.entity.TargetUtils;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.world.PlaceData;
import dev.lemonclient.utils.world.hole.HoleUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class SelfWeb extends Module {
    public SelfWeb() {
        super(Categories.Combat, "Self Web", "Automatically places webs on you.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("The mode to use for selfweb.")
        .defaultValue(Mode.Normal)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Player Range")
        .description(".")
        .defaultValue(3.5)
        .visible(() -> mode.get().equals(Mode.Smart))
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("The mode to swap to place web.")
        .defaultValue(SwitchMode.InvSwitch)
        .build()
    );
    private final Setting<Boolean> turnOff = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Toggle")
        .description("Toggle off after placing the webs.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );

    public enum SwitchMode {
        Silent,
        PickSilent,
        InvSwitch
    }

    public enum Mode {
        Normal,
        Smart
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        PlayerEntity target = TargetUtils.getPlayerTarget(range.get(), SortPriority.LowestDistance);

        switch (mode.get()) {
            case Normal -> placeWeb();
            case Smart -> {
                if (target != null && mc.player.getY() < target.getY() && HoleUtils.inHole(mc.player)) placeWeb();
            }
        }
    }

    private void placeWeb() {
        FindItemResult web = switchMode.get().equals(SwitchMode.InvSwitch) || switchMode.get().equals(SwitchMode.PickSilent) ? InvUtils.find(Items.COBWEB) : InvUtils.findInHotbar(Items.COBWEB);

        if (!web.found()) {
            if (turnOff.get()) {
                sendDisableMsg("No cobweb found!");
                toggle();
            }
            return;
        }

        BlockPos webPos = mc.player.getBlockPos();
        PlaceData data = SettingUtils.getPlaceData(webPos);

        if (!data.valid()) {
            return;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(webPos, priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return;
        }

        boolean switched = switch (switchMode.get()) {
            case Silent -> InvUtils.swap(web.slot(), true);
            case InvSwitch -> InvUtils.invSwitch(web.slot());
            case PickSilent -> InvUtils.pickSwitch(web.slot());
        };

        if (!switched) {
            return;
        }

        placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
        if (placeSwing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        switch (switchMode.get()) {
            case Silent -> InvUtils.swapBack();
            case InvSwitch -> InvUtils.invSwapBack();
            case PickSilent -> InvUtils.pickSwapBack();
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "placing"));

        if (turnOff.get()) toggle();
    }
}
