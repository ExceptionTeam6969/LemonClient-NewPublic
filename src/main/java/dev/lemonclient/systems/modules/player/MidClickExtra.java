package dev.lemonclient.systems.modules.player;

import dev.lemonclient.events.client.MouseButtonEvent;
import dev.lemonclient.events.entity.player.FinishUsingItemEvent;
import dev.lemonclient.events.entity.player.StoppedUsingItemEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.BoolSetting;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.misc.input.KeyAction;
import dev.lemonclient.utils.player.FindItemResult;
import dev.lemonclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class MidClickExtra extends Module {
    public MidClickExtra() {
        super(Categories.Player, "Mid Click Extra", "Lets you use items when you middle click.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    //--------------------General--------------------//
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Which item to use when you middle click.")
        .defaultValue(Mode.Pearl)
        .build()
    );
    private final Setting<Boolean> autoToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Toggle")
        .description("Auto toggle when item not found.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
        .defaultValue(SwitchMode.InvSwitch)
        .build()
    );
    private final Setting<Boolean> noInventory = sgGeneral.add(new BoolSetting.Builder()
        .name("Anti Inventory")
        .description("Not work in inventory.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("notify")
        .description("Notifies you when you do not have the specified item in your hotbar.")
        .defaultValue(true)
        .build()
    );

    private boolean isUsing;

    private enum Type {
        Immediate,
        LongerSingleClick,
        Longer
    }

    public enum Mode {
        Pearl(Items.ENDER_PEARL, Type.Immediate),
        Rocket(Items.FIREWORK_ROCKET, Type.Immediate),

        Rod(Items.FISHING_ROD, Type.LongerSingleClick),

        Bow(Items.BOW, Type.Longer),
        Gap(Items.GOLDEN_APPLE, Type.Longer),
        EGap(Items.ENCHANTED_GOLDEN_APPLE, Type.Longer),
        Chorus(Items.CHORUS_FRUIT, Type.Longer);

        private final Item item;
        private final Type type;

        Mode(Item item, Type type) {
            this.item = item;
            this.type = type;
        }
    }

    public enum SwitchMode {
        Silent,
        PickSilent,
        InvSwitch
    }

    @Override
    public void onDeactivate() {
        stopIfUsing();
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action != KeyAction.Press || event.button != GLFW_MOUSE_BUTTON_MIDDLE) return;
        if (noInventory.get() && mc.currentScreen != null) return;

        FindItemResult result = !switchMode.get().equals(SwitchMode.Silent) ? InvUtils.find(mode.get().item) : InvUtils.findInHotbar(mode.get().item);

        if (!result.found()) {
            if (autoToggle.get()) toggle();
            if (notify.get()) sendDisableMsg("unable to find specified item");
            return;
        }

        switch (switchMode.get()) {
            case Silent -> InvUtils.swap(result.slot(), true);
            case InvSwitch -> InvUtils.invSwitch(result.slot());
            case PickSilent -> InvUtils.pickSwitch(result.slot());
        }

        switch (mode.get().type) {
            case Immediate -> {
                if (mc.interactionManager != null) {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

                    switch (switchMode.get()) {
                        case Silent -> InvUtils.swapBack();
                        case PickSilent -> InvUtils.pickSwapBack();
                        case InvSwitch -> InvUtils.invSwapBack();
                    }
                }
            }
            case LongerSingleClick -> {
                if (mc.interactionManager != null) {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                }
            }
            case Longer -> {
                mc.options.useKey.setPressed(true);
                isUsing = true;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        if (isUsing) {
            boolean pressed = true;

            if (mc.player != null && mc.player.getMainHandStack().getItem() instanceof BowItem) {
                pressed = BowItem.getPullProgress(mc.player.getItemUseTime()) < 1;
            }

            mc.options.useKey.setPressed(pressed);
        }
    }

    @EventHandler
    private void onFinishUsingItem(FinishUsingItemEvent event) {
        stopIfUsing();
    }

    @EventHandler
    private void onStoppedUsingItem(StoppedUsingItemEvent event) {
        stopIfUsing();
    }

    private void stopIfUsing() {
        if (isUsing) {
            mc.options.useKey.setPressed(false);

            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> InvUtils.pickSwapBack();
                case InvSwitch -> InvUtils.invSwapBack();
            }
            isUsing = false;
        }
    }
}
