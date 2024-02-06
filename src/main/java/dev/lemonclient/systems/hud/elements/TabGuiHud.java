package dev.lemonclient.systems.hud.elements;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.client.KeyEvent;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.hud.Hud;
import dev.lemonclient.systems.hud.HudElement;
import dev.lemonclient.systems.hud.HudElementInfo;
import dev.lemonclient.systems.hud.HudRenderer;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Category;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.utils.misc.input.KeyAction;
import dev.lemonclient.utils.render.Interpolations;
import dev.lemonclient.utils.render.color.Color;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TabGuiHud extends HudElement {
    public static final HudElementInfo<TabGuiHud> INFO = new HudElementInfo<>(Hud.GROUP, "Tab Gui", "Render tabgui", TabGuiHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> textColor = colorSetting(sgGeneral, "text-color", SettingColor.WHITE);
    private final Setting<SettingColor> selectedCategoryTextColor = colorSetting(sgGeneral, "selected-category-text-color", SettingColor.BLUE);

    private final Setting<SettingColor> moduleTextColor = colorSetting(sgGeneral, "module-text-color", SettingColor.GREEN);
    private final Setting<SettingColor> activeModuleTextColor = colorSetting(sgGeneral, "active-module-text-color", SettingColor.RED);
    private final Setting<SettingColor> quadColor = colorSetting(sgGeneral, "quad-color", SettingColor.DARK_GRAY);

    public TabGuiHud() {
        super(INFO);
        LemonClient.EVENT_BUS.subscribe(this);
    }

    private boolean expanded = false;
    private final List<Category> categories = categoryList();

    // Module List render
    public Category m_category;
    public List<Module> m_modules;
    private Module m_currentModule;
    public double m_panelSelectorY1, m_panelSelectorY2, m_panelHighLightY;
    public boolean m_closing = false;
    // tabgui render
    Category t_currentCategory = Categories.Combat;
    public double t_panelSelectorY1, t_panelSelectorY2, t_panelHighLightY;


    private void t_drawTabGui(HudRenderer hudRenderer, double posX, double posY) {
        double panelsOffsetX = posX;
        double panelOffsetY = posY;
        double panelWidth = 4 + hudRenderer.textWidth(longestCategoryName()), panelHeight = 6 + hudRenderer.textHeight();
        double panelSpacing = 2;

        int currentPanelIndex = Modules.getCategoryIndex(categories, t_currentCategory);
        double y1Dest = currentPanelIndex * (panelHeight + panelSpacing) + 3;
        double y2Dest = currentPanelIndex * (panelHeight + panelSpacing) + panelHeight - 3;
        float selectorSpeed = 0.7f;

        if (y2Dest > t_panelSelectorY2) {
            t_panelSelectorY2 = Interpolations.interpBezier(t_panelSelectorY2, y2Dest, selectorSpeed);

            if (t_panelSelectorY2 >= y2Dest - 0.2) {
                t_panelSelectorY1 = Interpolations.interpBezier(t_panelSelectorY1, y1Dest, selectorSpeed);
            }
        } else {
            t_panelSelectorY1 = Interpolations.interpBezier(t_panelSelectorY1, y1Dest, selectorSpeed);


            if (t_panelSelectorY1 <= y1Dest + 0.2) {
                t_panelSelectorY2 = Interpolations.interpBezier(t_panelSelectorY2, y2Dest, selectorSpeed);

            }
        }

        t_panelHighLightY = Interpolations.interpBezier(t_panelHighLightY, currentPanelIndex * (panelHeight + panelSpacing), 0.4);

        hudRenderer.quadRounded(panelsOffsetX - 4, panelOffsetY - 4, panelWidth + 8, (panelHeight + panelSpacing) * categories.size() - panelSpacing + 8, 2, new Color(0, 0, 0, 50));
        hudRenderer.quadRounded(panelsOffsetX, panelOffsetY + t_panelHighLightY, panelWidth, panelHeight, 2, new Color(255, 255, 255, 20));
        hudRenderer.quadRounded(panelsOffsetX, panelOffsetY + t_panelSelectorY1, 2, t_panelSelectorY2 - t_panelSelectorY1, 1, new Color(0xff0090ff));

        for (Category panel : categories) {
            hudRenderer.text(panel.name, panelsOffsetX + 3, panelOffsetY + panelHeight * 0.5 - hudRenderer.textHeight() * 0.5, Color.WHITE, false);
            panelOffsetY += panelHeight + panelSpacing;
        }

        if (expanded) {
            m_drawModuleList(hudRenderer, panelsOffsetX + panelWidth + 14, posY);
        }

        setSize(panelWidth, panelOffsetY - posY);
    }

    private void t_next() {
        int next = Modules.getCategoryIndex(categories, t_currentCategory) + 1;
        if (next < categories.size()) {
            t_currentCategory = categories.get(next);
        } else {
            t_currentCategory = categories.get(0);
        }
    }

    private void t_previous() {
        int prev = Modules.getCategoryIndex(categories, t_currentCategory) - 1;
        if (prev >= 0) {
            t_currentCategory = categories.get(prev);
        } else {
            t_currentCategory = categories.get(categories.size() - 1);
        }
    }

    public void t_keyTyped(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (!expanded) {
                m_setModuleList(t_currentCategory);
                expanded = true;
                m_closing = false;
                return;
            }
        }

        if (!expanded) {
            if (keyCode == GLFW.GLFW_KEY_DOWN)
                t_next();

            if (keyCode == GLFW.GLFW_KEY_UP)
                t_previous();
        } else {
            m_keyTyped(keyCode);
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT && expanded) {
            m_closing = true;
            expanded = false;
        }
    }

    private void m_drawModuleList(HudRenderer hudRenderer, double posX, double posY) {
        double panelsOffsetX = posX;
        double panelOffsetY = posY;
        double panelWidth = 8 + hudRenderer.textWidth(longestModuleNameInCategory(t_currentCategory)), panelHeight = 6 + hudRenderer.textHeight();
        double panelSpacing = 2;

        int currentPanelIndex = m_modules.indexOf(m_currentModule);
        double y1Dest = currentPanelIndex * (panelHeight + panelSpacing) + 3;
        double y2Dest = currentPanelIndex * (panelHeight + panelSpacing) + panelHeight - 3;
        float selectorSpeed = 0.7f;

        if (y2Dest > m_panelSelectorY2) {
            m_panelSelectorY2 = Interpolations.interpBezier(m_panelSelectorY2, y2Dest, selectorSpeed);

            if (m_panelSelectorY2 >= y2Dest - 0.2) {
                m_panelSelectorY1 = Interpolations.interpBezier(m_panelSelectorY1, y1Dest, selectorSpeed);
            }
        } else {
            m_panelSelectorY1 = Interpolations.interpBezier(m_panelSelectorY1, y1Dest, selectorSpeed);

            if (m_panelSelectorY1 <= y1Dest + 0.2) {
                m_panelSelectorY2 = Interpolations.interpBezier(m_panelSelectorY2, y2Dest, selectorSpeed);
            }
        }

        m_panelHighLightY = Interpolations.interpBezier(m_panelHighLightY, currentPanelIndex * (panelHeight + panelSpacing), 0.4);

        hudRenderer.quadRounded(panelsOffsetX - 4, panelOffsetY - 4, panelWidth + 8, (panelHeight + panelSpacing) * m_modules.size() - panelSpacing + 8, 2, new Color(28, 28, 28));
        hudRenderer.quadRounded(panelsOffsetX, panelOffsetY + m_panelSelectorY1, 2, m_panelSelectorY2 - m_panelSelectorY1, 1, new Color(0xff0090ff));

        for (Module module : this.m_modules) {
            hudRenderer.text(module.title, panelsOffsetX + 3, panelOffsetY + panelHeight * 0.5 - hudRenderer.textHeight() * 0.5, module.isActive() ? Color.WHITE : new Color(255, 255, 255, 120), false);
            panelOffsetY += panelHeight + panelSpacing;
        }
    }

    private void m_setModuleList(Category category) {
        this.m_category = category;
        this.m_modules = Modules.get().getModulesByCategory(category);
        this.m_currentModule = this.m_modules.get(0);
    }

    private void m_next() {
        int next = this.m_modules.indexOf(this.m_currentModule) + 1;
        if (next < this.m_modules.size()) {
            m_currentModule = this.m_modules.get(next);
        } else {
            m_currentModule = this.m_modules.get(0);
        }
    }

    private void m_previous() {
        int prev = this.m_modules.indexOf(this.m_currentModule) - 1;
        if (prev >= 0) {
            m_currentModule = this.m_modules.get(prev);
        } else {
            m_currentModule = this.m_modules.get(this.m_modules.size() - 1);
        }
    }

    public boolean m_keyTyped(int keyCode) {
        if (m_closing || !expanded)
            return false;

        if (keyCode == GLFW.GLFW_KEY_DOWN)
            m_next();

        if (keyCode == GLFW.GLFW_KEY_UP)
            m_previous();

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_RIGHT)
            m_currentModule.toggle();

        return false;
    }

    private List<Category> categoryList() {
        List<Category> categories = new ArrayList<>();
        Modules.loopCategories().forEach(categories::add);
        categories.remove(Categories.Settings);
        return categories;
    }

    private String longestCategoryName() {
        Category longestNameCategory = categories.get(0);

        for (Category category : categories) {
            if (category.name.length() > longestNameCategory.name.length()) {
                longestNameCategory = category;
            }
        }

        return longestNameCategory.name;
    }

    private String longestModuleNameInCategory(Category category) {
        Module longestNameInCategory = Modules.get().getModulesByCategory(category).get(0);

        for (Module module : Modules.get().getModulesByCategory(category)) {
            if (module.title.length() > longestNameInCategory.title.length()) {
                longestNameInCategory = module;
            }
        }

        return longestNameInCategory.title;
    }

    @Override
    public void render(HudRenderer renderer) {
        t_drawTabGui(renderer, this.x, this.y);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (event.action.equals(KeyAction.Press)) {
            t_keyTyped(event.key);
        }
    }
}
