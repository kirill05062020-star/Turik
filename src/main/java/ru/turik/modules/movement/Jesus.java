package ru.turik.modules.movement;

import net.minecraft.client.MinecraftClient;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 9. Jesus — changes water traversal: walk on the surface, hop or swim.
 */
public class Jesus extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Solid", "Hop", "Swim");
    private final NumberSetting surfaceOffset = new NumberSetting("Surface offset", 0.0, 0.5, 0.01, 0.08);
    private final BooleanSetting keepAboveWaterfall = new BooleanSetting("Stabilize", true);

    public Jesus() {
        super("Jesus", "Изменение поведения передвижения по воде",
                Category.MOVEMENT);
        add(mode, surfaceOffset, keepAboveWaterfall);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || !mc.player.isTouchingWater()) {
            return;
        }
        switch (mode.get()) {
            case "Solid" -> {
                // Hold the player just above the surface; fall damage resets.
                double surfaceY = Math.floor(mc.player.getY()) + surfaceOffset.get();
                if (mc.player.getY() < surfaceY) {
                    mc.player.setVelocity(mc.player.getVelocity().x,
                            Math.min(0.12, surfaceY - mc.player.getY()), mc.player.getVelocity().z);
                }
                mc.player.setFallDistance(0);
                mc.player.setOnGround(false);
            }
            case "Hop" -> {
                if (mc.player.horizontalCollision || mc.player.getVelocity().y <= 0) {
                    mc.player.setVelocity(mc.player.getVelocity().x, 0.34, mc.player.getVelocity().z);
                }
                mc.player.setFallDistance(0);
            }
            case "Swim" -> {
                if (keepAboveWaterfall.get()) {
                    mc.player.setSwimming(mc.player.isSprinting());
                }
            }
        }
    }
}
