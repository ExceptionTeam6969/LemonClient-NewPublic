package dev.lemonclient.utils.render.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.FadeUtils;
import dev.lemonclient.utils.render.Rainbow;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.timers.MeteorTimerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MeteorRenderer {
    public List<LineMeteorRenderer> meteorList = new ArrayList<>();
    private boolean rainbow;

    public MeteorRenderer(int initAmount, boolean rainbow) {
        this.addParticles(initAmount);
        this.rainbow = rainbow;
    }

    public MeteorRenderer(int initAmount) {
        this(initAmount, false);
    }

    public void addParticles(int amount) {
        for (int i = 0; i < amount; ++i) {
            this.meteorList.add(LineMeteorRenderer.generateMeteor());
        }
    }

    public void tick() {
        for (LineMeteorRenderer meteor : this.meteorList) {
            meteor.tick();
        }
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public void render(DrawContext context) {
        if (MinecraftClient.getInstance().currentScreen == null) {
            return;
        }
        this.meteorList.forEach(meteor -> {
            Color color = this.rainbow ? meteor.randomColor : Color.WHITE;

            Renderer2D.COLOR.begin();
            RenderSystem.lineWidth(meteor.getLineWidth());
            Renderer2D.COLOR.line(meteor.getX(), meteor.getY(), meteor.getX2(), meteor.getY2(), new Color(color.r, color.g, color.b, (int) meteor.getAlpha()));
            Renderer2D.COLOR.render(context.getMatrices());
        });
    }

    public static class LineMeteorRenderer {
        public Vector2d pos;
        public Vector2d pos2;
        public static Random random = new Random();
        public float lineWidth;
        public float lineLength;
        public float alpha;
        public long speedMS;
        private final MeteorTimerUtils meteorTimer = new MeteorTimerUtils();
        public Color randomColor;
        private final FadeUtils fadeUtils = new FadeUtils(170L);

        public LineMeteorRenderer(long speedMS, double x, double y, float LineWidth, float LineLength) {
            this.speedMS = speedMS;
            this.pos = new Vector2d(x, y);
            this.pos2 = new Vector2d(x, y);
            this.lineWidth = LineWidth;
            this.lineLength = LineLength;
            this.randomColor = new Color(java.awt.Color.HSBtoRGB(random.nextInt(360), 0.4f, 1.0f));
            this.fadeUtils.setLength(speedMS / 5L * 2L);
        }

        public static LineMeteorRenderer generateMeteor() {
            long speedMS = 3000 + random.nextInt(1200);
            float x = random.nextInt(Utils.getWindowWidth());
            float y = random.nextInt(Utils.getWindowHeight());
            float lineLength = 50 + random.nextInt(300);
            float lineWidth = (float) (Math.random() * 2.0) + 1.0f;
            return new LineMeteorRenderer(speedMS, x, y, lineWidth, lineLength);
        }

        public float getAlpha() {
            return this.alpha;
        }

        public float getLineWidth() {
            return this.lineWidth;
        }

        public double getX() {
            return this.pos.x;
        }

        public double getY() {
            return this.pos.y;
        }

        public double getX2() {
            return this.pos2.x;
        }

        public double getY2() {
            return this.pos2.y;
        }

        public void setLineWidth(float f) {
            this.lineWidth = f;
        }

        public void tick() {
            long speedMoves;
            if (this.meteorTimer.passedCurrent(this.speedMS)) {
                this.meteorTimer.resetCurrent();
                this.pos.x = random.nextInt(Utils.getWindowWidth());
                this.pos.y = random.nextInt(Utils.getWindowHeight());
                this.lineLength = 70 + random.nextInt(300);
                this.randomColor = new Color(Rainbow.getRainbow(random.nextInt(360), 0.4f, 1.0f));
                this.fadeUtils.reset();
                this.alpha = 0.0f;
            }
            if (this.meteorTimer.passedCurrent((speedMoves = (this.speedMS / 5L)) * 3L)) {
                this.pos.x = (this.pos2.x + this.lineLength * this.fadeUtils.getFade(FadeUtils.FadeMode.FADE_OUT));
                this.pos.y = (this.pos2.y - this.lineLength * this.fadeUtils.getFade(FadeUtils.FadeMode.FADE_OUT));
            } else if (this.meteorTimer.passedCurrent((long) (speedMoves * 2.0))) {
                this.fadeUtils.reset();
            } else {
                this.pos2.x = (this.pos.x - this.lineLength * this.fadeUtils.getFade(FadeUtils.FadeMode.FADE_IN));
                this.pos2.y = (this.pos.y + this.lineLength * this.fadeUtils.getFade(FadeUtils.FadeMode.FADE_IN));
            }
            if (this.alpha < 255.0f) {
                this.alpha += 15.0f;
            }
        }
    }
}
