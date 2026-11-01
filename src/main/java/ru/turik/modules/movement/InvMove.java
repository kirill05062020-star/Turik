package ru.turik.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import ru.turik.gui.ClickGuiScreen;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 11. InvMove — walk/jump while any inventory screen is open.
 */
public class InvMove extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 0.1, 1, 0.05, 0.22);
    private final BooleanSetting allowJump = new BooleanSetting("Jump", true);
    private final BooleanSetting allowSprint = new BooleanSetting("Sprint", true);

    public InvMove() {
        super("InvMove", "Движение при открытом инвентаре",
                Category.MOVEMENT);
        add(speed, allowJump, allowSprint);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.currentScreen == null || mc.currentScreen instanceof ClickGuiScreen) {
            return;
        }
        if (mc.currentScreen.getFocused() instanceof net.minecraft.client.gui.widget.TextFieldWidget) {
            return; // typing in chat/search — don't hijack keys
        }
        long h = mc.getWindow().getHandle();
        boolean fwd = key(h, GLFW.GLFW_KEY_W);
        boolean back = key(h, GLFW.GLFW_KEY_S);
        boolean left = key(h, GLFW.GLFW_KEY_A);
        boolean right = key(h, GLFW.GLFW_KEY_D);

        float strafe = (right ? 1 : 0) - (left ? 1 : 0);
        float forward = (fwd ? 1 : 0) - (back ? 1 : 0);
        if (forward == 0 && strafe == 0) {
            return;
        }
        float dist = speed.getFloat();
        double yawRad = Math.toRadians(mc.player.getYaw());
        double dx = (-Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe) * dist;
        double dz = (Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe) * dist;
        mc.player.setVelocity(dx, mc.player.getVelocity().y, dz);
        mc.player.setSprinting(allowSprint.get() && forward > 0);
        if (allowJump.get() && key(h, GLFW.GLFW_KEY_SPACE) && mc.player.isOnGround()) {
            mc.player.jump();
        }
        // Keep vanilla input from also applying (screen is open so it won't).
        KeyBinding.unpressAll();
    }

    private boolean key(long handle, int code) {
        return org.lwjgl.glfw.GLFW.glfwGetKey(handle, code) == GLFW.GLFW_PRESS;
    }
}
