package dev.lemonclient.systems.hud.elements;

import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.renderer.text.TextRenderer;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ToastNotificationsHud extends HudElement {
    public static final HudElementInfo<ToastNotificationsHud> INFO = new HudElementInfo<>(Hud.GROUP, "Toast Notifications", "Displays toast notifications on hud.", ToastNotificationsHud::new);

    public static ToastNotificationsHud INSTANCE;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> toggleMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("Toggle Message")
        .description("Sends info about toggled modules.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> sound = sgGeneral.add(new BoolSetting.Builder()
        .name("Sound")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> left = sgGeneral.add(new BoolSetting.Builder()
        .name("Left Sided")
        .defaultValue(false)
        .build()
    );
    public final Setting<List<Module>> toggleList = sgGeneral.add(new ModuleListSetting.Builder()
        .name("Modules For Displaying")
        .defaultValue(Modules.get().getModulesByCategory(Categories.Combat))
        .visible(toggleMessage::get)
        .build()
    );
    private final Setting<Integer> removeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Remove Delay")
        .description("Delay to clean latest message.")
        .defaultValue(7)
        .min(1)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("Shadow")
        .description("Renders a shadow behind the chars.")
        .defaultValue(false)
        .build()
    );

    public ToastNotificationsHud() {
        super(INFO);
        INSTANCE = this;
    }

    public static ArrayList<Notifications> toasts = new ArrayList<>();
    static int timer1;

    @Override
    public void tick(HudRenderer renderer) {
        updator();
        double width = 0;
        double height = 0;
        width = Math.max(width, renderer.textWidth("toast-messages"));
        height += renderer.textHeight();

        box.setSize(width, height);

        //if (mc.player.isUsingItem()) addToast("Player is using");
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            try {
                Color back = new Color(50, 50, 50, 255);
                Color textColor = new Color(255, 255, 255, 255);
                updator();
                double x = this.x - 0.5;
                double y = this.y - 0.5;

                int w = getWidth();
                int h = getHeight();

                if (isInEditor()) {
                    renderer.text("toast-messages", x, y, textColor, false);
                    Renderer2D.COLOR.begin();
                    Renderer2D.COLOR.quad(x, y, w, h, back);
                    Renderer2D.COLOR.render(null);
                    return;
                }
                int i = 0;

                if (toasts.isEmpty()) {
                    String t = "";
                    TextRenderer.get().render(t, x + alignX(renderer.textWidth(t), Alignment.Auto), y, textColor, shadow.get());
                } else {
                    for (Notifications mes : toasts) {
                        Notifications m;
                        m = mes;

                        double width = TextRenderer.get().getWidth(m.text) + 5;
                        double end = left.get() ? this.x + width : this.x + getWidth() - width;
                        double start = left.get() ? this.x - width : end + width;


                        if (m.pos < width)
                            m.pos = moveX(m.pos, width + 1);

                        if (m.pos > width)
                            m.pos = width;

                        if (i == 0 && timer1 >= removeDelay.get() * 140 - 100) {
                            m.pos = moveX(m.pos, -(width + 1));
                        }

                        start = left.get() ? start + m.pos + 6 : start - m.pos;

                        Renderer2D.COLOR.begin();
                        Renderer2D.COLOR.quad(start - 6, y - 4, TextRenderer.get().getWidth(m.text) + 10, renderer.textHeight(), m.color);
                        Renderer2D.COLOR.quad(start - 2, y - 4, TextRenderer.get().getWidth(m.text) + 2, renderer.textHeight(), back);
                        Renderer2D.COLOR.render(null);

                        TextRenderer.get().render(m.text, start, y - 5, textColor, shadow.get());
                        y += renderer.textHeight();
                        if (i >= 0) y += 1;
                        i++;
                    }
                }
            } catch (ConcurrentModificationException e) {
                e.fillInStackTrace();
            }
        });
    }

    public static void addToast(String text, Color color) {
        if (INSTANCE == null) return;

        if (toasts.size() == 0) timer1 = 0;
        toasts.add(new Notifications(text, color));
        MinecraftClient mc;
        mc = MinecraftClient.getInstance();
        if (ToastNotificationsHud.INSTANCE.sound.get())
            mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    public static void addToast(String text) {
        if (INSTANCE == null) return;

        if (toasts.size() == 0) timer1 = 0;
        toasts.add(new Notifications(text, null));
        if (ToastNotificationsHud.INSTANCE.sound.get())
            MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    public static void addToggled(String text, Color color) {
        if (toasts.size() == 0) timer1 = 0;
        toasts.add(new Notifications(text, color));
    }

    public static void addToggled(Module module, String mes) {
        String nameToTitle = Utils.nameToTitle(module.name);

        toasts.removeIf(toasts -> toasts.text.endsWith("OFF"));
        toasts.removeIf(toasts -> toasts.text.endsWith("ON"));

        if (mes.contains("F")) {
            addToggled(nameToTitle + " OFF", new Color(255, 0, 0, 255));
        } else {
            addToggled(nameToTitle + " ON", new Color(0, 255, 0, 255));
        }
        if (toasts.size() == 0) timer1 = 0;
    }

    private void updator() {
        if (toasts.size() > 7) toasts.remove(0);
        if (toasts.isEmpty()) return;
        if (timer1 >= removeDelay.get() * 140) {
            toasts.remove(0);
            timer1 = 0;
        } else timer1++;
    }

    private static double moveX(double start, double end) {
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


    public static class Notifications {
        public final String text;
        public final Color color;
        public double pos = -1;

        public Notifications(String text, Color color) {
            if (color == null) color = new Color(0, 155, 255, 255);
            this.text = text;
            this.color = color;
        }
    }
}
