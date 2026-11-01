package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.ModeSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.TickTimer;

/**
 * 17. FastPlace — repeats block placement while the use key is held.
 */
public class FastPlace extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Spam", "Hold");
    private final NumberSetting delay = new NumberSetting("Delay", 0, 1000, 20, 100, "ms");
    private final BooleanSetting onlyBlocks = new BooleanSetting("Only blocks", true);
    private final BooleanSetting sprintProtect = new BooleanSetting("Respect max reach", true);

    private final TickTimer timer = new TickTimer();

    public FastPlace() {
        super("FastPlace", "Ускоренная установка блоков",
                Category.PLAYER);
        add(mode, delay, onlyBlocks, sprintProtect);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) {
            return;
        }
        if (!mc.options.useKey.isPressed()) {
            return;
        }
        if (onlyBlocks.get() && !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
            return;
        }
        if (!(mc.crosshairTarget instanceof BlockHitResult hit)) {
            return;
        }
        if (sprintProtect.get() && mc.player.getEyePos().distanceTo(hit.getPos()) > 4.5) {
            return;
        }
        long period = mode.is("Spam") ? (long) delay.get() : (long) delay.get();
        if (timer.elapsed(period)) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
