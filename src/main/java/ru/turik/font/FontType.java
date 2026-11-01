package ru.turik.font;

/** Typeface variants of the Turik font system (bundled Manrope). */
public enum FontType {
    REGULAR(0),
    MEDIUM(1),
    SEMIBOLD(2),
    BOLD(3);

    /** Extra rasterization passes used to synthesize heavier weights. */
    public final int passes;

    FontType(int passes) {
        this.passes = passes;
    }
}
