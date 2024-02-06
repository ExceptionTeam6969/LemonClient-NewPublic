package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.HeldItemRendererEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class HandTweaks extends Module {
    public HandTweaks() {
        super(Categories.Render, "Hand Tweaks", "Tweaks for main and off hands.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMainhand = settings.createGroup("Main Hand");
    private final SettingGroup sgOffhand = settings.createGroup("Off Hand");

    private final Setting<Boolean> noSwing = sgGeneral.add(new BoolSetting.Builder()
        .name("no-swing")
        .description("Preventing client-side swing animation.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of your hands.")
        .defaultValue(1)
        .sliderMax(5)
        .build()
    );

    private final Setting<Integer> speedX = sgMainhand.add(new IntSetting.Builder()
        .name("x-animation")
        .description("The speed of X orientation of your main hand.")
        .defaultValue(0)
        .sliderMin(-100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> speedY = sgMainhand.add(new IntSetting.Builder()
        .name("y-animation")
        .description("The speed of Y orientation of your main hand.")
        .defaultValue(0)
        .sliderMin(-100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> speedZ = sgMainhand.add(new IntSetting.Builder()
        .name("z-animation")
        .description("The speed of Z orientation of your main hand.")
        .defaultValue(0)
        .sliderMin(-100)
        .sliderMax(100)
        .build()
    );

    private final Setting<Integer> offspeedX = sgOffhand.add(new IntSetting.Builder()
        .name("x-animation")
        .description("The speed of X orientation of your off hand.")
        .defaultValue(0)
        .sliderMin(-100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> offspeedY = sgOffhand.add(new IntSetting.Builder()
        .name("y-animation")
        .description("The speed of Y orientation of your off hand.")
        .defaultValue(0)
        .sliderMin(-100)
        .sliderMax(100)
        .build()
    );
    private final Setting<Integer> offspeedZ = sgOffhand.add(new IntSetting.Builder()
        .name("z-animation")
        .description("The speed of Z orientation of your off hand.")
        .defaultValue(0)
        .sliderMin(-100)
        .sliderMax(100)
        .build()
    );

    private float nextRotationX = 0.0f, nextRotationY = 0.0f, nextRotationZ = 0.0f;

    @EventHandler
    public void onHeldItemRender(HeldItemRendererEvent event) {
        MatrixStack matrices = event.matrix;

        if (event.item.isEmpty()) return;
        float defRotation = 0;

        matrices.scale(scale.get().floatValue(), scale.get().floatValue(), scale.get().floatValue());

        if (event.hand == Hand.MAIN_HAND) {
            if (!speedX.get().equals(0)) {
                float finalRotationX = (nextRotationX++ / speedX.get());
                multiply(matrices, finalRotationX, defRotation, defRotation);
            }
            if (!speedY.get().equals(0)) {
                float finalRotationY = (nextRotationY++ / speedY.get());
                multiply(matrices, defRotation, finalRotationY, defRotation);
            }
            if (!speedZ.get().equals(0)) {
                float finalRotationZ = (nextRotationZ++ / speedZ.get());
                multiply(matrices, defRotation, defRotation, finalRotationZ);
            }
        } else {
            if (!offspeedX.get().equals(0)) {
                float finalRotationX = (nextRotationX++ / offspeedX.get());
                multiply(matrices, finalRotationX, defRotation, defRotation);
            }
            if (!offspeedY.get().equals(0)) {
                float finalRotationY = (nextRotationY++ / offspeedY.get());
                multiply(matrices, defRotation, finalRotationY, defRotation);
            }
            if (!offspeedZ.get().equals(0)) {
                float finalRotationZ = (nextRotationZ++ / offspeedZ.get());
                multiply(matrices, defRotation, defRotation, finalRotationZ);
            }
        }
    }

    private void multiply(MatrixStack matrices, float x, float y, float z) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(x));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(y));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(z));
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (noSwing.get() && event.packet instanceof HandSwingC2SPacket) {
            mc.player.handSwinging = false;
        }
    }

    @EventHandler
    private void onRecievePacket(PacketEvent.Receive event) {
        if (noSwing.get() && event.packet instanceof HandSwingC2SPacket) {
            mc.player.handSwinging = false;
        }
    }
}
