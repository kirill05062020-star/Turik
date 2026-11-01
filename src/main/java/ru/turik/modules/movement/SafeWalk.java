package ru.turik.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;

/**
 * 15. SafeWalk — prevents walking off block edges (sneak-like clamping).
 */
public class SafeWalk extends Module {
    private final BooleanSetting whileSneaking = new BooleanSetting("Include sneaking", true);
    private final BooleanSetting includeJump = new BooleanSetting("Include jump padding", false);

    public SafeWalk() {
        super("SafeWalk", "Не даёт случайно сойти с края блока",
                Category.MOVEMENT);
        add(whileSneaking, includeJump);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || !mc.player.isOnGround()) {
            return;
        }
        if (mc.player.isSneaking() && !whileSneaking.get()) {
            return;
        }
        double dx = mc.player.getX() - mc.player.prevX;
        double dz = mc.player.getZ() - mc.player.prevZ;
        double speed = Math.sqrt(dx * dx + dz * dz);
        if (speed < 0.05) {
            return;
        }
        double nx = dx / speed;
        double nz = dz / speed;
        double probe = 0.05;
        double halfWidth = mc.player.getWidth() / 2.0;
        double y = mc.player.getY() - 0.6;

        boolean edgeAhead = !solid(mc.world, mc.player.getX() + nx * (halfWidth + probe), y, mc.player.getZ() + nz * (halfWidth + probe))
                || !solid(mc.world, mc.player.getX() + nx * (halfWidth + probe) + nz * halfWidth, y, mc.player.getZ() + nz * (halfWidth + probe) + nx * halfWidth)
                || !solid(mc.world, mc.player.getX() + nx * (halfWidth + probe) - nz * halfWidth, y, mc.player.getZ() + nz * (halfWidth + probe) - nx * halfWidth);
        if (edgeAhead) {
            // Cancel horizontal motion this tick (server sees a stop at the edge).
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        }
    }

    private boolean solid(net.minecraft.world.World world, double x, double y, double z) {
        BlockPos pos = BlockPos.ofFloored(x, y, z);
        BlockState state = world.getBlockState(pos);
        return !state.isAir() && !state.isReplaceable() && state.isSolidBlock(world, pos);
    }
}
