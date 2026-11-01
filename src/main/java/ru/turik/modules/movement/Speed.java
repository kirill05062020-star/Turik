package ru.turik.modules.movement;

import net.minecraft.client.MinecraftClient;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 14. Speed — movement speed modes.
 */
public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Strafe", "Sprint", "Ground");
    private final NumberSetting multiplier = new NumberSetting("Multiplier", 1.0, 3.0, 0.05, 1.35);
    private final BooleanSetting onlyForward = new BooleanSetting("Only forward", true);

    public Speed() {
        super("Speed", "Настройка скорости передвижения",
                Category.MOVEMENT);
        add(mode, multiplier, onlyForward);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.player.isUsingItem()) {
            return;
        }
        double v = mc.player.getVelocity().horizontalLength();
        if (v < 0.01) {
            return;
        }
        boolean ground = mc.player.isOnGround();
        boolean apply = switch (mode.get()) {
            case "Strafe" -> true;
            case "Sprint" -> mc.player.isSprinting();
            case "Ground" -> ground;
            default -> false;
        };
        if (!apply) {
            return;
        }
        if (onlyForward.get() && mc.player.forwardSpeed <= 0) {
            return;
        }
        double factor = multiplier.get();
        // Keep velocity bounded so servers see a plausible movement profile.
        if (v < 0.9 * factor) {
            mc.player.setVelocity(mc.player.getVelocity().multiply(factor, 1.0, factor));
        }
    }
}
