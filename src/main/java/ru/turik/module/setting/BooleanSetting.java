package ru.turik.module.setting;

public class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
        markChanged();
    }

    public void toggle() {
        set(!value);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValueRaw(String value) {
        this.value = Boolean.parseBoolean(value.trim());
    }
}
