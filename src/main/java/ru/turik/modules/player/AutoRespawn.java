package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.TickTimer;

/**
 * 16. AutoRespawn — respawns automatically after death.
 */
public class AutoRespawn extends Module {
    private final NumberSetting delay = new NumberSetting("Delay", 0, 5000, 100, 1000, "ms");

    private final TickTimer timer = new TickTimer();
    private boolean notified;

    public AutoRespawn() {
        super("AutoRespawn", "Автоматическое возрождение после смерти",
                Category.PLAYER);
        add(delay);
    }

    /** Called from the client-player mixin. */
    public void onPlayerTick(ClientPlayerEntity player) {
        MinecraftClient mc = Turik.get().mc();
        if (!isEnabled() || !player.isDead()) {
            notified = false;
            return;
        }
        if (!notified) {
            notified = true;
            timer.reset();
        }
        if (timer.elapsed((long) delay.get())) {
            notified = false;
            player.requestRespawn();
            Turik.get().notifications().info("AutoRespawn", "Возрождение выполнено");
        }
    }
}
