package ru.turik.font;

import ru.turik.Turik;
import ru.turik.render.RenderEngine;

/**
 * Turik's own font renderer. Rasterizes the bundled TTF via Java2D with
 * antialiasing, uploads glyph textures with its own GL engine and draws
 * quads through the RenderEngine — never touches Minecraft's font stack.
 */
public final class FontRenderer {
    private FontRenderer() {
    }

    private static final char[] WHITESPACE_ONLY = {' '};

    /** Draws text with top-left origin; returns total width in GUI px. */
    public static float drawText(String text, float x, float y, float size, FontType type, int color, boolean shadow) {
        if (text == null || text.isEmpty()) {
            return 0f;
        }
        RenderEngine engine = RenderEngine.get();
        float factor = Math.max(1f, engine.guiScale());
        float cursorX = x;
        float cursorY = y;
        float lineHeight = height(size) * 1.25f;

        if (shadow) {
            int shadowColor = ((color & 0x00FFFFFF) == 0 ? color : ((color & 0x00FFFFFF) | 0x99000000)) & 0xFF000000 | (color & 0x00FFFFFF);
            drawLine(text, cursorX + 1f, cursorY + 1f, size, type,
                    (0x90 << 24) | (color & 0x00FFFFFF), factor, engine);
        }
        drawLine(text, cursorX, cursorY, size, type, color, factor, engine);

        // Report the widest line.
        float width = 0;
        for (String line : text.split("\n", -1)) {
            width = Math.max(width, width(line, size, type));
        }
        return width;
    }

    private static void drawLine(String text, float x, float y, float size, FontType type,
                                 int color, float factor, RenderEngine engine) {
        String[] lines = text.split("\n", -1);
        float lineHeight = height(size) * 1.25f;
        for (int li = 0; li < lines.length; li++) {
            float cursor = x;
            float top = y + li * lineHeight;
            CachedFont cf = Turik.get().fonts().font(size * factor, type);
            for (char c : lines[li].toCharArray()) {
                if (c == ' ') {
                    cursor += cf.glyph(' ').advance / factor;
                    continue;
                }
                Glyph g = cf.glyph(c);
                engine.drawTextured(g.textureId,
                        cursor - g.bearingX / factor,
                        top - g.bearingY / factor,
                        g.width / factor,
                        g.height / factor,
                        0f, 0f, 1f, 1f,
                        color, true);
                cursor += g.advance / factor;
            }
        }
    }

    public static float width(String text, float size, FontType type) {
        if (text == null || text.isEmpty()) {
            return 0f;
        }
        float factor = Math.max(1f, RenderEngine.get().guiScale());
        CachedFont cf = Turik.get().fonts().font(size * factor, type);
        float max = 0;
        for (String line : text.split("\n", -1)) {
            float w = 0;
            for (char c : line.toCharArray()) {
                w += cf.glyph(c).advance / factor;
            }
            max = Math.max(max, w);
        }
        return max;
    }

    public static float height(float size) {
        float factor = Math.max(1f, RenderEngine.get().guiScale());
        return Turik.get().fonts().font(size * factor, FontType.REGULAR).height() / factor;
    }
}
