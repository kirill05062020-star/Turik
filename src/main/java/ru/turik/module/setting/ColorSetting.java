package ru.turik.module.setting;

/** ARGB color setting. */
public class ColorSetting extends Setting {
    private int argb;

    public ColorSetting(String name, int argb) {
        super(name);
        this.argb = argb;
    }

    public int get() {
        return argb;
    }

    public int red() {
        return (argb >> 16) & 0xFF;
    }

    public int green() {
        return (argb >> 8) & 0xFF;
    }

    public int blue() {
        return argb & 0xFF;
    }

    public int alpha() {
        return (argb >>> 24) & 0xFF;
    }

    /** Same color with another alpha. */
    public int withAlpha(int alpha) {
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    public float redF() {
        return red() / 255f;
    }

    public float greenF() {
        return green() / 255f;
    }

    public float blueF() {
        return blue() / 255f;
    }

    public float alphaF() {
        return alpha() / 255f;
    }

    public void set(int argb) {
        this.argb = argb;
        markChanged();
    }

    @Override
    public Object getValue() {
        return argb;
    }

    @Override
    public void setValueRaw(String value) {
        try {
            String v = value.trim().replace("#", "");
            if (v.length() == 6) {
                v = "FF" + v;
            }
            this.argb = (int) Long.parseLong(v, 16);
        } catch (NumberFormatException ignored) {
        }
    }
}
