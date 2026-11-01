package ru.turik.gui;

import ru.turik.Turik;
import ru.turik.font.FontType;
import ru.turik.module.Module;
import ru.turik.render.Render2D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Turik's own toast notifications: stacked plates with slide/fade
 * animation, rendered by the Turik 2D engine on top of the HUD.
 */
public final class NotificationManager {
    private static final long LIFETIME_MS = 3500;

    private record Toast(String title, String message, long born, boolean warning) {
    }

    private final Turik turik;
    private final ArrayDeque<Toast> queue = new ArrayDeque<>();
    private final List<Toast> active = new ArrayList<>();

    public NotificationManager(Turik turik) {
        this.turik = turik;
    }

    public void info(String title, String message) {
        push(new Toast(title, message, System.currentTimeMillis(), false));
    }

    public void warn(String title, String message) {
        push(new Toast(title, message, System.currentTimeMillis(), true));
    }

    private void push(Toast toast) {
        queue.push(toast);
        if (queue.size() > 4) {
            queue.removeLast();
        }
        turik.modules().get(ru.turik.modules.misc.Sounds.class).notification();
    }

    public void moduleToggled(Module module) {
        info(module.name(), module.isEnabled() ? "Включен" : "Выключен");
        turik.modules().get(ru.turik.modules.misc.Sounds.class).moduleToggled();
    }

    /** Render pass; called from the HUD manager. */
    public void render(long now) {
        // move queued toasts in
        while (!queue.isEmpty() && active.size() < 4) {
            active.add(queue.removeLast());
        }
        Iterator<Toast> it = active.iterator();
        int index = 0;
        while (it.hasNext()) {
            Toast toast = it.next();
            float age = (now - toast.born()) / (float) LIFETIME_MS;
            if (age >= 1f) {
                it.remove();
                continue;
            }
            // slide in (0..0.15), hold, fade out (0.85..1)
            float slide = Math.min(1f, age / 0.15f);
            float fade = age > 0.85f ? (1f - age) / 0.15f : 1f;
            float w = Math.max(
                    Render2D.textWidth(toast.title(), 9, FontType.SEMIBOLD),
                    Render2D.textWidth(toast.message(), 9, FontType.REGULAR)) + 22;
            float h = 40;
            float x = RenderEngineWidth() - w * slide - 10;
            float y = 10 + index * (h + 8);

            int accent = toast.warning() ? 0xFFFF6B6B : 0xFF2BD9FF;
            int alpha = (int) (0xFF * fade);
            Render2D.shadow(x, y, w, h, 6f, 6f, 0x80000000);
            Render2D.roundedRect(x, y, w, h, 6f, argbAlpha((0xD8 << 24) | 0x101216, fade));
            Render2D.rect(x, y + 6, 3, h - 12, argbAlpha(accent, fade));
            Render2D.text(toast.title(), x + 11, y + 7, 9, FontType.SEMIBOLD, argbAlpha(accent, fade));
            Render2D.text(toast.message(), x + 11, y + 22, 9, FontType.REGULAR, argbAlpha(0xFFE8F6FF, fade));
            index++;
        }
    }

    private static int argbAlpha(int argb, float fade) {
        return (int) (((argb >>> 24) & 0xFF) * fade) << 24 | (argb & 0x00FFFFFF);
    }

    private float RenderEngineWidth() {
        var engine = ru.turik.render.RenderEngine.get();
        return engine.viewWidth() / Math.max(1f, engine.guiScale());
    }
}