package ru.turik.render;

import ru.turik.Turik;
import ru.turik.font.FontRenderer;
import ru.turik.font.FontType;

/**
 * Static facade over the render engine + font renderer, used by the
 * ClickGUI, HUD and notification system. Everything here draws with
 * Turik's own GL pipeline and the Turik typeface — never with vanilla.
 */
public final class Render2D {
    private Render2D() {
    }

    private static RenderEngine e() {
        return RenderEngine.get();
    }

    // ---- text ------------------------------------------------------------

    public static float text(String text, float x, float y, float size, FontType type, int color) {
        return FontRenderer.drawText(text, x, y, size, type, color, false);
    }

    public static float textShadow(String text, float x, float y, float size, FontType type, int color) {
        return FontRenderer.drawText(text, x, y, size, type, color, true);
    }

    public static float textCentered(String text, float cx, float y, float size, FontType type, int color) {
        float w = FontRenderer.width(text, size, type);
        return FontRenderer.drawText(text, cx - w / 2f, y, size, type, color, false);
    }

    public static float textWidth(String text, float size, FontType type) {
        return FontRenderer.width(text, size, type);
    }

    public static float textHeight(float size) {
        return FontRenderer.height(size);
    }

    // ---- shapes ----------------------------------------------------------

    public static void panel(float x, float y, float w, float h, int fill, int border) {
        e().panel(x, y, w, h, 6f, fill, border);
    }

    public static void rect(float x, float y, float w, float h, int color) {
        e().rect(x, y, w, h, color);
    }

    public static void roundedRect(float x, float y, float w, float h, float radius, int color) {
        e().roundedRect(x, y, w, h, radius, color);
    }

    public static void roundedOutline(float x, float y, float w, float h, float radius, float thickness, int color) {
        e().roundedOutline(x, y, w, h, radius, thickness, color);
    }

    public static void gradientH(float x, float y, float w, float h, int left, int right) {
        e().gradientH(x, y, w, h, left, right);
    }

    public static void circle(float cx, float cy, float radius, int color) {
        e().circle(cx, cy, radius, color);
    }

    public static void outline(float x, float y, float w, float h, float radius, float thickness, int color) {
        e().roundedOutline(x, y, w, h, radius, thickness, color);
    }

    public static void gradientV(float x, float y, float w, float h, int top, int bottom) {
        e().gradientV(x, y, w, h, top, bottom);
    }

    public static void shadow(float x, float y, float w, float h, float radius, float size, int color) {
        e().shadow(x, y, w, h, radius, size, color);
    }

    public static void blurPanel(float x, float y, float w, float h, float radius, float dim) {
        e().blurPanel(x, y, w, h, radius, dim);
    }

    /** Bright cyan → blue Turik accent used across the UI. */
    public static int accent() {
        return 0xFF2BD9FF;
    }

    public static int accentDim() {
        return 0xFF1782A8;
    }

    public static int panelFill() {
        return 0xD2101216;
    }

    public static int panelBorder() {
        return 0x26FFFFFF;
    }

    /** Turik default accent color used by HUD/ESP modules. */
    public static int accentColor() {
        return 0xFF2BD9FF;
    }

    /** Scissor helpers for scrollable lists. */
    public static void scissor(float x, float y, float w, float h) {
        e().pushScissor(x, y, w, h);
    }

    public static void scissorEnd() {
        e().popScissor();
    }

    /** Friend color from the shared FriendManager theme. */
    public static int friendColor() {
        return 0xFF4CE07B;
    }

    public static int friendColorFromManager() {
        return Turik.get().friends().friendColor();
    }
}
