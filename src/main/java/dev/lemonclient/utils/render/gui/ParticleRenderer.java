package dev.lemonclient.utils.render.gui;

import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.lemonclient.LemonClient.mc;

public class ParticleRenderer {
    private final List<CParticle> particles = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    public ParticleRenderer(int in) {
        addParticle(in);
    }

    public void render(DrawContext context, int width, int height) {
        drawParticles(context, width, height);
    }

    public static double getDistance(float x, float y, float x1, float y1) {
        return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
    }

    public void addParticle(int in) {
        for (int i = 0; i < in; ++i) {
            this.particles.add(CParticle.getParticle());
        }
    }

    private void drawTracer(float f, float f2, float f3, float f4, Color firstColor, Color secondColor, Color thirdColor) {
        Renderer2D renderer = Renderer2D.COLOR;
        float y = f2 >= f4 ? f4 + (f2 - f4) / 2.0f : f2 + (f4 - f2) / 2.0f;
        float x = f >= f3 ? f3 + (f - f3) / 2.0f : f + (f3 - f) / 2.0f;

        int iFirst = renderer.lines.vec2(f, f2).color(firstColor).next();
        int iSecond = renderer.lines.vec2(x, y).color(secondColor).next();
        int iForth = renderer.lines.vec2(x, y).color(secondColor).next();
        int iFifth = renderer.lines.vec2(f3, f4).color(thirdColor).next();

        renderer.lines.line(iFirst, iSecond);
        renderer.lines.line(iForth, iFifth);
    }

    public void drawParticles(DrawContext context, double sWidth, double sHeight) {
        Renderer2D.COLOR.begin();
        for (CParticle particle : this.particles) {
            particle.setup(2, 0.1f);
            int width = (int) mc.mouse.getX();
            int height = (int) mc.mouse.getY();
            float alpha = (float) MathHelper.clamp((double) particle.getAlpha() - (double) (particle.getAlpha() / 300.0f) * getDistance(width, height, particle.getX(), particle.getY()), 0.0, particle.getAlpha());
            Color color = Render2DUtils.injectAlpha(new Color(-1714829883), (int) alpha);

            float middleSize = particle.getSize() / 2;
            Renderer2D.COLOR.quad(particle.getX() - middleSize, particle.getY() - middleSize, particle.getSize(), particle.getSize(), color);

            float nearestDistance = 0.0f;
            CParticle nearestParticle = null;
            for (CParticle secondParticle : this.particles) {
                float distance = particle.getDistanceTo(secondParticle);
                if (!(distance <= 300.0f) || !(getDistance(width, height, particle.getX(), particle.getY()) <= 300.0) && !(getDistance(width, height, secondParticle.getX(), secondParticle.getY()) <= 300.0) || nearestDistance > 0.0f && distance > nearestDistance)
                    continue;
                nearestDistance = distance;
                nearestParticle = secondParticle;
            }
            if (nearestParticle == null) continue;
            Renderer2D.COLOR.quad(nearestParticle.getX() - middleSize, nearestParticle.getY() - middleSize, particle.getSize(), particle.getSize(), color);
            this.drawTracer(particle.getX(), particle.getY(), nearestParticle.getX(), nearestParticle.getY(), color, Render2DUtils.injectAlpha(new Color(0x838080), (int) alpha), color);
        }
        Renderer2D.COLOR.render(context.getMatrices());
    }

    public static class CParticle {
        private static final Random random = new Random();
        private final Vector2f pos;
        private final Vector2f velocity;
        private float alpha;
        private float size;

        public CParticle(Vector2f velocity, float x, float y, float size) {
            this.velocity = velocity;
            this.pos = new Vector2f(x, y);
            this.size = size;
        }

        public static CParticle getParticle() {
            Vector2f velocity = new Vector2f((float) (Math.random() * 3.0 - 1.0), (float) (Math.random() * 3.0 - 1.0));
            float x = random.nextInt(Utils.getWindowWidth());
            float y = random.nextInt(Utils.getWindowHeight());
            float size = (float) (Math.random() * 4.0) + 2.0f;
            return new CParticle(velocity, x, y, size);
        }

        public float getAlpha() {
            return this.alpha;
        }

        public float getDistanceTo(CParticle particle) {
            return this.getDistanceTo(particle.getX(), particle.getY());
        }

        public float getDistanceTo(float f, float f2) {
            return (float) getDistance(this.getX(), this.getY(), f, f2);
        }

        public float getSize() {
            return this.size;
        }

        public void setSize(float f) {
            this.size = f;
        }

        public float getX() {
            return this.pos.x;
        }

        public void setX(float f) {
            this.pos.x = f;
        }

        public float getY() {
            return this.pos.y();
        }

        public void setY(float f) {
            this.pos.y = f;
        }

        public void setup(int delta, float speed) {
            float width = Utils.getWindowWidth();
            float height = Utils.getWindowHeight();

            Vector2f pos = this.pos;
            pos.x += this.velocity.x() * (float) delta * (speed / 2.0f);
            Vector2f pos2 = this.pos;
            pos2.y += this.velocity.y() * (float) delta * (speed / 2.0f);
            if (this.alpha < 180.0f) {
                this.alpha += 0.75f;
            }
            if (this.pos.x() > width) {
                this.pos.x = 0.0f;
            }
            if (this.pos.x() < 0.0f) {
                this.pos.x = width;
            }
            if (this.pos.y() > height) {
                this.pos.y = 0.0f;
            }
            if (this.pos.y() < 0.0f) {
                this.pos.y = height;
            }
        }
    }
}
