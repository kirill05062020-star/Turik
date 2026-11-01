package ru.turik.module.setting;

import java.util.Locale;

/** Numeric setting with min/max and a fixed step, stored as double. */
public class NumberSetting extends Setting {
    private double value;
    private final double min;
    private final double max;
    private final double step;
    private final String unit;

    public NumberSetting(String name, double min, double max, double step, double defaultValue) {
        this(name, min, max, step, defaultValue, "");
    }

    public NumberSetting(String name, double min, double max, double step, double defaultValue, String unit) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = defaultValue;
        this.unit = unit;
    }

    public double get() {
        return value;
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public float getFloat() {
        return (float) value;
    }

    public void set(double value) {
        value = Math.round(value / step) * step;
        this.value = Math.max(min, Math.min(max, value));
        markChanged();
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public String unit() {
        return unit;
    }

    public String display() {
        double snapped = value;
        if (step >= 1.0) {
            return String.format(Locale.ROOT, "%.0f%s", snapped, unit.isEmpty() ? "" : " " + unit);
        }
        return String.format(Locale.ROOT, "%.2f%s", snapped, unit.isEmpty() ? "" : " " + unit);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValueRaw(String value) {
        try {
            set(Double.parseDouble(value.replace(',', '.').trim()));
        } catch (NumberFormatException ignored) {
            // keep the previous value on malformed input
        }
    }
}
