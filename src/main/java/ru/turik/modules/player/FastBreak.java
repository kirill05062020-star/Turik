package ru.turik.modules.player;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.turik.Turik;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;

/**
 * 17b. FastBreak — accelerates block breaking by running extra mining
 * progress ticks per game tick (tool-aware: the vanilla progress logic,
 * including tool speed and efficiency, is re-run per extra tick).
 */
public class FastBreak extends Module {
    /** Functional handle onto the interaction manager's mining tick. */
    public interface MiningTick {
        boolean mine(BlockPos pos, Direction direction);
    }

    private final ModeSetting mode = new ModeSetting("Mode", "Multiplier", "Instant (creative)");
    private final NumberSetting multiplier = new NumberSetting("Extra ticks", 1, 16, 1, 4, "x");

    public FastBreak() {
        super("FastBreak", "Ускоряет разрушение блоков с учётом инструментов",
                Category.PLAYER);
        add(mode, multiplier);
    }

    /** Called from the interaction-manager mixin after the normal mining tick. */
    public void accelerate(MiningTick tick, BlockPos pos, Direction direction) {
        if (!isEnabled()) {
            return;
        }
        int extra = mode.is("Instant (creative)") ? 64 : multiplier.getInt() - 1;
        for (int i = 0; i < extra; i++) {
            if (!tick.mine(pos, direction)) {
                break;
            }
        }
    }

    public static FastBreak get() {
        return Turik.get().modules().get(FastBreak.class);
    }
}
