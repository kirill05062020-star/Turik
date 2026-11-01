package ru.turik.module.setting;

/** Keybind setting storing a GLFW key code; -1 means unbound. */
public class KeybindSetting extends Setting {
    private int key;
    private boolean listening;

    public KeybindSetting(String name, int defaultKey) {
        super(name);
        this.key = defaultKey;
    }

    public int get() {
        return key;
    }

    public void set(int key) {
        this.key = key;
        this.listening = false;
        markChanged();
    }

    public boolean isListening() {
        return listening;
    }

    public void startListening() {
        this.listening = true;
    }

    public void cancelListening() {
        this.listening = false;
    }

    @Override
    public Object getValue() {
        return key;
    }

    @Override
    public void setValueRaw(String value) {
        try {
            this.key = Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
        }
    }
}
