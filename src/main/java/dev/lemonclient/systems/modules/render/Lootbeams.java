package dev.lemonclient.systems.modules.render;

import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.mixin.IItemEntity;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Box;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lootbeams extends Module {
    public Lootbeams() {
        super(Categories.Render, "Lootbeams", "There is smoke coming out of your ancestral grave.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showWhiteItems = boolSetting(sgGeneral, "Show While Items", "If items with a white (common) item name should get a beam.", true);
    private final Setting<Integer> particleCount = intSetting(sgGeneral, "Particle Count", "How many particles to create per tick.", 1, 0, 10);
    private final Setting<Double> beamHeight = doubleSetting(sgGeneral, "Beam Height", "How tall the loot beam is (in blocks).", 0.8, 0, 10);
    private final Setting<Double> beamOffset = doubleSetting(sgGeneral, "Beam Offset", "How much to offset the beam vertically (in blocks).", 0.2, 0, 5);
    private final Setting<Integer> minimumAge = intSetting(sgGeneral, "Min Age", "How old the item needs to be before it gets a beam (in ticks).", 10, 0, 100);
    private final Setting<Double> beamDistance = doubleSetting(sgGeneral, "Beam Distance", "How close items need to be to get a beam.", 64.0, 0, 500);

    private final HashMap<ItemEntity, Integer> itemsToColors = new HashMap<>();

    private int resetTimer = 0;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (resetTimer++ > 40) {
            itemsToColors.clear();
            resetTimer = 0;
        }

        List<ItemEntity> items = mc.world.getEntitiesByType(EntityType.ITEM, new Box(mc.player.getBlockPos()).expand(beamDistance.get()), entity -> !itemsToColors.containsKey(entity));

        items.forEach(entity -> {
            try {
                if (entity != null) {
                    List<Text> text = entity.getStack().getTooltip(null, TooltipContext.Default.BASIC);

                    TextColor color = getTextColor(text);

                    if (color != null) {
                        itemsToColors.put(entity, color.getRgb());
                    }
                }
            } catch (Throwable ignored) {
            }
        });

        ArrayList<ItemEntity> toRemove = new ArrayList<>();
        itemsToColors.forEach((entity, color) -> {
            if (entity.isRemoved() || !entity.isAlive()) {
                toRemove.add(entity);
                return;
            }

            if (!showWhiteItems.get() && color == 16777215) {
                return;
            }

            if (((IItemEntity) entity).getItemAge() >= minimumAge.get()) {
                if (MinecraftClient.getInstance().player != null) {
                    for (int i = 0; i < particleCount.get(); i++) {
                        mc.world.addParticle(
                            new DustParticleEffect(new Vector3f(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f), 1.0f),
                            true,
                            entity.getX(),
                            entity.getY() + beamOffset.get() + (mc.world.random.nextDouble() * beamHeight.get()),
                            entity.getZ(),
                            0.0,
                            0.0,
                            0.0
                        );
                    }
                }
            }
        });

        toRemove.forEach(itemsToColors::remove);
    }

    private TextColor getTextColor(List<Text> text) {
        TextColor color = text.get(0).getStyle().getColor();

        if (color == null || color.getName().equals("white")) {
            if (!text.get(0).getSiblings().isEmpty()) {
                TextColor tmp = text.get(0).getSiblings().get(0).getStyle().getColor();
                if (tmp != null && !tmp.getName().equals("white")) {
                    color = tmp;
                }
            }
        }
        return color;
    }
}
