package dev.lemonclient.systems.modules.chat;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.gui.GuiTheme;
import dev.lemonclient.gui.widgets.WLabel;
import dev.lemonclient.gui.widgets.WWidget;
import dev.lemonclient.gui.widgets.containers.WTable;
import dev.lemonclient.gui.widgets.pressable.WButton;
import dev.lemonclient.settings.*;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.misc.RandomUtils;
import dev.lemonclient.utils.player.ChatUtils;
import dev.lemonclient.utils.timers.MSTimer;
import meteordevelopment.orbit.EventHandler;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class SuperSpammer extends Module {
    public SuperSpammer() {
        super(Categories.Chat, "Super Spammer", "Index line.. copy.. send..");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> minDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Min Delay")
        .min(0)
        .sliderRange(0.0, 60000)
        .defaultValue(3500.0)
        .build()
    );
    private final Setting<Double> maxDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Delay")
        .range(0.0, Double.MAX_VALUE)
        .sliderRange(0.0, Double.MAX_VALUE)
        .defaultValue(4000.0)
        .build()
    );
    private final Setting<String> startStr = sgGeneral.add(new StringSetting.Builder()
        .name("Start Message")
        .defaultValue("[%s%n%s%n]")
        .build()
    );
    private final Setting<String> endStr = sgGeneral.add(new StringSetting.Builder()
        .name("End Message")
        .defaultValue("[%n%s%n%s]")
        .build()
    );
    private final Setting<Boolean> autodisable = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Disable")
        .defaultValue(true)
        .build()
    );

    private boolean isPlaying = false;
    private boolean isPause = false;
    private boolean noTextsFound = true;
    private WLabel status;
    private ArrayList<String> loadedText = new ArrayList<>();
    private int currentIndex = 0;
    private final MSTimer msTimer = new MSTimer();
    private long delay = RandomUtils.randomDelay(minDelay.get().intValue(), maxDelay.get().intValue());

    @EventHandler
    public void onUpdate(TickEvent.Pre event) {
        if (msTimer.hasTimePassed(delay)) {
            if (isPlaying && !isPause) {
                String stStr = startStr.get();
                while (stStr.contains("%n") || stStr.contains("%s")) {
                    if (stStr.contains("%n")) {
                        stStr = stStr.replaceFirst("%n", String.valueOf(RandomUtils.nextInt(0, 9)));
                    }
                    if (stStr.contains("%s")) {
                        stStr = stStr.replaceFirst("%s", RandomUtils.randomString(1));
                    }
                }
                String enStr = endStr.get();
                while (enStr.contains("%n") || enStr.contains("%s")) {
                    if (enStr.contains("%n")) {
                        enStr = enStr.replaceFirst("%n", String.valueOf(RandomUtils.nextInt(0, 9)));
                    }
                    if (enStr.contains("%s")) {
                        enStr = enStr.replaceFirst("%s", RandomUtils.randomString(1));
                    }
                }
                if (!loadedText.isEmpty()) ChatUtils.sendPlayerMsg((stStr + loadedText.get(currentIndex) + enStr));
                if (currentIndex < loadedText.size() - 1) currentIndex += 1;
                else if (autodisable.get()) stop();
                else currentIndex = 0;
            }
            msTimer.reset();
            delay = RandomUtils.randomDelay(minDelay.get().intValue(), maxDelay.get().intValue());
        }
        if (status != null) status.set(getStatus());
    }

    @EventHandler
    public void onRender(Render2DEvent e) {
        if (minDelay.get() > maxDelay.get()) {
            minDelay.set(maxDelay.get());
        }
        if (maxDelay.get() < minDelay.get()) {
            maxDelay.set(minDelay.get());
        }

        noTextsFound = loadedText.isEmpty();

        if (mc.world == null) {
            if (isActive()) toggle();
        }
    }

    private boolean isValidFile(Path file) {
        String extension = FilenameUtils.getExtension(file.toFile().getName());
        return (extension.equals("txt"));
    }

    private String getFileLabel(Path file) {
        return file
            .getFileName()
            .toString()
            .replace(".txt", "");
    }

    public String getStatus() {
        if (!this.isActive()) return "Module disabled.";
        if (loadedText.isEmpty()) return "No text loaded.";
        String playing = "Playing text. " + currentIndex + "/" + loadedText.size();
        if (isPlaying) return playing;
        return "None";
    }

    public String getInfo() {
        String playing = currentIndex + "/" + loadedText.size();
        if (isPlaying) return playing;
        return "None";
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WTable table = theme.table();

        // Label
        status = table.add(theme.label(getStatus())).expandCellX().widget();

        // Pause
        WButton pause = table.add(theme.button(!isPause ? "Pause" : "Resume")).right().widget();
        pause.action = () -> {
            pause();
            pause.set(!isPause ? "Pause" : "Resume");
            status.set(getStatus());
        };

        // Stop
        WButton stop = table.add(theme.button("Stop")).right().widget();
        stop.action = this::stop;

        table.row();

        try {
            File idk = new File(LemonClient.FOLDER, "superspammer");
            if (!idk.exists()) idk.mkdirs();

            Files.list(LemonClient.FOLDER.toPath().resolve("superspammer")).forEach(path -> {
                if (isValidFile(path)) {
                    table.add(theme.label(getFileLabel(path))).expandCellX();
                    WButton load = table.add(theme.button("Play")).right().widget();
                    load.action = () -> {
                        if (!isActive()) toggle();
                        playText(path.toFile());
                        status.set(getStatus());
                    };
                    table.row();
                }
            });
        } catch (IOException e) {
            table.add(theme.label("Missing client-client/superspammer folder.")).expandCellX();
            table.row();
        }

        if (noTextsFound) {
            table.add(theme.label("No texts found.")).expandCellX();
            table.row();
        }

        return table;
    }

    public void pause() {
        isPause = !isPause;
        info((isPause ? "Resume" : "Pause") + " play text");
    }

    private void playText(File file) {
        this.loadedText = readText(file);
        noTextsFound = loadedText.isEmpty();
        info("Loaded text: " + getFileLabel(file.toPath()));
        currentIndex = 0;
        isPause = false;
        isPlaying = true;
    }

    public static ArrayList<String> readText(File inputFile) {
        final ArrayList<String> readContent = new ArrayList<>();
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                readContent.add(str);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readContent;
    }

    @Override
    public String getInfoString() {
        return getInfo();
    }

    public void stop() {
        info("Stopping.");
        isPlaying = false;
        currentIndex = 0;
        if (status != null) status.set(getStatus());
    }
}
