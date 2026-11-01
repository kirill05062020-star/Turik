package ru.turik.module;

/** Turik module categories, in display order. */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    VISUALS("Visuals"),
    WORLD("World"),
    MISC("Misc");

    private final String display;

    Category(String display) {
        this.display = display;
    }

    public String display() {
        return display;
    }
}
