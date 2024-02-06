package dev.lemonclient.systems.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.LemonClient;
import dev.lemonclient.music.player.MusicControl;
import dev.lemonclient.music.player.MusicInfo;
import dev.lemonclient.renderer.GL;
import dev.lemonclient.renderer.Renderer2D;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.HudElementInfo;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.render.MSAAFramebuffer;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import static dev.lemonclient.LemonClient.mc;

public class MusicHud extends HudElement {
    public static final HudElementInfo<MusicHud> INFO = new HudElementInfo<>(Hud.GROUP, "music-hud", "music.", MusicHud::new);

    private final SettingGroup sgA = settings.getDefaultGroup();
    private final SettingGroup sgKey = settings.createGroup("key");
    public static MusicHud INSTANCE;

    public final Setting<Integer> volume = sgA.add(new IntSetting.Builder()
        .name("volume")
        .defaultValue(80)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );

    public final Setting<Integer> maxMusicNameLength = sgA.add(new IntSetting.Builder()
        .name("music-name-length")
        .defaultValue(15)
        .min(1)
        .sliderMin(1)
        .build()
    );

    public final Setting<Boolean> enablePitch = sgA.add(new BoolSetting.Builder()
        .name("enable-pitch")
        .defaultValue(false)
        .build()
    );
    public final Setting<Integer> pitch = sgA.add(new IntSetting.Builder()
        .name("pitch")
        .defaultValue(10)
        .sliderRange(0, 150)
        .visible(enablePitch::get)
        .build()
    );

    private final Setting<Boolean> onInventory = sgA.add(new BoolSetting.Builder()
        .name("inventory")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> noChat = sgA.add(new BoolSetting.Builder()
        .name("chat")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> debug = sgA.add(new BoolSetting.Builder()
        .name("debug")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> onlyPlay = sgA.add(new BoolSetting.Builder()
        .name("only-play")
        .defaultValue(true)
        .build()
    );

    public final Setting<SettingColor> anaColor = sgKey.add(new ColorSetting.Builder()
        .name("bar-color")
        .defaultValue(new SettingColor(255, 255, 255, 200))
        .build()
    );

    public static boolean onlyPlay() {
        return INSTANCE.isActive() ? INSTANCE.onlyPlay.get() : false;
    }

    public static boolean debug() {
        return INSTANCE.isActive() ? INSTANCE.debug.get() : false;
    }

    @Override
    public void tick(HudRenderer renderer) {
        setSize(75, 75);
        super.tick(renderer);
    }

    public void draw(DrawContext context, String message, float x, float y) {
        this.draw(context, message, x, y, mc.getWindow().getScaleFactor());
    }

    public void draw(DrawContext context, String message, float xPos, float yPos, double scale) {
        float x = xPos;
        float y = yPos;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        x += (float) (0.5 * scale);
        y += (float) (0.5 * scale);
        matrices.scale((float) scale, (float) scale, 1);
        context.drawText(mc.textRenderer, message, (int) (x / scale), (int) (y / scale), Color.WHITE.getPacked(), false);
        matrices.pop();
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.currentScreen instanceof ChatScreen) {
            if (!noChat.get()) {
                return;
            }
        }
        if (mc.currentScreen instanceof InventoryScreen) {
            if (!onInventory.get()) {
                return;
            }
        }

        renderer.post(() -> {
            MSAAFramebuffer.use(() -> {
                MusicInfo musicInfo = MusicControl.INSTANCE.nowPlayMusic;
                if (musicInfo != null && musicInfo.author() != null && musicInfo.name() != null) {
                    Renderer2D.COLOR.begin();
                    Renderer2D.TEXTURE.begin();

                    String name = musicInfo.name();
                    if (name.length() > this.maxMusicNameLength.get()) {
                        name = name.substring(0, this.maxMusicNameLength.get()) + "..";
                    }
                    String author = musicInfo.name();
                    if (author.length() > this.maxMusicNameLength.get()) {
                        author = author.substring(0, this.maxMusicNameLength.get()) + "..";
                    }
                    double infoWidth = width(name);
                    double authorWidth = width(author);
                    double playStatusWidth = width(musicInfo.statusInfo());
                    double timeWidth = width(musicInfo.timeInfo());
                    double maxWidth = 95 + Math.max(Math.max(infoWidth, authorWidth), Math.max(playStatusWidth, timeWidth));
                    int x = this.x - 10;
                    int y = this.y - 10;
                    drawRoundRect(x, y, maxWidth == 0 ? 100 : maxWidth, 100, 3, new Color(0, 0, 0, 120));
                    drawRoundRect(x, y, maxWidth == 0 ? 100 : maxWidth, 2, 3, new Color(0, 35, 220, 120));
                    draw(renderer.drawContext, name, x + 10 + 80, y + 10, 2);
                    draw(renderer.drawContext, author, x + 10 + 80, y + 10 + 20, 2);
                    draw(renderer.drawContext, musicInfo.statusInfo(), x + 10 + 80, y + 10 + 20 * 2, 2);
                    draw(renderer.drawContext, musicInfo.timeInfo(), x + 10 + 80, y + 10 + 20 * 3, 2);
                    if (musicInfo.picture() != null && Utils.canUpdate()) {
                        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                        GL.bindTexture(musicInfo.picture());
                        Renderer2D.TEXTURE.quad(x + 10, y + 10, 75, 75, Color.WHITE);
                    }

                    if (musicInfo.musicProgress != 0) {
                        drawRoundRect(x + 2, y + 100 - 3, ((maxWidth == 0 ? 100 : maxWidth) - 4), 2, 2, new Color(0, 0, 0, 170));
                        drawRoundRect(x + 2, y + 100 - 3, ((maxWidth == 0 ? 100 : maxWidth) - 4) * musicInfo.musicProgress, 2, 2, anaColor.get());
                    }

                    Renderer2D.COLOR.render(null);
                    Renderer2D.TEXTURE.render(null);
                }
            });
        });
        super.render(renderer);
    }

    public double width(String message) {
        return this.width(message, mc.getWindow().getScaleFactor());
    }

    public double width(String message, double scale) {
        return mc.textRenderer.getWidth(message) * scale;
    }

    private void drawRoundRect(double x, double y, double width, double height, double radius, Color color) {
        Renderer2D.COLOR.quadRounded(x, y, width, height, color, radius, true);
    }


    public MusicHud() {
        super(INFO);
        INSTANCE = this;
        LemonClient.EVENT_BUS.subscribe(this);
    }
}
