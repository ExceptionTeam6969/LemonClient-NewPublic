package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.render.HeldItemRendererEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IHeldItemRenderer;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class ViewModel extends Module {
    public ViewModel() {
        super(Categories.Render, "View Model", "Alters the way items are rendered in your hands.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMainHand = settings.createGroup("Main Hand");
    private final SettingGroup sgOffHand = settings.createGroup("Off Hand");
    private final SettingGroup sgEat = settings.createGroup("Eat");

    private final Setting<Boolean> oldAnimationsM = boolSetting(sgGeneral, "Disable Swap Main", false);
    private final Setting<Boolean> oldAnimationsOff = boolSetting(sgGeneral, "Disable Swap Off", false);

    private final Setting<Double> scaleMainX = doubleSetting(sgMainHand, "Scale Main X", 1f, 0.1f, 5f);
    private final Setting<Double> scaleMainY = doubleSetting(sgMainHand, "Scale Main Y", 1f, 0.1f, 5f);
    private final Setting<Double> scaleMainZ = doubleSetting(sgMainHand, "Scale Main Z", 1f, 0.1f, 5f);
    private final Setting<Double> positionMainX = doubleSetting(sgMainHand, "Position Main X", 0f, -3.0f, 3f);
    private final Setting<Double> positionMainY = doubleSetting(sgMainHand, "Position Main Y", 0f, -3.0f, 3f);
    private final Setting<Double> positionMainZ = doubleSetting(sgMainHand, "Position Main Z", 0f, -3.0f, 3f);
    private final Setting<Double> rotationMainX = doubleSetting(sgMainHand, "Rotation Main X", 0f, -180.0f, 180f);
    private final Setting<Double> rotationMainY = doubleSetting(sgMainHand, "Rotation Main Y", 0f, -180.0f, 180f);
    private final Setting<Double> rotationMainZ = doubleSetting(sgMainHand, "Rotation Main Z", 0f, -180.0f, 180f);
    private final Setting<Boolean> animateMainX = boolSetting(sgMainHand, "Animate Main X", false);
    private final Setting<Boolean> animateMainY = boolSetting(sgMainHand, "Animate Main Y", false);
    private final Setting<Boolean> animateMainZ = boolSetting(sgMainHand, "Animate Main Z", false);
    private final Setting<Double> speedAnimateMain = doubleSetting(sgMainHand, "Speed Animate Main", 1f, 1f, 5f);

    private final Setting<Double> scaleOffX = doubleSetting(sgOffHand, "Scale Off X", 1f, 0.1f, 5f);
    private final Setting<Double> scaleOffY = doubleSetting(sgOffHand, "Scale Off Y", 1f, 0.1f, 5f);
    private final Setting<Double> scaleOffZ = doubleSetting(sgOffHand, "Scale Off Z", 1f, 0.1f, 5f);
    private final Setting<Double> positionOffX = doubleSetting(sgOffHand, "Position Off X", 0f, -3.0f, 3f);
    private final Setting<Double> positionOffY = doubleSetting(sgOffHand, "Position Off Y", 0f, -3.0f, 3f);
    private final Setting<Double> positionOffZ = doubleSetting(sgOffHand, "Position Off Z", 0f, -3.0f, 3f);
    public final Setting<Double> rotationOffX = doubleSetting(sgOffHand, "Rotation Off X", 0f, -180.0f, 180f);
    private final Setting<Double> rotationOffY = doubleSetting(sgOffHand, "Rotation Off Y", 0f, -180.0f, 180f);
    private final Setting<Double> rotationOffZ = doubleSetting(sgOffHand, "Rotation Off Z", 0f, -180.0f, 180f);
    private final Setting<Boolean> animateOffX = boolSetting(sgOffHand, "Animate Off X", false);
    private final Setting<Boolean> animateOffY = boolSetting(sgOffHand, "Animate Off Y", false);
    private final Setting<Boolean> animateOffZ = boolSetting(sgOffHand, "Animate Off Z", false);
    private final Setting<Double> speedAnimateOff = doubleSetting(sgOffHand, "Speed Animate Off", 1f, 1f, 5f);

    public final Setting<Double> eatX = doubleSetting(sgEat, "Eat X", 1f, -1f, 2f);
    public final Setting<Double> eatY = doubleSetting(sgEat, "Eat Y", 1f, -1f, 2f);

    private double changeRotate(double value, double speed) {
        return value - speed <= 180 && value - speed > -180 ? value - speed : 180;
    }

    @EventHandler
    private void onHeldItemRender(HeldItemRendererEvent event) {
        if (event.hand == Hand.MAIN_HAND) {
            if (animateMainX.get())
                rotationMainX.set(changeRotate(rotationMainX.get(), speedAnimateMain.get()));
            if (animateMainY.get())
                rotationMainY.set(changeRotate(rotationMainY.get(), speedAnimateMain.get()));
            if (animateMainZ.get())
                rotationMainZ.set(changeRotate(rotationMainZ.get(), speedAnimateMain.get()));
            event.matrix.translate(positionMainX.get(), positionMainY.get(), positionMainZ.get());
            event.matrix.scale(scaleMainX.get().floatValue(), scaleMainY.get().floatValue(), scaleMainZ.get().floatValue());
            event.matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationMainX.get().floatValue()));
            event.matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationMainY.get().floatValue()));
            event.matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationMainZ.get().floatValue()));
        } else {
            if (animateOffX.get())
                rotationOffX.set(changeRotate(rotationOffX.get(), speedAnimateOff.get()));
            if (animateOffY.get())
                rotationOffY.set(changeRotate(rotationOffY.get(), speedAnimateOff.get()));
            if (animateOffZ.get())
                rotationOffZ.set(changeRotate(rotationOffZ.get(), speedAnimateOff.get()));
            event.matrix.translate(positionOffX.get(), positionOffY.get(), positionOffZ.get());
            event.matrix.scale(scaleOffX.get().floatValue(), scaleOffY.get().floatValue(), scaleOffZ.get().floatValue());
            event.matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationOffX.get().floatValue()));
            event.matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationOffY.get().floatValue()));
            event.matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationOffZ.get().floatValue()));
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        if (oldAnimationsM.get() && ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressMainHand() <= 1f) {
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(1f);
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(mc.player.getMainHandStack());
        }

        if (oldAnimationsOff.get() && ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressOffHand() <= 1f) {
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(1f);
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(mc.player.getOffHandStack());
        }
    }
}
