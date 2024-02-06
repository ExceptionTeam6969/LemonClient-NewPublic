package dev.lemonclient.systems.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.math.MathUtils;
import dev.lemonclient.utils.render.Render3DUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static dev.lemonclient.utils.render.Render2DUtils.TEXTURE_COLOR_PROGRAM;
import static dev.lemonclient.utils.render.Render2DUtils.injectAlpha;

public class Particles extends Module {

    public Particles() {
        super(Categories.Render, "Particles", "Render some particles to make your game look better.");
    }

    private final SettingGroup sgFireFiles = settings.createGroup("FireFlies");
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //--------------------FireFlies--------------------//
    private final Setting<Boolean> fireFilesSet = boolSetting(sgFireFiles, "FireFiles", true);
    private final Setting<Integer> ffcount = intSetting(sgFireFiles, "FFCount", 30, 20, 200, fireFilesSet::get);
    private final Setting<Double> ffsize = doubleSetting(sgFireFiles, "FFSize", 1.0, 0.1, 2.0, fireFilesSet::get);

    //--------------------General--------------------//
    private final Setting<Mode> mode = enumSetting(sgGeneral, "Mode", Mode.Snowflake);
    private final Setting<Physics> physics = enumSetting(sgGeneral, "Physics", Physics.Fly, () -> mode.get() != Mode.Off);
    private final Setting<Integer> count = intSetting(sgGeneral, "Count", 100, 20, 8000);
    private final Setting<Double> size = doubleSetting(sgGeneral, "Size", 1.0, 0.1, 6.0);
    private final Setting<SettingColor> color = colorSetting(sgGeneral, "Color", new SettingColor(255, 255, 255));

    private final Identifier star = new Identifier(LemonClient.MOD_ID, "textures/star.png");
    private final Identifier snowflake = new Identifier(LemonClient.MOD_ID, "textures/snowflake.png");
    private final Identifier vanillaSnowflake = new Identifier("textures/environment/snow.png");
    private final Identifier firefly = new Identifier(LemonClient.MOD_ID, "textures/firefly.png");

    public enum Mode {
        Off,
        Star,
        Snowflake,
        VanillaSnowflake,
        Firefly
    }

    public enum Physics {
        Drop, Fly
    }


    private final ArrayList<ParticleBase> fireFlies = new ArrayList<>();
    private final ArrayList<ParticleBase> particles = new ArrayList<>();

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        fireFlies.removeIf(ParticleBase::tick);
        particles.removeIf(ParticleBase::tick);

        for (int i = fireFlies.size(); i < ffcount.get(); i++) {
            if (fireFilesSet.get())
                fireFlies.add(new FireFly(
                    (float) (mc.player.getX() + MathUtils.random(-25f, 25f)),
                    (float) (mc.player.getY() + MathUtils.random(2f, 15f)),
                    (float) (mc.player.getZ() + MathUtils.random(-25f, 25f)),
                    MathUtils.random(-0.2f, 0.2f),
                    MathUtils.random(-0.1f, 0.1f),
                    MathUtils.random(-0.2f, 0.2f)));
        }

        for (int j = particles.size(); j < count.get(); j++) {
            boolean drop = physics.get() == Physics.Drop;
            if (mode.get() != Mode.Off)
                particles.add(new ParticleBase(
                    (float) (mc.player.getX() + MathUtils.random(-48f, 48f)),
                    (float) (mc.player.getY() + MathUtils.random(2, 48f)),
                    (float) (mc.player.getZ() + MathUtils.random(-48f, 48f)),
                    drop ? 0 : MathUtils.random(-0.4f, 0.4f),
                    drop ? MathUtils.random(-0.2f, -0.05f) : MathUtils.random(-0.1f, 0.1f),
                    drop ? 0 : MathUtils.random(-0.4f, 0.4f)));
        }
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (fireFilesSet.get()) {
            event.matrices.push();
            RenderSystem.setShaderTexture(0, firefly);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(() -> TEXTURE_COLOR_PROGRAM.backingProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            fireFlies.forEach(p -> p.render(bufferBuilder));

            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            event.matrices.pop();
        }

        if (mode.get() != Mode.Off) {
            event.matrices.push();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(() -> TEXTURE_COLOR_PROGRAM.backingProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            particles.forEach(p -> p.render(bufferBuilder));

            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            event.matrices.pop();
        }
    }

    public class FireFly extends ParticleBase {
        private final List<Trail> trails = new ArrayList<>();


        public FireFly(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            super(posX, posY, posZ, motionX, motionY, motionZ);
        }

        @Override
        public boolean tick() {
            if (mc.player.squaredDistanceTo(posX, posY, posZ) > 100) age -= 4;
            else if (!mc.world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ)).isAir()) age -= 8;
            else age--;

            if (age < 0)
                return true;

            trails.removeIf(Trail::update);

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            trails.add(new Trail(new Vec3d(prevposX, prevposY, prevposZ), new Vec3d(posX, posY, posZ), color.get()));

            motionX *= 0.99f;
            motionY *= 0.99f;
            motionZ *= 0.99f;

            return false;
        }

        @Override
        public void render(BufferBuilder bufferBuilder) {
            RenderSystem.setShaderTexture(0, firefly);
            if (!trails.isEmpty()) {
                Camera camera = mc.gameRenderer.getCamera();
                for (Trail ctx : trails) {
                    Vec3d pos = ctx.interpolate(1f);
                    MatrixStack matrices = new MatrixStack();

                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                    matrices.translate(pos.x, pos.y, pos.z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

                    Matrix4f matrix = matrices.peek().getPositionMatrix();
                    float size = ffsize.get().floatValue();

                    bufferBuilder.vertex(matrix, 0, -size, 0).texture(0f, 1f).color(injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(mc.getTickDelta()))).getPacked()).next();
                    bufferBuilder.vertex(matrix, -size, -size, 0).texture(1f, 1f).color(injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(mc.getTickDelta()))).getPacked()).next();
                    bufferBuilder.vertex(matrix, -size, 0, 0).texture(1f, 0).color(injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(mc.getTickDelta()))).getPacked()).next();
                    bufferBuilder.vertex(matrix, 0, 0, 0).texture(0, 0).color(injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(mc.getTickDelta()))).getPacked()).next();
                }
            }
        }
    }

    private class Trail {
        private final Vec3d from;
        private final Vec3d to;
        private final Color color;
        private int ticks, prevTicks;

        public Trail(Vec3d from, Vec3d to, Color color) {
            this.from = from;
            this.to = to;
            this.ticks = 10;
            this.color = color;
        }

        public Vec3d interpolate(float pt) {
            double x = from.x + ((to.x - from.x) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double y = from.y + ((to.y - from.y) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double z = from.z + ((to.z - from.z) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
            return new Vec3d(x, y, z);
        }

        public double animation(float pt) {
            return (this.prevTicks + (this.ticks - this.prevTicks) * pt) / 10.;
        }

        public boolean update() {
            this.prevTicks = this.ticks;
            return this.ticks-- <= 0;
        }

        public Color color() {
            return color;
        }
    }

    public class ParticleBase {
        protected float prevposX, prevposY, prevposZ, posX, posY, posZ, motionX, motionY, motionZ;
        protected int age, maxAge;

        public ParticleBase(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            age = (int) MathUtils.random(100, 300);
            maxAge = age;
        }

        public boolean tick() {
            if (mc.player.squaredDistanceTo(posX, posY, posZ) > 4096) age -= 8;
            else age--;

            if (age < 0)
                return true;

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            motionX *= 0.9f;

            if (physics.get() == Physics.Fly) {
                motionY *= 0.9f;
            }

            motionZ *= 0.9f;

            motionY -= 0.001f;

            return false;
        }

        public void render(BufferBuilder bufferBuilder) {
            switch (mode.get()) {
                case Star -> RenderSystem.setShaderTexture(0, star);
                case Snowflake -> RenderSystem.setShaderTexture(0, snowflake);
                case VanillaSnowflake -> RenderSystem.setShaderTexture(0, vanillaSnowflake);
                case Firefly -> RenderSystem.setShaderTexture(0, firefly);
            }

            MatrixStack matrices = new MatrixStack();
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d pos = Render3DUtils.interpolatePos(prevposX, prevposY, prevposZ, posX, posY, posZ);

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(pos.x, pos.y, pos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            Matrix4f matrix1 = matrices.peek().getPositionMatrix();
            float fSize = size.get().floatValue();

            bufferBuilder.vertex(matrix1, 0, -fSize, 0).texture(0f, 1f).color(injectAlpha(color.get(), (int) (255 * ((float) age / (float) maxAge))).getPacked()).next();
            bufferBuilder.vertex(matrix1, -fSize, -fSize, 0).texture(1f, 1f).color(injectAlpha(color.get(), (int) (255 * ((float) age / (float) maxAge))).getPacked()).next();
            bufferBuilder.vertex(matrix1, -fSize, 0, 0).texture(1f, 0).color(injectAlpha(color.get(), (int) (255 * ((float) age / (float) maxAge))).getPacked()).next();
            bufferBuilder.vertex(matrix1, 0, 0, 0).texture(0, 0).color(injectAlpha(color.get(), (int) (255 * ((float) age / (float) maxAge))).getPacked()).next();
        }
    }
}
