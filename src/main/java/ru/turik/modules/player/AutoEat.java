package ru.turik.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import ru.turik.module.Category;
import ru.turik.module.Module;
import ru.turik.module.setting.BooleanSetting;
import ru.turik.module.setting.NumberSetting;
import ru.turik.util.TickTimer;

/**
 * 23. AutoEat — eats when hunger drops below the threshold.
 */
public class AutoEat extends Module {
    private final NumberSetting hunger = new NumberSetting("Hunger threshold", 2, 18, 1, 14, "ед");
    private final BooleanSetting preferBest = new BooleanSetting("Prefer best food", true);
    private final BooleanSetting onlyNatural = new BooleanSetting("Only edible items", true);

    private final TickTimer safety = new TickTimer();
    private boolean eating;
    private int prevSlot = -1;

    public AutoEat() {
        super("AutoEat", "Автоеда при низком уровне голода",
                Category.PLAYER);
        add(hunger, preferBest, onlyNatural);
    }

    @Override
    public void onDisable() {
        stopEating(Turik.get().mc());
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mc.player.getHungerManager().getFoodLevel() > hunger.getInt() && !eating) {
            return;
        }
        if (eating && mc.player.getHungerManager().isNotFull()) {
            mc.options.useKey.setPressed(true);
            if (safety.elapsed(15000)) {
                stopEating(mc); // stuck (menu, elytra...) — reset
            }
            return;
        }
        if (eating) {
            stopEating(mc);
            return;
        }
        int bestSlot = -1;
        float bestScore = -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            var food = stack.getComponents().get(DataComponentTypes.FOOD);
            if (food == null) {
                continue;
            }
            if (onlyNatural.get() && stack.getItem() == net.minecraft.item.Items.GOLDEN_APPLE) {
                // golden apples are valuable; still usable in extreme hunger
                if (mc.player.getHungerManager().getFoodLevel() > 4) continue;
            }
            float score = food.nutrition() + food.saturation() * 2;
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        if (bestSlot >= 0) {
            prevSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(bestSlot);
            eating = true;
            safety.reset();
            mc.options.useKey.setPressed(true);
        }
    }

    private void stopEating(MinecraftClient mc) {
        if (!eating) {
            return;
        }
        eating = false;
        mc.options.useKey.setPressed(false);
        if (prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
            prevSlot = -1;
        }
    }
}
