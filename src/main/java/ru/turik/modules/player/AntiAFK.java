package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.TickTimer;

/**
 * 20. AntiAFK — keeps the session alive: swings, turns or hops.
 */
public class AntiAFK extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Swing", "Rotate", "Jump", "Combo");
    private final NumberSetting interval = new NumberSetting("Interval", 1, 120, 1, 20, "с");
    private final NumberSetting turnAngle = new NumberSetting("Turn angle", 1, 90, 1, 12, "°");

    private final TickTimer timer = new TickTimer();
    private boolean direction;

    public AntiAFK() {
        super("AntiAFK", "Автоматизация действий против АФК-кика",
                Category.PLAYER);
        add(mode, interval, turnAngle);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (!timer.elapsed((long) (interval.get() * 1000))) {
            return;
        }
        String m = mode.get();
        boolean swing = m.equals("Swing") || m.equals("Combo");
        boolean rotate = m.equals("Rotate") || m.equals("Combo");
        boolean jump = m.equals("Jump") || (m.equals("Combo") && direction);

        if (swing) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (rotate) {
            direction = !direction;
            mc.player.setYaw(mc.player.getYaw() + (direction ? turnAngle.getFloat() : -turnAngle.getFloat()));
        }
        if (jump && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }
}
