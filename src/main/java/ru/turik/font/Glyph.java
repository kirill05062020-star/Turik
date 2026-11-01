package ru.turik.font;

/** A rasterized glyph: its GL texture and metrics. Texture lives as long as the cache. */
public final class Glyph {
    public final int textureId;
    public final int width;
    public final int height;
    public final int bearingX;
    public final int bearingY;
    public final float advance;

    public Glyph(int textureId, int width, int height, int bearingX, int bearingY, float advance) {
        this.textureId = textureId;
        this.width = width;
        this.height = height;
        this.bearingX = bearingX;
        this.bearingY = bearingY;
        this.advance = advance;
    }
}
