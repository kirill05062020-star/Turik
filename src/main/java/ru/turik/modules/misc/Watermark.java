package ru.turik.modules.misc;

import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ColorSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 46. Watermark — Turik branding plate with FPS, ping, server, time.
 * Rendered by the HudManager; this module owns its settings.
 */
public class Watermark extends Module {
    private final ColorSetting color = new ColorSetting("Color", 0xFF2BD9FF);
    private final NumberSetting size = new NumberSetting("Text size", 6, 14, 0.5, 9);
    private final BooleanSetting showFps = new BooleanSetting("FPS", true);
    private final BooleanSetting showPing = new BooleanSetting("Ping", true);
    private final BooleanSetting showServer = new BooleanSetting("Server", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", true);
    private final BooleanSetting showCoords = new BooleanSetting("Coordinates", false);

    public Watermark() {
        super("Watermark", "Название клиента, FPS и информация о сессии",
                Category.MISC);
        add(color, size, showFps, showPing, showServer, showTime, showCoords);
    }

    public ColorSetting colorSetting() {
        return color;
    }

    public NumberSetting sizeSetting() {
        return size;
    }

    public boolean showFps() {
        return showFps.get();
    }

    public boolean showPing() {
        return showPing.get();
    }

    public boolean showServer() {
        return showServer.get();
    }

    public boolean showTime() {
        return showTime.get();
    }

    public boolean showCoords() {
        return showCoords.get();
    }
}