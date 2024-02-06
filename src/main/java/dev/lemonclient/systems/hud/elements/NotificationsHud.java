package dev.lemonclient.systems.hud.elements;

import dev.lemonclient.managers.Managers;
import dev.lemonclient.managers.impl.notification.Notification;
import dev.lemonclient.renderer.GL;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.HudElementInfo;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.utils.render.MSAAFramebuffer;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsHud extends HudElement {
    public static final HudElementInfo<NotificationsHud> INFO = new HudElementInfo<>(Hud.GROUP, "Notification Hud", "simple notification.", NotificationsHud::new);

    private static final Identifier ERROR_ID = new Identifier("lemon-client", "notification/error.png");
    private static final Identifier INFO_ID = new Identifier("lemon-client", "notification/info.png");
    private static final Identifier SUCCESS_ID = new Identifier("lemon-client", "notification/success.png");
    private static final Identifier WARN_ID = new Identifier("lemon-client", "notification/warning.png");

    private static final Color ERROR_COLOR = new Color(255, 0, 0);
    private static final Color INFO_COLOR = new Color(255, 255, 255);
    private static final Color SUCCESS_COLOR = new Color(0, 255, 0);
    private static final Color WARN_COLOR = Color.YELLOW;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColor = settings.createGroup("Color");

    private final Setting<Integer> maxNotifications = sgGeneral.add(new IntSetting.Builder()
        .name("max-notifications")
        .description("out of the num will remove")
        .defaultValue(7)
        .build()
    );
    private final Setting<Boolean> useCalcWidth = sgGeneral.add(new BoolSetting.Builder()
        .name("use-calc-width")
        .description("Automatic width calculation.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> reverse = sgGeneral.add(new BoolSetting.Builder()
        .name("reverse-notifications")
        .description("Reverse the notification render.")
        .defaultValue(false)
        .build()
    );

    /*private final Setting<Boolean> titleEnable = sgGeneral.add(new BoolSetting.Builder()
        .name("render-title")
        .description("Allow render notification title.")
        .defaultValue(false)
        .build()
    );*/
    private final Setting<SettingColor> backgroundColor = sgColor.add(new ColorSetting.Builder()
        .name("Background Color")
        .description(COLOR)
        .defaultValue(new SettingColor(70, 70, 70, 150))
        .build()
    );

    public NotificationsHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        setSize(250, 50);
        super.tick(renderer);
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            MSAAFramebuffer.use(() -> {
                double boxX = this.x;
                double boxY = this.y;

                TextRenderer font = TextRenderer.get();
                GL.enableBlend();
                double offset = 4;
                final List<Notification> copied = new ArrayList<>(Managers.NOTIFICATION.notifications);
                if (reverse.get()) {
                    Collections.reverse(copied);
                }

                Renderer2D.COLOR.begin();
                for (Notification n : copied) {
                    if (copied.size() > maxNotifications.get()) {
                        Managers.NOTIFICATION.notifications.get(0).remove();
                    }

                    double width = useCalcWidth.get() ? offset + (35 + getWidth(n.text, 1.1)) + offset : 250;

                    if (isInEditor()) {
                        Renderer2D.COLOR.quadRounded(n.x, n.y, width, 50, 4, backgroundColor.get());
                        Renderer2D.COLOR.render(null);
                        return;
                    }

                    if (n.showTime <= 1 && n.startUpdated) {
                        n.x = smoothMove(n.x, boxX + width);
                        if (n.x >= (boxX + width) - 2) {
                            n.willRemove = true;
                        }
                    } else if (n.startUpdated) {
                        n.x = smoothMove(n.x, (boxX + 250) - width);
                    }

                    n.y = smoothMove(n.y, boxY);

                    Renderer2D.COLOR.quadRounded(n.x, n.y, width, 50, 4, backgroundColor.get());

                    Color proColor = new Color();

                    switch (n.type) {
                        case INFO -> {
                            GL.bindTexture(INFO_ID);
                            proColor = INFO_COLOR;
                        }
                        case ERROR -> {
                            GL.bindTexture(ERROR_ID);
                            proColor = ERROR_COLOR;
                        }
                        case WARING -> {
                            GL.bindTexture(WARN_ID);
                            proColor = WARN_COLOR;
                        }
                        case SUCCESS -> {
                            GL.bindTexture(SUCCESS_ID);
                            proColor = SUCCESS_COLOR;
                        }
                    }

                    Renderer2D.TEXTURE.begin();
                    Renderer2D.TEXTURE.texQuad(n.x + offset, n.y + 15, 23, 23, Color.WHITE);
                    Renderer2D.TEXTURE.render(null);

                    font.begin(1.1);
                    font.render(n.text, n.x + 35, n.y + 15, proColor, false);
                    font.end();

                    boxY -= 50 + offset;
                }
                Renderer2D.COLOR.render(null);

                GL.disableBlend();
            });
        });
        super.render(renderer);
    }

    public static double getWidth(String text, double scale) {
        TextRenderer font = TextRenderer.get();
        font.begin(scale);
        double w = font.getWidth(text);
        font.end();
        return w;
    }

    private double smoothMove(double start, double end) {
        double speed = (end - start) * 0.1;

        if (speed > 0) {
            speed = Math.max(0.1, speed);
            speed = Math.min(end - start, speed);
        } else if (speed < 0) {
            speed = Math.min(-0.1, speed);
            speed = Math.max(end - start, speed);
        }
        return start + speed;
    }
}
