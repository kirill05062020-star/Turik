package ru.turik.modules.movement;

import net.minecraft.client.MinecraftClient;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 8. Spider — climbs vertical surfaces.
 */
public class Spider extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Climb", "Jump");
    private final NumberSetting climbSpeed = new NumberSetting("Climb speed", 0.1, 4, 0.05, 0.28);
    private final NumberSetting jumpHeight = new NumberSetting("Jump height", 0.2, 1.5, 0.05, 0.42, "блоков");

    public Spider() {
        super("Spider", "Передвижение вверх по вертикальным поверхностям",
                Category.MOVEMENT);
        add(mode, climbSpeed, jumpHeight);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.player.horizontalCollision) {
            if (mc.player != null && mc.player.horizontalCollision) {
                if (mode.is("Climb")) {
                    mc.player.setVelocity(mc.player.getVelocity().x, climbSpeed.get(), mc.player.getVelocity().z);
                } else if (mc.player.isOnGround()) {
                    // Jump mode: bounce off the wall one block at a time.
                    mc.player.setVelocity(mc.player.getVelocity().x, jumpHeight.get() * 0.42, mc.player.getVelocity().z);
                    mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                }
            }
        }
    }
}
