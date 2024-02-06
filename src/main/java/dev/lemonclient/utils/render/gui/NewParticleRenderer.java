package dev.lemonclient.utils.render.gui;

import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.Render2DUtils;
import dev.lemonclient.utils.render.color.Color;
import org.joml.Vector2f;

import java.util.concurrent.ThreadLocalRandom;

import static dev.lemonclient.LemonClient.mc;

public class NewParticleRenderer {
    private final AParticle[] particles;

    public NewParticleRenderer() {
        this.particles = new AParticle[200];
        for (int i = 0; i < 200; ++i) {
            this.particles[i] = new AParticle(new Vector2f((float) (Math.random() * Utils.getWindowWidth()), (float) (Math.random() * Utils.getWindowHeight())));
        }
    }

    public static double map(double value, final double a, final double b, final double c, final double d) {
        value = (value - a) / (b - a);
        return c + value * (d - c);
    }

    public void update() {
        for (int i = 0; i < 200; ++i) {
            final AParticle particle = this.particles[i];
            if (mc.getWindow() != null) {
                final boolean isOffScreenX = particle.getPos().x > Utils.getWindowWidth() || particle.getPos().x < 0.0f;
                final boolean isOffScreenY = particle.getPos().y > Utils.getWindowHeight() || particle.getPos().y < 0.0f;
                if (isOffScreenX || isOffScreenY) {
                    particle.respawn();
                }
            }
            particle.update();
        }
    }

    public void render(int mouseX, int mouseY) {
        for (int i = 0; i < 200; ++i) {
            final AParticle particle = this.particles[i];
            for (int j = 1; j < 200; ++j) {
                if (i != j) {
                    final AParticle otherParticle = this.particles[j];
                    final Vector2f diffPos = new Vector2f(particle.getPos());
                    diffPos.sub(otherParticle.getPos());
                    final float diff = diffPos.length();
                    final int distance = (int) (80.0 / ((mc.getWindow().getScaleFactor() <= 1) ? 3 : mc.getWindow().getScaleFactor()));
                    if (diff < distance) {
                        final int lineAlpha = (int) map(diff, distance, 0.0, 0.0, 127.0);
                        if (lineAlpha > 8) {
                            Renderer2D renderer = Renderer2D.COLOR;
                            Color color = Render2DUtils.injectAlpha(new Color(-1714829883), particle.alpha - 20);
                            int i1 = renderer.lines.vec2(particle.getPos().x + particle.getSize() / 2.0f, particle.getPos().y + particle.getSize() / 2.0f).color(color).next();
                            int i2 = renderer.lines.vec2(otherParticle.getPos().x + otherParticle.getSize() / 2.0f, otherParticle.getPos().y + otherParticle.getSize() / 2.0f).color(color).next();
                            renderer.lines.line(i1, i2);
                        }
                    }
                }
            }
            particle.render(mouseX, mouseY);
        }
    }

    public static class AParticle {
        private final int maxAlpha;
        private Vector2f pos;
        private Vector2f velocity;
        private Vector2f acceleration;
        private int alpha;
        private float size;

        public AParticle(final Vector2f pos) {
            this.pos = pos;
            final int lowVel = -1;
            final int highVel = 1;
            final float resultXVel = -1.0f + ThreadLocalRandom.current().nextFloat() * 2.0f;
            final float resultYVel = -1.0f + ThreadLocalRandom.current().nextFloat() * 2.0f;
            this.velocity = new Vector2f(resultXVel, resultYVel);
            this.acceleration = new Vector2f(0.0f, 0.35f);
            this.alpha = 0;
            this.maxAlpha = ThreadLocalRandom.current().nextInt(32, 192);
            this.size = 0.5f + ThreadLocalRandom.current().nextFloat() * 1.5f;
        }

        public static int changeAlpha(int origColor, final int userInputedAlpha) {
            origColor &= 0xFFFFFF;
            return userInputedAlpha << 24 | origColor;
        }

        public void respawn() {
            this.pos = new Vector2f((float) (Math.random() * Utils.getWindowWidth()), (float) (Math.random() * Utils.getWindowHeight()));
        }

        public void update() {
            if (this.alpha < this.maxAlpha) {
                this.alpha += 8;
            }
            if (this.acceleration.x > 0.35f) {
                this.acceleration.x = (this.acceleration.x() * 0.975f);
            } else if (this.acceleration.x() < -0.35f) {
                this.acceleration.x = (this.acceleration.x() * 0.975f);
            }
            if (this.acceleration.y() > 0.35f) {
                this.acceleration.y = (this.acceleration.y() * 0.975f);
            } else if (this.acceleration.y() < -0.35f) {
                this.acceleration.y = (this.acceleration.y() * 0.975f);
            }
            this.pos.add(this.acceleration);
            this.pos.add(this.velocity);
        }

        public void render(final int mouseX, final int mouseY) {
            if (mc.mouse.wasLeftButtonClicked()) {
                final float deltaXToMouse = mouseX - this.pos.x();
                final float deltaYToMouse = mouseY - this.pos.y();
                if (Math.abs(deltaXToMouse) < 50.0f && Math.abs(deltaYToMouse) < 50.0f) {
                    this.acceleration.x = (this.acceleration.x() + deltaXToMouse * 0.0015f);
                    this.acceleration.y = (this.acceleration.y() + deltaYToMouse * 0.0015f);
                }
            }

            Color color = Render2DUtils.injectAlpha(new Color(-1714829883), this.alpha);
            Renderer2D.COLOR.quad(this.pos.x, this.pos.y, this.size, this.size, color);
        }

        public Vector2f getPos() {
            return this.pos;
        }

        public void setPos(final Vector2f pos) {
            this.pos = pos;
        }

        public Vector2f getVelocity() {
            return this.velocity;
        }

        public void setVelocity(final Vector2f velocity) {
            this.velocity = velocity;
        }

        public Vector2f getAcceleration() {
            return this.acceleration;
        }

        public void setAcceleration(final Vector2f acceleration) {
            this.acceleration = acceleration;
        }

        public int getAlpha() {
            return this.alpha;
        }

        public void setAlpha(final int alpha) {
            this.alpha = alpha;
        }

        public float getSize() {
            return this.size;
        }

        public void setSize(final float size) {
            this.size = size;
        }
    }
}
