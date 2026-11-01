package ru.turik.font;

import ru.turik.Turik;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A typeface prepared for one pixel size and one weight. Rasterizes glyphs
 * on demand with Java2D (antialiased) and uploads them as GL textures.
 * Handles Latin + Cyrillic from the bundled TTF; caches every glyph.
 */
final class CachedFont {
    private final Font font;
    private final FontMetrics metrics;
    private final int passCount;
    private final Map<Character, Glyph> glyphs = new ConcurrentHashMap<>();

    CachedFont(InputStream ttf, float size, FontType type) throws Exception {
        Font base = Font.createFont(Font.TRUETYPE_FONT, ttf);
        float scaled = size;
        this.font = base.deriveFont(Font.PLAIN, scaled);
        // Metrics must come from a real graphics configuration for subpixel AA.
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = probe.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        this.metrics = g2.getFontMetrics(this.font);
        g2.dispose();
        this.passCount = Math.max(1, type.passes);
    }

    FontMetrics metrics() {
        return metrics;
    }

    int ascent() {
        return metrics.getAscent();
    }

    int height() {
        return metrics.getHeight();
    }

    Glyph glyph(char c) {
        return glyphs.computeIfAbsent(c, this::rasterize);
    }

    private Glyph rasterize(char c) {
        String s = String.valueOf(c);
        int advance = metrics.stringWidth(s);
        int pad = 2 + passCount;
        int w = Math.max(1, advance + pad * 2);
        int h = Math.max(1, metrics.getHeight() + pad * 2);

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setFont(font);
        g.setColor(Color.WHITE);
        int baseX = pad;
        int baseY = pad + metrics.getAscent();
        for (int pass = 0; pass < passCount; pass++) {
            // Multi-pass stamping thickens strokes for Medium/SemiBold/Bold.
            g.drawString(s, baseX + pass * 0.75f, baseY - pass * 0.5f);
        }
        g.dispose();

        int textureId = org.lwjgl.opengl.GL11.glGenTextures();
        com.mojang.blaze3d.platform.GlStateManager._bindTexture(textureId);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER, org.lwjgl.opengl.GL11.GL_LINEAR);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER, org.lwjgl.opengl.GL11.GL_LINEAR);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);

        // AWT is top-down, GL is bottom-up: flip rows while copying.
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(w * h * 4);
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                int argb = image.getRGB(x, y);
                buffer.put((byte) ((argb >> 16) & 0xFF)); // R
                buffer.put((byte) ((argb >> 8) & 0xFF));  // G
                buffer.put((byte) (argb & 0xFF));         // B
                buffer.put((byte) ((argb >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();
        org.lwjgl.opengl.GL11.glPixelStorei(org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT, 1);
        org.lwjgl.opengl.GL11.glTexImage2D(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0,
                org.lwjgl.opengl.GL11.GL_RGBA8, w, h, 0,
                org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, buffer);

        return new Glyph(textureId, w, h, baseX, pad, advance + (passCount - 1) * 0.75f);
    }
}
