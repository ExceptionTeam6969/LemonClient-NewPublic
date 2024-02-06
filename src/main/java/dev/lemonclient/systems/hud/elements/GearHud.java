package dev.lemonclient.systems.hud.elements;

import dev.lemonclient.settings.*;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.HudElementInfo;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.utils.player.InvUtils;
import dev.lemonclient.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.List;

import static dev.lemonclient.LemonClient.mc;

public class GearHud extends HudElement {
    public static final HudElementInfo<GearHud> INFO = new HudElementInfo<>(Hud.GROUP, "Gear Hud", "Item Hud lol.", GearHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("Items")
        .description("Items to show.")
        .defaultValue(Items.END_CRYSTAL, Items.EXPERIENCE_BOTTLE, Items.OBSIDIAN, Items.TOTEM_OF_UNDYING)
        .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("The scale.")
        .defaultValue(1.5)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description(COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("Shadow")
        .description("Renders a shadow behind the chars.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> experienceInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("Experience Info")
        .description("Displays mend percentage for armor next to experience bottles.")
        .defaultValue(true)
        .build()
    );


    public GearHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(55 * scale.get() * scale.get(), 20 * scale.get() * scale.get() * items.get().size());

        for (int i = 0; i < items.get().size(); i++) {
            int posY = (int) Math.round(y + i * 20 * scale.get() * scale.get());

            MatrixStack drawStack = renderer.drawContext.getMatrices();
            drawStack.push();

            drawStack.translate(x, y, 0);
            drawStack.scale((float) (scale.get() * scale.get()), (float) (scale.get() * scale.get()), 1);

            renderer.drawContext.drawItem(items.get().get(i).getDefaultStack(), x, posY);

            drawStack.pop();

            renderer.text(getText(items.get().get(i).asItem()), x + 25 * scale.get() * scale.get(), posY, color.get(), shadow.get(), scale.get());
        }
    }

    private int amountOf(Item item) {
        return InvUtils.find(itemStack -> itemStack.getItem().equals(item)).count();
    }

    private String getText(Item item) {
        if (item == Items.EXPERIENCE_BOTTLE && armorDur() > 0 && experienceInfo.get()) {
            return amountOf(item) + "  " + Math.round(amountOf(item) * 14 / armorDur() * 100) + "%";
        }

        return String.valueOf(amountOf(item));
    }

    private double armorDur() {
        double rur = 0;
        if (mc.player != null) {
            for (int i = 0; i < 4; i++) {
                rur += mc.player.getInventory().armor.get(i).getMaxDamage();
            }
        }
        return rur;
    }
}
