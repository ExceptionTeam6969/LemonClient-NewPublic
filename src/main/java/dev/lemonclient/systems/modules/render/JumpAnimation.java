package dev.lemonclient.systems.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.AstolfoAnimation;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.RenderCapUtils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import dev.lemonclient.utils.timers.TimerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL32;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JumpAnimation extends Module {
    public JumpAnimation() {
        super(Categories.Render, "Jump Animation", "Adds a visual animation when you jump.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColor = settings.createGroup("Color");

    //--------------------General--------------------//
    public Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Color Mode")
        .description("Select the color mode for the animation.")
        .defaultValue(Mode.Astolfo)
        .build()
    );
    public Setting<Integer> lifetime = sgGeneral.add(new IntSetting.Builder()
        .name("Live Time")
        .description("Set the lifetime of each animation circle in seconds.")
        .defaultValue(3)
        .sliderRange(1, 10)
        .build()
    );

    //--------------------Color--------------------//
    public final Setting<SettingColor> color = sgColor.add(new ColorSetting.Builder()
        .name("Color")
        .description("The primary color for the animation.")
        .defaultValue(new SettingColor(3649978))
        .build()
    );
    public final Setting<SettingColor> color2 = sgColor.add(new ColorSetting.Builder()
        .name("Color2")
        .description("The secondary color for the Two-Color mode.")
        .defaultValue(new SettingColor(3646789))
        .build()
    );
    public Setting<Integer> colorOffset1 = sgGeneral.add(new IntSetting.Builder()
        .name("Color Offset")
        .description("Offset for the Two-Color mode.")
        .defaultValue(10)
        .sliderRange(1, 20)
        .build()
    );

    private boolean check = false;
    public static AstolfoAnimation astolfo = new AstolfoAnimation();
    static List<Circle> circles = new ArrayList<>();

    public enum Mode {
        Custom,
        Rainbow,
        TwoColor,
        Astolfo
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player.isOnGround() && check) {
            circles.add(new Circle(new Vec3d(mc.player.getX(), mc.player.getY() + 0.0625, mc.player.getZ())));
            check = false;
        }

        if (!mc.player.isOnGround() && !check) {
            check = true;
        }

        astolfo.update();

        for (Circle circle : circles) {
            circle.update();
        }

        circles.removeIf(Circle::update);
        mc.player.distanceTraveled = 4f;
    }

    @EventHandler
    public void onRender(Render3DEvent.Unlimited event) {
        Collections.reverse(circles);

        try {
            for (Circle c : circles) {
                double x = c.getPosition().x - mc.getEntityRenderDispatcher().camera.getPos().getX();
                double y = c.getPosition().y - mc.getEntityRenderDispatcher().camera.getPos().getY();
                double z = c.getPosition().z - mc.getEntityRenderDispatcher().camera.getPos().getZ();
                float k = (float) c.timer.getPassedTimeMs() / (float) (lifetime.get() * 1000);
                float start = k * 2.2f;
                float middle = (start + k) / 2;

                event.matrices.push();
                event.matrices.translate(x, y, z);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuffer();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderCapUtils.enable(this.name, GL32.GL_LINE_SMOOTH);
                RenderSystem.disableDepthTest();
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);

                bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (int i = 0; i <= 360; i += 5) {
                    int clr = getColor(i);
                    double v = Math.sin(Math.toRadians(i));
                    double u = Math.cos(Math.toRadians(i));
                    bufferBuilder.vertex(event.matrices.peek().getPositionMatrix(), (float) u * start, (float) 0, (float) v * start).color(Render2DUtils.injectAlpha(new Color(clr), 0).getPacked()).next();
                    bufferBuilder.vertex(event.matrices.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DUtils.injectAlpha(new Color(clr), (int) (150f * (1.0F - (float) c.timer.getPassedTimeMs() / (float) (lifetime.get() * 1000)))).getPacked()).next();
                }

                tessellator.draw();
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

                for (int i = 0; i <= 360; i += 5) {
                    int clr = getColor(i);
                    double v = Math.sin(Math.toRadians(i));
                    double u = Math.cos(Math.toRadians(i));

                    bufferBuilder.vertex(event.matrices.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DUtils.injectAlpha(new Color(clr), (int) (150 * (1.0F - (float) c.timer.getPassedTimeMs() / (float) (lifetime.get() * 1000)))).getPacked()).next();
                    bufferBuilder.vertex(event.matrices.peek().getPositionMatrix(), (float) u * k, (float) 0, (float) v * k).color(Render2DUtils.injectAlpha(new Color(clr), 0).getPacked()).next();
                }

                tessellator.draw();

                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (int i = 0; i <= 360; i += 5) {
                    int clr = getColor(i);
                    double v = Math.sin(Math.toRadians(i));
                    double u = Math.cos(Math.toRadians(i));

                    bufferBuilder.vertex(event.matrices.peek().getPositionMatrix(), (float) u * middle, (float) 0, (float) v * middle).color(Render2DUtils.injectAlpha(new Color(clr), (int) (255f * (1.0F - (float) c.timer.getPassedTimeMs() / (float) (lifetime.get() * 1000)))).getPacked()).next();
                    bufferBuilder.vertex(event.matrices.peek().getPositionMatrix(), (float) u * (middle - 0.04f), (float) 0, (float) v * (middle - 0.04f)).color(Render2DUtils.injectAlpha(new Color(clr), 0).getPacked()).next();
                }
                tessellator.draw();

                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderCapUtils.reset(this.name);
                event.matrices.translate(-x, -y, -z);
                event.matrices.pop();
            }
        } catch (Exception ignored) {
        }

        Collections.reverse(circles);
    }

    private int getColor(int stage) {
        return switch (mode.get()) {
            case Custom -> color.get().getPacked();
            case Rainbow -> Render2DUtils.rainbow(stage, 1f, 1f).getPacked();
            case TwoColor -> getColor2(color.get(), color2.get(), stage).getPacked();
            case Astolfo -> astolfo.getColor(((stage + 90) / 360.));
        };
    }

    private Color getColor2(Color color1, Color color2, int offset) {
        return TwoColoreffect(color1, color2, Math.abs(System.currentTimeMillis() / 10) / 100.0 + offset * ((20f - colorOffset1.get()) / 200));
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed) {
        double thing = speed / 4.0 % 1.0;
        float val = MathHelper.clamp((float) Math.sin(Math.PI * 6 * thing) / 2.0f + 0.5f, 0.0f, 1.0f);
        return new Color((int) lerp((float) cl1.r / 255.0f, (float) cl2.r / 255.0f, val), (int) lerp((float) cl1.g / 255.0f, (float) cl2.g / 255.0f, val), (int) lerp((float) cl1.b / 255.0f, (float) cl2.b / 255.0f, val));
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    private class Circle {
        private final Vec3d vec3d;
        private final TimerUtils timer = new TimerUtils();

        Circle(Vec3d vec3d) {
            this.vec3d = vec3d;
            timer.reset();
        }

        Vec3d getPosition() {
            return this.vec3d;
        }

        public boolean update() {
            return timer.passedMs(lifetime.get() * 10000);
        }
    }
}
