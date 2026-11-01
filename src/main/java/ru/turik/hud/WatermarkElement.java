package ru.turik.hud;

import ru.turik.Turik;
import ru.turik.font.FontType;
import ru.turik.modules.misc.Watermark;
import ru.turik.render.Render2D;
import ru.turik.util.PlayerUtils;

/**
 * Watermark plate: Turik branding + session info. Driven by the
 * Watermark module's settings.
 */
public class WatermarkElement extends HudElement {
    public WatermarkElement() {
        super(8, 8);
    }

    @Override
    public boolean visible() {
        Watermark wm = Turik.get().modules().get(Watermark.class);
        return wm != null && wm.isEnabled();
    }

    @Override
    public void render() {
        var mc = Turik.get().mc();
        Watermark wm = Turik.get().modules().get(Watermark.class);
        if (wm == null) {
            return;
        }
        float size = wm.sizeSetting().getFloat();
        int color = wm.colorSetting().get();

        String main = "Turik " + Turik.VERSION;
        if (wm.showFps()) {
            main += "  ·  " + PlayerUtils.fps(mc) + " fps";
        }
        if (wm.showPing() && mc.player != null) {
            main += "  ·  " + PlayerUtils.ping(mc.player) + " ms";
        }
        if (wm.showServer()) {
            String server = mc.getCurrentServerEntry() != null
                    ? mc.getCurrentServerEntry().address
                    : "singleplayer";
            main += "  ·  " + server;
        }
        if (wm.showTime()) {
            main += "  ·  " + java.time.LocalTime.now().withSecond(0).toString();
        }

        String coords = wm.showCoords() && mc.player != null
                ? String.format("XYZ: %.0f / %.0f / %.0f",
                        mc.player.getX(), mc.player.getY(), mc.player.getZ())
                : null;

        width = Render2D.textWidth(main, size, FontType.SEMIBOLD) + 16;
        if (coords != null) {
            width = Math.max(width, Render2D.textWidth(coords, size, FontType.REGULAR) + 16);
            height = Render2D.textHeight(size) * 2 + 14;
        } else {
            height = Render2D.textHeight(size) + 12;
        }
        plate(0);
        Render2D.rect(x, y + 5, 2, height - 10, color);
        Render2D.text(main, x + 8, y + 6, size, FontType.SEMIBOLD, color);
        if (coords != null) {
            Render2D.text(coords, x + 8, y + Render2D.textHeight(size) + 7, size,
                    FontType.REGULAR, 0xFFE8F6FF);
        }
    }
}