package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.Hand;
import ru.turik.Turik;
import ru.turik.mixin.FishHookAccessor;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.TickTimer;

/**
 * 21. AutoFish — detects the bite through the bobber's private latch,
 * reels in and recasts after the configured delay.
 */
public class AutoFish extends Module {
    private final NumberSetting recastDelay = new NumberSetting("Recast delay", 100, 5000, 100, 800, "ms");
    private final BooleanSetting notify = new BooleanSetting("Notification", true);
    private final BooleanSetting autoRecast = new BooleanSetting("Auto recast", true);

    private final TickTimer reelTimer = new TickTimer();
    private final TickTimer castTimer = new TickTimer();
    private boolean waitingToRecast;

    public AutoFish() {
        super("AutoFish", "Автоматическая рыбалка с определением поклёвки",
                Category.PLAYER);
        add(recastDelay, notify, autoRecast);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        FishingBobberEntity bobber = mc.player.fishHook;
        if (bobber == null) {
            // rod not cast — cast it when ready
            if (autoRecast.get() && waitingToRecast && castTimer.elapsed((long) recastDelay.get())) {
                cast(mc);
                waitingToRecast = false;
            }
            return;
        }
        boolean caught = ((FishHookAccessor) bobber).turik$caughtFish();
        if (caught) {
            if (notify.get()) {
                Turik.get().notifications().info("AutoFish", "Поклёвка! Рыба поймана");
            }
            reel(mc);
            waitingToRecast = true;
            castTimer.reset();
        }
    }

    private void cast(MinecraftClient mc) {
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void reel(MinecraftClient mc) {
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
