package dev.lemonclient.systems.modules.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.enums.ShapeMode;
import dev.lemonclient.events.entity.player.TotemPopEvent;
import dev.lemonclient.events.packets.PacketEvent;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IEntity;
import dev.lemonclient.mixininterface.IVec3d;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.entity.fakeplayer.FakePlayerEntity;
import dev.lemonclient.utils.math.MathUtils;
import dev.lemonclient.utils.render.WireframeEntityRenderer;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class PopChams extends Module {
    public PopChams() {
        super(Categories.Render, "Pop Chams", "Renders a ghost where players pop totem.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColor = settings.createGroup("Color");

    private final Setting<RenderMode> renderMode = sgGeneral.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description(".")
        .defaultValue(RenderMode.Future)
        .build()
    );
    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Self")
        .description("Do not render self.")
        .defaultValue(true)
        .visible(() -> renderMode.get().equals(RenderMode.Future))
        .build()
    );
    private final Setting<Integer> ySpeed = sgGeneral.add(new IntSetting.Builder()
        .name("Y Speed")
        .defaultValue(0)
        .sliderRange(-10, 10)
        .visible(() -> renderMode.get().equals(RenderMode.Future))
        .build()
    );
    private final Setting<Integer> aSpeed = sgGeneral.add(new IntSetting.Builder()
        .name("Alpha Speed")
        .defaultValue(5)
        .sliderRange(1, 100)
        .visible(() -> renderMode.get().equals(RenderMode.Future))
        .build()
    );
    private final Setting<Boolean> onlyOne = sgGeneral.add(new BoolSetting.Builder()
        .name("only-one")
        .description("Only allow one ghost per player.")
        .defaultValue(false)
        .visible(() -> renderMode.get().equals(RenderMode.Meteor))
        .build()
    );
    private final Setting<Double> renderTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("render-time")
        .description("How long the ghost is rendered in seconds.")
        .defaultValue(1)
        .min(0.1)
        .sliderMax(6)
        .visible(() -> renderMode.get().equals(RenderMode.Meteor))
        .build()
    );
    private final Setting<Double> yModifier = sgGeneral.add(new DoubleSetting.Builder()
        .name("y-modifier")
        .description("How much should the Y position of the ghost change per second.")
        .defaultValue(0.75)
        .sliderRange(-4, 4)
        .visible(() -> renderMode.get().equals(RenderMode.Meteor))
        .build()
    );
    private final Setting<Double> scaleModifier = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale-modifier")
        .description("How much should the scale of the ghost change per second.")
        .defaultValue(-0.25)
        .sliderRange(-4, 4)
        .visible(() -> renderMode.get().equals(RenderMode.Meteor))
        .build()
    );
    private final Setting<Boolean> fadeOut = sgGeneral.add(new BoolSetting.Builder()
        .name("fade-out")
        .description("Fades out the color.")
        .defaultValue(true)
        .visible(() -> renderMode.get().equals(RenderMode.Meteor))
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgColor.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(() -> renderMode.get().equals(RenderMode.Meteor))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgColor.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 255, 255, 25))
        .visible(() -> renderMode.get().equals(RenderMode.Meteor))
        .build()
    );
    private final Setting<SettingColor> color = sgColor.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(COLOR)
        .defaultValue(new SettingColor(0x8800FF00))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgColor.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 255, 255, 127))
        .build()
    );


    private final List<GhostPlayer> ghosts = new ArrayList<>();
    public final CopyOnWriteArrayList<Person> popList = new CopyOnWriteArrayList<>();

    public enum RenderMode {
        Meteor,
        Future
    }

    @Override
    public void onDeactivate() {
        if (renderMode.get().equals(RenderMode.Meteor)) {
            synchronized (ghosts) {
                ghosts.clear();
            }
        }
    }

    @EventHandler
    public void onTickPre(TickEvent.Pre event) {
        popList.forEach(person -> person.update(popList));
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (renderMode.get().equals(RenderMode.Meteor)) {
            if (!(event.packet instanceof EntityStatusS2CPacket p)) return;
            if (p.getStatus() != 35) return;

            Entity entity = p.getEntity(mc.world);
            if (!(entity instanceof PlayerEntity player) || entity == mc.player) return;

            synchronized (ghosts) {
                if (onlyOne.get()) ghosts.removeIf(ghostPlayer -> ghostPlayer.uuid.equals(entity.getUuid()));

                ghosts.add(new GhostPlayer(player));
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        switch (renderMode.get()) {
            case Meteor -> {
                synchronized (ghosts) {
                    ghosts.removeIf(ghostPlayer -> ghostPlayer.render(event));
                }
            }
            case Future -> {
                RenderSystem.depthMask(false);
                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(770, 771, 0, 1);

                popList.forEach(person -> {
                    person.modelPlayer.leftPants.visible = false;
                    person.modelPlayer.rightPants.visible = false;
                    person.modelPlayer.leftSleeve.visible = false;
                    person.modelPlayer.rightSleeve.visible = false;
                    person.modelPlayer.jacket.visible = false;
                    person.modelPlayer.hat.visible = false;
                    renderEntity(event.matrices, person.player, person.modelPlayer, person.getAlpha());
                });

                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
            }
        }
    }

    @EventHandler
    public void onTotemPop(TotemPopEvent event) {
        if (!renderMode.get().equals(RenderMode.Future)) return;
        if (ignoreSelf.get() && event.entity.equals(mc.player)) return;

        PlayerEntity entity = new AbstractClientPlayerEntity(mc.world, new GameProfile(event.entity.getUuid(), event.entity.getName().getString())) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };

        entity.copyPositionAndRotation(event.entity);
        entity.bodyYaw = event.entity.bodyYaw;
        entity.headYaw = event.entity.headYaw;
        entity.handSwingProgress = event.entity.handSwingProgress;
        entity.handSwingTicks = event.entity.handSwingTicks;
        entity.setSneaking(event.entity.isSneaking());
        entity.limbAnimator.setSpeed(event.entity.limbAnimator.getSpeed());
        entity.limbAnimator.pos = event.entity.limbAnimator.getPos();
        popList.add(new Person(entity));
    }

    private void renderEntity(MatrixStack matrices, LivingEntity entity, BipedEntityModel<PlayerEntity> modelBase, int alpha) {
        double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        ((IEntity) entity).setPos(entity.getPos().add(0, ySpeed.get() / 50D, 0));

        matrices.push();
        matrices.translate((float) x, (float) y, (float) z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rad(180 - entity.bodyYaw)));
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.scale(1.6f, 1.8f, 1.6f);
        matrices.translate(0.0F, -1.501F, 0.0F);

        modelBase.animateModel((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), mc.getTickDelta());
        modelBase.setAngles((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());

        RenderSystem.enableBlend();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        modelBase.render(matrices, buffer, 10, 0, sideColor.get().r / 255f, sideColor.get().g / 255f, sideColor.get().b / 255f, alpha / 255f);
        tessellator.draw();

        RenderSystem.disableBlend();
        matrices.pop();
    }

    private float rad(float angle) {
        return (float) (angle * Math.PI / 180);
    }

    private class Person {
        private final PlayerEntity player;
        private final PlayerEntityModel<PlayerEntity> modelPlayer;
        private int alpha;

        public Person(PlayerEntity player) {
            this.player = player;
            modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
            modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));
            alpha = color.get().a;
        }

        public void update(CopyOnWriteArrayList<Person> arrayList) {
            if (alpha <= 0) {
                arrayList.remove(this);
                player.kill();
                player.remove(Entity.RemovalReason.KILLED);
                player.onRemoved();
                return;
            }
            alpha -= aSpeed.get();
        }

        public int getAlpha() {
            return MathUtils.clamp(alpha, 0, 255);
        }
    }

    private class GhostPlayer extends FakePlayerEntity {
        private final UUID uuid;
        private double timer, scale = 1;

        public GhostPlayer(PlayerEntity player) {
            super(player, "ghost", 20, false);

            uuid = player.getUuid();
        }

        public boolean render(Render3DEvent event) {
            // Increment timer
            timer += event.frameTime;
            if (timer > renderTime.get()) return true;

            // Y Modifier
            lastRenderY = getY();
            ((IVec3d) getPos()).setY(getY() + yModifier.get() * event.frameTime);

            // Scale Modifier
            scale += scaleModifier.get() * event.frameTime;

            // Fade out
            int preSideA = sideColor.get().a;
            int preLineA = lineColor.get().a;

            if (fadeOut.get()) {
                sideColor.get().a *= 1 - timer / renderTime.get();
                lineColor.get().a *= 1 - timer / renderTime.get();
            }

            // Render
            WireframeEntityRenderer.render(event, this, scale, sideColor.get(), lineColor.get(), shapeMode.get());

            // Restore colors
            sideColor.get().a = preSideA;
            lineColor.get().a = preLineA;

            return false;
        }
    }
}
