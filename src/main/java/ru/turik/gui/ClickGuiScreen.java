package ru.turik.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.turik.Turik;
import ru.turik.font.FontType;
import ru.turik.hud.HudElement;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.*;
import ru.turik.render.Render2D;
import ru.turik.render.RenderEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Turik ClickGUI. Dark graphite panels, cyan accents, the Turik typeface,
 * smooth animations, search, settings (toggles, sliders, modes, colors,
 * keybinds, text), scrolling, scaling, HUD editor and a configs window.
 * Everything is drawn by Turik's own GLSL engine — no vanilla widgets.
 */
public final class ClickGuiScreen extends Screen {
    private static final int PANEL_W = 150;
    private static final int HEADER_H = 24;
    private static final int ROW_H = 24;
    private static final int SETTING_H = 22;

    private enum Modal { NONE, CONFIGS }

    private final RenderEngine engine = RenderEngine.get();
    private final float[] panelScroll = new float[Category.values().length];
    private final boolean[] expanded = new boolean[Category.values().length];
    private Module expandedModule;
    private float expandAnim;
    private Modal modal = Modal.NONE;
    private boolean hudEditor;

    // search
    private boolean searchActive;
    private final StringBuilder search = new StringBuilder();

    // inline text editing (new profile name / string settings)
    private StringSetting editingSetting;
    private final StringBuilder editBuffer = new StringBuilder();
    private String profileDraft = "";
    private boolean editingProfileDraft;

    // color editing popup
    private ColorSetting editingColor;

    // ui scale (extra multiplier on top of Minecraft's GUI scale)
    private float uiScale = 1f;
    private float sliderDragValue = -1;

    private float mx, my;
    private KeybindSetting listeningBind;
    private float animTime;

    public ClickGuiScreen() {
        super(Text.literal("Turik ClickGUI"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ================= RENDER =================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.mx = mouseX;
        this.my = mouseY;
        animTime += delta;

        engine.beginGui(this.client);
        engine.blurCapture(this.client);

        float gw = engine.viewWidth() / engine.guiScale();
        float gh = engine.viewHeight() / engine.guiScale();

        // dim backdrop
        engine.rect(0, 0, gw, gh, 0x66000000);

        if (hudEditor) {
            renderHudEditor(gw, gh);
        } else {
            renderTopBar(gw, gh);
            renderSearchBar(gw);
            if (modal == Modal.CONFIGS) {
                renderConfigsModal(gw, gh);
            } else {
                renderPanels(gw, gh, mouseX, mouseY);
                if (editingColor != null) {
                    renderColorPopup();
                }
            }
        }

        engine.endGui(this.client);
    }

    private void renderTopBar(float gw, float gh) {
        float barW = 320;
        float x = gw - barW - 14;
        float y = 8;
        // configs button
        button(x, y, 80, 22, "Configs", modal == Modal.CONFIGS);
        // HUD editor toggle
        button(x + 86, y, 110, 22, "HUD Editor", hudEditor);
        // scale slider (0.6..1.6)
        float sx = x + 202;
        Render2D.text("Scale", sx - 26, y + 7, 8, FontType.MEDIUM, 0xFF9FC8E8);
        slider(sx + 2, y + 8, 60, uiScale, 0.6f, 1.6f, v -> {
            uiScale = (float) v;
            RenderEngine.uiScaleExtra = uiScale;
        });
    }

    private void renderSearchBar(float gw) {
        float w = 220;
        float x = gw / 2f - w / 2f;
        float y = 8;
        String query = search.toString();
        String display = searchActive || !query.isEmpty() ? query + (searchActive ? "_" : "") : "Поиск функций...";
        Render2D.roundedRect(x, y, w, 22, 6f, 0xD2101216);
        Render2D.roundedOutline(x, y, w, 22, 6f, 1f, searchActive ? 0xFF2BD9FF : 0x26FFFFFF);
        Render2D.text(display, x + 8, y + 6, 9, FontType.MEDIUM,
                query.isEmpty() && !searchActive ? 0xFF6B7680 : 0xFFE8F6FF);
    }

    private void renderPanels(float gw, float gh, int mouseX, int mouseY) {
        Category[] cats = Category.values();
        float gap = 8;
        float totalW = cats.length * PANEL_W + (cats.length - 1) * gap;
        float startX = Math.max(10, gw / 2f - totalW / 2f);
        float panelTop = 40;
        float maxH = gh - panelTop - 10;

        String query = search.toString().toLowerCase(Locale.ROOT);

        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            if (!query.isEmpty() && i > 0) {
                continue; // search results render in the first panel only
            }
            List<Module> modules = query.isEmpty()
                    ? Turik.get().modules().byCategory(cat)
                    : Turik.get().modules().search(query);
            if (query.isEmpty() && modules.isEmpty()) {
                continue;
            }
            float px = startX + i * (PANEL_W + gap);
            float contentH = HEADER_H + modules.size() * ROW_H;
            float h = Math.min(maxH, Math.max(contentH, HEADER_H + 40));

            Render2D.shadow(px, panelTop, PANEL_W, h, 8f, 8f, 0x90000000);
            Render2D.roundedRect(px, panelTop, PANEL_W, h, 8f, 0xE2101216);
            Render2D.roundedOutline(px, panelTop, PANEL_W, h, 8f, 1f, 0x1FFFFFFF);
            // header
            Render2D.gradientH(px, panelTop, PANEL_W, HEADER_H, 0xFF12A0C8, 0xFF0F2A38);
            Render2D.text(query.isEmpty() ? cat.display() : "Поиск: " + query,
                    px + 10, panelTop + 6, 10, FontType.SEMIBOLD, 0xFFE8FBFF);

            float listTop = panelTop + HEADER_H;
            float listH = h - HEADER_H;
            engine.pushScissor(px, listTop, PANEL_W, listH);
            float contentTop = listTop - panelScroll[i];
            float rowY = contentTop;
            for (Module module : modules) {
                if (rowY > listTop + listH + 4) break;
                if (rowY + ROW_H >= listTop - 4) {
                    renderModuleRow(px, rowY, module, mouseX, mouseY);
                }
                rowY += ROW_H;
                if (module == expandedModule && expanded[i]) {
                    float sh = settingsHeight(module);
                    float setY = rowY;
                    for (Setting setting : module.settings()) {
                        if (!setting.isVisible()) continue;
                        renderSetting(px + 6, setY, module, setting, mouseX, mouseY);
                        setY += rowHeight(setting);
                    }
                    rowY = setY;
                }
            }
            engine.popScissor();
            // scrollbar
            if (contentH > listH) {
                float barH = Math.max(24, listH * listH / contentH);
                float barY = listTop + (panelScroll[i] / Math.max(1, contentH - listH)) * (listH - barH);
                Render2D.roundedRect(px + PANEL_W - 4, barY, 2.5f, barH, 1f, 0x502BD9FF);
            }
        }
    }

    private void renderModuleRow(float px, float y, Module module, int mouseX, int mouseY) {
        boolean hover = mouseX >= px && mouseX <= px + PANEL_W && mouseY >= y && mouseY <= y + ROW_H;
        int bg = module.isEnabled() ? 0xFF142C38 : hover ? 0xFF181F26 : 0x00101216;
        Render2D.rect(px + 6, y + 1, PANEL_W - 12, ROW_H - 2, bg);
        if (module.isEnabled()) {
            Render2D.rect(px + 6, y + 1, 2, ROW_H - 2, 0xFF2BD9FF);
        }
        Render2D.text(module.name(), px + 12, y + 6, 9.5f, FontType.MEDIUM,
                module.isEnabled() ? 0xFF7DE8FF : 0xFFD5DEE6);
        if (hover) {
            Render2D.text("⋯", px + PANEL_W - 16, y + 6, 9.5f, FontType.MEDIUM, 0xFF6B7680);
        }
        if (module.bind() > 0) {
            String key = GLFW.glfwGetKeyName(module.bind(), 0);
            String label = key == null ? "[" + module.bind() + "]" : "[" + key.toUpperCase(Locale.ROOT) + "]";
            Render2D.text(label, px + PANEL_W - 14 - Render2D.textWidth(label, 7.5f, FontType.REGULAR),
                    y + 8, 7.5f, FontType.REGULAR, 0xFF6B7680);
        }
    }

    private float settingsHeight(Module module) {
        float h = 4;
        for (Setting setting : module.settings()) {
            if (setting.isVisible()) {
                h += rowHeight(setting);
            }
        }
        return h + 2;
    }

    private float rowHeight(Setting setting) {
        if (setting instanceof NumberSetting) {
            return SETTING_H + 10; // slider track below label
        }
        return SETTING_H;
    }

    private void renderSetting(float x, float y, Module module, Setting setting, int mouseX, int mouseY) {
        if (setting instanceof BooleanSetting b) {
            Render2D.text(setting.name(), x + 4, y + 5, 8.5f, FontType.REGULAR, 0xFFB8C6D2);
            boolean on = b.get();
            float tx = x + PANEL_W - 34;
            Render2D.roundedRect(tx, y + 6, 24, 10, 5f, on ? 0xFF1782A8 : 0xFF232B33);
            float knob = on ? tx + 14 : tx + 2;
            Render2D.circle(knob + 4, y + 11, 4, on ? 0xFF2BD9FF : 0xFF6B7680);
        } else if (setting instanceof NumberSetting n) {
            Render2D.text(setting.name(), x + 4, y + 1, 8.5f, FontType.REGULAR, 0xFFB8C6D2);
            Render2D.text(n.display(), x + PANEL_W - 14 - Render2D.textWidth(n.display(), 8.5f, FontType.MEDIUM),
                    y + 1, 8.5f, FontType.MEDIUM, 0xFF7DE8FF);
            slider(x + 6, y + SETTING_H + 2, PANEL_W - 22, n.get(), n.min(), n.max(), n::set);
        } else if (setting instanceof ModeSetting m) {
            Render2D.text(setting.name(), x + 4, y + 5, 8.5f, FontType.REGULAR, 0xFFB8C6D2);
            String value = m.get();
            float vx = x + PANEL_W - 14 - Render2D.textWidth(value, 8.5f, FontType.MEDIUM);
            Render2D.text("<", vx - 12, y + 5, 8.5f, FontType.MEDIUM, 0xFF6B7680);
            Render2D.text(value, vx, y + 5, 8.5f, FontType.MEDIUM, 0xFF7DE8FF);
            Render2D.text(">", x + PANEL_W - 10, y + 5, 8.5f, FontType.MEDIUM, 0xFF6B7680);
        } else if (setting instanceof ColorSetting c) {
            Render2D.text(setting.name(), x + 4, y + 5, 8.5f, FontType.REGULAR, 0xFFB8C6D2);
            float swx = x + PANEL_W - 26;
            Render2D.roundedRect(swx, y + 4, 18, 12, 3f, c.get());
            Render2D.roundedOutline(swx, y + 4, 18, 12, 3f, 1f, 0x40FFFFFF);
            Render2D.text("…", swx - 12, y + 5, 8.5f, FontType.MEDIUM, 0xFF6B7680);
        } else if (setting instanceof KeybindSetting k) {
            Render2D.text(setting.name(), x + 4, y + 5, 8.5f, FontType.REGULAR, 0xFFB8C6D2);
            String label = k.isListening() ? "[...]" : k.get() <= 0 ? "[нет]"
                    : "[" + (GLFW.glfwGetKeyName(k.get(), 0) == null ? "key" + k.get()
                            : GLFW.glfwGetKeyName(k.get(), 0).toUpperCase(Locale.ROOT)) + "]";
            float lx = x + PANEL_W - 14 - Render2D.textWidth(label, 8.5f, FontType.MEDIUM);
            Render2D.text(label, lx, y + 5, 8.5f, FontType.MEDIUM,
                    k.isListening() ? 0xFF2BD9FF : 0xFF7DE8FF);
        } else if (setting instanceof StringSetting s) {
            boolean editing = editingSetting == s;
            String text = editing ? editBuffer + "_" : s.get();
            if (text.isEmpty()) {
                text = s.name();
            }
            Render2D.text(setting.name(), x + 4, y + 2, 7.5f, FontType.REGULAR, 0xFF8A97A3);
            Render2D.text(text, x + 4, y + 11, 8.5f, FontType.MEDIUM,
                    editing ? 0xFF2BD9FF : 0xFFD5DEE6);
        }
    }

    private void slider(float x, float y, float w, double value, double min, double max,
                        java.util.function.DoubleConsumer setter) {
        float t = (float) ((value - min) / (max - min));
        t = Math.max(0f, Math.min(1f, t));
        Render2D.roundedRect(x, y, w, 4, 2f, 0xFF232B33);
        Render2D.roundedRect(x, y, w * t, 4, 2f, 0xFF2BD9FF);
        Render2D.circle(x + w * t, y + 2, 5, 0xFF7DE8FF);
        if (sliderDragValue >= 0) {
            setter.accept(min + Math.max(0, Math.min(1, (mx - x) / w)) * (max - min));
        }
    }

    private void button(float x, float y, float w, float h, String label, boolean active) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        Render2D.roundedRect(x, y, w, h, 6f, active ? 0xFF1782A8 : hover ? 0xFF1A2530 : 0xE2101216);
        Render2D.roundedOutline(x, y, w, h, 6f, 1f, active ? 0xFF2BD9FF : 0x26FFFFFF);
        Render2D.text(label, x + w / 2f - Render2D.textWidth(label, 9, FontType.MEDIUM) / 2f,
                y + h / 2f - 5, 9, FontType.MEDIUM, active ? 0xFFE8FBFF : 0xFFB8C6D2);
    }

    private void renderColorPopup() {
        float w = 150, h = 120;
        float x = Math.min(mx + 12, engine.viewWidth() / engine.guiScale() - w - 8);
        float y = Math.min(my + 6, engine.viewHeight() / engine.guiScale() - h - 8);
        Render2D.shadow(x, y, w, h, 8f, 10f, 0x90000000);
        Render2D.roundedRect(x, y, w, h, 8f, 0xF0101216);
        Render2D.text("Цвет", x + 10, y + 6, 9, FontType.SEMIBOLD, 0xFFE8FBFF);

        ColorSetting c = editingColor;
        String[] labels = {"R", "G", "B", "A"};
        float[] values = {c.red(), c.green(), c.blue(), c.alpha()};
        float yy = y + 26;
        for (int i = 0; i < 4; i++) {
            Render2D.text(labels[i], x + 10, yy - 2, 8.5f, FontType.MEDIUM, 0xFFB8C6D2);
            float v = values[i];
            int channel = i;
            slider(x + 28, yy + 2, w - 40, v, 0, 255, value -> {
                int argb = c.get();
                switch (channel) {
                    case 0 -> argb = (argb & 0xFF00FFFF) | ((int) value << 16);
                    case 1 -> argb = (argb & 0xFFFF00FF) | ((int) value << 8);
                    case 2 -> argb = (argb & 0xFFFFFF00) | (int) value;
                    case 3 -> argb = (argb & 0x00FFFFFF) | ((int) value << 24);
                }
                c.set(argb);
            });
            yy += 22;
        }
    }

    private void renderConfigsModal(float gw, float gh) {
        float w = 260, h = 260;
        float x = gw / 2f - w / 2f;
        float y = gh / 2f - h / 2f;
        Render2D.shadow(x, y, w, h, 10f, 14f, 0xB0000000);
        Render2D.roundedRect(x, y, w, h, 10f, 0xF0101216);
        Render2D.roundedOutline(x, y, w, h, 10f, 1f, 0x30FFFFFF);
        Render2D.text("Конфигурации", x + 12, y + 8, 11, FontType.SEMIBOLD, 0xFFE8FBFF);
        Render2D.text("ESC — закрыть", gw / 2f, y + h - 16, 8, FontType.REGULAR, 0xFF6B7680);

        float listY = y + 30;
        List<String> profiles = Turik.get().configs().profiles();
        for (String profile : profiles) {
            if (listY > y + h - 46) break;
            Render2D.roundedRect(x + 10, listY, w - 20, 20, 4f, 0xFF181F26);
            Render2D.text(profile, x + 16, listY + 6, 9, FontType.MEDIUM, 0xFFD5DEE6);
            Render2D.text("load", x + w - 86, listY + 6, 9, FontType.MEDIUM, 0xFF7DE8FF);
            Render2D.text("del", x + w - 46, listY + 6, 9, FontType.MEDIUM, 0xFFFF6B6B);
            listY += 24;
        }
        // create profile row
        float fy = y + h - 56;
        Render2D.roundedRect(x + 10, fy, w - 20, 20, 4f, 0xFF181F26);
        Render2D.text(editingProfileDraft ? profileDraft + "_" : "новый профиль...",
                x + 16, fy + 6, 9, FontType.MEDIUM,
                editingProfileDraft ? 0xFF2BD9FF : 0xFF6B7680);
        button(x + 10, fy + 24, w - 20, 20, "Save current as new profile", false);
    }

    private void renderHudEditor(float gw, float gh) {
        for (HudElement element : Turik.get().hud().elements()) {
            if (element.visible()) {
                element.render();
                boolean hover = element.mouseOver(mx, my);
                engine.roundedOutline(element.x() - 2, element.y() - 2,
                        element.width() + 4, element.height() + 4, 5f, 1f,
                        hover ? 0xFF2BD9FF : 0x602BD9FF);
            }
        }
        String hint = "HUD Editor: тяните элементы мышью · ESC для выхода";
        Render2D.text(hint, gw / 2f - Render2D.textWidth(hint, 9, FontType.MEDIUM) / 2f,
                gh - 24, 9, FontType.MEDIUM, 0xFF9FC8E8);
    }

    // ================= INPUT =================

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        mx = (float) mouseX;
        my = (float) mouseY;

        // keybind capture takes priority
        if (listeningBind != null) {
            return true;
        }

        if (hudEditor) {
            Turik.get().hud().editorClick(mx, my);
            return true;
        }

        // top bar
        if (button == 0 && hitTopBar()) {
            return true;
        }

        if (modal == Modal.CONFIGS) {
            if (handleConfigsClick(button)) {
                return true;
            }
            return true;
        }

        // search bar
        float gw = engine.viewWidth() / engine.guiScale();
        float sbx = gw / 2f - 110;
        if (button == 0 && my >= 8 && my <= 30 && mx >= sbx && mx <= sbx + 220) {
            searchActive = true;
            return true;
        }
        searchActive = false;

        if (button == 0 || button == 1) {
            if (handlePanelClick(button)) {
                return true;
            }
        }
        return true; // consume everything
    }

    private boolean hitTopBar() {
        float gw = engine.viewWidth() / engine.guiScale();
        float x = gw - 334;
        if (my >= 8 && my <= 30) {
            if (mx >= x && mx <= x + 80) { // configs
                modal = modal == Modal.CONFIGS ? Modal.NONE : Modal.CONFIGS;
                editingColor = null;
                return true;
            }
            if (mx >= x + 86 && mx <= x + 196) { // hud editor
                hudEditor = !hudEditor;
                modal = Modal.NONE;
                return true;
            }
        }
        return false;
    }

    private boolean handlePanelClick(int button) {
        Category[] cats = Category.values();
        float gap = 8;
        float totalW = cats.length * PANEL_W + (cats.length - 1) * gap;
        float startX = Math.max(10, engine.viewWidth() / engine.guiScale() / 2f - totalW / 2f);
        float panelTop = 40;

        for (int i = 0; i < cats.length; i++) {
            float px = startX + i * (PANEL_W + gap);
            if (mx < px || mx > px + PANEL_W) {
                continue;
            }
            List<Module> modules = modulesOf(cats[i]);
            float listTop = panelTop + HEADER_H;
            float rowY = listTop - panelScroll[i];
            int index = 0;
            for (Module module : modules) {
                float rowBottom = rowY + ROW_H;
                if (my >= rowY && my < rowBottom && my >= listTop) {
                    if (button == 0) {
                        module.toggle();
                        Turik.get().configs().requestSave();
                    } else {
                        expanded[i] = true;
                        expandedModule = expandedModule == module ? null : module;
                        editingColor = null;
                    }
                    return true;
                }
                rowY += ROW_H;
                if (module == expandedModule && expanded[i]) {
                    float setY = rowY;
                    for (Setting setting : module.settings()) {
                        if (!setting.isVisible()) continue;
                        if (my >= setY && my < setY + rowHeight(setting)) {
                            handleSettingClick(px, setY, setting, button);
                            return true;
                        }
                        setY += rowHeight(setting);
                    }
                    rowY = setY;
                }
                index++;
            }
            // header click = collapse panel
            if (my >= panelTop && my < panelTop + HEADER_H && button == 0) {
                expanded[i] = !expanded[i];
                return true;
            }
        }
        return false;
    }

    private List<Module> modulesOf(Category cat) {
        String query = search.toString().toLowerCase(Locale.ROOT);
        return query.isEmpty() ? Turik.get().modules().byCategory(cat)
                : Turik.get().modules().search(query);
    }

    private void handleSettingClick(float px, float y, Setting setting, int button) {
        if (button == 0 && setting instanceof BooleanSetting b) {
            b.toggle();
            Turik.get().configs().requestSave();
        } else if (button == 0 && setting instanceof ModeSetting m) {
            m.cycle(1);
            Turik.get().configs().requestSave();
        } else if (button == 0 && setting instanceof ColorSetting c) {
            editingColor = editingColor == c ? null : c;
        } else if (button == 0 && setting instanceof KeybindSetting k) {
            listeningBind = k;
            k.startListening();
        } else if (button == 0 && setting instanceof StringSetting s) {
            editingSetting = s;
            editBuffer.setLength(0);
            editBuffer.append(s.get());
        } else if (setting instanceof NumberSetting n) {
            sliderDragValue = button; // start drag on either button
        }
    }

    private boolean handleConfigsClick(int button) {
        float gw = engine.viewWidth() / engine.guiScale();
        float gh = engine.viewHeight() / engine.guiScale();
        float w = 260, h = 260;
        float x = gw / 2f - w / 2f;
        float y = gh / 2f - h / 2f;
        float listY = y + 30;
        List<String> profiles = Turik.get().configs().profiles();
        for (String profile : profiles) {
            if (listY > y + h - 46) break;
            if (my >= listY && my < listY + 20) {
                if (mx >= x + w - 92 && mx <= x + w - 52) {
                    Turik.get().configs().loadProfile(profile);
                    Turik.get().notifications().info("Configs", "Загружен: " + profile);
                } else if (mx >= x + w - 52 && mx <= x + w - 16) {
                    Turik.get().configs().deleteProfile(profile);
                    Turik.get().notifications().info("Configs", "Удалён: " + profile);
                }
                return true;
            }
            listY += 24;
        }
        float fy = y + h - 56;
        if (my >= fy && my < fy + 20) {
            editingProfileDraft = true;
            return true;
        }
        if (my >= fy + 24 && my < fy + 44) {
            if (profileDraft.isBlank()) {
                Turik.get().notifications().warn("Configs", "Введите имя профиля");
            } else {
                Turik.get().configs().saveActive();
                Turik.get().configs().createProfile(profileDraft);
                Turik.get().notifications().info("Configs", "Профиль создан: " + profileDraft);
                profileDraft = "";
                editingProfileDraft = false;
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        mx = (float) click.x();
        my = (float) click.y();
        sliderDragValue = -1;
        Turik.get().hud().editorRelease();
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        mx = (float) click.x();
        my = (float) click.y();
        if (hudEditor) {
            Turik.get().hud().editorDrag(mx, my);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (modal == Modal.NONE && !hudEditor) {
            Category[] cats = Category.values();
            float gap = 8;
            float totalW = cats.length * PANEL_W + (cats.length - 1) * gap;
            float startX = Math.max(10, engine.viewWidth() / engine.guiScale() / 2f - totalW / 2f);
            for (int i = 0; i < cats.length; i++) {
                float px = startX + i * (PANEL_W + gap);
                if (mx >= px && mx <= px + PANEL_W) {
                    List<Module> modules = modulesOf(cats[i]);
                    float contentH = HEADER_H + modules.size() * ROW_H;
                    float listH = engine.viewHeight() / engine.guiScale() - 50;
                    if (contentH > listH) {
                        panelScroll[i] = Math.max(0, Math.min(contentH - listH,
                                panelScroll[i] - (float) verticalAmount * 24));
                    }
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        // 1) keybind capture
        if (listeningBind != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningBind.set(-1);
            } else {
                listeningBind.set(keyCode);
            }
            Turik.get().configs().requestSave();
            listeningBind = null;
            return true;
        }
        // 2) inline string edit
        if (editingSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                editingSetting.set(editBuffer.toString());
                editingSetting = null;
                Turik.get().configs().requestSave();
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                editingSetting = null;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !editBuffer.isEmpty()) {
                editBuffer.deleteCharAt(editBuffer.length() - 1);
            }
            return true;
        }
        // 3) profile draft
        if (editingProfileDraft) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                editingProfileDraft = false;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !profileDraft.isEmpty()) {
                profileDraft = profileDraft.substring(0, profileDraft.length() - 1);
            }
            return true;
        }
        // 4) search typing
        if (searchActive) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchActive = false;
                search.setLength(0);
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search.deleteCharAt(search.length() - 1);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Turik.get().mc().setScreen(null);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        String chars = input.asString();
        if (!input.isValidChar() || chars.isEmpty()) {
            return false;
        }
        if (editingSetting != null) {
            editBuffer.append(chars);
            return true;
        }
        if (editingProfileDraft) {
            profileDraft += chars;
            return true;
        }
        if (searchActive) {
            search.append(chars);
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void removed() {
        Turik.get().configs().saveActive();
        super.removed();
    }

    /** Applied inside the render engine so panels/text scale uniformly. */
    public float uiScale() {
        return uiScale;
    }
}