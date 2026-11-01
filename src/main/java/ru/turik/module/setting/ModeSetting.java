package ru.turik.module.setting;

/** Enumerated (mode) setting. */
public class ModeSetting extends Setting {
    private final String[] modes;
    private int index;

    public ModeSetting(String name, String... modes) {
        super(name);
        if (modes == null || modes.length == 0) {
            throw new IllegalArgumentException("ModeSetting needs at least one mode");
        }
        this.modes = modes;
    }

    public String get() {
        return modes[index];
    }

    public boolean is(String mode) {
        return modes[index].equalsIgnoreCase(mode);
    }

    public String[] modes() {
        return modes;
    }

    public void set(String mode) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(mode)) {
                index = i;
                markChanged();
                return;
            }
        }
    }

    public void cycle(int direction) {
        index = Math.floorMod(index + direction, modes.length);
        markChanged();
    }

    @Override
    public Object getValue() {
        return modes[index];
    }

    @Override
    public void setValueRaw(String value) {
        set(value);
    }
}
