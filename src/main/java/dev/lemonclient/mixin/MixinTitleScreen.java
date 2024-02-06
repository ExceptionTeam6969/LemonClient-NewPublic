package dev.lemonclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.LemonClient;
import dev.lemonclient.enums.MainMenuShaders;
import dev.lemonclient.managers.Managers;
import dev.lemonclient.pathing.BaritoneUtils;
import dev.lemonclient.render.gui.ClickMenuBox;
import dev.lemonclient.render.gui.DropdownBox;
import dev.lemonclient.render.gui.GuiRenderer;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.systems.config.Config;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.network.Executor;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.prompts.OkPrompt;
import dev.lemonclient.utils.render.prompts.YesNoPrompt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static dev.lemonclient.LemonClient.mc;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
    @Unique
    private static final Identifier PANORAMA_OVERLAY = new Identifier("lemon-client", "textures/background.png");

    @Unique
    private static final Text COPYRIGHT = Text.literal("Copyright LemonClient Development. Do not distribute!");

    @Shadow
    private long backgroundFadeStart;

    @Mutable
    @Final
    @Shadow
    private final boolean doBackgroundFade;

    @Mutable
    @Final
    @Shadow
    private final RotatingCubeMapRenderer backgroundRenderer;

    @Mutable
    @Final
    @Shadow
    private final LogoDrawer logoDrawer;

    @Shadow
    @Nullable
    private TitleScreen.DeprecationNotice deprecationNotice;

    @Shadow
    @Nullable
    private SplashTextRenderer splashText;

    @Shadow
    private void initWidgetsNormal(int l, int m) {
    }

    @Shadow
    private void initWidgetsDemo(int y, int spacingY) {
    }

    @Unique
    private DropdownBox<MainMenuShaders> shaderDropdown;
    @Unique
    private ClickMenuBox menuBox;
    @Unique
    private GuiRenderer renderer;


    public MixinTitleScreen(Text title, boolean doBackgroundFade, RotatingCubeMapRenderer backgroundRenderer, LogoDrawer logoDrawer) {
        super(title);
        this.doBackgroundFade = doBackgroundFade;
        this.backgroundRenderer = backgroundRenderer;
        this.logoDrawer = logoDrawer;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (shaderDropdown == null) {
            shaderDropdown = new DropdownBox<>(5, 5, 100, 35, Config.get().titleScreenShaderType.get(), 5, new Color(255, 255, 255, 100), new Color(0, 183, 253, 120), (last, current) -> Config.get().titleScreenShaderType.set(current));
        }

        shaderDropdown.preMouseClick(mouseX, mouseY, button);
        if (menuBox != null) {
            menuBox.preMouseClick(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        super.tick();
        if (shaderDropdown == null) {
            shaderDropdown = new DropdownBox<>(5, 5, 100, 35, Config.get().titleScreenShaderType.get(), 5, new Color(255, 255, 255, 100), new Color(0, 183, 253, 120), (last, current) -> Config.get().titleScreenShaderType.set(current));
        }

        shaderDropdown.preTick();
        if (menuBox != null) {
            menuBox.preTick();
        }
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true, require = 0)
    public void onInit(CallbackInfo ci) {
        ci.cancel();

        if (this.splashText == null) {
            this.splashText = this.client.getSplashTextLoader().get();
        }

        Map<String, Consumer<ClickMenuBox>> menus = new HashMap<>();
        menus.put("Shader", w -> {
            EnumSetting<MainMenuShaders> setting = (EnumSetting<MainMenuShaders>) Config.get().titleScreenShaderType;
            MainMenuShaders current = setting.get();
            int next = current.type + 1;
            if (next > 4) {
                next = 0;
            }
            setting.set(MainMenuShaders.get(next));
            shaderDropdown.set(MainMenuShaders.get(next));
            w.hiddenMenu();
        });
        menus.put("Close", ClickMenuBox::hiddenMenu);
        menuBox = new ClickMenuBox(5, 5, 100, 35, 5, new Color(0, 0, 0, 170), new Color(255, 255, 255, 120), menus);

        int i = this.textRenderer.getWidth(COPYRIGHT);
        int j = this.width - i - 2;
        int l = this.height / 4 + 48;
        if (this.client.isDemo()) {
            this.initWidgetsDemo(l, 24);
        } else {
            this.initWidgetsNormal(l, 24);
        }

        this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 124, l + 72 + 12, 20, 20, 0, 106, 20, ButtonWidget.WIDGETS_TEXTURE, 256, 256, button -> this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager())), Text.translatable("narrator.button.language")));

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), button -> this.client.setScreen(new OptionsScreen(this, this.client.options))).dimensions(this.width / 2 - 100, l + 72 + 12, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Shader"), button -> {
            mc.setScreen(this);

            EnumSetting<MainMenuShaders> setting = (EnumSetting<MainMenuShaders>) Config.get().titleScreenShaderType;
            MainMenuShaders current = setting.get();
            int next = current.type + 1;
            if (next > 4) {
                next = 0;
            }
            setting.set(MainMenuShaders.get(next));
            shaderDropdown.set(MainMenuShaders.get(next));
        }).dimensions(this.width / 2 + 120, l, 50, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), button -> this.client.scheduleStop()).dimensions(this.width / 2 + 2, l + 72 + 12, 98, 20).build());

        this.addDrawableChild(new TexturedButtonWidget(this.width / 2 + 104, l + 72 + 12, 20, 20, 0, 0, 20, ButtonWidget.ACCESSIBILITY_TEXTURE, 32, 64, button -> this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options)), Text.translatable("narrator.button.accessibility")));

        this.addDrawableChild(new TextWidget(j, this.height - 10, i, 10, COPYRIGHT, this.textRenderer));
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();

        if (shaderDropdown == null) {
            shaderDropdown = new DropdownBox<>(5, 5, 100, 35, Config.get().titleScreenShaderType.get(), 5, new Color(255, 255, 255, 100), new Color(0, 183, 253, 120), (last, current) -> Config.get().titleScreenShaderType.set(current));
        }

        if (renderer == null) {
            renderer = new GuiRenderer(context);
            renderer.mesh = false;
        }

        if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }

        float f = this.doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
        this.backgroundRenderer.render(delta, MathHelper.clamp(f, 0.0F, 1.0F));
        RenderSystem.enableBlend();
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.doBackgroundFade ? (float) MathHelper.ceil(MathHelper.clamp(f, 0.0F, 1.0F)) : 1.0F);
        context.drawTexture(PANORAMA_OVERLAY, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);

        Managers.MESH_RENDERER.begin();
        Managers.MESH_RENDERER.mainMenu(0, 0, width, height, Config.get() == null ? MainMenuShaders.Kevin.type : Config.get().titleScreenShaderType.get().type);
        Managers.MESH_RENDERER.render();

        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        this.logoDrawer.draw(context, this.width, g);
        int i = MathHelper.ceil(g * 255.0F) << 24;
        if ((i & -67108864) != 0) {
            if (this.deprecationNotice != null) {
                this.deprecationNotice.render(context, i);
            }

            if (this.splashText != null) {
                this.splashText.render(context, this.width, this.textRenderer, i);
            }

            String string = LemonClient.NAME + " " + LemonClient.VERSION;
            if (this.client.isDemo()) {
                string = string + " Demo";
            } else {
                string = string + ("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType());
            }

            if (MinecraftClient.getModStatus().isModded()) {
                string = string + I18n.translate("menu.modded");
            }

            context.drawTextWithShadow(this.textRenderer, string, 2, this.height - 10, 16777215 | i);

            if (Utils.firstTimeTitleScreen) {
                Utils.firstTimeTitleScreen = false;

                Executor.execute(() -> {
                    if (!BaritoneUtils.IS_AVAILABLE) {
                        YesNoPrompt.create()
                            .title("Download Baritone")
                            .message("We see that you have not downloaded Baritone, we hope you will download it.")
                            .message("Do you want to download?")
                            .onYes(() -> Util.getOperatingSystem().open("https://github.com/cabaletta/baritone/releases/download/v1.10.1/baritone-api-fabric-1.10.1.jar"))
                            .onNo(() -> OkPrompt.create()
                                .title("Are you sure?")
                                .message("If you download Baritone, your gaming experience will be better")
                                .id("download-baritone-no")
                                .onOk(this::close)
                                .show())
                            .id("download-baritone")
                            .show();
                    }
                });
            }

            for (Element element : this.children()) {
                if (element instanceof ClickableWidget) {
                    ((ClickableWidget) element).setAlpha(g);
                }
            }

            super.render(context, mouseX, mouseY, delta);
        }

        Utils.unscaledProjection();
        shaderDropdown.preRender(renderer, mouseX, mouseY, delta);
        if (menuBox != null) {
            menuBox.preRender(renderer, mouseX, mouseY, delta);
        }
        Utils.scaledProjection();
    }
}
