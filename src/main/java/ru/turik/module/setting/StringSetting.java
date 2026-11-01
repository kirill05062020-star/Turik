package ru.turik.module.setting;

/** Free-text setting (block lists, messages, item ids). */
public class StringSetting extends Setting {
    private String value;

    public StringSetting(String name, String defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public String get() {
        return value;
    }

    public void set(String value) {
        this.value = value;
        markChanged();
    }

    public String[] list() {
        String[] parts = value.split("[,;]");
        int n = 0;
        for (String p : parts) {
            if (!p.isBlank()) n++;
        }
        String[] out = new String[n];
        int i = 0;
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) out[i++] = t;
        }
        return out;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValueRaw(String value) {
        this.value = value;
    }
}
