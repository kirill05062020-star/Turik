package ru.turik.module.setting;

import java.util.function.Supplier;

/** Base of all module settings. Serialization is delegated to subclasses. */
public abstract class Setting {
    private final String name;
    private Supplier<Boolean> visible = () -> true;

    protected Setting(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public abstract Object getValue();

    public abstract void setValueRaw(String value);

    /** Called by the ClickGUI to persist an already-typed value. */
    public void markChanged() {
    }

    public boolean isVisible() {
        try {
            return visible.get();
        } catch (Throwable t) {
            return true;
        }
    }

    public Setting visibleWhen(Supplier<Boolean> condition) {
        this.visible = condition;
        return this;
    }
}
