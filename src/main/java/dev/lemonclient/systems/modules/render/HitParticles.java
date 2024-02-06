package dev.lemonclient.systems.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.math.MathUtils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.concurrent.CopyOnWriteArrayList;

public class HitParticles extends Module {
    public HitParticles() {
        super(Categories.Render, "Hit Particles", "hit-time animation.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> mainColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Main Color")
        .defaultValue(new SettingColor(-6974059))
        .build()
    );
    private final Setting<SettingColor> colorrr = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .defaultValue(new SettingColor(0x8800FF00))
        .build()
    );
    private final Setting<Boolean> selfp = sgGeneral.add(new BoolSetting.Builder()
        .name("Self")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("Amount")
        .defaultValue(2)
        .range(1, 5)
        .sliderRange(1, 5)
        .build()
    );
    private final Setting<Integer> lifeTime = sgGeneral.add(new IntSetting.Builder()
        .name("Life Time")
        .defaultValue(2)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
        .name("Speed")
        .defaultValue(2)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );
    private final Setting<Integer> starsScale = sgGeneral.add(new IntSetting.Builder()
        .name("Stars Scale")
        .defaultValue(3)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .defaultValue(Mode.Stars)
        .build()
    );
    private final Setting<ColorMode> colorMode = sgGeneral.add(new EnumSetting.Builder<ColorMode>()
        .name("Color Mode")
        .defaultValue(ColorMode.Sync)
        .build()
    );

    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    public enum Mode {
        Orbiz, Stars
    }

    public enum ColorMode {
        Custom, Sync
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!selfp.get() && player == mc.player) continue;
            if (player.hurtTime > 0) {
                Color c = colorMode.get() == ColorMode.Sync ? mainColor.get() : colorrr.get();
                for (int i = 0; i < amount.get(); i++) {
                    particles.add(new Particle(player.getX(), MathUtils.random((float) (player.getY() + player.getHeight()), (float) player.getY()), player.getZ(), c));
                }
            }
            particles.removeIf(particle -> System.currentTimeMillis() - particle.getTime() > lifeTime.get() * 1000);
        }
    }


    @EventHandler
    public void onPreRender3D(Render3DEvent event) {
        RenderSystem.enableDepthTest();
        if (mc.player != null && mc.world != null) {
            for (Particle particle : particles) {
                particle.render(event.matrices);
            }
        }
        RenderSystem.disableDepthTest();
    }

    public class Particle {
        double x;
        double y;
        double z;
        double motionX;
        double motionY;
        double motionZ;
        long time;
        Color color;

        public Particle(double x, double y, double z, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            motionX = MathUtils.random(-(float) speed.get() / 100f, (float) speed.get() / 100f);
            motionY = MathUtils.random(-(float) speed.get() / 100f, (float) speed.get() / 100f);
            motionZ = MathUtils.random(-(float) speed.get() / 100f, (float) speed.get() / 100f);
            time = System.currentTimeMillis();
            this.color = color;
        }


        public long getTime() {
            return time;
        }

        public void update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ) * 1;
            x += motionX;
            y += motionY;

            if (posBlock(x, y, z)) {
                motionY = -motionY / 1.1;
            } else {
                if (posBlock(x, y, z) || posBlock(x, y, z) || posBlock(x, y, z) || posBlock(x - sp, y, z - sp)
                    || posBlock(x + sp, y, z + sp) || posBlock(x + sp, y, z - sp) || posBlock(x - sp, y, z + sp)
                    || posBlock(x + sp, y, z) || posBlock(x - sp, y, z) || posBlock(x, y, z + sp) || posBlock(x, y, z - sp)
                    || posBlock(x - sp, y, z - sp) || posBlock(x + sp, y, z + sp) || posBlock(x + sp, y, z - sp)
                    || posBlock(x - sp, y, z + sp) || posBlock(x + sp, y, z) || posBlock(x - sp, y, z) || posBlock(x, y, z + sp)
                    || posBlock(x, y, z - sp) || posBlock(x - sp, y, z - sp) || posBlock(x + sp, y, z + sp) || posBlock(x + sp, y, z - sp)
                    || posBlock(x - sp, y, z + sp) || posBlock(x + sp, y, z) || posBlock(x - sp, y, z) || posBlock(x, y, z + sp)
                    || posBlock(x, y, z - sp)) {
                    motionX = -motionX;
                    motionZ = -motionZ;
                }
            }
            z += motionZ;
            motionX /= 1.005;
            motionZ /= 1.005;
            motionY /= 1.005;
        }

        public void render(MatrixStack matrixStack) {
            update();
            float scale = 0.07f;
            final double posX = x - mc.getEntityRenderDispatcher().camera.getPos().getX();
            final double posY = y - mc.getEntityRenderDispatcher().camera.getPos().getY();
            final double posZ = z - mc.getEntityRenderDispatcher().camera.getPos().getZ();

            matrixStack.push();
            matrixStack.translate(posX, posY, posZ);
            matrixStack.scale(-scale, -scale, -scale);

            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));

            if (mode.get() == Mode.Orbiz) {
                drawOrbiz(matrixStack, 0.0f, 0.7, color);
                drawOrbiz(matrixStack, 0.1f, 1.4, color);
                drawOrbiz(matrixStack, 0.2f, 2.3, color);
            } else {
                drawStar(matrixStack, color);
            }

            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            return (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.WATER && mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() != Blocks.LAVA);
        }
    }

    public void drawOrbiz(MatrixStack matrices, float z, final double r, Color c) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 20; i++) {
            final double x2 = Math.sin(((i * 18 * Math.PI) / 180)) * r;
            final double y2 = Math.cos(((i * 18 * Math.PI) / 180)) * r;
            bufferBuilder.vertex(matrix, (float) (x2), (float) (y2), z).color(c.r / 255f, c.g / 255f, c.b / 255f, 0.4f).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public void drawStar(MatrixStack matrices, Color c) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, new Identifier(LemonClient.MOD_ID, "textures/star.png"));
        RenderSystem.setShaderColor(c.r / 255f, c.g / 255f, c.b / 255f, c.a / 255f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(mc.player.age * 2));
        Render2DUtils.renderTexture(matrices, 0, 0, starsScale.get(), starsScale.get(), 0, 0, 256, 256, 256, 256);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
