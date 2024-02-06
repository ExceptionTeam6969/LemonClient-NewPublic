package dev.lemonclient.gui;

import dev.lemonclient.LemonClient;
import dev.lemonclient.gui.themes.bozetheme.BozeGuiTheme;
import dev.lemonclient.gui.themes.cliontheme.CLionGuiTheme;
import dev.lemonclient.gui.themes.defaulttheme.LCGuiTheme;
import dev.lemonclient.gui.themes.mercurytheme.MercuryGuiTheme;
import dev.lemonclient.utils.PostInit;
import dev.lemonclient.utils.PreInit;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiThemes {
    private static final File FOLDER = new File(LemonClient.FOLDER, "gui");
    private static final File THEMES_FOLDER = new File(FOLDER, "themes");
    private static final File FILE = new File(FOLDER, "gui.nbt");

    private static final List<GuiTheme> themes = new ArrayList<>();
    private static GuiTheme theme;

    @PreInit
    public static void init() {
        add(new LCGuiTheme());
        add(new BozeGuiTheme());
        add(new CLionGuiTheme());
        add(new MercuryGuiTheme());
    }

    @PostInit
    public static void postInit() {
        if (FILE.exists()) {
            try {
                NbtCompound tag = NbtIo.read(FILE);

                if (tag != null) select(tag.getString("currentTheme"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (theme == null) select("Default");
    }

    public static void add(GuiTheme theme) {
        for (Iterator<GuiTheme> it = themes.iterator(); it.hasNext(); ) {
            if (it.next().name.equals(theme.name)) {
                it.remove();

                LemonClient.LOG.error("Theme with the name '{}' has already been added.", theme.name);
                break;
            }
        }

        themes.add(theme);
    }

    public static void select(String name) {
        // Find theme with the provided name
        GuiTheme theme = null;

        for (GuiTheme t : themes) {
            if (t.name.equals(name)) {
                theme = t;
                break;
            }
        }

        if (theme != null) {
            // Save current theme
            saveTheme();

            // Select new theme
            GuiThemes.theme = theme;

            // Load new theme
            try {
                File file = new File(THEMES_FOLDER, get().name + ".nbt");

                if (file.exists()) {
                    NbtCompound tag = NbtIo.read(file);
                    if (tag != null) get().fromTag(tag);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Save global gui settings with the new theme
            saveGlobal();
        }
    }

    public static GuiTheme get() {
        return theme;
    }

    public static String[] getNames() {
        String[] names = new String[themes.size()];

        for (int i = 0; i < themes.size(); i++) {
            names[i] = themes.get(i).name;
        }

        return names;
    }

    // Saving

    private static void saveTheme() {
        if (get() != null) {
            try {
                NbtCompound tag = get().toTag();

                THEMES_FOLDER.mkdirs();
                NbtIo.write(tag, new File(THEMES_FOLDER, get().name + ".nbt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveGlobal() {
        try {
            NbtCompound tag = new NbtCompound();
            tag.putString("currentTheme", get().name);

            FOLDER.mkdirs();
            NbtIo.write(tag, FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @UltraLock
    public static void save() {
        saveTheme();
        saveGlobal();
    }
}
