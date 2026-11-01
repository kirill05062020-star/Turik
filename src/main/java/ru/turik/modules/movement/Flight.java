package ru.turik.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 13. Flight — free movement. Vanilla mode toggles abilities.flying
 * (single-player/creative friendly); Velocity mode drives raw motion.
 */
public class Flight extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Velocity", "Vanilla");
    private final NumberSetting speed = new NumberSetting("Speed", 0.1, 10, 0.1, 1.2);
    private final NumberSetting verticalSpeed = new NumberSetting("Vertical speed", 0.1, 5, 0.1, 0.8);
    private final BooleanSetting noKick = new BooleanSetting("Anti-kick", true);

    private boolean vanillaWasFlying;

    public Flight() {
        super("Flight", "Свободное перемещение",
                Category.MOVEMENT);
        add(mode, speed, verticalSpeed, noKick);
    }

    @Override
    public void onEnable() {
        if (mode.is("Vanilla") && Turik.get().mc().player != null) {
            vanillaWasFlying = Turik.get().mc().player.getAbilities().flying;
        }
    }

    @Override
    public void onDisable() {
        if (mode.is("Vanilla") && Turik.get().mc().player != null) {
            Turik.get().mc().player.getAbilities().flying = vanillaWasFlying;
        }
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) {
            return;
        }
        long h = mc.getWindow().getHandle();
        boolean up = GLFW.glfwGetKey(h, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        boolean down = GLFW.glfwGetKey(h, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;

        if (mode.is("Vanilla")) {
            mc.player.getAbilities().setFlySpeed(speed.getFloat() * 0.02f);
            mc.player.getAbilities().flying = true;
            return;
        }
        // Velocity mode
        mc.player.setNoGravity(true);
        Vec3d v = mc.player.getVelocity();
        double x = 0, z = 0;
        if (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) {
            double yaw = Math.toRadians(mc.player.getYaw());
            float f = mc.player.forwardSpeed;
            float s = mc.player.sidewaysSpeed;
            x = (-Math.sin(yaw) * f + Math.cos(yaw) * s) * speed.get() * 0.1;
            z = (Math.cos(yaw) * f + Math.sin(yaw) * s) * speed.get() * 0.1;
        }
        double y = 0;
        if (up) y = verticalSpeed.get() * 0.1;
        else if (down) y = -verticalSpeed.get() * 0.1;
        mc.player.setVelocity(x, y, z);
        mc.player.setOnGround(false);
        mc.player.fallDistance = 0;
        if (noKick.get()) {
            // gentle downward drift keeps pure-hover patterns from stacking
            mc.player.setVelocity(mc.player.getVelocity().add(0, -0.01, 0));
        }
    }
}
