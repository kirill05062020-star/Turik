package ru.turik.hud;

import ru.turik.Turik;
import ru.turik.font.FontType;
import ru.turik.render.Render2D;

/** Active-modules list: every enabled module with its bind. */
public class ActiveModulesElement extends HudElement {
    public ActiveModulesElement() {
        super(8, 90);
    }

    @Override
    public boolean visible() {
        return !Turik.get().modules().enabled().isEmpty();
    }

    @Override
    public void render() {
        var enabled = Turik.get().modules().enabled();
        if (enabled.isEmpty()) {
            return;
        }
        float size = 9;
        float lineH = Render2D.textHeight(size) + 3;
        float w = 0;
        for (var m : enabled) {
            String text = m.name() + (m.bind() > 0 ? " [" + org.lwjgl.glfw.GLFW.glfwGetKeyName(m.bind()) + "]" : "");
            w = Math.max(w, Render2D.textWidth(text, size, FontType.MEDIUM));
        }
        width = w + 16;
        height = lineH * enabled.size() + 8;
        Render2D.roundedRect(x, y, width, height, 5f, 0xB2101216);
        float yy = y + 4;
        for (var m : enabled) {
            String text = m.name() + (m.bind() > 0 ? " [" + String.valueOf(org.lwjgl.glfw.GLFW.glfwGetKeyName(m.bind())) + "]" : "");
            Render2D.rect(x + 4, yy + 2, 2, lineH - 6, 0xFF2BD9FF);
            Render2D.text(text, x + 9, yy, size, FontType.MEDIUM, 0xFFE8F6FF);
            yy += lineH;
        }
    }
}