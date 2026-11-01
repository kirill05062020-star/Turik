package ru.turik.modules.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.module.setting.StringSetting;
import ru.turik.util.TickTimer;

/**
 * 43. AutoGG — sends a configurable message after a round ends
 * (triggered by death or manually via the keybind-less tick hook).
 */
public class AutoGG extends Module {
    private final StringSetting message = new StringSetting("Message", "GG");
    private final NumberSetting delay = new NumberSetting("Delay", 0, 5000, 100, 1500, "ms");
    private final BooleanSetting onDeath = new BooleanSetting("Trigger on death", true);
    private final BooleanSetting oncePerDeath = new BooleanSetting("Once per round", true);

    private final TickTimer timer = new TickTimer();
    private boolean waiting;

    public AutoGG() {
        super("AutoGG", "Автоотправка сообщения после конца раунда",
                Category.MISC);
        add(message, delay, onDeath, oncePerDeath);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null || mc.world == null) {
            return;
        }
        if (onDeath.get() && mc.player.isDead()) {
            if (!waiting || !oncePerDeath.get()) {
                waiting = true;
                timer.reset();
            }
        }
        if (waiting && timer.elapsed((long) delay.get())) {
            waiting = false;
            String msg = message.get().trim();
            if (!msg.isEmpty()) {
                mc.player.networkHandler.sendChatMessage(msg, Text.empty());
                Turik.get().notifications().info("AutoGG", "Отправлено: " + msg);
            }
        }
    }
}