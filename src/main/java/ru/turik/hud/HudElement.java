package ru.turik.hud;

import ru.turik.Turik;
import ru.turik.font.FontType;
import ru.turik.render.Render2D;
import ru.turik.render.RenderEngine;

/**
 * Base of all HUD plates: position (dragged in the HUD editor), size and
 * drawing. Coordinates are in GUI pixels.
 */
public abstract class HudElement {
    protected float x, y;
    protected float width = 100, height = 20;
    private boolean dragging;

    protected HudElement(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public abstract void render();

    public boolean visible() {
        return true;
    }

    public void updateBounds() {
        // subclasses refresh width/height after measuring content
    }

    // ---- interaction (HUD editor) ---------------------------------------

    public boolean mouseOver(float mx, float my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public void startDrag() {
        dragging = true;
    }

    public void dragTo(float mx, float my) {
        if (dragging) {
            this.x = mx - width / 2f;
            this.y = my - height / 2f;
        }
    }

    public void stopDrag() {
        dragging = false;
        Turik.get().configs().requestSave();
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public String name() {
        return getClass().getSimpleName();
    }

    // helpers -------------------------------------------------------------

    protected float guiWidth() {
        var e = RenderEngine.get();
        return e.viewWidth() / Math.max(1f, e.guiScale());
    }

    protected float guiHeight() {
        var e = RenderEngine.get();
        return e.viewHeight() / Math.max(1f, e.guiScale());
    }

    protected void plate(float padding) {
        Render2D.shadow(x, y, width, height, 5f, 4f, 0x73000000);
        Render2D.roundedRect(x, y, width, height, 5f, 0xD2101216);
    }
}