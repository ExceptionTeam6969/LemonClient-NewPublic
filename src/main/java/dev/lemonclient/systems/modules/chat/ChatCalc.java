package dev.lemonclient.systems.modules.chat;

import dev.lemonclient.events.game.ReceiveMessageEvent;
import dev.lemonclient.settings.EnumSetting;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.settings.StringSetting;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

public class ChatCalc extends Module {
    public ChatCalc() {
        super(Categories.Chat, "Chat Calc", "Automatic calculation.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Computing mode.")
        .defaultValue(Mode.Pit)
        .build()
    );

    private final Setting<String> prefixContains = sgGeneral.add(new StringSetting.Builder()
        .name("prefix-contains")
        .defaultValue("速算")
        .visible(() -> mode.get().equals(Mode.Pit))
        .build()
    );

    private final Setting<String> split = sgGeneral.add(new StringSetting.Builder()
        .name("split")
        .description("Split char")
        .defaultValue(":")
        .build()
    );

    private Step currentStage = Step.None;

    @EventHandler
    private void onChat(ReceiveMessageEvent event) {
        try {
            String message = event.getMessage().getString();
            if (split.get().isEmpty()) return;
            if (!message.contains(split.get())) return;
            String[] texts = message.split(split.get());
            String precede = texts[0];
            if (mode.get().equals(Mode.Pit) && !precede.contains(prefixContains.get())) return;
            boolean hasEmptyChar = texts[1].startsWith(" ");
            String receive = hasEmptyChar ? message.replace(precede + split.get() + "  ", "") : message.replace(precede + split.get(), "");
            currentStage = updateStage(receive);
            if (currentStage.equals(Step.None)) return;
            switch (mode.get()) {
                case Pit -> {
                    double out = 0.0;
                    switch (currentStage) {
                        case Subtract -> {
                            String m1 = receive;
                            if (receive.startsWith("-")) {
                                m1 = m1.substring(1);
                            }
                            String[] numbers = m1.split("-");
                            double n1 = Double.parseDouble(receive.startsWith("-") ? "-" + numbers[0] : numbers[0]);
                            double n2 = Double.parseDouble(numbers[1]);
                            out = n1 - n2;
                        }
                        case Add -> {
                            String[] numbers = receive.split("\\+");
                            double n1 = Double.parseDouble(numbers[0]);
                            double n2 = Double.parseDouble(numbers[1]);
                            out = n1 + n2;
                        }
                        case Divide -> {
                            String[] numbers = (receive.contains("/")) ? receive.split("/") : receive.split("÷");
                            double n1 = Double.parseDouble(numbers[0]);
                            double n2 = Double.parseDouble(numbers[1]);
                            out = n1 / n2;
                        }
                        case Multiply -> {
                            String[] numbers = (receive.contains("x")) ? receive.split("x") : receive.split("\\*");
                            double n1 = Double.parseDouble(numbers[0]);
                            double n2 = Double.parseDouble(numbers[1]);
                            out = n1 * n2;
                        }
                        case Pow -> {
                            String[] numbers = receive.split("^");
                            double n1 = Double.parseDouble(numbers[0]);
                            double n2 = Double.parseDouble(numbers[1]);
                            out = Math.pow(n1, n2);
                        }
                    }
                    String outString = String.valueOf(out);
                    String send = outString.endsWith(".0") ? String.valueOf(((int) out)) : outString;
                    ChatUtils.sendPlayerMsg(send);
                }
            }
        } catch (Exception e) {
            event.setMessage(event.getMessage());
        }
    }

    private Step updateStage(String message) {
        Step stage = Step.None;
        if (message.contains("-")) {
            if (message.startsWith("-")) {
                String t = message.substring(1);
                if (t.contains("-")) {
                    stage = Step.Subtract;
                }
            } else {
                stage = Step.Subtract;
            }
        }
        if (message.contains("+")) {
            stage = Step.Add;
        }
        if (message.contains("^")) {
            stage = Step.Pow;
        }
        if (message.contains("x") || message.contains("*")) {
            stage = Step.Multiply;
        }
        if (message.contains("÷") || message.contains("/")) {
            stage = Step.Divide;
        }
        return stage;
    }

    public enum Step {
        Multiply,
        Divide,
        Add,
        Subtract,
        Pow,
        None
    }

    public enum Mode {
        Pit
    }
}
