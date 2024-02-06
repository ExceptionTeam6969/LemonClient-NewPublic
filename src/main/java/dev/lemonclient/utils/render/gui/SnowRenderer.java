package dev.lemonclient.utils.render.gui;

import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.timers.MSTimer;
import net.minecraft.client.gui.DrawContext;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class SnowRenderer {
    private final List<Snow> snow = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private final MSTimer timer = new MSTimer();

    public SnowRenderer() {
    }

    public void render(DrawContext context) {
        if (!this.snow.isEmpty()) {
            this.snow.forEach(snow -> snow.drawSnow(context));
        }
    }

    public void tick(long delay, int height) {
        if (timer.hasTimePassed(delay)) {
            Random random = new Random();
            for (int i = 0; i < 100; ++i) {
                for (int y = 0; y < 3; ++y) {
                    Snow snow = new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1);
                    this.snow.add(snow);
                }
            }
            timer.reset();
        }

        for (Snow snow : snow) {
            if (snow.c.a < 10) {
                this.snow.remove(snow);
            }

            if (snow.y > height - 35) {
                if (snow.c.a > 0) {
                    snow.c.a--;
                }
            }
        }
    }

    public static class Snow {
        private float x;
        private float y;
        private int fallingSpeed;
        private float moveSpeed;
        private int size;
        private final Color c = new Color(-1714829883);

        public Snow(int x, int y, int fallingSpeed, int size) {
            this.x = x;
            this.y = y;
            this.fallingSpeed = fallingSpeed;
            this.size = size;
        }

        public float getX() {
            return this.x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return this.y;
        }

        public void setY(float _y) {
            this.y = _y;
        }

        public void drawSnow(DrawContext context) {
            Renderer2D.COLOR.begin();
            double midSize = this.size / 2D;
            Renderer2D.COLOR.drawCircle(this.getX() - midSize, this.getY() - midSize, this.size, c);
            Renderer2D.COLOR.render(context.getMatrices());

            this.setY(this.getY() + this.fallingSpeed);
            this.setX(this.getX() - this.moveSpeed);

            Random rand = new Random();
            this.moveSpeed = rand.nextFloat();
            if (this.getY() > Utils.getWindowHeight() * 2 + 10 || this.getY() < -10) {
                this.setY(-10);
                this.fallingSpeed = rand.nextInt(10) + 1;
                this.size = rand.nextInt(4) + 1;
            }
        }
    }
}
