package dev.lemonclient.systems.hud.elements;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.HudElementInfo;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.Render3DUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import static dev.lemonclient.LemonClient.mc;

public class ArmorHudPlus extends HudElement {
    public static final HudElementInfo<ArmorHudPlus> INFO = new HudElementInfo<>(Hud.GROUP, "Armor Hud Plus", "A target hud the fuck you thinkin bruv.", ArmorHudPlus::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Scale to render at.")
        .defaultValue(1)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );
    private final Setting<Boolean> showCount = sgGeneral.add(new BoolSetting.Builder()
        .name("Show Count")
        .description("Show the count of the armor.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> rounding = sgGeneral.add(new IntSetting.Builder()
        .name("Rounding")
        .description("How rounded should the background be.")
        .defaultValue(50)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Boolean> bg = sgGeneral.add(new BoolSetting.Builder()
        .name("Background")
        .description("Renders a background behind armor pieces.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SettingColor> bgColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Background Color")
        .description(COLOR)
        .defaultValue(new SettingColor(0, 0, 0, 150))
        .build()
    );
    private final Setting<SettingColor> durColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Durability Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<DurMode> durMode = sgGeneral.add(new EnumSetting.Builder<DurMode>()
        .name("Durability Mode")
        .description("Where should durability be rendered at.")
        .defaultValue(DurMode.Bottom)
        .build()
    );

    public ArmorHudPlus() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player == null) return;

        setSize(100 * scale.get() * 2, 28 * scale.get() * 2);
        MatrixStack stack = new MatrixStack();

        stack.translate(x, y, 0);
        stack.scale((float) (scale.get() * 2), (float) (scale.get() * 2), 1);

        if (bg.get()) {
            Render3DUtils.rounded(stack, rounding.get() * 0.14f, rounding.get() * 0.14f, 100 - rounding.get() * 0.28f, 28 - rounding.get() * 0.28f, rounding.get() * 0.14f, 10, bgColor.get().getPacked());
        }

        MatrixStack drawStack = renderer.drawContext.getMatrices();
        drawStack.push();

        drawStack.translate(x, y, 0);
        drawStack.scale((float) (scale.get() * 2), (float) (scale.get() * 2), 1);

        for (int i = 3; i >= 0; i--) {
            ItemStack itemStack = !showCount.get() ? mc.player.getInventory().armor.get(i) :
                new ItemStack(mc.player.getInventory().armor.get(i).getRegistryEntry(), InvUtils.find(mc.player.getInventory().armor.get(i).getItem()).count()
                );

            renderer.item(itemStack, i * 20 + 12, durMode.get() == DurMode.Top ? 10 : 0, scale.get().floatValue(), true);

            if (itemStack.isEmpty()) continue;

            centeredText(stack,
                String.valueOf(Math.round(100 - (float) itemStack.getDamage() / itemStack.getMaxDamage() * 100f)),
                i * 20 + 20, durMode.get() == DurMode.Top ? 3 : 17, durColor.get().getPacked());
        }
        drawStack.pop();
    }

    private void centeredText(MatrixStack stack, String text, int x, int y, int color) {
        Render3DUtils.text(text, stack, x - mc.textRenderer.getWidth(text) / 2f, y, color);
    }

    public enum DurMode {
        Top, // 3, 10
        Bottom // 0, 17
    }
}
